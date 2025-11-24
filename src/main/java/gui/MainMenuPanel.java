package gui; 

import KioskService.*;
import SeatManager.SeatManager;
import ReadingRoomLogin.Member;
import Ticket.DurationTicket;
import Ticket.TimeTicket;
import Seat.UsageSession;
import KioskService.SeatMoveService;

import javax.swing.JPanel;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.SwingConstants;
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.Timer;
import java.awt.GridLayout;
import java.awt.BorderLayout;
import java.awt.Color;

public class MainMenuPanel extends JPanel {

    private KioskMainFrame mainFrame;
    private JLabel welcomeLabel;
    private JLabel sessionInfoLabel;

    private SeatManager seatManager;
    private CheckInService checkInService;
    private CheckOutService checkOutService;
    private SessionManager sessionManager;
    private SeatMoveService seatMoveService;
    private Timer sessionTimer;

    public MainMenuPanel(KioskMainFrame mainFrame, CheckInService checkIn, CheckOutService checkOut, SeatManager seatManager, SessionManager sessionManager, SeatMoveService seatMoveService) {
        this.mainFrame = mainFrame;
        this.checkInService = checkIn;
        this.checkOutService = checkOut;
        this.seatManager = seatManager;
        this.sessionManager = sessionManager;
        this.seatMoveService = seatMoveService;

        setLayout(new BorderLayout());
        setBackground(Theme.BACKGROUND_COLOR);

        JPanel headerPanel = new JPanel();
        headerPanel.setLayout(new BoxLayout(headerPanel, BoxLayout.Y_AXIS));
        headerPanel.setBackground(Theme.BACKGROUND_COLOR);

        welcomeLabel = new JLabel("", SwingConstants.CENTER);
        Theme.styleLabel(welcomeLabel, Theme.TITLE_FONT);
        welcomeLabel.setBorder(BorderFactory.createEmptyBorder(30, 0, 10, 0));
        welcomeLabel.setAlignmentX(CENTER_ALIGNMENT);

        sessionInfoLabel = new JLabel("", SwingConstants.CENTER);
        Theme.styleLabel(sessionInfoLabel, Theme.MAIN_FONT);
        sessionInfoLabel.setBorder(BorderFactory.createEmptyBorder(0, 0, 20, 0));
        sessionInfoLabel.setAlignmentX(CENTER_ALIGNMENT);

        headerPanel.add(welcomeLabel);
        headerPanel.add(sessionInfoLabel);
        add(headerPanel, BorderLayout.NORTH);

        JPanel buttonPanel = new JPanel(new GridLayout(3, 2, 20, 20));
        buttonPanel.setBackground(Theme.BACKGROUND_COLOR);
        buttonPanel.setBorder(BorderFactory.createEmptyBorder(20, 50, 50, 50));
        
        JButton checkInBtn = new JButton("입실 / 좌석배정");
        JButton checkOutBtn = new JButton("퇴실");
        JButton extendBtn = new JButton("시간 연장");
        JButton orderBtn = new JButton("상품 주문");
        JButton logoutBtn = new JButton("로그아웃");
        JButton placeholderBtn = new JButton("자리 이동하기");

        buttonPanel.add(checkInBtn);
        buttonPanel.add(checkOutBtn);
        buttonPanel.add(placeholderBtn);
        buttonPanel.add(extendBtn);
        buttonPanel.add(orderBtn);
        buttonPanel.add(logoutBtn);
        
        add(buttonPanel, BorderLayout.CENTER);

        Theme.styleButton(checkInBtn);
        Theme.styleButton(checkOutBtn);
        Theme.styleButton(extendBtn);
        Theme.styleButton(placeholderBtn);
        Theme.styleButton(orderBtn);
        Theme.styleSecondaryButton(logoutBtn);
        logoutBtn.setBackground(new Color(200, 100, 100));

        checkInBtn.addActionListener(e -> {
            System.out.println("--- 입실 버튼 클릭 ---");
            Member member = mainFrame.getCurrentMember(); // Member 객체 사용
            
            if (member == null) {
                System.out.println("멤버가 null입니다. 로그인 화면으로 전환합니다.");
                JOptionPane.showMessageDialog(mainFrame, "로그인이 필요합니다.");
                mainFrame.showPanel(KioskMainFrame.LOGIN_PANEL);
                return;
            }
            
            // 디버깅: 서비스 객체 null 체크
            if (seatManager == null) {
                System.err.println("SeatManager가 null입니다!");
                JOptionPane.showMessageDialog(mainFrame, "시스템 오류: 좌석 관리 객체 초기화 실패.");
                return;
            }

            if (seatManager.findSeatByMember(member.getId()) != null) {
                System.out.println("이미 입실 상태입니다.");
                JOptionPane.showMessageDialog(mainFrame, "이미 입실 상태입니다.");
                return;
            }
            
            System.out.println("유효 티켓 확인: " + member.hasValidTicket());
            if (member.hasValidTicket()) {
                // 일반 입실 흐름에서는 이동 모드 해제
                mainFrame.endSeatMoveMode();
                mainFrame.showPanel(KioskMainFrame.SEAT_MAP_PANEL); 
            } else {
                mainFrame.showPanel(KioskMainFrame.TICKET_SELECTION_PANEL);
            }
        });

        checkOutBtn.addActionListener(e -> {
            Member member = mainFrame.getCurrentMember();
            if (member == null) return;

            // 4. 실제 서비스 호출
            boolean success = checkOutService.checkOut(member.getId()); //
            if (success) {
                JOptionPane.showMessageDialog(mainFrame, "퇴실 처리되었습니다.");
            } else {
                JOptionPane.showMessageDialog(mainFrame, "입실하지 않은 회원입니다.");
            }
        });


        extendBtn.addActionListener(e -> {
            Member member = mainFrame.getCurrentMember();
            if (member == null) {
                JOptionPane.showMessageDialog(mainFrame, "로그인이 필요합니다.");
                mainFrame.showPanel(KioskMainFrame.LOGIN_PANEL);
                return;
            }

            if (member.getTicket() instanceof TimeTicket) {
                JOptionPane.showMessageDialog(mainFrame, "시간권 연장 화면으로 이동합니다.");
                mainFrame.showPassPurchaseForTime();
            } else if (member.getTicket() instanceof DurationTicket) {
                JOptionPane.showMessageDialog(mainFrame, "기간권/정기권 연장 화면으로 이동합니다.");
                mainFrame.showPassPurchaseForDuration();
            } else {
                // 티켓이 없으면 기존 흐름 유지
                JOptionPane.showMessageDialog(mainFrame, "이용권이 없습니다. 구매 화면으로 이동합니다.");
                mainFrame.showPanel(KioskMainFrame.TICKET_SELECTION_PANEL);
            }
        });

        // 11/17 주문 버튼 리스너 구현
        orderBtn.addActionListener(e -> {
        	mainFrame.showPanel(KioskMainFrame.SHOP_PANEL);
        }); 

        placeholderBtn.addActionListener(e -> {
            Member member = mainFrame.getCurrentMember();
            if (member == null) {
                JOptionPane.showMessageDialog(mainFrame, "로그인이 필요합니다.");
                return;
            }
            JOptionPane.showMessageDialog(mainFrame, "이동할 좌석을 선택하세요.");
            mainFrame.startSeatMoveMode();
        });

        logoutBtn.addActionListener(e -> {
            int confirm = JOptionPane.showConfirmDialog(mainFrame, "로그아웃 하시겠습니까?", "확인", JOptionPane.YES_NO_OPTION);
            if (confirm == JOptionPane.YES_OPTION) {
                mainFrame.setCurrentMember(null);
                mainFrame.showPanel(KioskMainFrame.LOGIN_PANEL);
            }
        });

        // 30초마다 진행 중 세션 시간 갱신
        sessionTimer = new Timer(30_000, e -> refreshSessionInfo());
        sessionTimer.start();
    }

