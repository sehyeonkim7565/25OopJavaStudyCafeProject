package Seat;

import java.time.Duration;
import java.time.LocalDateTime;

//1회 좌석 이용에 대한 시작-종료 시간 기록
public class UsageSession {
    private Seat seat;
    private String userId;
    private LocalDateTime startTime;
    private LocalDateTime endTime;     // 퇴실 시각 (null이면 진행 중)
    private Long durationMinutes;       // 종료 시점에 확정된 이용 시간

    public UsageSession(String userId, Seat seat) {
        this(userId, seat, LocalDateTime.now());
    }

    public UsageSession(String userId, Seat seat, LocalDateTime startTime) {
        if (userId == null || userId.isBlank())
            throw new IllegalArgumentException("이용자 ID는 반드시 입력해야 합니다.");
        if (seat == null)
            throw new IllegalArgumentException("좌석 정보가 올바르지 않습니다.");

        this.userId = userId;
        this.seat = seat;
        this.startTime = (startTime != null) ? startTime : LocalDateTime.now();
        this.endTime = null;
        this.durationMinutes = null;
        // seat.occupy()를 호출하지 않는다.
        // 실제 좌석 점유/해제는 SeatManager가 담당.
    }

    public void endSession() {
        if (this.endTime != null)
            throw new IllegalStateException("이미 종료된 이용 세션입니다.");
        this.endTime = LocalDateTime.now();
        this.durationMinutes = Math.max(0, Duration.between(startTime, endTime).toMinutes());
        // seat.vacate()를 호출하지 않는다.
        // 좌석 비우기는 SeatManager가 수행.
    }

    public long getDurationInMinutes() {
        if (durationMinutes != null) {
            return durationMinutes;
        }
        LocalDateTime to = (endTime != null) ? endTime : LocalDateTime.now();
        return Math.max(0, Duration.between(startTime, to).toMinutes());
    }

    // 필요 getter들
    public String getUserId() {
        return userId;
    }

    public Seat getSeat() {
        return seat;
    }

    public String getSeatNumber() {
        return seat.getSeatNumber();
    }

    public LocalDateTime getStartTime() {
        return startTime;
    }

    public LocalDateTime getEndTime() {
        return endTime;
    }

    /**
     * 좌석 이동 시 세션에 좌석 정보를 반영.
     */
    public void switchSeat(Seat newSeat) {
        if (newSeat == null) throw new IllegalArgumentException("새 좌석이 올바르지 않습니다.");
        this.seat = newSeat;
    }
}
