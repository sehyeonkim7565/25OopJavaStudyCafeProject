package SeatManager;

import java.util.Collections;
import java.util.List;
import Seat.Seat;

public class SeatManager {

    // 모든 좌석 객체
    private List<Seat> seatList;

    // SeatFactory에서 만든 리스트를 주입
    public SeatManager(List<Seat> seatList) {
        this.seatList = seatList;
    }

    /**
     * 특정 회원이 앉아 있는 좌석을 찾아서 반환.
     * 없으면 null 반환.
     */
    public Seat findSeatByMember(String memberId) {
        if (memberId == null) return null;

        for (Seat seat : seatList) {
            if (memberId.equals(seat.getOccupantId())) {
                return seat;
            }
        }
        return null;
    }

    /**
     * 좌석 배정
     * seatNumber에 해당하는 좌석을 찾고
     * 사용 가능하면 해당 회원에게 배정 후 true,
     * 없거나 이미 사용 중이면 false 반환.
     */
    public boolean assignSeat(String memberId, String seatNumber) {
        if (memberId == null || seatNumber == null) return false;

        Seat target = findSeatByNumber(seatNumber);
        if (target == null) {
            // 존재하지 않는 좌석
            return false;
        }

        if (!target.isAvailable()) {
            // 이미 사용 중인 좌석
            return false;
        }

        target.occupy(memberId);
        return true;
    }

    /**
     * 좌석 비우기 (퇴실, 외출).
     * memberId가 앉아 있는 좌석을 찾고
     * 있으면 vacate() 호출, 성공 시 true 없으면 false 반환.
     */
    public boolean vacateSeat(String memberId) {
        if (memberId == null) return false;

        Seat seat = findSeatByMember(memberId);
        if (seat == null) {
            // 해당 회원이 사용 중인 좌석이 없음
            return false;
        }

        seat.vacate();
        return true;
    }

    public List<Seat> getSeatMap() {
        return Collections.unmodifiableList(seatList);
    }

    /**
     * findSeatByNumber(String seatNumber)
     * seatNumber로 좌석을 찾는 내부 전용 메서드.
     * KioskService에서 사용할 일이 있을 수 있어서 public으로 열어둬도 됨.
     */
    public Seat findSeatByNumber(String seatNumber) {
        if (seatNumber == null) return null;

        for (Seat seat : seatList) {
            if (seatNumber.equals(seat.getSeatNumber())) {
                return seat;
            }
        }
        return null;
    }

    /**
     * 좌석 이동: 현재 좌석을 비우고 새 좌석을 점유.
     */
    public Seat moveSeat(String memberId, String newSeatNumber) {
        if (memberId == null || newSeatNumber == null) return null;
        Seat current = findSeatByMember(memberId);
        if (current == null) return null;

        Seat target = findSeatByNumber(newSeatNumber);
        if (target == null || !target.isAvailable()) {
            return null;
        }

        current.vacate();
        target.occupy(memberId);
        return target;
    }
}
