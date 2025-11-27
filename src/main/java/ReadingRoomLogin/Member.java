package ReadingRoomLogin;

import Ticket.Ticket;
import Ticket.TimeTicket;
import Ticket.DurationTicket;
import java.time.LocalDateTime;

/**
 * Member 클래스 (티켓 객체 기반)
 */
public class Member {

    private String memberId;
    private String password;
    private String name;
    private Ticket ticket;   // Ticket 객체 보유
    // 스탬프/쿠폰 관리용 (120분당 쿠폰 1개)
    private int couponCount;
    private int stampCarryMinutes; // 120분 미만 누적분
    private int stampCount; // 적립된 스탬프(10개 → 쿠폰 1개 전환)

    
    public Member(String id, String pw, String name, Ticket ticket) {
        this.memberId = id;
        this.password = pw;
        this.name = name;
        this.ticket = ticket;
        this.couponCount = 0;
        this.stampCarryMinutes = 0;
        this.stampCount = 0;
    }

    // Getter / Setter
    public String getId() { return memberId; }
    // SeatMap 등에서,,
    public String getMemberId() { return memberId; }
    public String getName() { return name; }
    public void setName(String newName) { this.name = newName; }

    public boolean checkPassword(String pw) {
        return this.password.equals(pw);
    }

    public void setTicket(Ticket ticket) {
        this.ticket = ticket;
    }

    public Ticket getTicket() {
        return ticket;
    }

    public int getCouponCount() {
        return couponCount;
    }

    public void addCoupons(int count) {
        if (count <= 0) return;
        this.couponCount = Math.max(0, this.couponCount + count);
    }

    public int getStampCarryMinutes() {
        return stampCarryMinutes;
    }

    public void setStampCarryMinutes(int minutes) {
        this.stampCarryMinutes = Math.max(0, minutes);
    }

    public int getStampCount() {
        return stampCount;
    }

    public void addStamps(int stamps) {
        if (stamps <= 0) return;
        this.stampCount = Math.max(0, this.stampCount + stamps);
        if (this.stampCount >= 10) {
            int newCoupons = this.stampCount / 10;
            this.couponCount = Math.max(0, this.couponCount + newCoupons);
            this.stampCount = this.stampCount % 10;
        }
    }

    public boolean hasValidTicket() {
        return ticket != null && ticket.isValid();
    }

    // File Save/Load

    /**
     * 파일 저장 형식: id,pw,name,ticketType,value,couponCount,stampCarryMinutes,stampCount
     * ex:
     * user01,1234,홍길동,TIME,180
     * user02,9999,김철수,DURATION,2024-01-05T23:59,2,30,4
     * user03,1111,박영희,NONE,-,0,0,0
     */
    public String toFileString() {
        if (ticket == null) {
            return memberId + "," + password + "," + name + ",NONE,-," + couponCount + "," + stampCarryMinutes + "," + stampCount;
        }

        if (ticket instanceof TimeTicket) {
            return memberId + "," + password + "," + name
                    + ",TIME," + ((TimeTicket) ticket).getRemainingMinutes()
                    + "," + couponCount + "," + stampCarryMinutes + "," + stampCount;
        }

        if (ticket instanceof DurationTicket) {
            return memberId + "," + password + "," + name
                    + ",DURATION," + ((DurationTicket) ticket).getExpiryDate()
                    + "," + couponCount + "," + stampCarryMinutes + "," + stampCount;
        }

        return memberId + "," + password + "," + name + ",NONE,-," + couponCount + "," + stampCarryMinutes + "," + stampCount;
    }

    /**
     * 파일 문자열 → Member 객체 변환
     * Ticket도 여기서 함께 복원
     */
    public static Member fromFileString(String line) {
        String[] p = line.split(",");
        if (p.length < 5) return null;

        String id = p[0];
        String pw = p[1];
        String name = p[2];
        String type = p[3];
        String value = p[4];

        Ticket ticket = null;

        switch (type) {
            case "TIME":
                ticket = new TimeTicket(Integer.parseInt(value));
                break;

            case "DURATION":
                ticket = new DurationTicket(LocalDateTime.parse(value));
                break;

            case "NONE":
            default:
                ticket = null;
        }

        Member m = new Member(id, pw, name, ticket);
        // 쿠폰/스탬프 필드는 선택적으로 뒤에 추가되므로 length 체크
        if (p.length >= 6) {
            try {
                m.couponCount = Integer.parseInt(p[5]);
            } catch (NumberFormatException ignored) {}
        }
        if (p.length >= 7) {
            try {
                m.stampCarryMinutes = Integer.parseInt(p[6]);
            } catch (NumberFormatException ignored) {}
        }
        if (p.length >= 8) {
            try {
                m.stampCount = Integer.parseInt(p[7]);
            } catch (NumberFormatException ignored) {}
        }
        return m;
    }
}
