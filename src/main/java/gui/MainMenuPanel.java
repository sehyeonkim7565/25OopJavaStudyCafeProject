package gui; 

import KioskService.*;
import SeatManager.SeatManager;
import ReadingRoomLogin.Member;

import javax.swing.JPanel;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.SwingConstants;
import java.awt.GridLayout;
import java.awt.BorderLayout;

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

        setLayout(new BorderLayout(10, 10));

        welcomeLabel = new JLabel("", SwingConstants.CENTER);
        add(welcomeLabel, BorderLayout.NORTH);

        JPanel buttonPanel = new JPanel(new GridLayout(6, 1, 10, 10));
        
        JButton checkInBtn = new JButton("입실");
        JButton checkOutBtn = new JButton("퇴실");
        JButton breakBtn = new JButton("외출/복귀");
        JButton extendBtn = new JButton("시간 연장(당일권)");
        JButton orderBtn = new JButton("상품 주문 (준비중)");
        JButton logoutBtn = new JButton("로그아웃");

        buttonPanel.add(checkInBtn);
        buttonPanel.add(checkOutBtn);
        buttonPanel.add(breakBtn);
        buttonPanel.add(extendBtn);
        buttonPanel.add(orderBtn);
        buttonPanel.add(logoutBtn);
        
        add(buttonPanel, BorderLayout.CENTER);
        
        // addNotify(); 

        // 11/17 checkInBtn 디버깅 모드로 수정
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


        // 아래 버튼도 유사하게 수정 필요
        breakBtn.addActionListener(e -> {
            JOptionPane.showMessageDialog(mainFrame, "외출/복귀 처리되었습니다.");
        });

        extendBtn.addActionListener(e -> {
            mainFrame.showPanel(KioskMainFrame.DAILY_TICKET_PANEL);
        });

        // 11/17 주문 버튼 리스너 구현
        orderBtn.addActionListener(e -> {
        	mainFrame.showPanel(KioskMainFrame.SHOP_PANEL);
        }); 

        logoutBtn.addActionListener(e -> {
            int confirm = JOptionPane.showConfirmDialog(mainFrame, "로그아웃 하시겠습니까?", "확인", JOptionPane.YES_NO_OPTION);
            if (confirm == JOptionPane.YES_OPTION) {
                mainFrame.setCurrentMember(null);
                mainFrame.showPanel(KioskMainFrame.LOGIN_PANEL);
            }
        });

        add(buttonPanel, BorderLayout.CENTER);
    }

    // 11/17 updateWelcomeMessage 메인메뉴 라벨 메시지 디버그 모드로 생성
    public void updateWelcomeMessage() {
    	Member member = mainFrame.getCurrentMember();
        
        if(member != null) {
            String message = member.getName() + "님, 환영합니다! 🎉";
            welcomeLabel.setText(message);
            System.out.println("[MainMenuPanel Debug] 메시지 설정 성공: " + message);
        } else {
            welcomeLabel.setText("로그인 하지 않음!");
            System.out.println("[MainMenuPanel Debug] 멤버 없음: 로그인 하지 않음!");
        }
        
        // 🌟 Label뿐만 아니라 MainMenuPanel 전체를 갱신하도록 요청
        this.revalidate(); 
        this.repaint();
    }

    @Override
    public void addNotify() {
        super.addNotify();
    }
}
