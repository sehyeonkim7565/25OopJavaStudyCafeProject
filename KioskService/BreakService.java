package StudyCafe;

public class BreakService {

    private SeatManager seatManager;
    private SessionManager sessionManager;

    public BreakService(SeatManager seatManager,
                        SessionManager sessionManager) {
        this.seatManager = seatManager;
        this.sessionManager = sessionManager;
    }

    /*
     * 외출 처리
     * 외출은 UsageSession은 유지, checkOut만 세션 종료
     */
    public boolean goOut(String memberId) {
        // 현재 사용 중인 좌석 확인
        Seat seat = seatManager.findSeatByMember(memberId);
        if (seat == null) {
            // 입실 상태가 아님
            return false;
        }

        // 좌석만 비우기 (세션은 유지)
        boolean success = seatManager.vacateSeat(memberId);
        if (!success) {
            return false;
        }

        return true;
    }

    /*
     * 복귀 처리.
     */
    public boolean returnToSeat(String memberId) {
        // 진행 중인 세션 조회
        UsageSession session = sessionManager.getActiveSession(memberId);
        if (session == null) {
            // 진행 중인 세션이 없으면 복귀할 수 없음
            return false;
        }

        // 원래 사용하던 좌석 번호 가져오기
        String originalSeatNumber = session.getSeatNumber();

        // 해당 좌석 상태 확인
        Seat seat = seatManager.findSeatByNumber(originalSeatNumber);
        if (seat == null) {
            // 존재하지 않는 좌석
            return false;
        }

        if (!seat.isAvailable()) {
            // 이미 다른 사용자가 사용 중
            return false;
        }

        // 원래 좌석으로 재배정 (세션 유지)
        boolean assigned = seatManager.assignSeat(memberId, originalSeatNumber);
        return assigned;
    }
}
