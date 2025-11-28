package gui;

import KioskService.CheckInService;
import KioskService.SeatMoveService;
import ReadingRoomLogin.Member;
import Seat.Seat;
import SeatManager.SeatManager;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SeatMapPanel extends JPanel {

    private final KioskMainFrame mainFrame;
    private final SeatManager seatManager;
    private final CheckInService checkInService;
    private final SeatMoveService seatMoveService;

    // 필터 UI 컴포넌트
    JCheckBox weakCb = new JCheckBox("약 냉난방");
    JCheckBox strongCb = new JCheckBox("강 냉난방");

    String[] zones = {"Zone 선택", "노트북", "집중"};
    JComboBox<String> zoneCombo = new JComboBox<>(zones);
    String[] seatTypes = {"좌석 유형 선택", "FLOW", "WINDOW", "TABLE", "BOOTH", "PREMIUM"};
    JComboBox<String> seatTypeCombo = new JComboBox<>(seatTypes);

    JButton resetBtn = new JButton("전체 좌석 조회");

    // 좌석 전용 영역
    private JPanel seatAreaPanel;
    private Map<String, int[]> seatPositions = new HashMap<>();

    private final int SEAT_SIZE = 45;

    public SeatMapPanel(KioskMainFrame mainFrame, SeatManager seatManager, CheckInService checkInService, SeatMoveService seatMoveService) {
        this.mainFrame = mainFrame;
        this.seatManager = seatManager;
        this.checkInService = checkInService;
        this.seatMoveService = seatMoveService;

        setLayout(null);  // 전체 절대좌표 배치

        // 필터 UI 배치
        weakCb.setBounds(20, 10, 80, 30);
        strongCb.setBounds(110, 10, 90, 30);
        zoneCombo.setBounds(210, 10, 110, 30);
        seatTypeCombo.setBounds(330, 10, 130, 30);
        resetBtn.setBounds(480, 10, 120, 30);

        add(weakCb);
        add(strongCb);
        add(zoneCombo);
        add(seatTypeCombo);
        add(resetBtn);

        // 냉난방 라디오 버튼
        weakCb.addActionListener(e -> {
            if (weakCb.isSelected()) strongCb.setSelected(false);
            runAutoFilter();
        });

        strongCb.addActionListener(e -> {
            if (strongCb.isSelected()) weakCb.setSelected(false);
            runAutoFilter();
        });
        zoneCombo.addActionListener(e -> runAutoFilter());
        seatTypeCombo.addActionListener(e -> runAutoFilter());

        // 전체보기 버튼
        resetBtn.addActionListener(e -> {
            weakCb.setSelected(false);
            strongCb.setSelected(false);
            zoneCombo.setSelectedIndex(0);
            seatTypeCombo.setSelectedIndex(0);
        
            // 전체 좌석 다시 표시
            initializeSeats();
        });

        // 좌석 배치 영역 패널
        seatAreaPanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                drawBackground(g);
            }
        };

        seatAreaPanel.setLayout(null);
        seatAreaPanel.setBounds(0, 50, 1000, 650);  // 필터 UI 아래
        seatAreaPanel.setBackground(new Color(240, 240, 240));
        add(seatAreaPanel);

        // 뒤로가기 버튼
        JButton backBtn = new JButton("뒤로가기");
        backBtn.setBounds(20, 620, 100, 40);
        add(backBtn);

        backBtn.addActionListener(e -> {
            Member member = mainFrame.getCurrentMember();
            if (member != null && member.hasValidTicket()) {
                mainFrame.showPanel(KioskMainFrame.MAIN_MENU_PANEL);
            } else {
                mainFrame.showPanel(KioskMainFrame.TICKET_SELECTION_PANEL);
            }
        });

        // 좌표 로딩 & 전체 좌석 출력
        initializeSeats();
    }

    /** 배경 요소 (영역 라벨 + 복도 라인 등) */
    private void drawBackground(Graphics g) {
        Graphics2D g2 = (Graphics2D) g;
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        g2.setColor(new Color(0, 0, 0));
        g2.setStroke(new BasicStroke(4));

        // 복도 라인 (스케치 기준)
        g2.drawLine(0, 400, 400, 400);   // 가운데 가로 복도
        
        g2.drawLine(500, 0, 500, 400);
        g2.drawLine(500, 500, 500, 600);   // 가운데 세로 복도

        //BOOTH 존 칸막이
        g2.setColor(new Color(200, 200, 200));
        int boothBaseX = 520;
        int boothBaseY = 50;
        int boothGap = 20;
        int boothSize = SEAT_SIZE;
        int boothStartX = boothBaseX - 10;
        int boothEndX = boothBaseX + boothSize + 10;
        for (int i = 0; i <= 5; i++) {
            int y = boothBaseY - 11 + i * (boothSize + boothGap);
            g2.drawLine(boothStartX, y, boothEndX, y);
        }

         //프리미엄 존 칸막이
         g2.setColor(new Color(200, 200, 200));
         int premiumBaseX = 902;
         int premiumBaseY = 50;
         int premiumGap = 20;
         int premiumSize = SEAT_SIZE;
         int premiumStartX = premiumBaseX - 10;
         int premiumEndX = premiumBaseX + premiumSize + 8;
         for (int i = 0; i <= 8; i++) {
             int y = premiumBaseY - 11 + i * (premiumSize + premiumGap);
             g2.drawLine(premiumStartX, y, premiumEndX, y);
         }
         g2.drawLine(premiumStartX, 39, premiumStartX, 559);


         //집중존 플로우 좌석 칸막이
        g2.setColor(new Color(200, 200, 200));
        g2.drawLine(735, 50, 735, 290);
        g2.drawLine(735, 390, 735, 565);

        // 라벨 텍스트
        g2.setColor(Color.DARK_GRAY);
        g2.setFont(new Font("돋움", Font.BOLD, 22));

        g2.drawString("노트북 ZONE", 150, 370);
        g2.drawString("Window", 200, 40);
        g2.drawString("Window", 700, 40);
        g2.drawString("집중 ZONE", 520, 550);
        g2.drawString("휴게실", 200, 520);
    }

    /** 둥근 좌석 버튼 클래스 */
    private class RoundSeatButton extends JButton {

        private Color normalColor;   // 기본 배경색
        private Color hoverColor;    // hover 시 색상

        public RoundSeatButton(String text) {

            super(text);
            setBorderPainted(false);
            setContentAreaFilled(false);
            setFocusPainted(false);
            setOpaque(false);
            setFont(new Font("맑은 고딕", Font.BOLD, 14));
            setForeground(Color.WHITE);
            setHorizontalAlignment(SwingConstants.CENTER);
            setVerticalAlignment(SwingConstants.CENTER);

            hoverColor = new Color(96, 195, 100);  // 밝은 초록색

            addMouseListener(new java.awt.event.MouseAdapter() {

                @Override
                public void mouseEntered(java.awt.event.MouseEvent e) {
                    if (isEnabled()) {
                        setBackground(hoverColor);
                        repaint();
                    }
                }

                @Override
                public void mouseExited(java.awt.event.MouseEvent e) {
                    if (isEnabled()) {
                        setBackground(normalColor);
                        repaint();
                    }
                }
            });
        }

        /** 구역색을 설정하는 함수 — 여기서 normalColor를 저장한다 */
        public void setZoneColor(Color c) {
            this.normalColor = c;
            super.setBackground(c);
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            int arc = 18; // 둥근 모서리 크기
            int w = getWidth();
            int h = getHeight();

            // 둥근 사각형 배경
            g2.setColor(getBackground());
            g2.fillRoundRect(0, 0, w, h, arc, arc);

            // 텍스트 수동 중앙 정렬
            g2.setColor(Color.WHITE);
            FontMetrics fm = g2.getFontMetrics();
            String text = getText();
            int textWidth = fm.stringWidth(text);
            int textHeight = fm.getAscent();

            int x = (getWidth() - textWidth) / 2;
            int y = (getHeight() + textHeight) / 2 - 3;

            g2.drawString(text, x, y);
            g2.dispose();
        }

        // 투명도 적용 메서드
        public void setTransparent(Color baseColor) {
            Color transparent = new Color(
                baseColor.getRed(),
                baseColor.getGreen(),
                baseColor.getBlue(),
                90  // 투명도 설정 (0~255)
            );
            this.normalColor = transparent;
            setBackground(transparent);
        }
    }

    // 필터된 좌석 표시 메서드
    private void applyFilterEffect(List<Seat> filtered) {

        seatAreaPanel.removeAll();
        loadSeatPositions();

        List<Seat> allSeats = seatManager.getSeatListInternal();

        for (Seat seat : allSeats) {
            String id = seat.getSeatNumber();
            int[] xy = seatPositions.get(id);
            if (xy == null) continue;

            RoundSeatButton btn = new RoundSeatButton(id);
            btn.setBounds(xy[0], xy[1], SEAT_SIZE, SEAT_SIZE);

            // zone별 색상
            Color zoneColor =
                (seat.getZoneType() == Seat.ZoneType.FOCUS) ?
                    new Color(255, 128, 82) : new Color(66, 165, 245);

            // 사용중 좌석 처리
            if (!seat.isAvailable()) {
                btn.setZoneColor(new Color(180, 180, 180));
                btn.setEnabled(false);
                seatAreaPanel.add(btn);
                continue;
            }

            // 필터 적용 규칙
            if (!filtered.contains(seat)) {
                btn.setTransparent(zoneColor);
                btn.setEnabled(false);
            } else {
                btn.setZoneColor(zoneColor);
                btn.setEnabled(true);
            }

            btn.addActionListener(e -> handleSeatSelection(seat));
            seatAreaPanel.add(btn);
        }

        seatAreaPanel.revalidate();
        seatAreaPanel.repaint();
    }

    /** 좌석 번호별 좌표 설정 */
    private void loadSeatPositions() {

        seatPositions.clear();

        int size = SEAT_SIZE;
        int gap = 20;
        int num = 1;

        // 1) 노트북존 왼쪽 세로 (5석) = 1~5
        int baseX = 30, baseY = 50;
        for (int r = 0; r < 5; r++)
            seatPositions.put(String.valueOf(num++),
                    new int[]{baseX, baseY + r * (size + gap)});

        // 2) 창가 가로 (5석) = 6~10
        baseX = 100; baseY = 50;
        for (int c = 0; c < 5; c++)
            seatPositions.put(String.valueOf(num++),
                    new int[]{baseX + c * (size + gap), baseY});

        // 3) 중앙 테이블 Zone (2행 × 4열 = 11~18)
        baseX = 135; baseY = 180;
        for (int r = 0; r < 2; r++)
            for (int c = 0; c < 4; c++)
                seatPositions.put(String.valueOf(num++),
                        new int[]{baseX + c * (size + gap), baseY + r * (size + gap)});

        // 4) 노트북존 오른쪽 세로 (5석 = 19~23)
        baseX = 440; baseY = 50;
        for (int r = 0; r < 5; r++)
            seatPositions.put(String.valueOf(num++),
                    new int[]{baseX, baseY + r * (size + gap)});

        // 집중존 1) 첫 세로줄 (5석 = 24~28)
        baseX = 520; baseY = 50;
        for (int r = 0; r < 5; r++)
            seatPositions.put(String.valueOf(num++),
                    new int[]{baseX, baseY + r * (size + gap)});


        // 집중존 2) 위쪽 2열 (29~36)
        baseX = 680; baseY = 50;
        for (int c = 0; c < 2; c++) {
            for (int r = 0; r < 4; r++) {
                seatPositions.put(String.valueOf(num++),
                        new int[]{baseX + c * (size + gap), baseY + r * (size + gap)});
            }
        }


        // 집중존 3) 아래쪽 2열 (37~42)
        baseX = 680; baseY = 390;
        for (int c = 0; c < 2; c++) {
            for (int r = 0; r < 3; r++) {
                seatPositions.put(String.valueOf(num++),
                        new int[]{baseX + c * (size + gap), baseY + r * (size + gap)});
            }
        }


        // 집중존 4) 맨 오른쪽 8행 1열 (43~50)
        baseX = 902; baseY = 50;
        for (int r = 0; r < 8; r++)
            seatPositions.put(String.valueOf(num++),
                    new int[]{baseX, baseY + r * (size + gap)});
    }

    // 기본 전체 좌석 표시
    private void initializeSeats() {

        seatAreaPanel.removeAll();
        loadSeatPositions();

        List<Seat> seats = seatManager.getSeatListInternal();

        for (Seat seat : seats) {

            String id = seat.getSeatNumber();
            int[] xy = seatPositions.get(id);
            if (xy == null) continue;

            RoundSeatButton btn = new RoundSeatButton(id);
            btn.setBounds(xy[0], xy[1], SEAT_SIZE, SEAT_SIZE);

            if (!seat.isAvailable()) {
                btn.setZoneColor(new Color(180, 180, 180));
                btn.setEnabled(false);
            } else {
                if (seat.getZoneType() == Seat.ZoneType.FOCUS)
                    btn.setZoneColor(new Color(255, 128, 82));
                else
                    btn.setZoneColor(new Color(66, 165, 245));
            }
            btn.addActionListener(e -> handleSeatSelection(seat));

            seatAreaPanel.add(btn);
        }

        seatAreaPanel.revalidate();
        seatAreaPanel.repaint();
    }

    public void updateSeatStatus() {
        initializeSeats();
    }

    // 자동 검색
    private void runAutoFilter() {
        // 냉난방 필터
        Seat.AirType air = null;
        if (weakCb.isSelected()) air = Seat.AirType.WEAK;
        if (strongCb.isSelected()) air = Seat.AirType.STRONG;

        // 존 필터
        Seat.ZoneType zone = null;
        String sel = (String) zoneCombo.getSelectedItem();
        if (sel != null) {
            if (sel.equals("노트북")) zone = Seat.ZoneType.LAPTOP;
            else if (sel.equals("집중")) zone = Seat.ZoneType.FOCUS;
        }

        // 좌석 타입 필터
        Seat.SeatType type = null;
        String typeSel = (String) seatTypeCombo.getSelectedItem();
        if (typeSel != null) {
            switch (typeSel) {
                case "FLOW" -> type = Seat.SeatType.FLOW;
                case "WINDOW" -> type = Seat.SeatType.WINDOW;
                case "TABLE" -> type = Seat.SeatType.TABLE;
                case "BOOTH" -> type = Seat.SeatType.BOOTH;
                case "PREMIUM" -> type = Seat.SeatType.PREMIUM;
            }
        }

        // 필터 적용
        List<Seat> filtered = seatManager.filterSeats(air, zone, type);
        applyFilterEffect(filtered);
    }

    // 좌석 클릭 시 처리
    private void handleSeatSelection(Seat seat) {

        Member member = mainFrame.getCurrentMember();

        // 자리 이동 모드
        if (mainFrame.isSeatMoveMode()) {
            if (member == null) {
                JOptionPane.showMessageDialog(mainFrame, "회원만 자리 이동이 가능합니다.");
                return;
            }
            Seat currentSeat = seatManager.findSeatByMember(member.getId());
            if (currentSeat == null) {
                JOptionPane.showMessageDialog(mainFrame, "현재 사용 중인 좌석이 없습니다. 입실 후 이용해 주세요.");
                mainFrame.endSeatMoveMode();
                return;
            }
            if (!seat.isAvailable()) {
                JOptionPane.showMessageDialog(mainFrame, "이미 사용 중인 좌석입니다.");
                return;
            }
            int confirm = JOptionPane.showConfirmDialog(
                mainFrame,
                seat.getSeatNumber() + "번 좌석으로 이동하시겠습니까?",
                "자리 이동",
                JOptionPane.YES_NO_OPTION
            );
            if (confirm == JOptionPane.YES_OPTION) {
                boolean moved = seatMoveService != null && seatMoveService.move(member.getId(), seat.getSeatNumber());
                if (moved) {
                    JOptionPane.showMessageDialog(mainFrame, "자리 이동이 완료되었습니다.");
                    updateSeatStatus();
                    mainFrame.endSeatMoveMode();
                    mainFrame.showPanel(KioskMainFrame.MAIN_MENU_PANEL);
                } else {
                    JOptionPane.showMessageDialog(mainFrame, "자리 이동에 실패했습니다.");
                }
            }
            return;
        }

        // 비회원: 당일권 결제 화면으로 안내
        if (member == null) {
            JOptionPane.showMessageDialog(mainFrame, seat.getSeatNumber() + "번 좌석 선택. 당일권 결제 화면으로 이동합니다.");
            mainFrame.showPanel(KioskMainFrame.DAILY_TICKET_PANEL);
            return;
        }

        // 회원 + 이용권 없음
        if (!member.hasValidTicket()) {
            JOptionPane.showMessageDialog(mainFrame,
                    "이용권이 없습니다.\n입실하려면 먼저 이용권을 구매해주세요.");
            mainFrame.showPanel(KioskMainFrame.DAILY_TICKET_PANEL);
            return;
        }

        // 회원 + 이용권 있음 → 입실 가능
        if (!seat.isAvailable()) {
            JOptionPane.showMessageDialog(mainFrame, "이미 사용 중인 좌석입니다.");
            updateSeatStatus();
            return;
        }

        int confirm = JOptionPane.showConfirmDialog(
                mainFrame,
                seat.getSeatNumber() + "번 좌석으로 입실하시겠습니까?",
                "입실 확인",
                JOptionPane.YES_NO_OPTION
        );

        if (confirm == JOptionPane.YES_OPTION) {
            boolean success = checkInService != null && checkInService.checkIn(member.getId(), seat.getSeatNumber());
            if (success) {
                JOptionPane.showMessageDialog(mainFrame, "입실 완료!");
                updateSeatStatus();
                mainFrame.showPanel(KioskMainFrame.MAIN_MENU_PANEL);
            } else {
                JOptionPane.showMessageDialog(mainFrame, "입실에 실패했습니다. 이미 사용 중인 좌석이거나 티켓이 없습니다.");
                updateSeatStatus();
            }
        }
    }
}
