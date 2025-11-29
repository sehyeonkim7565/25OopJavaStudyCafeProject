
package ReadingRoomLogin;

// --------------------------------------------------
// 회원가입 화면
// --------------------------------------------------

import java.awt.GridLayout;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPasswordField;
import javax.swing.JTextField;

class RegisterFrame extends JFrame {
    private JTextField idField, nameField;
    private JPasswordField pwField;
    private MemberManager manager;

    public RegisterFrame(MemberManager manager) {
        this.manager = manager;
        setTitle("회원가입");
        setSize(400, 300);
        setLocationRelativeTo(null);
        setLayout(new GridLayout(5, 2, 10, 10));
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);

        add(new JLabel("아이디:"));
        idField = new JTextField();
        add(idField);

        add(new JLabel("비밀번호:"));
        pwField = new JPasswordField();
        add(pwField);

        add(new JLabel("이름:"));
        nameField = new JTextField();
        add(nameField);

        JButton registerBtn = new JButton("등록");
        add(registerBtn);

        registerBtn.addActionListener(e -> {
            String id = idField.getText().trim();
            String pw = new String(pwField.getPassword());
            String name = nameField.getText().trim();

            if (id.isEmpty() || pw.isEmpty() || name.isEmpty()) {
                JOptionPane.showMessageDialog(this, "모든 항목을 입력해주세요.");
                return;
            }

            if (manager.findMemberById(id) != null) {
                JOptionPane.showMessageDialog(this, "이미 존재하는 아이디입니다.");
                return;
            }

            // 기존
            // manager.addMember(new Member(id, pw, name, "none"));

            // 수정 (티켓 null)
            manager.addMember(new Member(id, pw, name, null));

            JOptionPane.showMessageDialog(this, "회원가입 완료!");
            dispose();
        });
    }
}
