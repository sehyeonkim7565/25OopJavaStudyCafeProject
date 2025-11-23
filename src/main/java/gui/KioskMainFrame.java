package gui;

import KioskService.*;
import ReadingRoomLogin.Member;
import ReadingRoomLogin.MemberManager;
import Seat.Seat;
import SeatManager.SeatFactory;
import SeatManager.SeatManager;
import payment.*;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import java.awt.CardLayout;
import java.util.List;

public class KioskMainFrame extends JFrame {

    private CardLayout cardLayout;
    private JPanel mainPanelContainer;

    // 관리자 객체들 
    private MemberManager memberManager;
    private PriceManager priceManager;
    private SeatManager seatManager;
    private SessionManager sessionManager;
    private CheckInService checkInService;
    private CheckOutService checkOutService;
    private BreakService breakService;
    private PurchaseService purchaseService;
    private PaymentService paymentService;
    private TicketFactory ticketFactory;
    private ILogManager logManager;

    // 현재 로그인한 회원 정보
    private Member currentMember;

    public static final String LOGIN_PANEL = "Login";
    public static final String MAIN_MENU_PANEL = "MainMenu";
    public static final String TICKET_SELECTION_PANEL = "TicketSelection";
    public static final String SEAT_MAP_PANEL = "SeatMap";
    public static final String DAILY_TICKET_PANEL = "DailyTicket";
    public static final String PASS_PURCHASE_PANEL = "PassPurchase";


    public KioskMainFrame() {
        setTitle("자리있조 스터디 카페 키오스크");
        setSize(1000, 700);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(EXIT_ON_CLOSE);

        // 관리 객체 초기화
        initializeManagersAndServices();

        // 메인 패널 컨테이너 설정
        cardLayout = new CardLayout();
        mainPanelContainer = new JPanel(cardLayout);

        LoginPanel loginPanel = new LoginPanel(this, memberManager);
        MainMenuPanel mainMenuPanel = new MainMenuPanel(this, checkInService, checkOutService, breakService, seatManager);
        TicketSelectionPanel ticketSelectionPanel = new TicketSelectionPanel(this);
        SeatMapPanel seatMapPanel = new SeatMapPanel(this, seatManager);
        DailyTicketPanel dailyTicketPanel = new DailyTicketPanel(this, priceManager);
        PassPurchasePanel passPurchasePanel = new PassPurchasePanel(this, priceManager);

        loginPanel.setName(LOGIN_PANEL);
        mainMenuPanel.setName(MAIN_MENU_PANEL);
        ticketSelectionPanel.setName(TICKET_SELECTION_PANEL);
        seatMapPanel.setName(SEAT_MAP_PANEL);
        dailyTicketPanel.setName(DAILY_TICKET_PANEL);
        passPurchasePanel.setName(PASS_PURCHASE_PANEL);

        mainPanelContainer.add(loginPanel, LOGIN_PANEL);
        mainPanelContainer.add(mainMenuPanel, MAIN_MENU_PANEL);
        mainPanelContainer.add(ticketSelectionPanel, TICKET_SELECTION_PANEL);
        mainPanelContainer.add(seatMapPanel, SEAT_MAP_PANEL);
        mainPanelContainer.add(dailyTicketPanel, DAILY_TICKET_PANEL);
        mainPanelContainer.add(passPurchasePanel, PASS_PURCHASE_PANEL);

        add(mainPanelContainer);

        // 첫 화면은 로그인 화면
        cardLayout.show(mainPanelContainer, LOGIN_PANEL);
    }

    private void initializeManagersAndServices() {
        // 다른 패키지의 클래스들을 초기화
        this.logManager = new LogManager();
        this.memberManager = new MemberManager();
        this.priceManager = new PriceManager("config/price.json"); 

        SeatFactory seatFactory = new SeatFactory(25);
        this.seatManager = new SeatManager(seatFactory.getSeatListInternal());

        // 서비스 객체 초기화
        this.paymentService = new PaymentService();
        this.ticketFactory = new TicketFactory();
        this.sessionManager = new SessionManager(seatManager);

        // 서비스 객체 생성
        this.checkInService = new CheckInService(memberManager, seatManager, sessionManager);
        this.checkOutService = new CheckOutService(seatManager, sessionManager, logManager); // LogManager 주입 (수정 필요)
        this.breakService = new BreakService(seatManager, sessionManager);

        this.purchaseService = new PurchaseService(priceManager, paymentService, ticketFactory, memberManager, logManager);
    }

    public void showPanel(String panelName) {
        if (panelName.equals(SEAT_MAP_PANEL)) {
            JPanel panel = findPanelByName(SEAT_MAP_PANEL);
            if (panel instanceof SeatMapPanel) {
                ((SeatMapPanel) panel).updateSeatStatus();
            }
        }
        cardLayout.show(mainPanelContainer, panelName);
    }

    private JPanel findPanelByName(String panelName) {
        for (java.awt.Component comp : mainPanelContainer.getComponents()) {
            if (comp.getName() != null && comp.getName().equals(panelName)) {
                return (JPanel) comp;
            }
        }
        return null;
    }

    // 현재 사용자 설정
    public void setCurrentMember(Member member) {
        this.currentMember = member;
    }

    // 현재 사용자 정보 가져오기
    public Member getCurrentMember() {
        return this.currentMember;
    }

    // SeatManager getter (SeatMapPanel에서 사용)
    public SeatManager getSeatManager() {
        return this.seatManager;
    }
    
    // PriceManager getter (Ticket 패널들에서 사용)
    public PriceManager getPriceManager() {
        return this.priceManager;
    }
    
    // MemberManager getter (LoginPanel에서 사용)
    public MemberManager getMemberManager() {
        return this.memberManager;
    }

    public CheckInService getCheckInService() { return checkInService; }
    public PurchaseService getPurchaseService() { return purchaseService; }

    // 프로그램 시작
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            new KioskMainFrame().setVisible(true);
        });
    }
}