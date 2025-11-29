
package gui;

import ReadingRoomLogin.Member;
import ReadingRoomLogin.MemberManager;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JOptionPane;
import javax.swing.JPasswordField;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.BorderFactory;
import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.FlowLayout;
import java.awt.Dimension;

class RegisterFrame extends JFrame {
    private JTextField idField, nameField;
    private JPasswordField pwField;
    private MemberManager manager;

    public RegisterFrame(MemberManager manager) {
        this.manager = manager;
        setTitle("신규 회원가입");
        setSize(1000, 700);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);

        Container cp = getContentPane();
        cp.setBackground(Theme.BACKGROUND_COLOR);
        cp.setLayout(new BorderLayout());

        // 제목
        JLabel titleLabel = new JLabel("회원가입", SwingConstants.CENTER);
        Theme.styleLabel(titleLabel, Theme.TITLE_FONT);
        titleLabel.setBorder(BorderFactory.createEmptyBorder(50, 0, 30, 0));
        cp.add(titleLabel, BorderLayout.NORTH);

        // 중앙 폼
        JPanel centerPanel = new JPanel(new GridBagLayout());
        centerPanel.setBackground(Theme.BACKGROUND_COLOR);

        JPanel formPanel = new JPanel(new GridLayout(3, 2, 20, 20));
        formPanel.setBackground(Theme.BACKGROUND_COLOR);
        formPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        JLabel lblId = new JLabel("아이디", SwingConstants.RIGHT);
        Theme.styleLabel(lblId, Theme.MAIN_FONT);

        idField = new JTextField();
        idField.setFont(Theme.MAIN_FONT);
        idField.setPreferredSize(new Dimension(300, 40));

        JLabel lblPw = new JLabel("비밀번호", SwingConstants.RIGHT);
        Theme.styleLabel(lblPw, Theme.MAIN_FONT);

        pwField = new JPasswordField();
        pwField.setFont(Theme.MAIN_FONT);
        pwField.setPreferredSize(new Dimension(300, 40));

        JLabel lblName = new JLabel("이름", SwingConstants.RIGHT);
        Theme.styleLabel(lblName, Theme.MAIN_FONT);

        nameField = new JTextField();
        nameField.setFont(Theme.MAIN_FONT);
        nameField.setPreferredSize(new Dimension(300, 40));

        formPanel.add(lblId);
        formPanel.add(idField);
        formPanel.add(lblPw);
        formPanel.add(pwField);
        formPanel.add(lblName);
        formPanel.add(nameField);

        centerPanel.add(formPanel);
        cp.add(centerPanel, BorderLayout.CENTER);

        // 버튼 영역
        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 20));
        btnPanel.setBackground(Theme.BACKGROUND_COLOR);
        btnPanel.setBorder(BorderFactory.createEmptyBorder(0, 0, 80, 0));

        JButton registerBtn = new JButton("가입완료");
        Theme.styleButton(registerBtn);
        registerBtn.setPreferredSize(new Dimension(200, 60));

        JButton cancelBtn = new JButton("취소");
        Theme.styleSecondaryButton(cancelBtn);
        cancelBtn.setPreferredSize(new Dimension(200, 60));

        btnPanel.add(cancelBtn);
        btnPanel.add(registerBtn);

        cp.add(btnPanel, BorderLayout.SOUTH);

        registerBtn.addActionListener(e -> {
            String id = idField.getText().trim();
            String pw = new String(pwField.getPassword());
            String name = nameField.getText().trim();

            if (id.isEmpty() || pw.isEmpty() || name.isEmpty()) {
                JOptionPane.showMessageDialog(this, "모든 항목을 입력해주세요.", "입력 오류", JOptionPane.WARNING_MESSAGE);
                return;
            }

            if (manager.findMemberById(id) != null) {
                JOptionPane.showMessageDialog(this, "이미 존재하는 아이디입니다.", "아이디 중복", JOptionPane.WARNING_MESSAGE);
                return;
            }
            
            manager.addMember(new Member(id, pw, name, null));
            JOptionPane.showMessageDialog(this, "회원가입이 완료되었습니다!\n로그인 화면으로 돌아갑니다.");
            dispose();
        });

        cancelBtn.addActionListener(e -> dispose());
    }
}
