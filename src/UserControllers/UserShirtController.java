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

import java.sql.*;
import java.util.List;

public class UserShirtController {

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

    private static UserShirtController instance;

    private final ObservableList<Product> productList = FXCollections.observableArrayList();

    public UserShirtController() {
        instance = this;
    }

    public static void refreshTableWithSpinner() {
        if (instance != null) {
            instance.loadShirtProducts();
            instance.mytable.refresh();
        }
    }

    @FXML
    public void initialize() {
        setupColumns();
        loadShirtProducts();
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

    private void loadShirtProducts() {
        productList.clear();
        String query = "SELECT * FROM product WHERE category_id = 'C001 - SHIRT'";
        try (Connection conn = DatabaseHandler.getDBConnection();
             PreparedStatement stmt = conn.prepareStatement(query);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                Product product = new Product();
                product.setProductId(rs.getString("product_id"));
                product.setProductName(rs.getString("product_name"));
                product.setImageUrl(rs.getString("image_url"));
                product.setAmount(rs.getDouble("amount"));
                product.setQuantity(rs.getInt("quantity"));
                productList.add(product);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void updateProductQuantityInDB(String productId, int quantity) {
        String query = "UPDATE product SET quantity = ? WHERE product_id = ?";
        try (Connection conn = DatabaseHandler.getDBConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setInt(1, quantity);
            stmt.setString(2, productId);
            stmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
