
package Ticket;

import java.time.LocalDateTime;

// 기간권 (만료 시각 기반)
public class DurationTicket extends Ticket {

    private LocalDateTime expiryDate;

    public DurationTicket(LocalDateTime expiryDate) {
        this.expiryDate = expiryDate;
    }

    @Override
    public boolean isValid() {
        return LocalDateTime.now().isBefore(expiryDate);
    }

    public LocalDateTime getExpiryDate() {
        return expiryDate;
    }

    // 연장 기능: 주어진 시간(시간 단위)만큼 만료 시각 뒤로 미루기
    public void extendDuration(long hours) {
        if (hours <= 0) return;
        expiryDate = expiryDate.plusHours(hours);
    }
}
