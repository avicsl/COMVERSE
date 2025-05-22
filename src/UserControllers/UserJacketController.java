package UserControllers;
 
import Class.Product;
import Main.DatabaseHandler;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.VBox;
import javafx.util.Callback;
 
public class UserJacketController {
 
    @FXML
    private Button addtocartbtn;
 
    @FXML
    private TableView<Product> mytable;
 
    @FXML
    private TableColumn<Product, String> productnamecol;
 
    @FXML
    private TableColumn<Product, String> productimagecol;
 
    @FXML
    private TableColumn<Product, Double> pricecol;
 
    private final ObservableList<Product> productList = FXCollections.observableArrayList();
 
    private static UserJacketController instance;
 
    public UserJacketController() {
        instance = this;
    }
 
    public static void refreshTableWithSpinner() {
        if (instance != null) {
            instance.loadJacketProducts();
            instance.mytable.refresh();
        }
    }
 
    @FXML
    public void initialize() {
        setupColumns();
        loadJacketProducts();
        mytable.setItems(productList);
    }
 
    private void setupColumns() {
        productnamecol.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getProductName()));
 
        pricecol.setCellValueFactory(cellData -> new SimpleObjectProperty<>(cellData.getValue().getAmount()));
        pricecol.setCellFactory(col -> new TableCell<>() {
            private final Label priceLabel = new Label();
            private final Spinner<Integer> quantitySpinner = new Spinner<>(1, 100, 1);
            private final VBox vbox = new VBox(5);
 
            {
                quantitySpinner.setEditable(true);
                vbox.getChildren().addAll(priceLabel, quantitySpinner);
 
                quantitySpinner.valueProperty().addListener((obs, oldVal, newVal) -> {
                    Product product = getTableRow().getItem();
                    if (product != null) {
                        product.setQuantity(newVal);
                        updateProductQuantityInDB(product.getProductId(), newVal);
                    }
                });
            }
 
            @Override
            protected void updateItem(Double price, boolean empty) {
                super.updateItem(price, empty);
                if (empty || price == null) {
                    setGraphic(null);
                } else {
                    Product product = getTableRow().getItem();
                    if (product != null) {
                        priceLabel.setText(String.format("₱ %.2f", price));
                        quantitySpinner.getValueFactory().setValue(product.getQuantity());
                        setGraphic(vbox);
                    }
                }
            }
        });
 
        productimagecol.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getImageUrl()));
        productimagecol.setCellFactory(new Callback<>() {
            @Override
            public TableCell<Product, String> call(TableColumn<Product, String> param) {
                return new TableCell<>() {
                    private final ImageView imageView = new ImageView();
 
                    @Override
                    protected void updateItem(String item, boolean empty) {
                        super.updateItem(item, empty);
                        if (empty || item == null) {
                            setGraphic(null);
                        } else {
                            imageView.setImage(new Image(item, 60, 60, true, true));
                            setGraphic(imageView);
                        }
                    }
                };
            }
        });
    }
 
    private void loadJacketProducts() {
        productList.clear();
        productList.addAll(DatabaseHandler.getInstance().getAllJacketProducts());
    }
 
    private void updateProductQuantityInDB(String productId, int quantity) {
        boolean success = DatabaseHandler.getInstance().updateJacketProductQuantity(productId, quantity);
        if (!success) {
            System.err.println("Failed to update quantity for productId: " + productId);
        }
    }
}