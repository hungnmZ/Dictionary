package controller;

import com.jfoenix.controls.JFXTextField;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.web.HTMLEditor;
import javafx.stage.Stage;
import main.Main;

import java.net.URL;
import java.sql.SQLException;
import java.util.ResourceBundle;

public class EditController implements Initializable {

    @FXML
    JFXTextField word;

    @FXML
    JFXTextField editWord;

    @FXML
    HTMLEditor def;


    @FXML
    public void editWord(ActionEvent event) throws SQLException {

        String wordd = word.getText();
        String editWordd = editWord.getText();
        String deff = def.getHtmlText();

        Controller c = Main.getLoader().getController();
        if (wordd.equals("")) {
            c.alertNotEntered();
        } else {

            c.editWord(wordd, editWordd ,deff);
        }

        word.setText("");
        def.setHtmlText("");
        editWord.setText("");
    }

    public void home(ActionEvent event) {
        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        stage.close();
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {

    }
}
