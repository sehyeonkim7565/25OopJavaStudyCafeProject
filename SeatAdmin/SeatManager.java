package OopProject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;


public class SeatManager {

    // 모든 좌석 객체
    private List<Seat> seatList;

    /*
     * 외부에서 좌석 리스트를 주입
     * 파일 입출력으로 생성한 좌석 리스트를 넘겨주기
     */
    public SeatManager(List<Seat> seatList) {
        this.seatList = new ArrayList<>(seatList);
    }

    /*
     * 좌석 개수와 기본 정보를 받아 내부에서 좌석을 생성
     */
    public SeatManager(int seatCount) {
        this.seatList = new ArrayList<>();
        for (int i = 1; i <= seatCount; i++) {
            String seatNumber = String.valueOf(i);
            seatList.add(new Seat(seatNumber));
        }
    }

    /**
     * 현재 모든 좌석의 상태 목록 반환
     * unmodifiableList >> 외부에서 수정 x
     */
    public List<Seat> getSeatMap() {
        return Collections.unmodifiableList(seatList);
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


    /**
     * seatNumber로 좌석 찾기
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
}
