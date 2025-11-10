package Seat;

import java.time.Duration;
import java.time.LocalDateTime;

//1회 좌석 이용에 대한 시작-종료 시간 기록
public class UsageSession {
	private Seat seat;
	private String userId;
    private LocalDateTime startTime;
    private LocalDateTime endTime;     // 퇴실 시각 (null이면 진행 중)
    //private Payment payment;           // 이 이용에 대한 결제 정보
    
    public UsageSession(String userId, Seat seat) {
    	if (userId == null || userId.isBlank()) 
    		throw new IllegalArgumentException("이용자 ID는 반드시 입력해야 합니다.");
    	 if (seat == null)
             throw new IllegalArgumentException("좌석 정보가 올바르지 않습니다.");
        this.userId = userId;
        this.seat = seat;
        this.startTime = LocalDateTime.now();
        this.endTime = null;
        
        seat.occupy(userId); //세션이 시작되면 좌석 점유
    }
    
    public void endSession() {
    	if (this.endTime != null) 
    		throw new IllegalStateException("이미 종료된 이용 세션입니다.");
        this.endTime = LocalDateTime.now();
        seat.vacate(); //세션이 끝나면 좌석 해제
    }
    
    public long getDurationInMinutes() {
        LocalDateTime to = (endTime != null) ? endTime : LocalDateTime.now();
        return Math.max(0, Duration.between(startTime, to).toMinutes());
    }
    
}