    // 11/17 updateWelcomeMessage 메인메뉴 라벨 메시지 디버그 모드로 생성
    public void updateWelcomeMessage() {
    	Member member = mainFrame.getCurrentMember();
        
        if(member != null) {
            String message = member.getName() + "님, 오늘도 열공하세요!";
            welcomeLabel.setText(message);
            System.out.println("[MainMenuPanel Debug] 메시지 설정 성공: " + message);
            refreshSessionInfo();
        } else {
            welcomeLabel.setText("로그인 하지 않음!");
            sessionInfoLabel.setText("");
            System.out.println("[MainMenuPanel Debug] 멤버 없음: 로그인 하지 않음!");
        }
        
        // 🌟 Label뿐만 아니라 MainMenuPanel 전체를 갱신하도록 요청
        this.revalidate(); 
        this.repaint();
    }

    @Override
    public void addNotify() {
        super.addNotify();
        if (sessionTimer != null && !sessionTimer.isRunning()) {
            sessionTimer.start();
        }
    }

    @Override
    public void removeNotify() {
        super.removeNotify();
        if (sessionTimer != null && sessionTimer.isRunning()) {
            sessionTimer.stop();
        }
    }

    private void refreshSessionInfo() {
        Member member = mainFrame.getCurrentMember();
        if (member == null) {
            sessionInfoLabel.setText("");
            return;
        }
        UsageSession session = sessionManager.getActiveSession(member.getId());
        if (session != null) {
            long minutes = session.getDurationInMinutes();
            sessionInfoLabel.setText("오늘의 공부 시간 : " + minutes + "분");
        } else {
            sessionInfoLabel.setText("");
        }
    }
}
