package gui;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

public class Theme {
    // 색상 팔레트
    public static final Color PRIMARY_COLOR = new Color(70, 130, 180);
    public static final Color BACKGROUND_COLOR = new Color(245, 248, 250);
    public static final Color TEXT_COLOR = new Color(50, 50, 50);
    public static final Color ACCENT_COLOR = new Color(255, 140, 0);
    public static final Color AVAILABLE_SEAT = new Color(100, 200, 100);
    public static final Color OCCUPIED_SEAT = new Color(200, 80, 80);

    // 폰트 설정
    public static final Font TITLE_FONT = new Font("Malgun Gothic", Font.BOLD, 30);
    public static final Font SUBTITLE_FONT = new Font("Malgun Gothic", Font.BOLD, 20);
    public static final Font MAIN_FONT = new Font("Malgun Gothic", Font.PLAIN, 18);
    public static final Font BUTTON_FONT = new Font("Malgun Gothic", Font.BOLD, 18);

    // 버튼 스타일 적용 메서드
    public static void styleButton(JButton btn) {
        btn.setFont(BUTTON_FONT);
        btn.setBackground(PRIMARY_COLOR);
        btn.setForeground(Color.WHITE);
        btn.setFocusPainted(false);
        btn.setBorder(new EmptyBorder(10, 20, 10, 20)); //버튼 여백
        btn.setOpaque(true);
    }

    public static void styleSecondaryButton(JButton btn) {
        styleButton(btn);
        btn.setBackground(Color.GRAY);
    }

    //라벨 스타일 적용
    public static void styleLabel(JLabel label, Font font) {
        label.setFont(font);
        label.setForeground(TEXT_COLOR);
        label.setHorizontalAlignment(SwingConstants.CENTER);
    }
}