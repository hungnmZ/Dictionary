package controller;

import com.jfoenix.controls.JFXTreeTableColumn;
import com.jfoenix.controls.JFXTreeTableView;
import com.jfoenix.controls.RecursiveTreeItem;
import com.jfoenix.controls.datamodels.treetable.RecursiveTreeObject;

import data.DatabaseConnection;
import data.VerbsDatabaseConnection;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeTableColumn;
import javafx.stage.Stage;
import javafx.util.Callback;

import java.net.URL;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ResourceBundle;

public class VerbsController implements Initializable {


    @FXML
    private JFXTreeTableView<Verb> treeTableView;

    private Connection connection = VerbsDatabaseConnection.getConnection();
    private Statement statement = null;
    private ResultSet rs = null;



    public void home(ActionEvent event) {
        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        stage.close();
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        JFXTreeTableColumn<Verb,String>  infinitive = new JFXTreeTableColumn<>("Nguyên Mẫu");
        infinitive.setPrefWidth(188);
        infinitive.setCellValueFactory(new Callback<TreeTableColumn.CellDataFeatures<Verb, String>, ObservableValue<String>>() {
            @Override
            public ObservableValue<String> call(TreeTableColumn.CellDataFeatures<Verb, String> param) {
                return param.getValue().getValue().infinitive;
            }
        });
        JFXTreeTableColumn<Verb,String>  simple = new JFXTreeTableColumn<>("QK Đơn");
        simple.setPrefWidth(188);
        simple.setCellValueFactory(new Callback<TreeTableColumn.CellDataFeatures<Verb, String>, ObservableValue<String>>() {
            @Override
            public ObservableValue<String> call(TreeTableColumn.CellDataFeatures<Verb, String> param) {
                return param.getValue().getValue().simple;
            }
        });
        JFXTreeTableColumn<Verb,String>  participle = new JFXTreeTableColumn<>("QK Phân Từ");
        participle.setPrefWidth(188);
        participle.setCellValueFactory(new Callback<TreeTableColumn.CellDataFeatures<Verb, String>, ObservableValue<String>>() {
            @Override
            public ObservableValue<String> call(TreeTableColumn.CellDataFeatures<Verb, String> param) {
                return param.getValue().getValue().participle;
            }
        });



        ObservableList<Verb> verbs = FXCollections.observableArrayList();
        String query ="SELECT infinitive, simple, participle FROM verbs";

        try{
            statement=connection.createStatement();
            rs = statement.executeQuery(query);
            while (rs.next()){
                verbs.add( new Verb(rs.getString("infinitive"), rs.getString("simple"), rs.getString("participle")));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }



        final TreeItem<Verb> root = new RecursiveTreeItem<Verb>(verbs, RecursiveTreeObject::getChildren);

        treeTableView.getColumns().setAll(infinitive,simple,participle);
        treeTableView.setRoot(root);
        treeTableView.setShowRoot(false);
    }

    public class Verb extends RecursiveTreeObject<Verb> {
        private StringProperty infinitive, simple, participle;


        public Verb(String infinitive, String simple, String participle) {
            this.infinitive = new SimpleStringProperty(infinitive);
            this.simple = new SimpleStringProperty(simple);
            this.participle = new SimpleStringProperty(participle);
        }
    }
}
