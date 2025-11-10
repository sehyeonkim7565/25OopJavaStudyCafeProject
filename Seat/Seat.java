package Seat;
//좌석 1개의 상태를 관리
public class Seat {
	public enum Status { AVAILABLE, IN_USE }
	
	private String seatNumber;
	private Status status;
	private String occupantId;
	
	public Seat(String seatNumber) {
        if (seatNumber == null || seatNumber.isBlank())
            throw new IllegalArgumentException("좌석 번호를 반드시 입력해야 합니다.");
        this.seatNumber = seatNumber;
        this.status = Status.AVAILABLE;
        this.occupantId = null;
    }
	//좌석을 '사용 중'으로 변경 (입실)
	public void occupy(String memberId){
		if (memberId == null || memberId.isBlank())
            throw new IllegalArgumentException("이용자 ID를 반드시 입력해야 합니다.");
        if (status == Status.IN_USE)
            throw new IllegalStateException("이미 사용 중인 좌석입니다.: " + seatNumber);
		this.status = Status.IN_USE;
		this.occupantId = memberId;
	}
	//좌석을 '사용 가능'으로 변경 (퇴실, 외출)
	public void vacate(){
		this.status = Status.AVAILABLE;
		this.occupantId = null;
	}
	//현재 좌석이 사용 가능한지 확인
	public boolean isAvailable(){
		return status == Status.AVAILABLE; 
	}
	
		
}
