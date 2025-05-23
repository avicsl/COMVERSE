package UserControllers;

import Class.CartItem;
import Class.CurrentUser;
import Main.DatabaseHandler;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

import java.util.List;

public class UserCartController {

    @FXML private TableView<CartItem> cartTable;
    @FXML private TableColumn<CartItem, String> productnamecol;
    @FXML private TableColumn<CartItem, String> productimagecol;
    @FXML private TableColumn<CartItem, Double> pricecol;
    @FXML private TableColumn<CartItem, Integer> quantitycol;
    @FXML private TableColumn<CartItem, CheckBox> selectcol;
    @FXML private TextField subtotalTextfield;

    private static UserCartController instance;

    private static final ObservableList<CartItem> cartItems = FXCollections.observableArrayList();

    public static UserCartController getInstance() {
        return instance;
    }

    @FXML
    public void initialize() {
        instance = this;

        productnamecol.setCellValueFactory(data -> data.getValue().productIdProperty());
        pricecol.setCellValueFactory(data -> data.getValue().priceProperty().asObject());
        quantitycol.setCellValueFactory(data -> data.getValue().quantityProperty().asObject());

        // Image column
        productimagecol.setCellValueFactory(data -> data.getValue().imageUrlProperty());
        productimagecol.setCellFactory(col -> new TableCell<>() {
            private final ImageView iv = new ImageView();
            @Override
            protected void updateItem(String url, boolean empty) {
                super.updateItem(url, empty);
                if (empty || url == null) {
                    setGraphic(null);
                } else {
                    iv.setImage(new Image(url, 60, 60, true, true));
                    setGraphic(iv);
                }
            }
        });

        selectcol.setCellValueFactory(data -> {
            CheckBox cb = data.getValue().getSelect();
            cb.selectedProperty().addListener((obs, oldVal, newVal) -> updateSubtotal());
            return new SimpleObjectProperty<>(cb);
        });

        selectcol.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(CheckBox cb, boolean empty) {
                super.updateItem(cb, empty);
                setGraphic(empty ? null : cb);
            }
        });

        loadCartFromDatabase();

        cartTable.setItems(cartItems);

        updateSubtotal();
    }

    private void loadCartFromDatabase() {
        cartItems.clear();
        if (CurrentUser.getInstance() != null && CurrentUser.getInstance().getStudent() != null) {
            String studentNumber = CurrentUser.getInstance().getStudent().getStudentNumber();
            List<CartItem> dbItems = DatabaseHandler.getInstance().getCartItemsByStudentNumber(studentNumber);
            if (dbItems != null) {
                cartItems.addAll(dbItems);
            }
        } else {
            System.err.println("CurrentUser or Student is not initialized. Cannot load cart.");
        }
    }

    private void updateSubtotal() {
        double total = 0;
        for (CartItem item : cartItems) {
            if (item.getSelect().isSelected()) {
                total += item.getPrice() * item.getQuantity();
            }
        }
        subtotalTextfield.setText(String.format("%.2f", total));
    }

    public static void addToCart(CartItem item) {
        if (instance == null) {
            System.err.println("UserCartController instance not initialized!");
            return;
        }
        boolean exists = cartItems.stream().anyMatch(i -> i.getProductId().equals(item.getProductId()));
        if (!exists) {
            cartItems.add(item);
        } else {
            cartItems.stream().filter(i -> i.getProductId().equals(item.getProductId()))
                    .findFirst().ifPresent(existing -> existing.setQuantity(existing.getQuantity() + item.getQuantity()));
        }
        instance.updateSubtotal();
    }
}
