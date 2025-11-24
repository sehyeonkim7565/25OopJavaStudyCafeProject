package gui;

import KioskService.*;
import ReadingRoomLogin.Member;
import ReadingRoomLogin.MemberManager;
import SeatManager.SeatFactory;
import SeatManager.SeatManager;
import payment.*;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import java.awt.CardLayout;
import javax.swing.Timer;

public class KioskMainFrame extends JFrame {

    // 화면 전환용 CardLayout 및 패널 
    private CardLayout cardLayout;
    private JPanel mainPanelContainer;

    // 관리자 객체들 
    private MemberManager memberManager;
    private PriceManager priceManager;
    private SeatManager seatManager;
    private SessionManager sessionManager;
    private CheckInService checkInService;
    private CheckOutService checkOutService;
    private SeatMoveService seatMoveService;
    private PurchaseService purchaseService;
    private PaymentService paymentService;
    private TicketFactory ticketFactory;
    private ILogManager logManager;
    private PassPurchasePanel passPurchasePanel;
    private Timer usageLogTimer;
    private boolean seatMoveMode = false;

    private MainMenuPanel mainMenuPanel; // 11/17 클래스 멤버 변수 선언
    
    // 현재 로그인한 회원 정보
    private Member currentMember;

    // 패널 이름 상수 (같은 패키지 내에서 공유)
    public static final String LOGIN_PANEL = "Login";
    public static final String MAIN_MENU_PANEL = "MainMenu";
    public static final String TICKET_SELECTION_PANEL = "TicketSelection";
    public static final String SEAT_MAP_PANEL = "SeatMap";
    public static final String DAILY_TICKET_PANEL = "DailyTicket";
    public static final String PASS_PURCHASE_PANEL = "PassPurchase";
    public static final String SHOP_PANEL = "Shop"; // 11/17 상품 주문 추가


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
        // 11/17 로컬 변수 선언을 제거하고 멤버변수 할당
        this.mainMenuPanel = new MainMenuPanel(this, checkInService, checkOutService, seatManager, sessionManager, seatMoveService);
        TicketSelectionPanel ticketSelectionPanel = new TicketSelectionPanel(this);
        SeatMapPanel seatMapPanel = new SeatMapPanel(this, seatManager, checkInService, seatMoveService);
        // 결제/구매 흐름에서도 PurchaseService를 사용하도록 주입
        DailyTicketPanel dailyTicketPanel = new DailyTicketPanel(this, priceManager, purchaseService);
        this.passPurchasePanel = new PassPurchasePanel(this, priceManager, purchaseService);
        ShopPanel shopPanel = new ShopPanel(this, logManager); // 상품주문 패널
        
        loginPanel.setName(LOGIN_PANEL);
        mainMenuPanel.setName(MAIN_MENU_PANEL);
        ticketSelectionPanel.setName(TICKET_SELECTION_PANEL);
        seatMapPanel.setName(SEAT_MAP_PANEL);
        dailyTicketPanel.setName(DAILY_TICKET_PANEL);
        passPurchasePanel.setName(PASS_PURCHASE_PANEL);
        shopPanel.setName(SHOP_PANEL); // 11/17

        mainPanelContainer.add(loginPanel, LOGIN_PANEL);
        mainPanelContainer.add(mainMenuPanel, MAIN_MENU_PANEL);
        mainPanelContainer.add(ticketSelectionPanel, TICKET_SELECTION_PANEL);
        mainPanelContainer.add(seatMapPanel, SEAT_MAP_PANEL);
        mainPanelContainer.add(dailyTicketPanel, DAILY_TICKET_PANEL);
        mainPanelContainer.add(passPurchasePanel, PASS_PURCHASE_PANEL);
        mainPanelContainer.add(shopPanel, SHOP_PANEL); // 11/17

        add(mainPanelContainer);

        // 첫 화면은 로그인 화면
        cardLayout.show(mainPanelContainer, LOGIN_PANEL);

        // 진행 중 세션의 경과 시간을 usage.jsonl에 1분마다 갱신
        usageLogTimer = new Timer(60_000, e -> {
            if (logManager != null) {
                logManager.refreshOngoingUsageDurations();
            }
        });
        usageLogTimer.start();
    }

    private void initializeManagersAndServices() {
        // 다른 패키지의 클래스들을 초기화
        this.logManager = new LogManager();
        this.memberManager = new MemberManager();
        this.priceManager = new PriceManager("config/price.json"); 

        SeatFactory seatFactory = new SeatFactory(25); // 25개 좌석 예시
        this.seatManager = new SeatManager(seatFactory.getSeatListInternal());

        // 서비스 객체 초기화
        this.paymentService = new PaymentService();
        this.ticketFactory = new TicketFactory();
        this.sessionManager = new SessionManager(seatManager);

        // 서비스 객체 생성 (의존성 주입)
        this.checkInService = new CheckInService(memberManager, seatManager, sessionManager, logManager);
        this.checkOutService = new CheckOutService(seatManager, sessionManager, memberManager, logManager); // 로그/회원 정보 주입
        this.seatMoveService = new SeatMoveService(seatManager, sessionManager, logManager);
        this.purchaseService = new PurchaseService(priceManager, paymentService, ticketFactory, memberManager, logManager);
    }

    // 화면을 전환하는 공용 메서드
    public void showPanel(String panelName) {
        if (panelName.equals(SEAT_MAP_PANEL)) {
            JPanel panel = findPanelByName(SEAT_MAP_PANEL);
            // 11/17 panel이 null인지 확인 추가
            if (panel != null && panel instanceof SeatMapPanel) {
                ((SeatMapPanel) panel).updateSeatStatus();
            }
        }
        cardLayout.show(mainPanelContainer, panelName);

        // 11/17 메인 메뉴 패널로 전활될 때 환영 메시지 업데이트 호출
        if (panelName.equals(MAIN_MENU_PANEL)) {      
            SwingUtilities.invokeLater(() -> {
                this.mainMenuPanel.updateWelcomeMessage(); 
            });
        }
    }

    // 패널 이름으로 객체를 찾는 헬퍼 메서드
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

    // 좌석 이동 모드 제어
    public void startSeatMoveMode() {
        seatMoveMode = true;
        showPanel(SEAT_MAP_PANEL);
    }

    public void endSeatMoveMode() {
        seatMoveMode = false;
    }

    public boolean isSeatMoveMode() {
        return seatMoveMode;
    }

    // 보유 티켓 유형에 맞춰 정기/시간권 탭을 선택 후 화면 표시
    public void showPassPurchaseForTime() {
        if (passPurchasePanel != null) {
            passPurchasePanel.setBackToMainMenu(true);
            passPurchasePanel.selectTimeTab();
        }
        showPanel(PASS_PURCHASE_PANEL);
    }

    public void showPassPurchaseForDuration() {
        if (passPurchasePanel != null) {
            passPurchasePanel.setBackToMainMenu(true);
            passPurchasePanel.selectDurationTab();
        }
        showPanel(PASS_PURCHASE_PANEL);
    }

    // 티켓이 없는 사용자가 구매 흐름에서 진입할 때 (뒤로가기 시 종류 선택 화면으로)
    public void showPassPurchaseForSelection() {
        if (passPurchasePanel != null) {
            passPurchasePanel.setBackToMainMenu(false);
        }
        showPanel(PASS_PURCHASE_PANEL);
    }

    // 프로그램 시작
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            new KioskMainFrame().setVisible(true);
        });
    }
}
