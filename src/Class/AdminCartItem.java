package Class;

import javafx.beans.property.*;

public class AdminCartItem {
    private final StringProperty cartId;
    private final StringProperty productId;
    private final StringProperty studentNumber;
    private final StringProperty imageUrl;
    private final IntegerProperty quantity;
    private final DoubleProperty amount;

    public AdminCartItem(String cartId, String productId, String studentNumber, String imageUrl, int quantity, double amount) {
        this.cartId = new SimpleStringProperty(cartId);
        this.productId = new SimpleStringProperty(productId);
        this.studentNumber = new SimpleStringProperty(studentNumber);
        this.imageUrl = new SimpleStringProperty(imageUrl);
        this.quantity = new SimpleIntegerProperty(quantity);
        this.amount = new SimpleDoubleProperty(amount);
    }

    public String getCartId() {
        return cartId.get();
    }

    public StringProperty cartIdProperty() {
        return cartId;
    }

    public String getProductId() {
        return productId.get();
    }

    public StringProperty productIdProperty() {
        return productId;
    }

    public String getStudentNumber() {
        return studentNumber.get();
    }

    public StringProperty studentNumberProperty() {
        return studentNumber;
    }

    public String getImageUrl() {
        return imageUrl.get();
    }

    public StringProperty imageUrlProperty() {
        return imageUrl;
    }

    public int getQuantity() {
        return quantity.get();
    }

    public IntegerProperty quantityProperty() {
        return quantity;
    }

    public double getAmount() {
        return amount.get();
    }

    public DoubleProperty amountProperty() {
        return amount;
    }
}
