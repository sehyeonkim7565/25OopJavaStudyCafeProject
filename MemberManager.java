package ReadingRoomLogin;
// --------------------------------------------------
// 회원 관리자 (파일 입출력 담당)
// --------------------------------------------------

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;

class MemberManager {
    private ArrayList<Member> members = new ArrayList<>();
    private final File file = new File("members.txt");

    public MemberManager() {
        loadMembers();
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

    public Member findMemberById(String id) {
        for (Member m : members) {
            if (m.getId().equals(id)) return m;
        }
        return null;
    }

    public ArrayList<Member> getAllMembers() {
        return members;
    }

    public void removeMember(Member m) {
        members.remove(m);
        saveMembers();
    }
}