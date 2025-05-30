package Class;
 
import javafx.beans.property.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
 
public class Product {
    private final StringProperty productId;
    private final StringProperty category;
    private final StringProperty productName;
    private final StringProperty imageUrl;
    private final DoubleProperty amount;
    private final ObjectProperty<ImageView> imageView;
    private final IntegerProperty quantity;   // <-- new quantity property
 
    public Product() {
        this.productId = new SimpleStringProperty();
        this.category = new SimpleStringProperty();
        this.productName = new SimpleStringProperty();
        this.imageUrl = new SimpleStringProperty();
        this.amount = new SimpleDoubleProperty();
        this.imageView = new SimpleObjectProperty<>();
        this.quantity = new SimpleIntegerProperty();  // initialize quantity
    }
 
    // Updated constructor - now includes quantity
    public Product(String productId, String category, String productName, String imageUrl, double amount, int quantity) {
        this.productId = new SimpleStringProperty(productId);
        this.category = new SimpleStringProperty(category);
        this.productName = new SimpleStringProperty(productName);
        this.imageUrl = new SimpleStringProperty(imageUrl);
        this.amount = new SimpleDoubleProperty(amount);
        this.quantity = new SimpleIntegerProperty(quantity);
 
        Image image = new Image(imageUrl, 100, 100, true, true);
        this.imageView = new SimpleObjectProperty<>(new ImageView(image));
    }
 
    // Properties getters
    public StringProperty productIdProperty() {
        return productId;
    }
 
    public StringProperty categoryProperty() {
        return category;
    }
 
    public StringProperty productNameProperty() {
        return productName;
    }
 
    public StringProperty imageUrlProperty() {
        return imageUrl;
    }
 
    public DoubleProperty amountProperty() {
        return amount;
    }
 
    public ObjectProperty<ImageView> imageViewProperty() {
        return imageView;
    }
 
    public IntegerProperty quantityProperty() {
        return quantity;
    }
 
    // Getters
    public String getProductId() {
        return productId.get();
    }
 
    public String getCategory() {
        return category.get();
    }
 
    public String getProductName() {
        return productName.get();
    }
 
    public String getImageUrl() {
        return imageUrl.get();
    }
 
    public double getAmount() {
        return amount.get();
    }
 
    public ImageView getImageView() {
        return imageView.get();
    }
 
    public int getQuantity() {
        return quantity.get();
    }
 
    // Setters
    public void setProductId(String productId) {
        this.productId.set(productId);
    }
 
    public void setCategory(String category) {
        this.category.set(category);
    }
 
    public void setProductName(String productName) {
        this.productName.set(productName);
    }
 
    public void setImageUrl(String imageUrl) {
        this.imageUrl.set(imageUrl);
        Image image = new Image(imageUrl, 100, 100, true, true);
        this.imageView.set(new ImageView(image));
    }
 
    public void setAmount(double amount) {
        this.amount.set(amount);
    }
 
    public void setImageView(ImageView imageView) {
        this.imageView.set(imageView);
    }
 
    public void setQuantity(int quantity) {
        this.quantity.set(quantity);
    }

    
}
 