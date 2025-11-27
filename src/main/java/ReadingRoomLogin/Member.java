
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

    
    public Member(String id, String pw, String name, Ticket ticket) {
        this.memberId = id;
        this.password = pw;
        this.name = name;
        this.ticket = ticket;
    }

    // Getter / Setter
    public String getId() { return memberId; }
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

    public boolean hasValidTicket() {
        return ticket != null && ticket.isValid();
    }

    // File Save/Load

    /**
     * 파일 저장 형식: id,pw,name,ticketType,value
     * ex:
     * user01,1234,홍길동,TIME,180
     * user02,9999,김철수,DURATION,2024-01-05T23:59
     * user03,1111,박영희,NONE,-
     */
    public String toFileString() {
        if (ticket == null) {
            return memberId + "," + password + "," + name + ",NONE,-";
        }

        if (ticket instanceof TimeTicket) {
            return memberId + "," + password + "," + name
                    + ",TIME," + ((TimeTicket) ticket).getRemainingMinutes();
        }

        if (ticket instanceof DurationTicket) {
            return memberId + "," + password + "," + name
                    + ",DURATION," + ((DurationTicket) ticket).getExpiryDate();
        }

        return memberId + "," + password + "," + name + ",NONE,-";
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

        return new Member(id, pw, name, ticket);
    }
}
