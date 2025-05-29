package UserControllers;

import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextField;
import javafx.scene.control.Alert.AlertType;

public class UserCheckoutController {

    @FXML
    private TextField shippingaddress;

    @FXML
    private TextField smethodfield;

    @FXML
    private TextField samountfield;

    @FXML
    private TextField totalamountfield; 

    @FXML
    private ComboBox<String> paymentmethod;

    @FXML
    private Button addtocartbtn;

    @FXML
    public void initialize() {
        // Set non-editable fields
        smethodfield.setText("Flash Express");
        smethodfield.setEditable(false);

        samountfield.setText("40.00");
        samountfield.setEditable(false);

        // Set payment method options
        paymentmethod.getItems().addAll(
            "Cash on Delivery",
            "American Express",
            "GCash",
            "Maya",
            "PayPal",
            "Visa",
            "JCB"
        );

       

        // Handle button click
        addtocartbtn.setOnAction(e -> handlePlaceOrder());
    }

    private void handlePlaceOrder() {
        String address = shippingaddress.getText().trim();
        String payment = paymentmethod.getValue();

        if (address.isEmpty()) {
            showAlert("Invalid Address", "Please enter a valid shipping address.");
            return;
        }

        if (payment == null || payment.isEmpty()) {
            showAlert("Payment Method Required", "Please select a payment method.");
            return;
        }

        // Proceed with order processing...
        showAlert("Order Placed", "Your order has been successfully placed!");
        // Implement order saving/logic here
    }

    private void showAlert(String title, String content) {
        Alert alert = new Alert(AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}
