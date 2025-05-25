package Class;

import javafx.beans.property.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

public class Cart {
    private final StringProperty cartId;
    private final StringProperty productId;
    private final StringProperty imageUrl;
    private final IntegerProperty quantity;
    private final DoubleProperty amount;
    private final ObjectProperty<ImageView> imageView;
    
    // For checkbox selection in UI
    private final BooleanProperty selected = new SimpleBooleanProperty(false);

    // Default constructor
    public Cart() {
        this.cartId = new SimpleStringProperty();
        this.productId = new SimpleStringProperty();
        this.imageUrl = new SimpleStringProperty();
        this.quantity = new SimpleIntegerProperty();
        this.amount = new SimpleDoubleProperty();
        this.imageView = new SimpleObjectProperty<>();
    }

    // Full constructor
    public Cart(String cartId, String productId, String imageUrl, int quantity, double amount) {
        this.cartId = new SimpleStringProperty(cartId);
        this.productId = new SimpleStringProperty(productId);
        this.imageUrl = new SimpleStringProperty(imageUrl);
        this.quantity = new SimpleIntegerProperty(quantity);
        this.amount = new SimpleDoubleProperty(amount);

        Image image = new Image(imageUrl, 100, 100, true, true);
        this.imageView = new SimpleObjectProperty<>(new ImageView(image));
    }

    // Property getters for binding
    public StringProperty cartIdProperty() {
        return cartId;
    }

    public StringProperty productIdProperty() {
        return productId;
    }

    public StringProperty imageUrlProperty() {
        return imageUrl;
    }

    public IntegerProperty quantityProperty() {
        return quantity;
    }

    public DoubleProperty amountProperty() {
        return amount;
    }

    public ObjectProperty<ImageView> imageViewProperty() {
        return imageView;
    }

    public BooleanProperty selectedProperty() {
        return selected;
    }

    // Standard getters
    public String getCartId() {
        return cartId.get();
    }

    public String getProductId() {
        return productId.get();
    }

    public String getImageUrl() {
        return imageUrl.get();
    }

    public int getQuantity() {
        return quantity.get();
    }

    public double getAmount() {
        return amount.get();
    }

    public ImageView getImageView() {
        return imageView.get();
    }

    public boolean isSelected() {
        return selected.get();
    }

    // Standard setters
    public void setCartId(String cartId) {
        this.cartId.set(cartId);
    }

    public void setProductId(String productId) {
        this.productId.set(productId);
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl.set(imageUrl);
        Image image = new Image(imageUrl, 100, 100, true, true);
        this.imageView.set(new ImageView(image));
    }

    public void setQuantity(int quantity) {
        this.quantity.set(quantity);
    }

    public void setAmount(double amount) {
        this.amount.set(amount);
    }

    public void setImageView(ImageView imageView) {
        this.imageView.set(imageView);
    }

    public void setSelected(boolean selected) {
        this.selected.set(selected);
    }
}
