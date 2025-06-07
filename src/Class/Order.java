package Class;

public class Order {
    private String orderId;
    private String transactionId;
    private String productId;
    private String studentNumber;
    private String orderDate;
    private String quantity; // keep as String if DB is VARCHAR
    private double totalAmount;
    private String imageUrl;

    public Order(String orderId, String transactionId, String productId, String studentNumber, String orderDate, String quantity, double totalAmount, String imageUrl) {
        this.orderId = orderId;
        this.transactionId = transactionId;
        this.productId = productId;
        this.studentNumber = studentNumber;
        this.orderDate = orderDate;
        this.quantity = quantity;
        this.totalAmount = totalAmount;
        this.imageUrl = imageUrl;
    }

    public String getOrderId() { return orderId; }
    public String getTransactionId() { return transactionId; }
    public String getProductId() { return productId; }
    public String getStudentNumber() { return studentNumber; }
    public String getOrderDate() { return orderDate; }
    public String getQuantity() { return quantity; }
    public double getTotalAmount() { return totalAmount; }
    public String getImageUrl() { return imageUrl; }
}
