package payment;

import Seat.UsageSession;
import payment.OrderLogEntry;
public interface ILogManager {
    void savePaymentLog(Payment payment);
    void saveUsageStart(UsageSession session);
    void updateUsageEnd(UsageSession session);
    void saveOrderLog(OrderLogEntry entry); // 주문 로그
    void refreshOngoingUsageDurations();
    void logSeatMove(String memberId, String fromSeat, String toSeat, java.time.LocalDateTime movedAt);
}
