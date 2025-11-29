
// 11/24 Payment/OrderLogEntry.java (새 파일)

package Payment;

public class OrderLogEntry {
    private String timestamp;
    private String type = "ORDER"; 
    private String memberId;
    private String seatNumber;
    private String detailedOrder;
    private long totalAmount;

    // KioskMainFrame.java에서 LocalDateTime import가 필요할 수 있음
    public OrderLogEntry(String timestamp, String memberId, String seatNumber, String detailedOrder, long totalAmount) {
        this.timestamp = timestamp;
        this.memberId = memberId;
        this.seatNumber = seatNumber;
        this.detailedOrder = detailedOrder;
        this.totalAmount = totalAmount;
    }
}
