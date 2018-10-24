package controller;

import GoogleAPI.Audio;
import GoogleAPI.GoogleTranslate;
import com.jfoenix.controls.JFXButton;
import com.jfoenix.controls.JFXComboBox;
import com.jfoenix.controls.JFXTextArea;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.stage.Stage;
import javazoom.jl.decoder.JavaLayerException;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.ResourceBundle;

public class GoogleController implements Initializable {
    private Map<String, String> map = new HashMap<String, String>();
    @FXML
    private JFXButton Home;
    @FXML
    private JFXButton GooglespeakButton;
    @FXML
    private JFXButton GooglespeakButton2;
    @FXML
    private JFXButton GooglesearchButton;
    @FXML
    private JFXTextArea GoogletextArea;
    @FXML
    private JFXTextArea GoogleTranslateTextArea;
    @FXML
    private JFXComboBox<String> Box1;
    @FXML
    private JFXComboBox<String> Box2;

    public void ReturnHome(ActionEvent event) {
        Stage stage = (Stage) Home.getScene().getWindow();
        stage.close();
    }

    public void setENTERKeyPressed() {
        GoogletextArea.setOnKeyPressed(new EventHandler<KeyEvent>() {
            @Override
            public void handle(KeyEvent event) {
                if (event.getCode() == KeyCode.ENTER) {
                    String text = GoogletextArea.getText();
                    if ("".equals(text)) {
                        alertNotEntered();
                    } else {
                        try {
                            String rescb1 = map.get(Box1.getValue());
                            String rescb2 = map.get(Box2.getValue());
                            String showtext = GoogleTranslate.translate(rescb1, rescb2, text);
                            GoogleTranslateTextArea.setText(showtext);
                        } catch (IOException e) {
                            System.out.println("Lỗi");
                        }
                    }
                }
            }
        });
    }

    public void setGooglesearchButton(ActionEvent event) throws IOException {
        if (event.getSource() == GooglesearchButton) {
            String text = GoogletextArea.getText();
            if ("".equals(text)) {
                alertNotEntered();
            } else {
                String rescb1 = map.get(Box1.getValue());
                String rescb2 = map.get(Box2.getValue());
                String showtext = GoogleTranslate.translate(rescb1, rescb2, text);
                GoogleTranslateTextArea.setText(showtext);
            }
        }
    }

    public void setGooglespeakButton(ActionEvent event) throws IOException, JavaLayerException {
        if (event.getSource() == GooglespeakButton) {
            String rescb1 = map.get(Box1.getValue());
            String text = GoogletextArea.getText();
            if ("".equals(text)) {
                alertNotEntered();
            } else {
                Audio audio = Audio.getInstance();
                InputStream sound = audio.getAudio(text.trim(), rescb1);
                audio.play(sound);
            }
        }
        if (event.getSource() == GooglespeakButton2) {
            String rescb2 = map.get(Box2.getValue());
            String text1 = GoogleTranslateTextArea.getText();

            if ("".equals(text1)) {
                alertNotEntered();
            } else {
                Audio audio1 = Audio.getInstance();
                InputStream sound1 = audio1.getAudio(text1, rescb2);
                audio1.play(sound1);
            }
        }
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        setENTERKeyPressed();
        map.put("Vietnamese", "vi");
        map.put("English", "en");
        map.put("Korean", "ko");
        map.put("Japanese", "ja");
        map.put("Chinese", "zh");
        map.put("Thailand", "th");
        map.put("French", "fr");
        map.put("German", "de");
        map.put("Spanish", "es");
        map.put("Russian", "ru");
        ObservableList<String> languages = FXCollections.observableArrayList("Russian", "Spanish", "German", "French", "Vietnamese", "English", "Korean", "Chinese", "Thailand", "Japanese");
        Box1.setItems(languages);
        Box2.setItems(languages);
        Box2.setValue("Vietnamese");
        Box1.setValue("English");
    }

    /**
     * thông báo văn bản chưa nhập
     */
    public void alertNotEntered() {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle("THÔNG BÁO:");
        alert.setHeaderText("VĂN BẢN CHƯA ĐƯỢC NHẬP!");
        alert.setContentText("*WARNING: FBI");
        alert.show();
    }
}
