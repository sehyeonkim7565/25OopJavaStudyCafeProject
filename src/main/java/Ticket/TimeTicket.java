
package Ticket;

public class TimeTicket extends Ticket {

    private int remainingMinutes;   // 남은 시간(분)

    public TimeTicket(int remainingMinutes) {
        this.remainingMinutes = Math.max(0, remainingMinutes);
    }

    /** 티켓 유효 여부: 남은 시간이 1분 이상이면 true */
    @Override
    public boolean isValid() {
        return remainingMinutes > 0;
    }

    /** 사용한 시간(분)을 차감 */
    public void deductTime(int minutes) {
        if (minutes < 0) return;
        remainingMinutes = Math.max(0, remainingMinutes - minutes);
    }

    //  연장 기능: 분 단위로 시간 추가
    public void addMinutes(long minutes) {
        if (minutes <= 0) return;
        long total = (long) remainingMinutes + minutes;
        // int 범위 넘지 않게
        remainingMinutes = (int) Math.min(Integer.MAX_VALUE, total);
    }

    public int getRemainingMinutes() {
        return remainingMinutes;
    }
}
