
package KioskService;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List; 
import java.util.Map;

import Seat.Seat;
import Seat.UsageSession;
import SeatManager.SeatManager;

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
}
