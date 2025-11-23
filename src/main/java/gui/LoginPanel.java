package gui;

import ReadingRoomLogin.Member;
import ReadingRoomLogin.MemberManager;

import javax.swing.*;
import java.awt.*;

public class LoginPanel extends JPanel {

    private JTextField idField;
    private JPasswordField pwField;
    private KioskMainFrame mainFrame;
    private MemberManager manager;

    public LoginPanel(KioskMainFrame mainFrame, MemberManager manager) {
        this.mainFrame = mainFrame;
        this.manager = manager;

        setLayout(new BorderLayout());
        setBackground(Theme.BACKGROUND_COLOR); //배경색

        //제목
        JLabel titleLabel = new JLabel("스터디 카페 키오스크", SwingConstants.CENTER);
        Theme.styleLabel(titleLabel, Theme.TITLE_FONT);
        titleLabel.setBorder(BorderFactory.createEmptyBorder(50, 0, 30, 0));
        add(titleLabel, BorderLayout.NORTH);

        //입력
        JPanel centerPanel = new JPanel(new GridLayout(4, 1, 10, 10));
        centerPanel.setBackground(Theme.BACKGROUND_COLOR);
        centerPanel.setBorder(BorderFactory.createEmptyBorder(20, 200, 20, 200));
        Dimension labelSize = new Dimension(100, 40);

        // 아이디
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

        // 비번
        JPanel pwPanel = new JPanel(new BorderLayout(10, 5));
        pwPanel.setBackground(Theme.BACKGROUND_COLOR);
        JLabel pwLabel = new JLabel("비밀번호",SwingConstants.RIGHT);
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

        JButton nonMemberBtn = new JButton("비회원 당일권 구매");
        Theme.styleSecondaryButton(nonMemberBtn);
        nonMemberBtn.setBackground(Theme.ACCENT_COLOR);

        buttonPanel.add(loginBtn);
        buttonPanel.add(registerBtn);
        buttonPanel.add(nonMemberBtn);

        add(buttonPanel, BorderLayout.SOUTH);

        loginBtn.addActionListener(e -> {
            String id = idField.getText().trim();
            String pw = new String(pwField.getPassword());

            Member member = manager.findMemberById(id);
            if (member != null && member.checkPassword(pw)) {
                mainFrame.setCurrentMember(member);
                if (id.equals("admin")) {
                    JOptionPane.showMessageDialog(mainFrame, "관리자 로그인 성공!");
                    new AdminFrame(manager).setVisible(true);
                } else {
                    JOptionPane.showMessageDialog(mainFrame, member.getName() + "님, 환영합니다!");
                    mainFrame.showPanel(KioskMainFrame.MAIN_MENU_PANEL);
                }
            } else {
                JOptionPane.showMessageDialog(mainFrame, "아이디 또는 비밀번호가 틀렸습니다.");
            }
            pwField.setText("");
        });

        registerBtn.addActionListener(e -> new RegisterFrame(manager).setVisible(true));
        nonMemberBtn.addActionListener(e -> {
            mainFrame.setCurrentMember(null);
            JOptionPane.showMessageDialog(mainFrame, "당일 시간제 이용 화면으로 이동합니다.");
            mainFrame.showPanel(KioskMainFrame.DAILY_TICKET_PANEL);
        });
    }
}