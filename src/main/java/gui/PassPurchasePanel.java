package gui;

import payment.PriceManager;
import payment.TicketProduct;

import javax.swing.*;
import java.awt.*;

public class PassPurchasePanel extends JPanel {

    private KioskMainFrame mainFrame;
    private PriceManager priceManager;

    public PassPurchasePanel(KioskMainFrame mainFrame, PriceManager priceManager) {
        this.mainFrame = mainFrame;
        this.priceManager = priceManager;

        setLayout(new BorderLayout());
        setBackground(Theme.BACKGROUND_COLOR);

        JLabel titleLabel = new JLabel("정기권 / 시간권 구매", SwingConstants.CENTER);
        Theme.styleLabel(titleLabel, Theme.TITLE_FONT);
        titleLabel.setBorder(BorderFactory.createEmptyBorder(30, 0, 20, 0));
        add(titleLabel, BorderLayout.NORTH);

        JTabbedPane tabPane = new JTabbedPane();
        tabPane.setFont(Theme.SUBTITLE_FONT);
        tabPane.setBackground(Color.WHITE);

        // 기간권
        JPanel durationPanel = new JPanel(new GridLayout(2, 2, 15, 15));
        durationPanel.setBackground(Color.WHITE);
        durationPanel.setBorder(BorderFactory.createEmptyBorder(30, 30, 30, 30));

        durationPanel.add(createTicketButton(TicketProduct.DURATION_1W));
        durationPanel.add(createTicketButton(TicketProduct.DURATION_2W));
        durationPanel.add(createTicketButton(TicketProduct.DURATION_1M));
        durationPanel.add(createTicketButton(TicketProduct.DURATION_3M));

        tabPane.addTab("  기간권 (일 단위)  ", durationPanel);

        // 시간권
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

        //버튼
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
            JOptionPane.showMessageDialog(mainFrame, "정기권 구매 완료! 좌석 배치도 화면으로 이동합니다.");
            mainFrame.showPanel(KioskMainFrame.SEAT_MAP_PANEL);
        });

        backBtn.addActionListener(e -> mainFrame.showPanel(KioskMainFrame.TICKET_SELECTION_PANEL));
    }

    private JButton createTicketButton(TicketProduct product) {
        int price = priceManager.getPrice(product);
        String text = String.format("<html><center>%s<br><font size=5>%d원</font></center></html>",
                product.toString(), price);
        JButton btn = new JButton(text);
        Theme.styleButton(btn);
        btn.setBackground(Theme.PRIMARY_COLOR);
        return btn;
    }
}