package Class;

import javafx.beans.property.*;
import javafx.scene.control.CheckBox;

public class CartItem {
    private final SimpleStringProperty productID;       // Added: product ID
    private final SimpleStringProperty productName;
    private final SimpleStringProperty imageUrl;
    private final SimpleDoubleProperty price;
    private final SimpleIntegerProperty quantity;
    private final CheckBox select;

    

    public CartItem(String productID, String productName, String imageUrl, double price) {
        this.productID = new SimpleStringProperty(productID); // New field
        this.productName = new SimpleStringProperty(productName);
        this.imageUrl = new SimpleStringProperty(imageUrl);
        this.price = new SimpleDoubleProperty(price);
        this.quantity = new SimpleIntegerProperty(1);
        this.select = new CheckBox();
        this.select.setSelected(true); // Auto-check when added
    }

    // Getters
    public String getProductID() { return productID.get(); }              // New
    public String getProductName() { return productName.get(); }
    public String getImageUrl() { return imageUrl.get(); }
    public double getPrice() { return price.get(); }
    public int getQuantity() { return quantity.get(); }
    public CheckBox getSelect() { return select; }

    // Setters
    public void setQuantity(int quantity) { this.quantity.set(quantity); }

    // Property Getters
    public StringProperty productIDProperty() { return productID; }       // New
    public StringProperty productNameProperty() { return productName; }
    public StringProperty imageUrlProperty() { return imageUrl; }
    public DoubleProperty priceProperty() { return price; }
    public IntegerProperty quantityProperty() { return quantity; }
}
