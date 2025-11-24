/*
화면 5: 당일 시간제 선택 및 결제 화면
 */
package gui; 

import payment.PriceManager;
import payment.PurchaseService;
import payment.TicketProduct;
import ReadingRoomLogin.Member;

import javax.swing.JPanel;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.BorderFactory;
import java.awt.GridLayout;
import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.Dimension;

public class DailyTicketPanel extends JPanel {

    private KioskMainFrame mainFrame;
    private PriceManager priceManager;
    private PurchaseService purchaseService;
    private TicketProduct selectedProduct;

    public DailyTicketPanel(KioskMainFrame mainFrame, PriceManager priceManager, PurchaseService purchaseService) {
        this.mainFrame = mainFrame;
        this.priceManager = priceManager;
        this.purchaseService = purchaseService;

        setLayout(new BorderLayout());
        setBackground(Theme.BACKGROUND_COLOR);

        // 제목 영역
        JLabel titleLabel = new JLabel("당일 이용권 선택", JLabel.CENTER);
        Theme.styleLabel(titleLabel, Theme.TITLE_FONT);
        titleLabel.setBorder(BorderFactory.createEmptyBorder(40, 0, 20, 0));
        add(titleLabel, BorderLayout.NORTH);

        JPanel ticketPanel = new JPanel(new GridLayout(2, 2, 20, 20));
        ticketPanel.setBackground(Theme.BACKGROUND_COLOR);
        ticketPanel.setBorder(BorderFactory.createEmptyBorder(20, 150, 40, 150));
        
        JButton btn3h = createTicketButton(TicketProduct.DAILY_3H);
        JButton btn6h = createTicketButton(TicketProduct.DAILY_6H);
        JButton btn12h = createTicketButton(TicketProduct.DAILY_12H); // <--- 오타 수정
        JButton btn24h = createTicketButton(TicketProduct.DAILY_24H);

        ticketPanel.add(btn3h);
        ticketPanel.add(btn6h);
        ticketPanel.add(btn12h);
        ticketPanel.add(btn24h);
        
        add(ticketPanel, BorderLayout.CENTER);

        // 결제/뒤로 버튼
        JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 20));
        bottomPanel.setBackground(Theme.BACKGROUND_COLOR);
        bottomPanel.setBorder(BorderFactory.createEmptyBorder(0, 0, 40, 0));

        JButton payBtn = new JButton("결제하기");
        Theme.styleButton(payBtn);
        payBtn.setBackground(Theme.ACCENT_COLOR);
        payBtn.setPreferredSize(new Dimension(200, 60));

        JButton backBtn = new JButton("뒤로가기");
        bottomPanel.add(payBtn);
        bottomPanel.add(backBtn);
        bottomPanel.add(payBtn);

        add(bottomPanel, BorderLayout.SOUTH);

        payBtn.addActionListener(e -> {
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

            boolean success = purchaseService.purchaseTicket(member.getId(), selectedProduct, "CARD");
            if (success) {
                JOptionPane.showMessageDialog(mainFrame, "결제 완료! 이용권이 발급되었습니다.");
                mainFrame.showPanel(KioskMainFrame.SEAT_MAP_PANEL);
            } else {
                JOptionPane.showMessageDialog(mainFrame, "결제 또는 이용권 발급에 실패했습니다.");
            }
        });
        
        backBtn.addActionListener(e -> {
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
            JOptionPane.showMessageDialog(mainFrame, product.toString() + "을(를) 선택했습니다.");
        });
        return button;
    }
}
