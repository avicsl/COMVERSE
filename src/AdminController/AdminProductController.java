package AdminController;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.control.cell.PropertyValueFactory;
import Class.Product;
import Class.ProductTransfer;
import Main.DatabaseHandler;

import java.io.File;

public class AdminProductController {

    @FXML
    private ComboBox<String> categorycombobox;

    @FXML
    private TextField pURLTextFIeld;

    @FXML
    private TextField productNameTextField;

    @FXML
    private TextField amountTextFIeld;

    @FXML
    private TableView<Product> mytable;

    @FXML
    private TableColumn<Product, Integer> productcol;

    @FXML
    private TableColumn<Product, String> categorycol;

    @FXML
    private TableColumn<Product, String> pURLcol;

    @FXML
    private TableColumn<Product, String> productnamecol;

    @FXML
    private TableColumn<Product, Double> amountcol;

    @FXML
    private TableColumn<Product, Integer> quantitycol;

    @FXML
    private Spinner<Integer> quantityspinner;

    @FXML
    private Button addproductbtn;

    @FXML
    private Button updateProductBtn;

    @FXML
    private Button deleteProductBtn;

    @FXML
    private TextField searchField;

    private final ObservableList<Product> productList = FXCollections.observableArrayList();

    private final FilteredList<Product> filteredProducts = new FilteredList<>(productList, p -> true);

    @FXML
    public void initialize() {
        categorycombobox.setItems(FXCollections.observableArrayList(
                "C001 - SHIRT", "C002 - CAP", "C003 - BAG", "C004 - JACKET", "C005 - LACE"));

        quantityspinner.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(1, 100, 1));

        productcol.setCellValueFactory(new PropertyValueFactory<>("productId"));
        categorycol.setCellValueFactory(new PropertyValueFactory<>("category"));
        productnamecol.setCellValueFactory(new PropertyValueFactory<>("productName"));
        amountcol.setCellValueFactory(new PropertyValueFactory<>("amount"));
        quantitycol.setCellValueFactory(new PropertyValueFactory<>("quantity"));
        pURLcol.setCellValueFactory(new PropertyValueFactory<>("imageUrl"));

        pURLcol.setCellFactory(col -> new TableCell<>() {
            private final ImageView imageView = new ImageView();
            @Override
            protected void updateItem(String imageUrl, boolean empty) {
                super.updateItem(imageUrl, empty);
                if (empty || imageUrl == null || imageUrl.isEmpty()) {
                    setGraphic(null);
                } else {
                    try {
                        Image img = new Image(imageUrl, 80, 80, true, true);
                        imageView.setImage(img);
                        setGraphic(imageView);
                    } catch (Exception e) {
                        setGraphic(null);
                    }
                }
            }
        });

        mytable.setItems(filteredProducts);
        refreshTable();

        addproductbtn.setOnAction(e -> addProduct());
        updateProductBtn.setOnAction(e -> updateProduct());
        deleteProductBtn.setOnAction(e -> deleteProduct());

        mytable.setOnMouseClicked(e -> {
            Product selected = mytable.getSelectionModel().getSelectedItem();
            if (selected != null) {
                categorycombobox.setValue(selected.getCategory());
                productNameTextField.setText(selected.getProductName());
                pURLTextFIeld.setText(selected.getImageUrl());
                amountTextFIeld.setText(String.valueOf(selected.getAmount()));
                quantityspinner.getValueFactory().setValue(selected.getQuantity());
            }
        });

