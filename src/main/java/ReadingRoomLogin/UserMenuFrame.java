package ReadingRoomLogin;

//--------------------------------------------------
//유저 메뉴 프레임 클래스
//--------------------------------------------------

import java.awt.GridLayout;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.SwingConstants;

class UserMenuFrame extends JFrame {
    private Member member;
    private MemberManager manager;

    public UserMenuFrame(Member member, MemberManager manager) {
        this.member = member;
        this.manager = manager;

        setTitle("좌석 예약 키오스크 - 메뉴");
        setSize(400, 300);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLayout(new GridLayout(4, 1, 10, 10));

        JLabel welcome = new JLabel(member.getName() + "님, 환영합니다!", SwingConstants.CENTER);
        JButton seatBtn = new JButton("좌석 선택");
        JButton chargeBtn = new JButton("충전하기");
        JButton logoutBtn = new JButton("로그아웃");

        add(welcome);
        add(seatBtn);
        add(chargeBtn);
        add(logoutBtn);

        // 버튼 동작
        seatBtn.addActionListener(e -> {
            JOptionPane.showMessageDialog(this, "좌석 선택 화면으로 이동합니다.");
            // 👉 나중에 SeatSelectionFrame(member, manager) 열면 됨
        });

        chargeBtn.addActionListener(e -> {
            JOptionPane.showMessageDialog(this, "충전 화면으로 이동합니다.");
            // 👉 나중에 ChargeFrame(member, manager) 열면 됨
        });

        logoutBtn.addActionListener(e -> {
            int confirm = JOptionPane.showConfirmDialog(this, "로그아웃 하시겠습니까?", "확인", JOptionPane.YES_NO_OPTION);
            if (confirm == JOptionPane.YES_OPTION) {
                new ReadingRoomLogin().setVisible(true);
                dispose();
            }
        });
    }
}