package KioskService;

import Seat.Seat;
import Seat.UsageSession;
import SeatManager.SeatManager;
import ReadingRoomLogin.Member;
import ReadingRoomLogin.MemberManager;
import Ticket.TimeTicket;
import payment.ILogManager;


public class CheckOutService {

    private SeatManager seatManager;
    private SessionManager sessionManager;
    private MemberManager memberManager;
    private ILogManager logManager;

    public CheckOutService(SeatManager seatManager,
                           SessionManager sessionManager,
                           MemberManager memberManager,
                           ILogManager logManager) {
        this.seatManager = seatManager;
        this.sessionManager = sessionManager;
        this.memberManager = memberManager;
        this.logManager = logManager;
    }

    /*
     * 퇴실 처리 
     */
    public boolean checkOut(String memberId) {
        // 현재 사용 중인 좌석 확인
        Seat seat = seatManager.findSeatByMember(memberId);
        if (seat == null) {
            // 사용 중인 좌석이 없으면
            return false;
        }

        // UsageSession 정보 가져오기
        UsageSession session = sessionManager.endSession(memberId);
        if (session == null) {
            // 진행 중인 세션이 없는 경우
            return false;
        }

        // 시간권이면 이용 시간만큼 차감
        if (memberManager != null) {
            Member member = memberManager.findMemberById(memberId);
            if (member != null && member.getTicket() instanceof TimeTicket) {
                long minutes = session.getDurationInMinutes();
                ((TimeTicket) member.getTicket()).deductTime((int) minutes);
                memberManager.saveMembers();
            }
        }

        // 입실/퇴실 시각과 이용 시간을 로그로 저장
        if (logManager != null) {
            logManager.updateUsageEnd(session);
        }

        // 좌석 비우기
        seatManager.vacateSeat(memberId);

        return true;
    }
}
