package payment;

import java.time.LocalDate;
import java.time.LocalDateTime;

import Ticket.Ticket;
import Ticket.DurationTicket;
import Ticket.TimeTicket;

public class TicketFactory {
    public Ticket createTicket(TicketProduct product) {
        switch (product) {
            case DAILY_3H:
                return new DurationTicket(LocalDateTime.now().plusHours(3));
            case DAILY_6H:
                return new DurationTicket(LocalDateTime.now().plusHours(6));
            case DAILY_12H:
                return new DurationTicket(LocalDateTime.now().plusHours(12));
            case DAILY_24H:
                return new DurationTicket(LocalDateTime.now().plusHours(24));

            case DURATION_1W:
                return new DurationTicket(LocalDate.now().plusWeeks(1).atTime(23, 59, 59));
            case DURATION_2W:
                return new DurationTicket(LocalDate.now().plusWeeks(2).atTime(23, 59, 59));
            case DURATION_1M:
                return new DurationTicket(LocalDate.now().plusMonths(1).atTime(23, 59, 59));
            case DURATION_3M:
                return new DurationTicket(LocalDate.now().plusMonths(3).atTime(23, 59, 59));

            case TIME_50H:
                return new TimeTicket(50 * 60);
            case TIME_100H:
                return new TimeTicket(100 * 60);
            case TIME_200H:
                return new TimeTicket(200 * 60);

            default:
                throw new IllegalArgumentException("존재하지 않는 이용권입니다.");
        }
    }

    public long getHoursFromProduct(TicketProduct product) {
        switch (product) {
            case DAILY_3H: return 3;
            case DAILY_6H: return 6;
            case DAILY_12H: return 12;
            case DAILY_24H: return 24;

            case TIME_50H: return 50;
            case TIME_100H: return 100;
            case TIME_200H: return 200;

            case DURATION_1W: return 7 * 24L;
            case DURATION_2W: return 14 * 24L;
            case DURATION_1M: return 30 * 24L;  // 월 단위는 30일 기준으로 확장
            case DURATION_3M: return 90 * 24L;

            default:
                System.err.println("해당 이용권은 연장할 수 없습니다.");
                return 0;
        }
    }
}
