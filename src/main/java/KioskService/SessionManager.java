package KioskService;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List; 
import java.util.Map;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import Seat.Seat;
import Seat.UsageSession;
import SeatManager.SeatManager;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;

/**
 * 좌석 이용 세션(UsageSession)을 관리하는 매니저.
 * - 세션 시작/종료
 * - 진행 중 세션 조회
 * - 세션 히스토리 관리
 */
public class SessionManager {

    // 현재 진행 중인 세션 (memberId 기준)
    private final Map<String, UsageSession> activeSessions = new HashMap<>();

    // 종료된 세션들 (통계/로그용)
    private final List<UsageSession> sessionHistory = new ArrayList<>();

    // 좌석 찾기용
    private final SeatManager seatManager;

    public SessionManager(SeatManager seatManager) {
        this.seatManager = seatManager;
    }

    /**
     * 새 좌석 이용 세션 시작.
     * - 이미 진행 중인 세션이 있으면 null 반환
     * - 좌석이 없거나 사용 중이면 null 반환
     * - 정상 시작 시 UsageSession 반환
     */
    public UsageSession startSession(String memberId, String seatNumber) {
        if (memberId == null || memberId.isBlank()) return null;
        if (seatNumber == null || seatNumber.isBlank()) return null;

        // 이미 진행 중인 세션이 있으면 시작 불가
        if (activeSessions.containsKey(memberId)) {
            return null;
        }

        // 좌석 찾기
        Seat seat = seatManager.findSeatByNumber(seatNumber);
        // 이미 다른 사람이 사용 중인 좌석이면 시작 불가, 본인이 점유한 상태는 허용
        if (seat == null) {
            return null;
        }
        if (!seat.isAvailable() && !memberId.equals(seat.getOccupantId())) {
            return null;
        }

        // UsageSession 생성하면서 좌석 occupy()
        UsageSession session = new UsageSession(memberId, seat);
        activeSessions.put(memberId, session);
        return session;
    }

    /**
     * memberId의 세션을 종료하고 반환.
     * - 세션이 없으면 null
     * - 종료 시 seat.vacate() 호출
     */
    public UsageSession endSession(String memberId) {
        if (memberId == null) return null;

        UsageSession session = activeSessions.remove(memberId);
        if (session == null) {
            return null;
        }

        // UsageSession 내부에서 seat.vacate() 처리
        session.endSession();
        sessionHistory.add(session);
        return session;
    }

    /**
     * 진행 중인 세션 조회. 없으면 null.
     */
    public UsageSession getActiveSession(String memberId) {
        if (memberId == null) return null;
        return activeSessions.get(memberId);
    }

    /**
     * 종료된 세션(히스토리) 전체 조회 (읽기 전용).
     */
    public List<UsageSession> getSessionHistory() {
        return Collections.unmodifiableList(sessionHistory);
    }

    /**
     * usage.jsonl 로그를 읽어 종료되지 않은 세션을 복원.
     * - type=SESSION & endTime==null 라인을 찾아 memberId 기준으로 최신 좌석을 반영
     * - type=MOVE 로그를 순차 적용해 최종 좌석을 맞춤
     * - 좌석이 없거나 이미 점유된 경우는 스킵
     */
    public void restoreActiveSessionsFromUsageLog(String usageLogPath) {
        Path path = Paths.get(usageLogPath);
        if (!Files.exists(path)) return;

        // memberId -> 임시 세션 정보
        class PendingSession {
            String seatNumber;
            LocalDateTime startTime;
        }

        Map<String, PendingSession> pending = new HashMap<>();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

        List<String> lines;
        try {
            lines = Files.readAllLines(path);
        } catch (Exception e) {
            System.err.println("usage 로그 읽기 실패: " + path);
            return;
        }

        for (String line : lines) {
            if (line == null || line.isBlank()) continue;
            JsonObject obj;
            try {
                obj = JsonParser.parseString(line).getAsJsonObject();
            } catch (JsonSyntaxException | IllegalStateException e) {
                continue;
            }

            String type = obj.has("type") ? obj.get("type").getAsString() : "SESSION";
            if ("MOVE".equals(type)) {
                // 이동 로그 -> 현재 열린 세션이 있을 때만 좌석 변경
                String memberId = obj.has("memberId") ? obj.get("memberId").getAsString() : null;
                String toSeat = obj.has("toSeat") ? obj.get("toSeat").getAsString() : null;
                if (memberId != null && toSeat != null && pending.containsKey(memberId)) {
                    pending.get(memberId).seatNumber = toSeat;
                }
                continue;
            }

            // SESSION 로그
            String memberId = obj.has("memberId") ? obj.get("memberId").getAsString() : null;
            String seatNumber = obj.has("seatNumber") ? obj.get("seatNumber").getAsString() : null;
            String endTime = obj.has("endTime") && !obj.get("endTime").isJsonNull() ? obj.get("endTime").getAsString() : null;
            if (memberId == null || seatNumber == null) continue;

            if (endTime != null && !endTime.isBlank()) {
                // 종료된 세션이면 후보에서 제거
                pending.remove(memberId);
                continue;
            }

            String startTimeStr = obj.has("startTime") && !obj.get("startTime").isJsonNull()
                    ? obj.get("startTime").getAsString() : null;
            LocalDateTime startTime = null;
            try {
                if (startTimeStr != null && !startTimeStr.isBlank()) {
                    startTime = LocalDateTime.parse(startTimeStr, formatter);
                }
            } catch (Exception ignored) {}

            PendingSession ps = new PendingSession();
            ps.seatNumber = seatNumber;
            ps.startTime = startTime;
            pending.put(memberId, ps);
        }

        // 실제 좌석/세션에 반영
        for (Map.Entry<String, PendingSession> entry : pending.entrySet()) {
            String memberId = entry.getKey();
            PendingSession ps = entry.getValue();
            if (memberId == null || ps == null || ps.seatNumber == null) continue;

            Seat seat = seatManager.findSeatByNumber(ps.seatNumber);
            if (seat == null) continue;
            if (!seat.isAvailable() && !memberId.equals(seat.getOccupantId())) {
                continue; // 다른 사람이 점유 중이면 복원하지 않음
            }

            try {
                seat.occupy(memberId);
            } catch (IllegalStateException ise) {
                continue;
            }

            UsageSession session = new UsageSession(memberId, seat, ps.startTime);
            activeSessions.put(memberId, session);
        }
    }
}
