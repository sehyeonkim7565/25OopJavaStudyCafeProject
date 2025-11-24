package gui;

import javax.swing.JPanel;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.BorderFactory;
import java.awt.GridLayout;
import java.awt.BorderLayout;
import java.awt.Font;
import java.awt.Dimension;

public class TicketSelectionPanel extends JPanel {

    private KioskMainFrame mainFrame;

    public TicketSelectionPanel(KioskMainFrame mainFrame) {
        this.mainFrame = mainFrame;
        setLayout(new BorderLayout());
        setBackground(Theme.BACKGROUND_COLOR);

        // 상단 안내 문구
        JLabel titleLabel = new JLabel("구매하실 이용권을 선택하세요", javax.swing.SwingConstants.CENTER);
        Theme.styleLabel(titleLabel, Theme.TITLE_FONT);
        titleLabel.setBorder(BorderFactory.createEmptyBorder(50, 0, 50, 0));
        add(titleLabel, BorderLayout.NORTH);

        JPanel buttonPanel = new JPanel(new GridLayout(1, 2, 40, 0)); // 가로로 2개 배치
        buttonPanel.setBackground(Theme.BACKGROUND_COLOR);
        buttonPanel.setBorder(BorderFactory.createEmptyBorder(50, 100, 100, 100));

        JButton dailyBtn = new JButton("<html><center>당일 시간제<br><small>(1회 이용)</small></center></html>");
        JButton passBtn = new JButton("<html><center>정기권 / 시간권<br><small>(다회 이용)</small></center></html>");

        Theme.styleButton(dailyBtn);
        Theme.styleButton(passBtn);

        dailyBtn.setFont(new Font("Malgun Gothic", Font.BOLD, 24));
        passBtn.setFont(new Font("Malgun Gothic", Font.BOLD, 24));

        buttonPanel.add(dailyBtn);
        buttonPanel.add(passBtn);

        add(buttonPanel, BorderLayout.CENTER);

        //뒤로가기
        JPanel bottomPanel = new JPanel();
        bottomPanel.setBackground(Theme.BACKGROUND_COLOR);
        bottomPanel.setBorder(BorderFactory.createEmptyBorder(0, 0, 40, 0));

        JButton backBtn = new JButton("메인 메뉴로 돌아가기");
        Theme.styleSecondaryButton(backBtn);
        backBtn.setPreferredSize(new Dimension(250, 50));

        bottomPanel.add(backBtn);
        add(bottomPanel, BorderLayout.SOUTH);

        dailyBtn.addActionListener(e -> mainFrame.showPanel(KioskMainFrame.SEAT_MAP_PANEL));
        passBtn.addActionListener(e -> mainFrame.showPassPurchaseForSelection());
        backBtn.addActionListener(e -> mainFrame.showPanel(KioskMainFrame.MAIN_MENU_PANEL));
    }
}
