package KioskService;

import Seat.Seat;
import Seat.UsageSession;
import SeatManager.SeatManager;
import payment.ILogManager;

import java.time.LocalDateTime;

/**
 * 좌석 이동 처리 서비스.
 * 세션은 유지한 채 좌석만 변경하고 로그를 남김.
 */
public class SeatMoveService {

    private final SeatManager seatManager;
    private final SessionManager sessionManager;
    private final ILogManager logManager;

    public SeatMoveService(SeatManager seatManager, SessionManager sessionManager, ILogManager logManager) {
        this.seatManager = seatManager;
        this.sessionManager = sessionManager;
        this.logManager = logManager;
    }

    public boolean move(String memberId, String targetSeatNumber) {
        if (memberId == null || targetSeatNumber == null) return false;

        UsageSession session = sessionManager.getActiveSession(memberId);
        if (session == null) {
            return false;
        }

        String fromSeatNumber = session.getSeatNumber();
        Seat newSeat = seatManager.moveSeat(memberId, targetSeatNumber);
        if (newSeat == null) {
            return false;
        }

        session.switchSeat(newSeat);

        if (logManager != null) {
            logManager.logSeatMove(memberId,
                    fromSeatNumber,
                    targetSeatNumber,
                    LocalDateTime.now());
        }
        return true;
    }
}
