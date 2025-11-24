package gui;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.HashMap;
import java.util.Map;
import java.util.Vector;
import Payment.OrderLogEntry; // 11/24 OrderLogEntry import
import java.time.LocalDateTime; //
import java.time.format.DateTimeFormatter; //

public class ShopPanel extends JPanel {
	
	private KioskMainFrame parentFrame; // KioskMainFrame 인스턴스 저장
	private ILogManager logManager; // 11/23 멤버변수 추가
	
    // 주문 정보를 저장할 맵 (상품명 -> 수량)
    private Map<String, Integer> orderMap = new HashMap<>();
    private JPanel cartPanel; // 장바구니 항목들을 담을 패널
    private JLabel totalLabel;
    private JPanel itemPanel; // 상품 버튼을 담을 중앙 패널

    // 상품 데이터 정의: [상품명, 가격]
    private final Map<String, String[][]> productData = new HashMap<>() {{
        put("라면", new String[][]{
            {"진라면", "4000"},
            {"신라면", "4500"},
            {"불닭볶음면", "5000"}
        });
        put("음료", new String[][]{
            {"콜라", "2000"},
            {"사이다", "2000"},
            {"에너지드링크", "3500"}
        });
        put("과자", new String[][]{
            {"새우깡", "1500"},
            {"감자칩", "2500"}
        });
    }};

