package UserControllers;

import Class.CartItem;
import Class.Product;
import Class.ProductTransfer;
import Class.Student;
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

public class UserShirtController {

    @FXML
    private Label shirtNameLabel;

    @FXML
    private Label shirtPriceLabel;

    @FXML
    private ImageView shirtImageView;

    @FXML
    private Spinner<Integer> quantitySpinner;

    @FXML
    private Button addToCartBtn;

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

    private Product selectedShirt;

    public static Student loggedInUser;

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
        // Load single shirt view (ImageView/Spinner)
        if (!ProductTransfer.getShirtProductList().isEmpty()) {
            selectedShirt = ProductTransfer.getShirtProductList().get(0); // or other logic
            loadProductDetails(selectedShirt);
        }

        // Load table-based product view
        setupColumns();
        loadShirtProducts();
        mytable.setItems(productList);
    }

    private void loadProductDetails(Product product) {
        shirtNameLabel.setText(product.getProductName());
        shirtPriceLabel.setText(String.format("₱%.2f", product.getAmount()));
        shirtImageView.setImage(new Image(product.getImageUrl()));

        int availableQuantity = product.getQuantity();

        if (availableQuantity > 0) {
            quantitySpinner.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(1, availableQuantity, 1));
            quantitySpinner.setDisable(false);
        } else {
            quantitySpinner.setDisable(true);
            showAlert("Sold Out", "This item is currently sold out.\nQuantity left: 0");
        }

        addToCartBtn.setOnAction(e -> handleAddToCartSingleView());
    }

    private void handleAddToCartSingleView() {
        if (selectedShirt == null) return;

        int quantityChosen = quantitySpinner.getValue();
        int availableQuantity = selectedShirt.getQuantity();

        if (availableQuantity == 0) {
            showAlert("Out of Stock", "This product is sold out.");
        } else if (quantityChosen > availableQuantity) {
            showAlert("Stock Limit", "Only " + availableQuantity + " item(s) available.");
        } else {
            showAlert("Added to Cart", "Added " + quantityChosen + " item(s) to cart.");
        }
    }

    @FXML
    private void handleAddToCart() {
        Product selectedProduct = mytable.getSelectionModel().getSelectedItem();

        if (selectedProduct != null) {
            int quantityToAdd = selectedProduct.getQuantity();
            if (quantityToAdd <= 0) {
                showAlert("Invalid Quantity", "Quantity must be greater than zero.");
                return;
            }

            String productId = selectedProduct.getProductId();
            String studentNumber = UserLoginController.loggedInUser.getStudentNumber();
            double pricePerUnit = selectedProduct.getAmount();

            try (Connection conn = DatabaseHandler.getDBConnection()) {
                // Check existing cart
                String selectSQL = "SELECT quantity FROM cart WHERE student_number = ? AND product_id = ?";
                PreparedStatement selectStmt = conn.prepareStatement(selectSQL);
                selectStmt.setString(1, studentNumber);
                selectStmt.setString(2, productId);

                ResultSet rs = selectStmt.executeQuery();

                if (rs.next()) {
                    int currentQuantity = rs.getInt("quantity");
                    int newQuantity = currentQuantity + quantityToAdd;

                    String updateSQL = "UPDATE cart SET quantity = ?, amount = ? WHERE student_number = ? AND product_id = ?";
                    PreparedStatement updateStmt = conn.prepareStatement(updateSQL);
                    updateStmt.setInt(1, newQuantity);
                    updateStmt.setDouble(2, pricePerUnit * newQuantity);
                    updateStmt.setString(3, studentNumber);
                    updateStmt.setString(4, productId);

                    updateStmt.executeUpdate();

                } else {
                    String insertSQL = "INSERT INTO cart (product_id, student_number, image_url, quantity, amount) VALUES (?, ?, ?, ?, ?)";
                    PreparedStatement insertStmt = conn.prepareStatement(insertSQL);
                    insertStmt.setString(1, productId);
                    insertStmt.setString(2, studentNumber);
                    insertStmt.setString(3, selectedProduct.getImageUrl());
                    insertStmt.setInt(4, quantityToAdd);
                    insertStmt.setDouble(5, pricePerUnit * quantityToAdd);

                    insertStmt.executeUpdate();
                }

                CartItem cartItem = new CartItem(productId, selectedProduct.getProductName(), selectedProduct.getImageUrl(), pricePerUnit);
                cartItem.setQuantity(quantityToAdd);
                UserCartController.addToCart(cartItem);

                showAlert("Success", "Product added to cart!");

            } catch (SQLException e) {
                e.printStackTrace();
                showAlert("Database Error", "Unable to add to cart.\n" + e.getMessage());
            }

        } else {
            showAlert("No Selection", "Please select a product to add to cart.");
        }
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

    private void showAlert(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}