        // 🔍 Search filter setup
        searchField.textProperty().addListener((obs, oldValue, newValue) -> {
            filteredProducts.setPredicate(product -> {
                if (newValue == null || newValue.isBlank()) return true;

                String lowerCaseFilter = newValue.toLowerCase();
                return product.getProductName().toLowerCase().contains(lowerCaseFilter) ||
                       product.getCategory().toLowerCase().contains(lowerCaseFilter) ||
                       String.valueOf(product.getAmount()).contains(lowerCaseFilter);
            });
        });
    }

    private void addProduct() {
        String category = categorycombobox.getValue();
        String productName = productNameTextField.getText().trim();
        String imageUrl = pURLTextFIeld.getText().trim();
        String amountText = amountTextFIeld.getText().trim();
        Integer quantity = quantityspinner.getValue();

        if (category == null || category.isEmpty() ||
            productName.isEmpty() || imageUrl.isEmpty() || amountText.isEmpty() || quantity == null) {
            showAlert(Alert.AlertType.WARNING, "Input Error", "Please fill in all fields including quantity.");
            return;
        }

        double amount;
        try {
            amount = Double.parseDouble(amountText);
        } catch (NumberFormatException e) {
            showAlert(Alert.AlertType.ERROR, "Input Error", "Amount must be a valid number.");
            return;
        }

        if (!imageUrl.startsWith("http://") && !imageUrl.startsWith("https://")) {
            File file = new File(imageUrl);
            if (!file.exists()) {
                showAlert(Alert.AlertType.ERROR, "File Error", "Local image file does not exist.");
                return;
            }
            imageUrl = file.toURI().toString();
        }

        Product product = new Product();
        product.setCategory(category);
        product.setProductName(productName);
        product.setImageUrl(imageUrl);
        product.setAmount(amount);
        product.setQuantity(quantity);

        boolean success = DatabaseHandler.getInstance().addProduct(product);
        if (success) {
            showAlert(Alert.AlertType.INFORMATION, "Success", "Product added successfully.");
            clearFields();
            refreshTable();

            if (category.equals("C001 - SHIRT")) {
                ProductTransfer.getShirtProductList().add(product);
            } else if (category.equals("C002 - CAP")) {
                ProductTransfer.getCapProductList().add(product);
            } else if (category.equals("C003 - BAG")) {
                ProductTransfer.getBagProductList().add(product);
            } else if (category.equals("C004 - JACKET")) {
                ProductTransfer.getBagProductList().add(product);
            } else if (category.equals("C005 - LACE")) {
                ProductTransfer.getBagProductList().add(product);
            }

        } else {
            showAlert(Alert.AlertType.ERROR, "Database Error", "Failed to add product to the database.");
        }
    }

    private void updateProduct() {
        Product selected = mytable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert(Alert.AlertType.WARNING, "Selection Error", "Please select a product to update.");
            return;
        }

        String category = categorycombobox.getValue();
        String productName = productNameTextField.getText().trim();
        String imageUrl = pURLTextFIeld.getText().trim();
        String amountText = amountTextFIeld.getText().trim();
        Integer quantity = quantityspinner.getValue();

        if (category == null || category.isEmpty() ||
            productName.isEmpty() || imageUrl.isEmpty() || amountText.isEmpty() || quantity == null) {
            showAlert(Alert.AlertType.WARNING, "Input Error", "Please fill in all fields including quantity.");
            return;
        }

        double amount;
        try {
            amount = Double.parseDouble(amountText);
        } catch (NumberFormatException e) {
            showAlert(Alert.AlertType.ERROR, "Input Error", "Amount must be a valid number.");
            return;
        }

        if (!imageUrl.equals(selected.getImageUrl())) {
            if (!imageUrl.startsWith("http://") && !imageUrl.startsWith("https://")) {
                File file = new File(imageUrl);
                if (!file.exists()) {
                    showAlert(Alert.AlertType.ERROR, "File Error", "Local image file does not exist.");
                    return;
                }
                imageUrl = file.toURI().toString();
            }
        }

        boolean noChanges =
                selected.getCategory().equals(category) &&
                selected.getProductName().equals(productName) &&
                selected.getImageUrl().equals(imageUrl) &&
                selected.getAmount() == amount &&
                selected.getQuantity() == quantity;

        if (noChanges) {
            showAlert(Alert.AlertType.INFORMATION, "No Changes Detected", "No changes were made to update.");
            return;
        }

        selected.setCategory(category);
        selected.setProductName(productName);
        selected.setImageUrl(imageUrl);
        selected.setAmount(amount);
        selected.setQuantity(quantity);

        boolean success = DatabaseHandler.getInstance().updateProduct(selected);
        if (success) {
            showAlert(Alert.AlertType.INFORMATION, "Success", "Product updated successfully.");
            refreshTable();
        } else {
            showAlert(Alert.AlertType.ERROR, "Update Failed", "Failed to update product.");
        }
    }

    private void deleteProduct() {
        Product selected = mytable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert(Alert.AlertType.WARNING, "Selection Error", "Please select a product to delete.");
            return;
        }

        boolean confirmed = confirmDialog("Delete Product", "Are you sure you want to delete this product?");
        if (!confirmed) return;

        boolean success = DatabaseHandler.getInstance().deleteProduct(selected.getProductId());
        if (success) {
            showAlert(Alert.AlertType.INFORMATION, "Deleted", "Product deleted successfully.");
            refreshTable();
        } else {
            showAlert(Alert.AlertType.ERROR, "Delete Failed", "Failed to delete product.");
        }
    }

    private void clearFields() {
        categorycombobox.setValue(null);
        productNameTextField.clear();
        pURLTextFIeld.clear();
        amountTextFIeld.clear();
        quantityspinner.getValueFactory().setValue(1);
    }

    private void refreshTable() {
        productList.setAll(DatabaseHandler.getInstance().getAllProducts());
    }

    private void showAlert(Alert.AlertType alertType, String title, String message) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private boolean confirmDialog(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);

        ButtonType yes = new ButtonType("Yes", ButtonBar.ButtonData.YES);
        ButtonType no = new ButtonType("No", ButtonBar.ButtonData.NO);
        alert.getButtonTypes().setAll(yes, no);

        return alert.showAndWait().orElse(no) == yes;
    }
}