    public ShopPanel(KioskMainFrame parentFrame) {
    	this.parentFrame = parentFrame;
		this.logManager = logManager; // 11/23 초기화
        
    	setLayout(new BorderLayout(10, 10));
    	
    	JPanel topPanel = new JPanel(new BorderLayout());
        JLabel titleLabel = new JLabel("✨ 상품 주문 페이지", SwingConstants.CENTER);
        titleLabel.setFont(new Font("맑은 고딕", Font.BOLD, 20));
        JButton backButton = new JButton("◀ 돌아가기");
        
        
        backButton.addActionListener(e -> {
            // 장바구니 비우기 확인 (선택 사항)
            if (!orderMap.isEmpty()) {
                int confirm = JOptionPane.showConfirmDialog(this, 
                    "장바구니에 상품이 남아있습니다. 정말 돌아가시겠습니까?", 
                    "경고", JOptionPane.YES_NO_OPTION);
                if (confirm != JOptionPane.YES_OPTION) return;
            }
            // 메인 메뉴 패널로 전환
            parentFrame.showPanel(KioskMainFrame.MAIN_MENU_PANEL); 
            // clearCart(); // 돌아갈 때 장바구니를 비우고 싶다면 이 주석을 해제
        });
        
        topPanel.add(backButton, BorderLayout.WEST);
        topPanel.add(titleLabel, BorderLayout.CENTER);
        add(topPanel, BorderLayout.NORTH); // 상단에 배치
        
        // 1. 카테고리 패널 (WEST)
        JList<String> categoryList = new JList<>(new Vector<>(productData.keySet()));
        categoryList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        categoryList.setFont(new Font("맑은 고딕", Font.BOLD, 18));
        categoryList.setPreferredSize(new Dimension(150, 0));
        
        categoryList.addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                String selectedCategory = categoryList.getSelectedValue();
                if (selectedCategory != null) {
                    displayItems(selectedCategory);
                }
            }
        });
        
        add(new JScrollPane(categoryList), BorderLayout.WEST);

        // 2. 상품 목록 패널 (CENTER)
        itemPanel = new JPanel(new GridLayout(0, 3, 10, 10)); // 3열 그리드, 간격 10
        itemPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        add(new JScrollPane(itemPanel), BorderLayout.CENTER);

        // 3. 장바구니/주문 패널 (EAST)
        JPanel orderPanel = new JPanel(new BorderLayout());
        
        cartPanel = new JPanel();
        cartPanel.setLayout(new BoxLayout(cartPanel, BoxLayout.Y_AXIS)); // 장바구니 항목을 세로로 쌓음
        
        JScrollPane cartScrollPane = new JScrollPane(cartPanel);
        cartScrollPane.setBorder(BorderFactory.createTitledBorder("🛒 장바구니 내역"));
        
        totalLabel = new JLabel("총 결제 금액: 0원", SwingConstants.RIGHT);
        totalLabel.setFont(new Font("맑은 고딕", Font.BOLD, 22));
        
        JButton confirmButton = new JButton("주문 완료 및 결제");
        confirmButton.setFont(new Font("맑은 고딕", Font.BOLD, 18));
        confirmButton.setBackground(new Color(60, 179, 113));
        confirmButton.setForeground(Color.WHITE);
        confirmButton.addActionListener(e -> completeOrder());

        JPanel bottomControlPanel = new JPanel(new GridLayout(2, 1, 5, 5));
        bottomControlPanel.add(totalLabel);
        bottomControlPanel.add(confirmButton);

        orderPanel.setPreferredSize(new Dimension(300, 0));
        orderPanel.add(cartScrollPane, BorderLayout.CENTER);
        orderPanel.add(bottomControlPanel, BorderLayout.SOUTH);

        add(orderPanel, BorderLayout.EAST);
        
        // 초기 상품 목록 표시 (첫 번째 카테고리)
        if (!productData.isEmpty()) {
            categoryList.setSelectedIndex(0);
        }

    }

    // 선택된 카테고리의 상품을 중앙 패널에 표시
    private void displayItems(String category) {
        itemPanel.removeAll();
        String[][] items = productData.get(category);

        for (String[] item : items) {
            String name = item[0];
            int price = Integer.parseInt(item[1]);
            
            // 상품 버튼 생성
            JButton itemButton = createItemButton(name, price);
            itemButton.addActionListener(e -> addItemToCart(name)); // 장바구니에 추가
            itemPanel.add(itemButton);
        }

        itemPanel.revalidate();
        itemPanel.repaint();
    }
    
    // 장바구니에 상품 추가
    private void addItemToCart(String name) {
        orderMap.put(name, orderMap.getOrDefault(name, 0) + 1);
        updateCartDisplay();
    }
    
    // 장바구니에서 상품 삭제
    private void removeItemFromCart(String name) {
        orderMap.remove(name);
        updateCartDisplay();
    }

    // 장바구니를 완전히 비움
    private void clearCart() {
        orderMap.clear();
        updateCartDisplay();
    }

    // 장바구니 UI를 갱신하고 총 금액을 계산
    private void updateCartDisplay() {
        cartPanel.removeAll(); // 기존 항목 전체 제거
        long totalAmount = 0;

        for (Map.Entry<String, Integer> entry : orderMap.entrySet()) {
            String name = entry.getKey();
            int quantity = entry.getValue();
            
            int price = getProductPrice(name);
            long itemTotal = (long) price * quantity;
            totalAmount += itemTotal;
            
            // 장바구니 항목 UI 생성 (상품명, 수량, 가격, 삭제 버튼)
            JPanel itemRow = new JPanel(new BorderLayout());
            itemRow.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
            
            String itemText = String.format("%s x %d (%,d원)", name, quantity, itemTotal);
            JLabel itemLabel = new JLabel(itemText);
            itemLabel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
            
            JButton deleteButton = new JButton("삭제");
            deleteButton.setPreferredSize(new Dimension(70, 30));
            deleteButton.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    removeItemFromCart(name); // 해당 상품명 제거
                }
            });

            itemRow.add(itemLabel, BorderLayout.CENTER);
            itemRow.add(deleteButton, BorderLayout.EAST);
            
            cartPanel.add(itemRow);
            cartPanel.add(Box.createVerticalStrut(5)); // 항목 사이의 간격 추가
        }
        
        // 장바구니가 비어 있을 때 메시지 표시
        if (orderMap.isEmpty()) {
            cartPanel.add(new JLabel("   장바구니가 비어 있습니다.", SwingConstants.CENTER));
        }

        // UI 갱신
        cartPanel.revalidate();
        cartPanel.repaint();

        // 총 금액 업데이트
        totalLabel.setText(String.format("총 결제 금액: %,d원", totalAmount));
    }
    
    // 상품 가격을 찾는 헬퍼 메서드
    private int getProductPrice(String name) {
        for (String[][] items : productData.values()) {
            for (String[] item : items) {
                if (item[0].equals(name)) {
                    return Integer.parseInt(item[1]);
                }
            }
        }
        return 0;
    }
    
    // 상품 버튼 생성 메서드 (이미지 + 텍스트) - 이전 코드와 동일
    private JButton createItemButton(String name, int price) {
        JButton button = new JButton();
        button.setLayout(new BorderLayout());
        button.setBackground(Color.WHITE);
        
        // 이미지 대체 영역 (빈 사각형)
        JLabel imageLabel = new JLabel(new EmptyIcon(100, 100, new Color(240, 240, 240)), SwingConstants.CENTER); 
        
        // 텍스트 영역
        String htmlText = String.format("<html><div style='text-align: center; padding: 5px;'>" +
                                        "<b>%s</b><br>%,d원</div></html>", name, price);
        JLabel textLabel = new JLabel(htmlText, SwingConstants.CENTER);
        textLabel.setFont(new Font("맑은 고딕", Font.PLAIN, 14));
        textLabel.setForeground(Color.BLACK);

        button.add(imageLabel, BorderLayout.CENTER);
        button.add(textLabel, BorderLayout.SOUTH);
        
        return button;
    }

    // 주문 완료 처리
    private void completeOrder() {
        if (orderMap.isEmpty()) {
            JOptionPane.showMessageDialog(this, "주문할 상품을 먼저 장바구니에 담아주세요.", "알림", JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        long total = calculateTotalAmount();
        StringBuilder summaryBuilder = new StringBuilder();
        
        for (Map.Entry<String, Integer> entry : orderMap.entrySet()) {
            String name = entry.getKey();
            int quantity = entry.getValue();
            long itemTotal = (long) getProductPrice(name) * quantity;
            summaryBuilder.append(String.format("%s x %d개 (%,d원)\n", name, quantity, itemTotal));
        }

        String message = String.format(
            "*** 최종 주문 내역 ***\n" +
            "%s\n" +
            "----------------------------\n" +
            "총 결제 금액: %,d원\n\n" +
            "결제가 완료되었습니다. 상품을 준비하겠습니다.",
            summaryBuilder.toString(), total
        );

        JOptionPane.showMessageDialog(this, message, "주문 완료", JOptionPane.INFORMATION_MESSAGE);
        
        // 주문 초기화
        clearCart();
    }
    
    // 총 금액 계산 헬퍼 메서드
    private long calculateTotalAmount() {
        long totalAmount = 0;
        for (Map.Entry<String, Integer> entry : orderMap.entrySet()) {
            int quantity = entry.getValue();
            int price = getProductPrice(entry.getKey());
            totalAmount += (long) price * quantity;
        }
        return totalAmount;
    }

    // 빈 사각형 아이콘 클래스 - 이전 코드와 동일
    private static class EmptyIcon implements Icon {
        private final int width;
        private final int height;
        private final Color color;

        public EmptyIcon(int width, int height, Color color) {
            this.width = width;
            this.height = height;
            this.color = color;
        }

        @Override
        public void paintIcon(Component c, Graphics g, int x, int y) {
            g.setColor(color);
            g.fillRect(x, y, width, height);
            g.setColor(Color.DARK_GRAY);
            g.drawRect(x, y, width - 1, height - 1);
            
            g.setFont(new Font("맑은 고딕", Font.PLAIN, 10));
            g.setColor(Color.DARK_GRAY);
            String text = "이미지 준비 중";
            FontMetrics fm = g.getFontMetrics();
            int textX = x + (width - fm.stringWidth(text)) / 2;
            int textY = y + (height - fm.getHeight()) / 2 + fm.getAscent();
            g.drawString(text, textX, textY);
        }

        @Override
        public int getIconWidth() { return width; }

        @Override
        public int getIconHeight() { return height; }
    }

	 /**
     * 11/23
     * 로그 파일에 주문 내역을 기록하는 메서드
     * 형식: 시간, 이용자ID, 좌석번호, 주문내역, 총액
     */
    private void logOrderDetails(String orderSummary, long totalAmount) {
        if (logManager == null) {
            System.err.println("LogManager가 초기화되지 않았습니다. 로그 기록 실패.");
            return;
        }

        // 현재 사용자 정보 가져오기
        Member currentMember = parentFrame.getCurrentMember();
        String memberId = (currentMember != null) ? currentMember.getId() : "NON_MEMBER";
        String seatNumber = "N/A"; // 좌석 번호 초기값

        Seat seat = parentFrame.getSeatManager().findSeatByMember(memberId);
        if (seat != null) {
            seatNumber = String.valueOf(seat.getSeatNumber()); 
        }

        String detailedOrder = orderSummary.trim()
                                           .replace("\n", ", ")
                                           .replaceAll(" +", " ")
                                           .replaceAll("[,;] $", ""); // 끝 콤마/세미콜론 제거
		// 11/24 현재 시간 문자열 생성
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        
        // 11/24 OrderLogEntry 객체 생성(jsonl)
        OrderLogEntry orderEntry = new OrderLogEntry(
            timestamp,
            memberId, 
            seatNumber, 
            detailedOrder, 
            totalAmount
        );
		
        String logMessage = String.format("ORDER, %s, %s, %s, %,d원", 
                                          memberId, seatNumber, detailedOrder, totalAmount);
        
        logManager.saveOrderLog(logMessage); // ILogManager의 log(String message) 메서드를 사용
        System.out.println("[LOG] 주문 기록: " + logMessage);
    }
}

