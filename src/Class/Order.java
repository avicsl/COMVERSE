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
    private boolean userMarkedReceived;
    private String receivedDate;

    // Constructor for orders without received status (backwards compatibility)
    public Order(String orderId, String transactionId, String productId, String studentNumber, String orderDate, String quantity, double totalAmount, String imageUrl) {
        this.orderId = orderId;
        this.transactionId = transactionId;
        this.productId = productId;
        this.studentNumber = studentNumber;
        this.orderDate = orderDate;
        this.quantity = quantity;
        this.totalAmount = totalAmount;
        this.imageUrl = imageUrl;
        this.userMarkedReceived = false;
        this.receivedDate = null;
    }

    // Constructor for orders with received status (for admin view)
    public Order(String orderId, String transactionId, String productId, String studentNumber, String orderDate, String quantity, double totalAmount, String imageUrl, boolean userMarkedReceived, String receivedDate) {
        this.orderId = orderId;
        this.transactionId = transactionId;
        this.productId = productId;
        this.studentNumber = studentNumber;
        this.orderDate = orderDate;
        this.quantity = quantity;
        this.totalAmount = totalAmount;
        this.imageUrl = imageUrl;
        this.userMarkedReceived = userMarkedReceived;
        this.receivedDate = receivedDate;
    }

    // Getters
    public String getOrderId() { return orderId; }
    public String getTransactionId() { return transactionId; }
    public String getProductId() { return productId; }
    public String getStudentNumber() { return studentNumber; }
    public String getOrderDate() { return orderDate; }
    public String getQuantity() { return quantity; }
    public double getTotalAmount() { return totalAmount; }
    public String getImageUrl() { return imageUrl; }
    public boolean isUserMarkedReceived() { return userMarkedReceived; }
    public String getReceivedDate() { return receivedDate != null ? receivedDate : "Not Received"; }
    
    // Status display method for admin view
    public String getStatus() {
        return userMarkedReceived ? "Received" : "Pending";
    }

    // Setters (if needed)
    public void setUserMarkedReceived(boolean userMarkedReceived) { this.userMarkedReceived = userMarkedReceived; }
    public void setReceivedDate(String receivedDate) { this.receivedDate = receivedDate; }
}