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
import java.util.HashMap;
import java.util.Map;

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
    private final Map<String, Spinner<Integer>> spinnerMap = new HashMap<>();

    private static UserJacketController instance;

    public static Student loggedInUser;

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

    @FXML
    private void handleAddToCart() {
        Product selectedProduct = mytable.getSelectionModel().getSelectedItem();

        if (selectedProduct != null) {
            int quantityToAdd = getQuantityFromTableSpinner(selectedProduct);
            int availableStock = getAvailableStockFromDB(selectedProduct.getProductId());

            if (quantityToAdd <= 0) {
                showAlert("Invalid Quantity", "Quantity must be greater than zero.");
                return;
            }

            if (availableStock == 0) {
                showAlert("Out of Stock", "This product is sold out.");
                return;
            }

            if (quantityToAdd > availableStock) {
                showAlert("Stock Limit", "Only " + availableStock + " item(s) available in stock.");
                return;
            }

            String productId = selectedProduct.getProductId();
            String studentNumber = UserLoginController.loggedInUser.getStudentNumber();
            double pricePerUnit = selectedProduct.getAmount();

            try (Connection conn = DatabaseHandler.getDBConnection()) {
                String selectSQL = "SELECT quantity FROM cart WHERE student_number = ? AND product_id = ?";
                PreparedStatement selectStmt = conn.prepareStatement(selectSQL);
                selectStmt.setString(1, studentNumber);
                selectStmt.setString(2, productId);
                ResultSet rs = selectStmt.executeQuery();

                if (rs.next()) {
                    int currentQuantity = rs.getInt("quantity");
                    int newQuantity = currentQuantity + quantityToAdd;

                    if (newQuantity > availableStock) {
                        int remainingCanAdd = availableStock - currentQuantity;
                        if (remainingCanAdd > 0) {
                            showAlert("Stock Limit", "You already have " + currentQuantity +
                                    " item(s) in cart. You can only add " + remainingCanAdd + " more.");
                        } else {
                            showAlert("Stock Limit", "You already have the maximum available quantity (" +
                                    currentQuantity + ") in your cart.");
                        }
                        return;
                    }

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
            private final Spinner<Integer> quantitySpinner = new Spinner<>();
            private final VBox vbox = new VBox(5);

            {
                quantitySpinner.setEditable(true);
                vbox.getChildren().addAll(priceLabel, quantitySpinner);

                quantitySpinner.valueProperty().addListener((obs, oldVal, newVal) -> {
                    Product product = getTableRow().getItem();
                    if (product != null && newVal != null) {
                        int availableStock = getAvailableStockFromDB(product.getProductId());
                        if (newVal > availableStock) {
                            quantitySpinner.getValueFactory().setValue(Math.min(oldVal, availableStock));
                            showAlert("Stock Limit", "Only " + availableStock + " item(s) available in stock.");
                        }
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
                        int availableStock = getAvailableStockFromDB(product.getProductId());

                        if (availableStock > 0) {
                            quantitySpinner.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(1, availableStock, 1));
                            quantitySpinner.setDisable(false);
                        } else {
                            quantitySpinner.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(0, 0, 0));
                            quantitySpinner.setDisable(true);
                        }

                        spinnerMap.put(product.getProductId(), quantitySpinner);
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
                            imageView.setImage(new Image(item, 120, 120, true, true));
                            setGraphic(imageView);
                        }
                    }
                };
            }
        });
    }

    private void loadJacketProducts() {
        productList.clear();
        String query = "SELECT * FROM product WHERE category_id = 'C004 - JACKET'";
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

    private int getAvailableStockFromDB(String productId) {
        String query = "SELECT quantity FROM product WHERE product_id = ?";
        try (Connection conn = DatabaseHandler.getDBConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setString(1, productId);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                return rs.getInt("quantity");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }

    private int getQuantityFromTableSpinner(Product product) {
        Spinner<Integer> spinner = spinnerMap.get(product.getProductId());
        return (spinner != null) ? spinner.getValue() : 1;
    }

    private void showAlert(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}
