package Class;
 
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
 
public class ProductTransfer {
    private static final ObservableList<Product> shirtProductList = FXCollections.observableArrayList();
    private static final ObservableList<Product> capProductList = FXCollections.observableArrayList();
    private static final ObservableList<Product> bagProductList = FXCollections.observableArrayList();
    private static final ObservableList<Product> jacketProductList = FXCollections.observableArrayList();
    private static final ObservableList<Product> laceProductList = FXCollections.observableArrayList();
 
    public static ObservableList<Product> getShirtProductList() {
        return shirtProductList;
    }
 
    public static ObservableList<Product> getCapProductList() {
        return capProductList;
    }
 
    public static ObservableList<Product> getBagProductList() {
        return bagProductList;
    }
 
    public static ObservableList<Product> getJacketProductList() {
        return jacketProductList;
    }
 
    public static ObservableList<Product> getLaceProductList() {
        return laceProductList;
    }
   
}
 