/* 화면 4: 좌석 배치도 화면 */

 package gui;

import ReadingRoomLogin.Member;
import Seat.Seat;
import SeatManager.SeatManager;
import KioskService.CheckInService;
import KioskService.SeatMoveService;

import javax.swing.JPanel;
import javax.swing.JButton;
import javax.swing.JOptionPane;
import javax.swing.BorderFactory;
import java.awt.GridLayout;
import java.awt.BorderLayout;
import java.awt.Color;
import java.util.List;

public class SeatMapPanel extends JPanel {

    private KioskMainFrame mainFrame;
    private SeatManager seatManager;
    private CheckInService checkInService;
    private SeatMoveService seatMoveService;
    private JPanel seatGridPanel;

    public SeatMapPanel(KioskMainFrame mainFrame, SeatManager seatManager, CheckInService checkInService, SeatMoveService seatMoveService) {
        this.mainFrame = mainFrame;
        this.seatManager = seatManager;
        this.checkInService = checkInService;
        this.seatMoveService = seatMoveService;

        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        seatGridPanel = new JPanel(new GridLayout(5, 5, 5, 5)); // 5x5 = 25석 예시
        add(seatGridPanel, BorderLayout.CENTER);

        JButton backBtn = new JButton("뒤로가기");
        add(backBtn, BorderLayout.SOUTH);

        initializeSeats();

        backBtn.addActionListener(e -> {
            Member member = mainFrame.getCurrentMember();
            if (member != null && member.hasValidTicket()) {
                mainFrame.showPanel(KioskMainFrame.MAIN_MENU_PANEL);
            } else {
                mainFrame.showPanel(KioskMainFrame.TICKET_SELECTION_PANEL);
            }
        });
    }

    // SeatManager의 정보를 바탕으로 좌석 버튼을 생성
    private void initializeSeats() {
        seatGridPanel.removeAll();
        List<Seat> seats = mainFrame.getSeatManager().getSeatMap();

        for (Seat seat : seats) {
            JButton seatButton = new JButton(seat.getSeatNumber());
            seatButton.setOpaque(true);
            
            if (seat.isAvailable()) {
                seatButton.setBackground(Color.GREEN);
            } else {
                seatButton.setBackground(Color.RED);
                seatButton.setEnabled(false);
            }

            seatButton.addActionListener(e -> {
                handleSeatSelection(seat);
            });
            seatGridPanel.add(seatButton);
        }
        seatGridPanel.revalidate();
        seatGridPanel.repaint();
    }
    
    public void updateSeatStatus() {
        initializeSeats();
    }

    private void handleSeatSelection(Seat seat) {
        Member member = mainFrame.getCurrentMember();
        if (member == null) { 
            JOptionPane.showMessageDialog(mainFrame, seat.getSeatNumber() + "번 좌석 선택. 결제 화면으로 이동합니다.");
            mainFrame.showPanel(KioskMainFrame.DAILY_TICKET_PANEL);

        } else if (mainFrame.isSeatMoveMode()) {
            if (!seat.isAvailable()) {
                JOptionPane.showMessageDialog(mainFrame, "이미 사용 중인 좌석입니다.");
                return;
            }
            int confirm = JOptionPane.showConfirmDialog(mainFrame,
                    seat.getSeatNumber() + "번 좌석으로 이동하시겠습니까?", "자리 이동", JOptionPane.YES_NO_OPTION);
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
        } else if (member.hasValidTicket()) {
            int confirm = JOptionPane.showConfirmDialog(mainFrame, 
                seat.getSeatNumber() + "번 좌석으로 입실하시겠습니까?", "입실 확인", JOptionPane.YES_NO_OPTION);
            
            if (confirm == JOptionPane.YES_OPTION) {
                boolean success = checkInService.checkIn(member.getId(), seat.getSeatNumber());
                if (success) {
                    JOptionPane.showMessageDialog(mainFrame, "입실 완료!");
                    updateSeatStatus();
                    mainFrame.showPanel(KioskMainFrame.MAIN_MENU_PANEL);
                } else {
                    JOptionPane.showMessageDialog(mainFrame, "입실에 실패했습니다. 이미 사용 중인 좌석이거나 티켓이 없습니다.");
                    updateSeatStatus();
                }
            }
        } else {
            JOptionPane.showMessageDialog(mainFrame, seat.getSeatNumber() + "번 좌석 선택. 당일권 결제 화면으로 이동합니다.");
            mainFrame.showPanel(KioskMainFrame.DAILY_TICKET_PANEL);
        }
    }
}
