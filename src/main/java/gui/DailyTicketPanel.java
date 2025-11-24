package gui; 

import ReadingRoomLogin.Member;
import payment.PriceManager;
import payment.PurchaseService;
import payment.TicketProduct;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridLayout;

/**
 * 화면 5: 당일 시간제 선택 및 결제 화면
 * - 상품 선택 상태를 표시하고, PaymentDialog를 통해 결제 방식을 입력받아 처리
 */
public class DailyTicketPanel extends JPanel {

    private final KioskMainFrame mainFrame;
    private final PriceManager priceManager;
    private final PurchaseService purchaseService;

    private TicketProduct selectedProduct;
    private int selectedPrice;
    private JLabel selectedInfoLabel;

    public DailyTicketPanel(KioskMainFrame mainFrame, PriceManager priceManager, PurchaseService purchaseService) {
        this.mainFrame = mainFrame;
        this.priceManager = priceManager;
        this.purchaseService = purchaseService;

        setLayout(new BorderLayout());
        setBackground(Theme.BACKGROUND_COLOR);
        setBorder(BorderFactory.createEmptyBorder(30, 50, 40, 50));

        // 제목
        JLabel titleLabel = new JLabel("당일 이용권 선택", SwingConstants.CENTER);
        Theme.styleLabel(titleLabel, Theme.TITLE_FONT);
        titleLabel.setBorder(BorderFactory.createEmptyBorder(20, 0, 20, 0));
        add(titleLabel, BorderLayout.NORTH);

        // 상품 버튼 영역
        JPanel ticketPanel = new JPanel(new GridLayout(2, 2, 20, 20));
        ticketPanel.setBackground(Theme.BACKGROUND_COLOR);
        ticketPanel.setBorder(BorderFactory.createEmptyBorder(20, 80, 20, 80));

        ticketPanel.add(createTicketButton(TicketProduct.DAILY_3H));
        ticketPanel.add(createTicketButton(TicketProduct.DAILY_6H));
        ticketPanel.add(createTicketButton(TicketProduct.DAILY_12H));
        ticketPanel.add(createTicketButton(TicketProduct.DAILY_24H));

        add(ticketPanel, BorderLayout.CENTER);

        // 선택 정보 + 하단 버튼
        JPanel bottomPanel = new JPanel(new BorderLayout(10, 10));
        bottomPanel.setBackground(Theme.BACKGROUND_COLOR);
        bottomPanel.setBorder(BorderFactory.createEmptyBorder(10, 0, 10, 0));

        selectedInfoLabel = new JLabel("상품을 선택하세요.", SwingConstants.CENTER);
        Theme.styleLabel(selectedInfoLabel, Theme.MAIN_FONT);
        selectedInfoLabel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(Theme.PRIMARY_COLOR, 1),
                BorderFactory.createEmptyBorder(10, 10, 10, 10)
        ));
        selectedInfoLabel.setPreferredSize(new Dimension(300, 45));
        bottomPanel.add(selectedInfoLabel, BorderLayout.NORTH);

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 10));
        buttonPanel.setBackground(Theme.BACKGROUND_COLOR);

        JButton payBtn = new JButton("결제하기");
        Theme.styleButton(payBtn);
        payBtn.setBackground(Theme.ACCENT_COLOR);
        payBtn.setPreferredSize(new Dimension(200, 55));

        JButton backBtn = new JButton("뒤로가기");
        Theme.styleSecondaryButton(backBtn);
        backBtn.setPreferredSize(new Dimension(200, 55));

        buttonPanel.add(backBtn);
        buttonPanel.add(payBtn);

        bottomPanel.add(buttonPanel, BorderLayout.SOUTH);
        add(bottomPanel, BorderLayout.SOUTH);

        payBtn.addActionListener(e -> handlePayment());
        backBtn.addActionListener(e -> {
            clearSelection();
            mainFrame.showPanel(KioskMainFrame.SEAT_MAP_PANEL);
        });
    }

    private JButton createTicketButton(TicketProduct product) {
        int price = priceManager.getPrice(product);
        String text = String.format("<html><center>%s<br><font size=5>%,d원</font></center></html>",
                product.toString(), price);
        JButton button = new JButton(text);
        Theme.styleButton(button);
        button.addActionListener(e -> {
            selectedProduct = product;
            selectedPrice = price;
            selectedInfoLabel.setText("선택: " + product + " / " + String.format("%,d원", price));
        });
        return button;
    }

    private void handlePayment() {
        Member member = mainFrame.getCurrentMember();
        if (member == null) {
            JOptionPane.showMessageDialog(mainFrame, "로그인이 필요합니다.");
            mainFrame.showPanel(KioskMainFrame.LOGIN_PANEL);
            return;
        }

        if (selectedProduct == null) {
            JOptionPane.showMessageDialog(mainFrame, "구매할 이용권을 선택해주세요.");
            return;
        }

        if (purchaseService.hasValidTicket(member.getId())) {
            JOptionPane.showMessageDialog(mainFrame, "이미 유효한 이용권을 보유 중입니다.\n추가 구매가 불가합니다.", "구매 불가", JOptionPane.ERROR_MESSAGE);
            return;
        }

        PaymentDialog dialog = new PaymentDialog(mainFrame, "결제하기", selectedPrice);
        dialog.setVisible(true);

        if (!dialog.isPaymentSuccess()) {
            return; // 사용자가 결제를 취소하거나 실패
        }

        String method = dialog.getSelectedMethod();
        if (method == null || method.isEmpty()) {
            method = "CARD";
        }

        boolean success = purchaseService.purchaseTicket(member.getId(), selectedProduct, method);
        if (success) {
            JOptionPane.showMessageDialog(mainFrame, "이용권이 발급되었습니다. 좌석 배치도 화면으로 이동합니다.");
            clearSelection();
            mainFrame.showPanel(KioskMainFrame.SEAT_MAP_PANEL);
        } else {
            JOptionPane.showMessageDialog(mainFrame, "발급 처리 중 오류가 발생했습니다.", "오류", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void clearSelection() {
        selectedProduct = null;
        selectedPrice = 0;
        if (selectedInfoLabel != null) {
            selectedInfoLabel.setText("상품을 선택하세요.");
        }
    }
}
