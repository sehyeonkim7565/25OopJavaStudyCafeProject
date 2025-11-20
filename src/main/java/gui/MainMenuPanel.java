package gui;

import KioskService.*;
import SeatManager.SeatManager;
import ReadingRoomLogin.Member;

import javax.swing.*;
import java.awt.*;

public class MainMenuPanel extends JPanel {

    private KioskMainFrame mainFrame;
    private JLabel welcomeLabel;

    private SeatManager seatManager;
    private CheckInService checkInService;
    private CheckOutService checkOutService;
    private BreakService breakService;

    public MainMenuPanel(KioskMainFrame mainFrame, CheckInService checkIn, CheckOutService checkOut, BreakService breakSvc, SeatManager seatManager) {
        this.mainFrame = mainFrame;
        this.checkInService = checkIn;
        this.checkOutService = checkOut;
        this.breakService = breakSvc;
        this.seatManager = seatManager;

        setLayout(new BorderLayout());
        setBackground(Theme.BACKGROUND_COLOR);

        // 환영
        welcomeLabel = new JLabel("", SwingConstants.CENTER);
        Theme.styleLabel(welcomeLabel, Theme.SUBTITLE_FONT);
        welcomeLabel.setBorder(BorderFactory.createEmptyBorder(40, 0, 40, 0));
        add(welcomeLabel, BorderLayout.NORTH);

        // 중앙 버튼
        JPanel buttonPanel = new JPanel(new GridLayout(3, 2, 20, 20));
        buttonPanel.setBackground(Theme.BACKGROUND_COLOR);
        buttonPanel.setBorder(BorderFactory.createEmptyBorder(20, 50, 50, 50));

        JButton checkInBtn = new JButton("입실 / 좌석배정");
        JButton checkOutBtn = new JButton("퇴실");
        JButton breakBtn = new JButton("자리 이동하기");
        JButton extendBtn = new JButton("시간 연장");
        JButton orderBtn = new JButton("상품 주문 (준비중)");
        JButton logoutBtn = new JButton("로그아웃");

        Theme.styleButton(checkInBtn);
        Theme.styleButton(checkOutBtn);
        Theme.styleButton(breakBtn);
        Theme.styleButton(extendBtn);

        Theme.styleSecondaryButton(orderBtn); // 준비중인건 회색으로
        orderBtn.setEnabled(false);

        Theme.styleSecondaryButton(logoutBtn);
        logoutBtn.setBackground(new Color(200, 100, 100)); // 로그아웃: 붉은 계열

        buttonPanel.add(checkInBtn);
        buttonPanel.add(checkOutBtn);
        buttonPanel.add(breakBtn);
        buttonPanel.add(extendBtn);
        buttonPanel.add(orderBtn);
        buttonPanel.add(logoutBtn);

        add(buttonPanel, BorderLayout.CENTER);

        checkInBtn.addActionListener(e -> {
            Member member = mainFrame.getCurrentMember();
            if (member == null) {
                JOptionPane.showMessageDialog(mainFrame, "로그인이 필요합니다.");
                mainFrame.showPanel(KioskMainFrame.LOGIN_PANEL);
                return;
            }
            if (seatManager.findSeatByMember(member.getId()) != null) {
                JOptionPane.showMessageDialog(mainFrame, "이미 입실 상태입니다.");
                return;
            }
            if (member.hasValidTicket()) {
                mainFrame.showPanel(KioskMainFrame.SEAT_MAP_PANEL);
            } else {
                mainFrame.showPanel(KioskMainFrame.TICKET_SELECTION_PANEL);
            }
        });

        checkOutBtn.addActionListener(e -> {
            Member member = mainFrame.getCurrentMember();
            if (member == null) return;
            boolean success = checkOutService.checkOut(member.getId());
            if (success) {
                JOptionPane.showMessageDialog(mainFrame, "퇴실 처리되었습니다.");
            } else {
                JOptionPane.showMessageDialog(mainFrame, "입실하지 않은 회원입니다.");
            }
        });

        breakBtn.addActionListener(e -> {
            JOptionPane.showMessageDialog(mainFrame, "자리 이동 되었습니다.");
        });

        extendBtn.addActionListener(e -> {
            mainFrame.showPanel(KioskMainFrame.DAILY_TICKET_PANEL);
        });

        logoutBtn.addActionListener(e -> {
            int confirm = JOptionPane.showConfirmDialog(mainFrame, "로그아웃 하시겠습니까?", "확인", JOptionPane.YES_NO_OPTION);
            if (confirm == JOptionPane.YES_OPTION) {
                mainFrame.setCurrentMember(null);
                mainFrame.showPanel(KioskMainFrame.LOGIN_PANEL);
            }
        });
    }

    @Override
    public void addNotify() {
        super.addNotify();
        Member member = mainFrame.getCurrentMember();
        if (member != null) {
            welcomeLabel.setText(member.getName() + "님, 오늘도 열공하세요!");
        } else {
            welcomeLabel.setText("로그인이 필요합니다.");
        }
    }
}