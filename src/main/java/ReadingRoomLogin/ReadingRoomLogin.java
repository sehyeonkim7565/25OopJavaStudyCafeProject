package ReadingRoomLogin;

import javax.swing.*;
import java.awt.GridLayout;


public class ReadingRoomLogin extends JFrame {
    private JTextField idField;
    private JPasswordField pwField;
    private MemberManager manager = new MemberManager();

    public ReadingRoomLogin() {
        setTitle("독서실 좌석 예약 - 로그인");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(400, 250);
        setLocationRelativeTo(null);

        JPanel panel = new JPanel(new GridLayout(4, 2, 10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(20, 30, 20, 30));

        panel.add(new JLabel("아이디:"));
        idField = new JTextField();
        panel.add(idField);

        panel.add(new JLabel("비밀번호:"));
        pwField = new JPasswordField();
        panel.add(pwField);

        JButton loginBtn = new JButton("로그인");
        JButton registerBtn = new JButton("회원가입");

        panel.add(loginBtn);
        panel.add(registerBtn);

        add(panel);

        // 로그인 버튼
        loginBtn.addActionListener(e -> {
            String id = idField.getText().trim();
            String pw = new String(pwField.getPassword());

            Member member = manager.findMemberById(id);
            if (member != null && member.checkPassword(pw)) {
                if (id.equals("admin")) {
                    JOptionPane.showMessageDialog(this, "관리자 로그인 성공!");
                    new AdminFrame(manager).setVisible(true);
                    dispose();
                } else {
                    JOptionPane.showMessageDialog(this, "로그인 성공!");
                    new UserMenuFrame(member, manager).setVisible(true);
                    dispose();
                }
            } else {
                JOptionPane.showMessageDialog(this, "아이디 또는 비밀번호가 틀렸습니다.");
            }
        });

        // 회원가입 버튼
        registerBtn.addActionListener(e -> new RegisterFrame(manager).setVisible(true));
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            new ReadingRoomLogin().setVisible(true);
        });
    }
}
