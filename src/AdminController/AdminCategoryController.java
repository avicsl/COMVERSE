package AdminController;
 
import Class.Category;
import Main.DatabaseHandler;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
 
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
 
public class AdminCategoryController {
 
    @FXML
    private TableView<Category> mytable;
 
    @FXML
    private TableColumn<Category, String> categoryidcol;
 
    @FXML
    private TableColumn<Category, String> categorynamecol;
 
    private ObservableList<Category> categoryList = FXCollections.observableArrayList();
 
    @FXML
    public void initialize() {
        loadCategoryData();
    }
 
    private void loadCategoryData() {
    Connection conn = DatabaseHandler.getDBConnection();
    if (conn == null) return;
 
    try {
        // FIXED: Added ORDER BY clause to sort category_id in ascending order
        String query = "SELECT * FROM category ORDER BY category_id ASC";
        Statement stmt = conn.createStatement();
        ResultSet rs = stmt.executeQuery(query);
 
        categoryList.clear(); // Optional but recommended to avoid duplicates on refresh
 
        while (rs.next()) {
            String id = rs.getString("category_id");
            String name = rs.getString("category_name");
            categoryList.add(new Category(id, name));
        }
 
        categoryidcol.setCellValueFactory(new PropertyValueFactory<>("categoryId"));
        categorynamecol.setCellValueFactory(new PropertyValueFactory<>("categoryName"));
        mytable.setItems(categoryList);
 
    } catch (Exception e) {
        e.printStackTrace();
    }
}
}
 