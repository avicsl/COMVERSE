package AdminController;

import Class.Category;
import Main.DatabaseHandler;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
import javafx.fxml.FXML;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
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

    @FXML
    private TextField searchField;

    private ObservableList<Category> categoryList = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        loadCategoryData();
        setupSearchFilter(); // ✅ Setup filtering after loading data
    }

    private void loadCategoryData() {
        Connection conn = DatabaseHandler.getDBConnection();
        if (conn == null) return;

        try {
            String query = "SELECT * FROM category ORDER BY category_id ASC";
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(query);

            categoryList.clear();

            while (rs.next()) {
                String id = rs.getString("category_id");
                String name = rs.getString("category_name");
                categoryList.add(new Category(id, name));
            }

            categoryidcol.setCellValueFactory(new PropertyValueFactory<>("categoryId"));
            categorynamecol.setCellValueFactory(new PropertyValueFactory<>("categoryName"));

            mytable.setItems(categoryList); // Set raw list first, filtered later

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void setupSearchFilter() {
        // 🔍 Add filter logic
        FilteredList<Category> filteredData = new FilteredList<>(categoryList, p -> true);

        searchField.textProperty().addListener((observable, oldValue, newValue) -> {
            filteredData.setPredicate(category -> {
                if (newValue == null || newValue.isEmpty()) {
                    return true;
                }

                String lowerCaseFilter = newValue.toLowerCase();

                return category.getCategoryId().toLowerCase().contains(lowerCaseFilter) ||
                       category.getCategoryName().toLowerCase().contains(lowerCaseFilter);
            });
        });

        SortedList<Category> sortedData = new SortedList<>(filteredData);
        sortedData.comparatorProperty().bind(mytable.comparatorProperty());

        mytable.setItems(sortedData); // ✅ Now using filtered data
    }
}