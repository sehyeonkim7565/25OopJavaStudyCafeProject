/*
화면 6: 정기권 상품 선택 및 결제 화면
 */
package gui; 

import ReadingRoomLogin.Member;
import payment.PriceManager;
import payment.PurchaseService;
import payment.TicketProduct;
import Ticket.TimeTicket;
import Ticket.DurationTicket;

import javax.swing.JPanel;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.BorderFactory;
import javax.swing.JTabbedPane;
import javax.swing.SwingConstants;
import java.awt.GridLayout;
import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.Dimension;
import java.awt.Color;

public class PassPurchasePanel extends JPanel {

    private KioskMainFrame mainFrame;
    private PriceManager priceManager;
    private PurchaseService purchaseService;
    private TicketProduct selectedProduct;
    private JTabbedPane tabPane;

    public PassPurchasePanel(KioskMainFrame mainFrame, PriceManager priceManager, PurchaseService purchaseService) {
        this.mainFrame = mainFrame;
        this.priceManager = priceManager;
        this.purchaseService = purchaseService;

        setLayout(new BorderLayout());
        setBackground(Theme.BACKGROUND_COLOR);

        JLabel titleLabel = new JLabel("정기권 / 시간권 구매", SwingConstants.CENTER);
        Theme.styleLabel(titleLabel, Theme.TITLE_FONT);
        titleLabel.setBorder(BorderFactory.createEmptyBorder(30, 0, 20, 0));
        add(titleLabel, BorderLayout.NORTH);

        tabPane = new JTabbedPane();
        tabPane.setFont(Theme.MAIN_FONT);
        tabPane.setBackground(Color.WHITE);

        // 기간권 탭
        JPanel durationPanel = new JPanel(new GridLayout(2, 2, 15, 15));
        durationPanel.setBackground(Color.WHITE);
        durationPanel.setBorder(BorderFactory.createEmptyBorder(30, 30, 30, 30));

        durationPanel.add(createTicketButton(TicketProduct.DURATION_1W));
        durationPanel.add(createTicketButton(TicketProduct.DURATION_2W));
        durationPanel.add(createTicketButton(TicketProduct.DURATION_1M));
        durationPanel.add(createTicketButton(TicketProduct.DURATION_3M));
        tabPane.addTab("  기간권 (일 단위)  ", durationPanel);

        // 시간권 탭
        JPanel timePanel = new JPanel(new GridLayout(1, 3, 15, 15));
        timePanel.setBackground(Color.WHITE);
        timePanel.setBorder(BorderFactory.createEmptyBorder(30, 30, 30, 30));

        timePanel.add(createTicketButton(TicketProduct.TIME_50H));
        timePanel.add(createTicketButton(TicketProduct.TIME_100H));
        timePanel.add(createTicketButton(TicketProduct.TIME_200H));
        tabPane.addTab("  시간권 (분 단위 차감)  ", timePanel);
        
        JPanel tabContainer = new JPanel(new BorderLayout());
        tabContainer.setBackground(Theme.BACKGROUND_COLOR);
        tabContainer.setBorder(BorderFactory.createEmptyBorder(0, 100, 20, 100));
        tabContainer.add(tabPane, BorderLayout.CENTER);

        add(tabContainer, BorderLayout.CENTER);

        JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 20));
        bottomPanel.setBackground(Theme.BACKGROUND_COLOR);
        bottomPanel.setBorder(BorderFactory.createEmptyBorder(0, 0, 30, 0));

        JButton payBtn = new JButton("결제하기");
        Theme.styleButton(payBtn);
        payBtn.setBackground(Theme.ACCENT_COLOR);
        payBtn.setPreferredSize(new Dimension(200, 50));

        JButton backBtn = new JButton("뒤로가기");
        Theme.styleSecondaryButton(backBtn);
        backBtn.setPreferredSize(new Dimension(200, 50));

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

            // 보유 티켓과 유형이 다르면 결제 차단
            if (member.getTicket() != null) {
                boolean hasTimeTicket = member.getTicket() instanceof TimeTicket;
                boolean hasDurationTicket = member.getTicket() instanceof DurationTicket;
                boolean selectedIsTime = isTimeProduct(selectedProduct);
                boolean selectedIsDuration = isDurationProduct(selectedProduct);
                if (hasTimeTicket && !selectedIsTime) {
                    JOptionPane.showMessageDialog(mainFrame, "보유중인 티켓과 다릅니다.");
                    return;
                }
                if (hasDurationTicket && !selectedIsDuration) {
                    JOptionPane.showMessageDialog(mainFrame, "보유중인 티켓과 다릅니다.");
                    return;
                }
            }

            boolean success = purchaseService.purchaseTicket(member.getId(), selectedProduct, "CARD");
            if (success) {
                JOptionPane.showMessageDialog(mainFrame, "정기권 구매 완료! 좌석 배치도 화면으로 이동합니다.");
                mainFrame.showPanel(KioskMainFrame.SEAT_MAP_PANEL);
            } else {
                JOptionPane.showMessageDialog(mainFrame, "결제 또는 이용권 발급에 실패했습니다.");
            }
        });

        backBtn.addActionListener(e -> {
            mainFrame.showPanel(KioskMainFrame.MAIN_MENU_PANEL); 
        });
    }

    public void selectTimeTab() {
        if (tabPane != null && tabPane.getTabCount() > 1) {
            tabPane.setSelectedIndex(1);
        }
    }

    public void selectDurationTab() {
        if (tabPane != null && tabPane.getTabCount() > 0) {
            tabPane.setSelectedIndex(0);
        }
    }

    private JButton createTicketButton(TicketProduct product) {
        int price = priceManager.getPrice(product);
        String text = String.format("<html><center>%s<br><font size=5>%,d원</font></center></html>",
                product.toString(), price);
        JButton button = new JButton(text);
        Theme.styleButton(button);
        button.setBackground(Theme.PRIMARY_COLOR);
        button.addActionListener(e -> {
            selectedProduct = product;
            JOptionPane.showMessageDialog(mainFrame, product.toString() + "을(를) 선택했습니다.");
        });
        return button;
    }

    private boolean isTimeProduct(TicketProduct product) {
        switch (product) {
            case TIME_50H:
            case TIME_100H:
            case TIME_200H:
                return true;
            default:
                return false;
        }
    }

    private boolean isDurationProduct(TicketProduct product) {
        switch (product) {
            case DURATION_1W:
            case DURATION_2W:
            case DURATION_1M:
            case DURATION_3M:
            case DAILY_3H:
            case DAILY_6H:
            case DAILY_12H:
            case DAILY_24H:
                return true;
            default:
                return false;
        }
    }
}
