package payment;

public class OrderLogEntry {
    private String timestamp;
    private String type = "ORDER";
    private String memberId;
    private String seatNumber;
    private String detailedOrder;
    private long totalAmount;

    public OrderLogEntry(String timestamp, String memberId, String seatNumber, String detailedOrder, long totalAmount) {
        this.timestamp = timestamp;
        this.memberId = memberId;
        this.seatNumber = seatNumber;
        this.detailedOrder = detailedOrder;
        this.totalAmount = totalAmount;
    }
}
