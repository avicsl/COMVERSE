package Class;

public class Order {
    private String orderId;
    private String productId;
    private String cartId;
    private String studentNumber;
    private String orderDate;
    private int quantity;
    private double totalAmount;
    private String imageUrl;

    public Order(String orderId, String productId, String cartId, String studentNumber, String orderDate, int quantity, double totalAmount, String imageUrl) {
        this.orderId = orderId;
        this.productId = productId;
        this.cartId = cartId;
        this.studentNumber = studentNumber;
        this.orderDate = orderDate;
        this.quantity = quantity;
        this.totalAmount = totalAmount;
        this.imageUrl = imageUrl;
    }

    // Getters
    public String getOrderId() { return orderId; }
    public String getProductId() { return productId; }
    public String getCartId() { return cartId; }
    public String getStudentNumber() { return studentNumber; }
    public String getOrderDate() { return orderDate; }
    public int getQuantity() { return quantity; }
    public double getTotalAmount() { return totalAmount; }
    public String getImageUrl() { return imageUrl; }
}

