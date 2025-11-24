package payment;

import Ticket.Ticket;
import Ticket.DurationTicket;
import Ticket.TimeTicket;
import ReadingRoomLogin.Member;

public class PurchaseService {
    private final PriceManager priceManager;
    private final PaymentService paymentService;
    private final TicketFactory ticketFactory;
    private final IMemberManager memberManager;
    private final ILogManager logManager;

    public PurchaseService(PriceManager pm, PaymentService ps, TicketFactory tf, IMemberManager mm, ILogManager lm) {
        this.priceManager = pm;
        this.paymentService = ps;
        this.ticketFactory = tf;
        this.memberManager = mm;
        this.logManager = lm;
    }

    public boolean purchaseTicket(String memberID, TicketProduct product, String paymentMethod) {
        int price = 0;

        try {
            price = processPayment(product, paymentMethod);

            Member member = memberManager.findMemberById(memberID);
            if (member == null) {
                throw new Exception("존재하지 않는 회원입니다.");
            }

            Ticket current = member.getTicket();
            Ticket newTicket;
            long hours = ticketFactory.getHoursFromProduct(product);

            if (current instanceof TimeTicket && isTimeProduct(product) && hours > 0) {
                // 시간권 보유 시 연장: 남은 시간 누적
                ((TimeTicket) current).addMinutes(hours * 60);
                newTicket = current;
            } else if (current instanceof DurationTicket && isDurationProduct(product) && hours > 0) {
                // 당일/기간권 보유 시 연장: 만료 시각 뒤로 밀기
                ((DurationTicket) current).extendDuration(hours);
                newTicket = current;
            } else {
                // 보유권이 없으면 새로 발급
                newTicket = ticketFactory.createTicket(product);
            }

            memberManager.setTicket(memberID, newTicket);
            memberManager.saveMembersToFile();

            logPayment(memberID, product, price, paymentMethod);
            System.out.printf("%s 구매 완료", product.toString());
            return true;
        } catch (Exception e) {
            handleCriticalError(e, price);
            return false;
        }
    }

    public boolean extendTime(String memberID, TicketProduct extensionProduct, String paymentMethod) {
        int price = 0;

        try {
            price = processPayment(extensionProduct, paymentMethod);

            Member member = memberManager.findMemberById(memberID);
            if (member == null || member.getTicket() == null) {
                throw new Exception("존재하지 않는 회원이거나 연장할 이용권이 없습니다.");
            }

            Ticket currentTicket = member.getTicket();
            long hoursToExtend = ticketFactory.getHoursFromProduct(extensionProduct);

            if (currentTicket instanceof DurationTicket) {
                ((DurationTicket) currentTicket).extendDuration(hoursToExtend);
            } else if (currentTicket instanceof TimeTicket) {
                ((TimeTicket) currentTicket).addMinutes(hoursToExtend * 60);
            } else {
                throw new Exception("연장이 불가한 이용권입니다.");
            }
            memberManager.saveMembersToFile();

            logPayment(memberID, extensionProduct, price, paymentMethod);
            System.out.println("연장 되었습니다.");
            return true;
        } catch (Exception e) {
            handleCriticalError(e, price);
            return false;
        }
    }

    private int processPayment(TicketProduct product, String paymentMethod) throws Exception {
        int price = priceManager.getPrice(product);
        if (price == 0) {
            throw new Exception("유효하지 않은 상품입니다.");
        }

        boolean paymentSuccess = paymentService.processPayment(price, paymentMethod);
        if (!paymentSuccess) {
            throw new Exception("결제에 실패했습니다.");
        }

        return price;
    }

    private void logPayment(String memberID, TicketProduct productPurchased, int price, String paymentMethod) {
        Payment paymentLog = new Payment(memberID, productPurchased, price, paymentMethod);
        logManager.savePaymentLog(paymentLog);
    }

    private void handleCriticalError(Exception e, int price) {
        System.err.println("오류 발생" + e.getMessage());
        e.printStackTrace();

        if (price > 0) {
            System.err.println("결제 성공, 이용권 발급 실패: 관리자에게" + price + "원 환불 문의 바람\n010-1234-5678");
        }
    }

    private boolean isTimeProduct(TicketProduct product) {
        switch (product) {
            case TIME_50H:
            case TIME_100H:
            case TIME_200H:
                return true;
            default:
                return false;
        }
    }

    private boolean isDurationProduct(TicketProduct product) {
        switch (product) {
            case DAILY_3H:
            case DAILY_6H:
            case DAILY_12H:
            case DAILY_24H:
            case DURATION_1W:
            case DURATION_2W:
            case DURATION_1M:
            case DURATION_3M:
                return true;
            default:
                return false;
        }
    }
}
