package payment;

public class PaymentService {
    public PaymentService() {

    }

    public boolean processPayment(int price, String paymentMethod) {
        // 간단한 결제 시뮬레이션: 결제수단 문자열을 표준화하고 항상 성공으로 처리
        String method = (paymentMethod == null) ? "" : paymentMethod.trim().toUpperCase();
        if (method.equals("CARD")) {
            // 카드 결제 성공 시뮬레이션
        } else if (method.equals("CASH")) {
            // 현금 결제 성공 시뮬레이션
        } else {
            // 알 수 없는 결제수단은 카드로 간주
            method = "CARD";
        }

        return true;
    }
}
