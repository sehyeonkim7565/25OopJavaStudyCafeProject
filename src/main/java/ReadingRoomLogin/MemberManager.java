
package ReadingRoomLogin;
// --------------------------------------------------
// 회원 관리자 (파일 입출력 담당)
// --------------------------------------------------

import Ticket.Ticket;
import payment.IMemberManager;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;

public class MemberManager implements IMemberManager { //public 추가 11/13
    private ArrayList<Member> members = new ArrayList<>();
    private final File file = new File("members.txt");

    // 11/17 생성자 추가: 프로그램 시작 시 파일에서 회원 정보 로드
    public MemberManager() {
    	loadMembers();
    }

    @Override
    public Member findMemberById(String id) {
        for (Member m : members) {
            if (m.getId().equals(id)) return m;
        }
        return null;
    }

    @Override
    public Member login(String id, String password) {
        Member m = findMemberById(id);
        if (m != null && m.checkPassword(password)) {
            return m;
        }
        return null;
    }

    @Override
    public void register(String id, String password, String name) {
        // 11/17 중복 ID 체크 추가
        if (findMemberById(id) != null) {
            System.err.println("ID " + id + "는 이미 존재합니다.");
            return; 
        }
        addMember(new Member(id, password, name, null));
    }

    @Override
    public void setTicket(String memberID, Ticket newTicket) {
        Member m = findMemberById(memberID);
        if (m != null) {
            m.setTicket(newTicket);
            saveMembers();
        }
    }

    @Override
    public void saveMembersToFile() {
        saveMembers();
    }

    public void loadMembers() {
        members.clear();
        if (!file.exists()) return;
        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = br.readLine()) != null) {
                Member m = Member.fromFileString(line);
                if (m != null) members.add(m);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void saveMembers() {
        try (PrintWriter pw = new PrintWriter(new FileWriter(file))) {
            for (Member m : members) pw.println(m.toFileString());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void addMember(Member m) {
        members.add(m);
        saveMembers();
    }

    public ArrayList<Member> getAllMembers() {
        return members;
    }

    public void removeMember(Member m) {
        members.remove(m);
        saveMembers();
    }
}
