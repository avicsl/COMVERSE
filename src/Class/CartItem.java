package Class;

import javafx.beans.property.*;
import javafx.scene.control.CheckBox;

public class CartItem {
    private final StringProperty productId = new SimpleStringProperty();
    private final StringProperty imageUrl = new SimpleStringProperty();
    private final DoubleProperty price = new SimpleDoubleProperty();
    private final IntegerProperty quantity = new SimpleIntegerProperty();
    private final CheckBox select = new CheckBox();

    public CartItem(String productId, String imageUrl, double price, int quantity) {
        this.productId.set(productId);
        this.imageUrl.set(imageUrl);
        this.price.set(price);
        this.quantity.set(quantity);
        this.select.setSelected(false);
    }

    public StringProperty productIdProperty() { return productId; }
    public StringProperty imageUrlProperty() { return imageUrl; }
    public DoubleProperty priceProperty() { return price; }
    public IntegerProperty quantityProperty() { return quantity; }
    public CheckBox getSelect() { return select; }

    public String getProductId() { return productId.get(); }
    public double getPrice() { return price.get(); }
    public int getQuantity() { return quantity.get(); }
    public void setQuantity(int quantity) { this.quantity.set(quantity); }
}
