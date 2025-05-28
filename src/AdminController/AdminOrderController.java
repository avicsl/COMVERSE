package AdminController;


import Class.Order;
import Main.DatabaseHandler;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;


import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class AdminOrderController {

    @FXML
    private TableView<Order> mytable;


    @FXML
    private TableColumn<Order, String> studnumcol; // Order ID
    @FXML
    private TableColumn<Order, String> fnamecol; // Product ID
    @FXML
    private TableColumn<Order, String> lnamecol; // Cart Order ID
    @FXML
    private TableColumn<Order, String> emailcol; // Student Number
    @FXML
    private TableColumn<Order, String> passwordcol; // Order Date
    @FXML
    private TableColumn<Order, Integer> coursecol; // Quantity
    @FXML
    private TableColumn<Order, Double> departmentcol; // Total Amount
    @FXML
    private TableColumn<Order, String> imagecol; // Product Image URL


    private ObservableList<Order> orderList = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        studnumcol.setCellValueFactory(new PropertyValueFactory<>("orderId"));
        fnamecol.setCellValueFactory(new PropertyValueFactory<>("productId"));
        lnamecol.setCellValueFactory(new PropertyValueFactory<>("cartId"));
        emailcol.setCellValueFactory(new PropertyValueFactory<>("studentNumber"));
        passwordcol.setCellValueFactory(new PropertyValueFactory<>("orderDate"));
        coursecol.setCellValueFactory(new PropertyValueFactory<>("quantity"));
        departmentcol.setCellValueFactory(new PropertyValueFactory<>("totalAmount"));


        // Setup image column to show image in ImageView
        imagecol.setCellValueFactory(new PropertyValueFactory<>("imageUrl"));
        imagecol.setCellFactory(col -> new TableCell<>() {
            private final ImageView imageView = new ImageView();


            {
                imageView.setFitWidth(100);
                imageView.setFitHeight(100);
                imageView.setPreserveRatio(true);
            }


            @Override
            protected void updateItem(String imageUrl, boolean empty) {
                super.updateItem(imageUrl, empty);
                if (empty || imageUrl == null || imageUrl.isEmpty()) {
                    setGraphic(null);
                } else {
                    try {
                        Image image = new Image(imageUrl, 100, 100, true, true, true);
                        imageView.setImage(image);
                        setGraphic(imageView);
                    } catch (Exception e) {
                        setGraphic(null);
                    }
                }
            }
        });


        loadOrdersFromDatabase();
    }


    private void loadOrdersFromDatabase() {
        String query = "SELECT order_id, product_id, cart_id, student_number, order_date, quantity, total_amount, image_url FROM orders";

        try (Connection conn = DatabaseHandler.getDBConnection();
             PreparedStatement pstmt = conn.prepareStatement(query);
             ResultSet rs = pstmt.executeQuery()) {


            orderList.clear();

            while (rs.next()) {
                String orderId = rs.getString("order_id");
                String productId = rs.getString("product_id");
                String cartId = rs.getString("cart_id");
                String studentNumber = rs.getString("student_number");
                String orderDate = rs.getString("order_date");
                int quantity = rs.getInt("quantity");
                double totalAmount = rs.getDouble("total_amount");
                String imageUrl = rs.getString("image_url");

                Order order = new Order(orderId, productId, cartId, studentNumber, orderDate, quantity, totalAmount, imageUrl);
                orderList.add(order);
            }

            mytable.setItems(orderList);


        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}