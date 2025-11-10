package ReadingRoomLogin;

// --------------------------------------------------
// 회원 클래스
// --------------------------------------------------
class Member {
    private String memberId;
    private String password;
    private String name;
    private String currentTicket; // ex) "1hour", "1day", "none"

    public Member(String id, String pw, String name, String ticket) {
        this.memberId = id;
        this.password = pw;
        this.name = name;
        this.currentTicket = ticket;
    }

    public String getId() { return memberId; }
    public String getName() { return name; }
    public void setName(String newName) { this.name = newName; }

    public boolean checkPassword(String pw) {
        return this.password.equals(pw);
    }

    public void setTicket(String ticket) {
        this.currentTicket = ticket;
    }

    public String getTicket() {
        return currentTicket;
    }

    public boolean hasValidTicket() {
        return currentTicket != null && !currentTicket.equals("none");
    }

    public String toFileString() {
        return memberId + "," + password + "," + name + "," + currentTicket;
    }

    public static Member fromFileString(String line) {
        String[] parts = line.split(",");
        if (parts.length < 4) return null;
        return new Member(parts[0], parts[1], parts[2], parts[3]);
    }
}