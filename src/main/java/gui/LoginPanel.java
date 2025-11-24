package gui;

import ReadingRoomLogin.Member;
import ReadingRoomLogin.MemberManager;

import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;
import javax.swing.JLabel;
import javax.swing.JButton;
import javax.swing.JOptionPane;
import javax.swing.BorderFactory;
import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.awt.Dimension;

public class LoginPanel extends JPanel {

    private JTextField idField;
    private JPasswordField pwField;
    private KioskMainFrame mainFrame; 
    private MemberManager manager;

    public LoginPanel(KioskMainFrame mainFrame, MemberManager manager) {
        this.mainFrame = mainFrame;
        this.manager = manager;

        setLayout(new BorderLayout());
        setBackground(Theme.BACKGROUND_COLOR);

        // 제목
        JLabel titleLabel = new JLabel("스터디 카페 키오스크", javax.swing.SwingConstants.CENTER);
        Theme.styleLabel(titleLabel, Theme.TITLE_FONT);
        titleLabel.setBorder(BorderFactory.createEmptyBorder(50, 0, 30, 0));
        add(titleLabel, BorderLayout.NORTH);

        // 입력 영역
        JPanel centerPanel = new JPanel(new GridLayout(4, 1, 10, 10));
        centerPanel.setBackground(Theme.BACKGROUND_COLOR);
        centerPanel.setBorder(BorderFactory.createEmptyBorder(20, 200, 20, 200));
        Dimension labelSize = new Dimension(100, 40);

        JPanel idPanel = new JPanel(new BorderLayout(10, 5));
        idPanel.setBackground(Theme.BACKGROUND_COLOR);
        JLabel idLabel = new JLabel("아이디  ");
        Theme.styleLabel(idLabel, Theme.MAIN_FONT);
        idLabel.setPreferredSize(labelSize);
        idField = new JTextField();
        idField.setFont(Theme.MAIN_FONT);
        idField.setPreferredSize(new Dimension(0, 40));
        idPanel.add(idLabel, BorderLayout.WEST);
        idPanel.add(idField, BorderLayout.CENTER);

        JPanel pwPanel = new JPanel(new BorderLayout(10, 5));
        pwPanel.setBackground(Theme.BACKGROUND_COLOR);
        JLabel pwLabel = new JLabel("비밀번호", javax.swing.SwingConstants.RIGHT);
        Theme.styleLabel(pwLabel, Theme.MAIN_FONT);
        pwLabel.setPreferredSize(labelSize);
        pwField = new JPasswordField();
        pwField.setPreferredSize(new Dimension(0, 40));
        pwPanel.add(pwLabel, BorderLayout.WEST);
        pwPanel.add(pwField, BorderLayout.CENTER);

        centerPanel.add(idPanel);
        centerPanel.add(pwPanel);
        centerPanel.add(new JLabel(""));

        add(centerPanel, BorderLayout.CENTER);

        // 하단 버튼
        JPanel buttonPanel = new JPanel(new GridLayout(3, 1, 10, 10));
        buttonPanel.setBackground(Theme.BACKGROUND_COLOR);
        buttonPanel.setBorder(BorderFactory.createEmptyBorder(20, 200, 80, 200));

        JButton loginBtn = new JButton("로그인");
        Theme.styleButton(loginBtn);

        JButton registerBtn = new JButton("신규 회원가입");
        Theme.styleSecondaryButton(registerBtn);

        buttonPanel.add(loginBtn);
        buttonPanel.add(registerBtn);
        buttonPanel.add(new JLabel());

        add(buttonPanel, BorderLayout.SOUTH);

        // 로그인 버튼
        loginBtn.addActionListener(e -> {
            String id = idField.getText().trim();
            String pw = new String(pwField.getPassword());

            Member member = manager.findMemberById(id);
            if (id.equals("admin") && pw.equals("admin")) {
                JOptionPane.showMessageDialog(mainFrame, "관리자 로그인 성공!");
                new AdminFrame(manager).setVisible(true);
            } else if (member != null && member.checkPassword(pw)) {
                mainFrame.setCurrentMember(member);
                JOptionPane.showMessageDialog(mainFrame, member.getName() + "님, 환영합니다!");
                mainFrame.showPanel(KioskMainFrame.MAIN_MENU_PANEL);
            } else {
                JOptionPane.showMessageDialog(mainFrame, "아이디 또는 비밀번호가 틀렸습니다.");
            }

            pwField.setText("");
        });

        // 회원가입 버튼 
        registerBtn.addActionListener(e -> new RegisterFrame(manager).setVisible(true));

    }
}
