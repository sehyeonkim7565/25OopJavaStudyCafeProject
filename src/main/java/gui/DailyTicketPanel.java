package gui;

import payment.PriceManager;
import payment.TicketProduct;

import javax.swing.*;
import java.awt.*;

public class DailyTicketPanel extends JPanel {

    private KioskMainFrame mainFrame;
    private PriceManager priceManager;

    public DailyTicketPanel(KioskMainFrame mainFrame, PriceManager priceManager) {
        this.mainFrame = mainFrame;
        this.priceManager = priceManager;

        setLayout(new BorderLayout());
        setBackground(Theme.BACKGROUND_COLOR);

        //제목
        JLabel titleLabel = new JLabel("당일 이용권 선택", SwingConstants.CENTER);
        Theme.styleLabel(titleLabel, Theme.TITLE_FONT);
        titleLabel.setBorder(BorderFactory.createEmptyBorder(40, 0, 20, 0));
        add(titleLabel, BorderLayout.NORTH);

        JPanel ticketPanel = new JPanel(new GridLayout(2, 2, 20, 20));
        ticketPanel.setBackground(Theme.BACKGROUND_COLOR);
        ticketPanel.setBorder(BorderFactory.createEmptyBorder(20, 150, 40, 150));

        JButton btn3h = createTicketButton(TicketProduct.DAILY_3H);
        JButton btn6h = createTicketButton(TicketProduct.DAILY_6H);
        JButton btn12h = createTicketButton(TicketProduct.DAILY_12H);
        JButton btn24h = createTicketButton(TicketProduct.DAILY_24H);

        ticketPanel.add(btn3h);
        ticketPanel.add(btn6h);
        ticketPanel.add(btn12h);
        ticketPanel.add(btn24h);

        add(ticketPanel, BorderLayout.CENTER);

        //결제, 뒤로가기
        JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 20));
        bottomPanel.setBackground(Theme.BACKGROUND_COLOR);
        bottomPanel.setBorder(BorderFactory.createEmptyBorder(0, 0, 40, 0));

        JButton payBtn = new JButton("결제하기");
        Theme.styleButton(payBtn);
        payBtn.setBackground(Theme.ACCENT_COLOR);
        payBtn.setPreferredSize(new Dimension(200, 60));

        JButton backBtn = new JButton("뒤로가기");
        Theme.styleSecondaryButton(backBtn);
        backBtn.setPreferredSize(new Dimension(200, 60));

        bottomPanel.add(backBtn);
        bottomPanel.add(payBtn);

        add(bottomPanel, BorderLayout.SOUTH);

        payBtn.addActionListener(e -> {
            JOptionPane.showMessageDialog(mainFrame, "결제 완료! 입실 처리되었습니다.");
            mainFrame.showPanel(KioskMainFrame.MAIN_MENU_PANEL);
        });

        backBtn.addActionListener(e -> mainFrame.showPanel(KioskMainFrame.SEAT_MAP_PANEL));
    }

    private JButton createTicketButton(TicketProduct product) {
        int price = priceManager.getPrice(product);
        String text = String.format("<html><center>%s<br><font size=5>%,d원</font></center></html>",
                product.toString(), price);
        JButton btn = new JButton(text);
        Theme.styleButton(btn);
        return btn;
    }
}