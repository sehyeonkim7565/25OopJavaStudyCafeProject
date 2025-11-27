package KioskService;

import ReadingRoomLogin.Member;
import ReadingRoomLogin.MemberManager;
import Seat.Seat;
import Seat.UsageSession;
import SeatManager.SeatManager;


public class CheckInService {

    private MemberManager memberManager;
    private SeatManager seatManager;
    private SessionManager sessionManager;

    public CheckInService(MemberManager memberManager,
                          SeatManager seatManager,
                          SessionManager sessionManager,
                          payment.ILogManager logManager) {
        this.memberManager = memberManager;
        this.seatManager = seatManager;
        this.sessionManager = sessionManager;
        this.logManager = logManager;
    }

    private payment.ILogManager logManager;

    /*
     *  입실 처리 
     */
    public boolean checkIn(String memberId, String seatNumber) {
        // 회원 조회
        Member member = memberManager.findMemberById(memberId);
        if (member == null) {
            // 존재하지 않는 회원
            return false;
        }

        // 이용권 유효성 확인
        if (!member.hasValidTicket()) {
            // 유효한 이용권이 없는 경우
            return false;
        }

        // 이미 좌석에 앉아 있는지 확인
        Seat currentSeat = seatManager.findSeatByMember(memberId);
        if (currentSeat != null) {
            // 이미 입실 상태
            return false;
        }

        // 좌석 배정 시도
        boolean assigned = seatManager.assignSeat(memberId, seatNumber);
        if (!assigned) {
            // 좌석이 없거나 이미 사용 중인 경우
            return false;
        }

        // UsageSession (세션 시작 시간, 좌석 정보 기록)
        UsageSession session = sessionManager.startSession(memberId, seatNumber);
        if (session != null && logManager != null) {
            logManager.saveUsageStart(session);
        }

        return true;
    }
}
