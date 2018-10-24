package controller;

import Internet.InternetConnected;
import GoogleAPI.Audio;
import com.jfoenix.controls.JFXListView;
import data.DatabaseConnection;
import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.util.Duration;
import javazoom.jl.decoder.JavaLayerException;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.Calendar;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Controller implements Initializable {

    public boolean checkTypeDictionary = true; // từ điển anh- việt là true

    private static String typeOfDictionary = "av";

    public static void setTypeOfDictionary(String typeOfDictionary) {
        Controller.typeOfDictionary = typeOfDictionary;
    }

    private Connection connection = DatabaseConnection.getConnection();
    private PreparedStatement preparedStatement = null;
    private ResultSet rs = null;
    private ObservableList<String> listWord = FXCollections.observableArrayList();
    private FilteredList<String> filteredData = new FilteredList<>(listWord, e -> true);


    static int index = 0;//biến lưu vị trí listview

    private int hour;
    private int minute;
    private int second;
    private int year;
    private int month;
    private int day;
    @FXML
    private Label ClockDisplay;
    @FXML
    private Label CalendarDisplay;
    @FXML
    private JFXListView listView;
    @FXML
    private WebView webView;
    @FXML
    private TextField search;

    /**
     * bắt sự kiện cho textfield Search bar
     */
    public void SearchTextFieldEvent() {
        search.setOnKeyPressed(new EventHandler<KeyEvent>() {
            @Override
            public void handle(KeyEvent e) {

                if (e.getCode() == KeyCode.ENTER) { //nhấn enter để tìm kiếm
                    if (index == 0) {
                        String text = search.getText();
                        if ("".equals(text)) {
                            alertNotEntered();
                        } else {
                            try {
                                String query = "Select * from " + typeOfDictionary + " WHERE word=?";
                                preparedStatement = connection.prepareStatement(query);
                                preparedStatement.setString(1, search.getText());
                                rs = preparedStatement.executeQuery();
                                while (rs.next()) {
                                    webView.getEngine().loadContent(rs.getString("html"));
                                }
                                preparedStatement.close();
                                rs.close();
                            } catch (SQLException ee) {
                                System.out.println(ee.getMessage());
                            }
                        }
                    } else {//update từ lên search bar
                        if (listView.getSelectionModel().getSelectedItem() != null) {
                            search.setText((String) listView.getSelectionModel().getSelectedItem());
                        }
                    }
                } else if (e.getCode() == KeyCode.DOWN) {
                    listView.getSelectionModel().select(index); //chọn item vị trí trong listview
                    listView.getFocusModel().focus(index);
                    listView.scrollTo(index); //cuộn list theo vị trí
                    index++;
                } else if (e.getCode() == KeyCode.UP) {
                    index--;
                    listView.getSelectionModel().select(index); //chọn item vị trí trong listview
                    listView.getFocusModel().focus(index);
                    listView.scrollTo(index); //cuộn list theo vị trí
                    if (index < 0) index = 0;
                } else index = 0;

            }
        });
    }

    /**
     * gợi ý từ tìm kiếm
     */
    public void searchWord() {
        listView.setItems(filteredData);
        search.textProperty().addListener((observable, oldValue, newValue) -> {
            filteredData.setPredicate(s -> {
                if (newValue == null || newValue.isEmpty()) {
                    return true;
                }
                String tolower = newValue.toLowerCase();
                if (s.toLowerCase().startsWith(tolower)) {
                    return true;
                }
                return false;
            });
            listView.setItems(filteredData);
        });
    }

    /**
     * hàm khởi tạo từ listview
     */
    public void showlistview() {
        try {
            String query = "select word from " + typeOfDictionary;
            preparedStatement = connection.prepareStatement(query);
            rs = preparedStatement.executeQuery();

            while (rs.next()) {

                listWord.add(rs.getString(1));
            }
            preparedStatement.close();
            rs.close();
        } catch (Exception e) {
            System.err.println(e);
        }
    }

    /**
     * bắt sự kiện cho listview khi item đc chọn
     */
    public void chooseitem() {
        listView.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<String>() {

            public void changed(
                    ObservableValue<? extends String> observable,
                    String oldValue, String newValue) {
                try {
                    String query = "Select * from " + typeOfDictionary + " WHERE word=?";
                    preparedStatement = connection.prepareStatement(query);
                    preparedStatement.setString(1, newValue);
                    rs = preparedStatement.executeQuery();
                    while (rs.next()) {
                        webView.getEngine().loadContent(rs.getString("html"));
                    }
                    preparedStatement.close();
                    rs.close();
                } catch (SQLException ee) {
                    System.out.println(ee.getMessage());

                }
            }
        });
    }

    /**
     * hàm sử lý Button Audio bằng Google API
     *
     * @throws IOException
     * @throws InterruptedException
     */
    public void speak() throws IOException, InterruptedException {
        String str = search.getText();
        if ("".equals(str)) str = (String) listView.getSelectionModel().getSelectedItem();

        //kiểm tra xem đã chọn từ nào hay chưa
        if (listView.getSelectionModel().getSelectedItem() == null & search.getText().equals("")) {
            alertNotSelected();
        } else if (InternetConnected.IsConnecting()) {
            if (!"".equals(str)) {
                InputStream sound;
                Audio audio;
                if (this.checkTypeDictionary) {
                    try {
                        audio = Audio.getInstance();
                        sound = audio.getAudio(str, "en");
                        audio.play(sound);
                    } catch (IOException var8) {
                        Logger.getLogger(Controller.class.getName()).log(Level.SEVERE, (String) null, var8);
                    } catch (JavaLayerException var9) {
                        Logger.getLogger(Controller.class.getName()).log(Level.SEVERE, (String) null, var9);
                    }
                } else {
                    try {
                        audio = Audio.getInstance();
                        sound = audio.getAudio(str, "vi");
                        audio.play(sound);
                    } catch (IOException var6) {
                        Logger.getLogger(Controller.class.getName()).log(Level.SEVERE, (String) null, var6);
                    } catch (JavaLayerException var7) {
                        Logger.getLogger(Controller.class.getName()).log(Level.SEVERE, (String) null, var7);
                    }
                }
            }
        } else {
            alertNotInternet();
        }

    }

    /**
     * bắt sự kiện cho nút search
     *
     * @throws SQLException
     */
    public void Submit() throws SQLException {
        String text = search.getText();
        if ("".equals(text)) {
            alertNotEntered();
        } else {
            String query = "Select * from " + typeOfDictionary + " WHERE word=?";
            preparedStatement = connection.prepareStatement(query);
            preparedStatement.setString(1, search.getText());
            rs = preparedStatement.executeQuery();
            while (rs.next()) {
                webView.getEngine().loadContent(rs.getString("html"));
                System.out.println(rs.getString("html"));
            }
            preparedStatement.close();
            rs.close();
        }
    }

    /**
     * @param location
     * @param resources
     */
    @Override
    public void initialize(URL location, ResourceBundle resources) {

        //hàm hiện từ lên listview
        showlistview();

        //gợi ý từ tìm kiếm
        searchWord();

        //bắt sự kiện cho listview khi item đc chọn
        chooseitem();

        // bắt sự kiện cho textfield SEARCH
        SearchTextFieldEvent();

        setCalendarDisplay();
        setClockDisplay();
    }

    /**
     * gọi cửa sổ thêm từ
     *
     * @throws IOException
     */
    public void addwordscene() throws IOException {
        FXMLLoader loader = new FXMLLoader();
        loader.setLocation(getClass().getResource("/fxml/addword.fxml"));
        loader.load();

        Parent p = loader.getRoot();
        Scene scene = new Scene(p);

        Stage stage = new Stage();
        stage.setResizable(false);
        stage.initStyle(StageStyle.UNDECORATED);
        Stage primary = (Stage) search.getScene().getWindow();
        stage.initOwner(primary);
        stage.setTitle("Add Word");
        stage.setScene(scene);
        stage.show();
    }

    /**
     * thêm từ vào database
     *
     * @param word từ mới
     * @param html nghĩa mới
     * @throws SQLException
     */
    public void addword(String word, String html) throws SQLException {
        String query = "INSERT INTO " + typeOfDictionary + " (word, html) VALUES(?,?)";

        preparedStatement = connection.prepareStatement(query);
        preparedStatement.setString(1, word);
        preparedStatement.setString(2, html);
        preparedStatement.execute();
        preparedStatement.close();

        listWord.clear();
        showlistview();
    }

    /**
     * gọi cửa sổ thêm từ
     *
     * @throws IOException
     * @throws SQLException
     */
    public void editwordscene() throws IOException, SQLException {
        if (listView.getSelectionModel().getSelectedItem() != null) {
            FXMLLoader loader = new FXMLLoader();
            loader.setLocation(getClass().getResource("/fxml/editword.fxml"));

            loader.load();

            Parent p = loader.getRoot();
            Scene scene = new Scene(p);

            //truyền dữ liệu qua scene edit
            EditController editController = loader.getController();

            editController.word.setText((String) listView.getSelectionModel().getSelectedItem());//set text cho textfield word bên scene edit


            String query = "Select * from " + typeOfDictionary + " WHERE word=?";
            preparedStatement = connection.prepareStatement(query);
            preparedStatement.setString(1, (String) listView.getSelectionModel().getSelectedItem());
            rs = preparedStatement.executeQuery();
            while (rs.next()) {
                editController.def.setHtmlText(rs.getString("html"));//set text cho phần sửa từ
            }
            preparedStatement.close();
            rs.close();


            Stage stage = new Stage();
            stage.setResizable(false);
            stage.initStyle(StageStyle.UNDECORATED);
            Stage primary = (Stage) search.getScene().getWindow();
            stage.initOwner(primary);
            stage.setTitle("Edit Word");
            stage.setScene(scene);
            stage.show();
        } else {
            alertNotSelected();
        }

    }

    /**
     * sửa từ trong database
     *
     * @param word     từ cần sửa
     * @param editWord từ sau khi sửa
     * @param newDef   nghĩa sau khi sửa
     * @throws SQLException
     */
    public void editWord(String word, String editWord, String newDef) throws SQLException {
        //chỉ sửa nghĩa
        if (editWord.equals("")) {
            //xóa từ cũ
            String query = "delete from " + typeOfDictionary + "  where word=?";
            preparedStatement = connection.prepareStatement(query);
            preparedStatement.setString(1, word);
            preparedStatement.execute();
            preparedStatement.close();
            //thêm từ word với nghĩa mới
            addword(word, newDef);
        }
        // chỉ sửa nghĩa của từ
        else if (newDef.equals("<html dir=\"ltr\"><head></head><body contenteditable=\"true\"></body></html>")) {
            String def = new String();

            String query = "Select * from " + typeOfDictionary + " WHERE word=?";
            preparedStatement = connection.prepareStatement(query);
            preparedStatement.setString(1, word);
            rs = preparedStatement.executeQuery();
            while (rs.next()) {
                def = rs.getString("html");//lấy nghĩa cũ
            }
            preparedStatement.close();
            rs.close();


            //xóa từ cũ
            query = "delete from " + typeOfDictionary + "  where word=?";

            preparedStatement = connection.prepareStatement(query);
            preparedStatement.setString(1, word);
            preparedStatement.execute();
            preparedStatement.close();


            //thêm từ mới với nghĩa cũ
            addword(editWord, def);
        }
        //sửa cả nghĩa và từ
        else {
            //xóa từ cũ
            String query = "delete from " + typeOfDictionary + "  where word=?";
            try {
                preparedStatement = connection.prepareStatement(query);
                preparedStatement.setString(1, word);
                preparedStatement.execute();
                preparedStatement.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }

            //thêm từ mới với nghĩa mới
            addword(editWord, newDef);
        }

        listWord.clear();
        showlistview();
        webView.getEngine().loadContent("");
    }

    /**
     * xóa từ trong database
     *
     * @throws SQLException
     */
    public void removeWord() throws SQLException {
        if (listView.getSelectionModel().getSelectedItem() != null) {
            Alert alert = new Alert(Alert.AlertType.NONE, "Bạn có chắc là xóa từ này?", ButtonType.YES, ButtonType.NO);
            if (alert.showAndWait().orElse(ButtonType.NO) == ButtonType.YES) {
                String word = (String) listView.getSelectionModel().getSelectedItem();
                String query = "delete from " + typeOfDictionary + "  where word=?";
                preparedStatement = connection.prepareStatement(query);
                preparedStatement.setString(1, word);
                preparedStatement.execute();
                preparedStatement.close();
                listWord.clear();
                showlistview();
                webView.getEngine().loadContent("");
            }
        } else {
            alertNotSelected();
        }
    }

    /**
     * phương thhuwcs xử lý cho Button WIkisearch*
     *
     * @param event
     * @throws InterruptedException
     * @throws IOException
     */
    public void setWikiButton(ActionEvent event) throws InterruptedException, IOException {
        String str = search.getText();
        if ("".equals(str)) str = (String) listView.getSelectionModel().getSelectedItem();

        //kiểm tra xem đã chọn từ nào hay chưa
        if (listView.getSelectionModel().getSelectedItem() == null & search.getText().equals("")) {
            alertNotSelected();
        } else if (!InternetConnected.IsConnecting()) {
            alertNotInternet();
        } else {
            WebEngine engine = webView.getEngine();
            if (checkTypeDictionary) {
                engine.load("https://en.wiktionary.org/wiki/" + str);
            } else {
                engine.load("https://vi.wiktionary.org/wiki/" + str);
            }
        }
    }


    /**
     * @param event
     * @throws InterruptedException
     * @throws IOException
     */
    public void setLabanButton(ActionEvent event) throws InterruptedException, IOException {
        String str = search.getText();
        if ("".equals(str)) str = (String) listView.getSelectionModel().getSelectedItem();

        //kiểm tra xem đã chọn từ nào hay chưa
        if (listView.getSelectionModel().getSelectedItem() == null & search.getText().equals("")) {
            alertNotSelected();
        } else if (!InternetConnected.IsConnecting()) {
            alertNotInternet();
        } else {
            WebEngine engine = webView.getEngine();
            if (checkTypeDictionary) {
                engine.load("https://dict.laban.vn/find?type=1&query=" + str);
            } else {
                engine.load("https://dict.laban.vn/find?type=2&query=" + str);
            }
        }
    }

    /**
     * hàm gọi cửa sổ Google Translate
     *
     * @throws IOException
     * @throws InterruptedException
     */
    public void loadGoogle() throws IOException, InterruptedException {
        if (InternetConnected.IsConnecting()) {
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/fxml/GoogleLoader.fxml"));
            Parent root1 = fxmlLoader.load();
            Stage stage = new Stage();
            Stage primary = (Stage) search.getScene().getWindow();
            Scene scene = new Scene(root1);
            stage.setScene(scene);
            stage.initOwner(primary);
            stage.initStyle(StageStyle.UNDECORATED);
            Platform.setImplicitExit(false);
            stage.show();
        } else {
            alertNotInternet();
        }
    }

    public void loadVerbs() throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/fxml/verbsLoader.fxml"));
        Parent root1 = fxmlLoader.load();
        Stage stage = new Stage();
        Stage primary = (Stage) search.getScene().getWindow();
        Scene scene = new Scene(root1);
        stage.setScene(scene);
        stage.initOwner(primary);
        stage.initStyle(StageStyle.UNDECORATED);
        Platform.setImplicitExit(false);
        stage.show();
    }

    /**
     * chuyển từ điển anh việt
     */
    public void changeAv() {
        setTypeOfDictionary("av");
        listWord.clear();
        showlistview();
        listView.scrollTo(0);
        webView.getEngine().loadContent("");
        search.setText("");
        checkTypeDictionary = true;
    }

    /**
     * chuyển từ điển việt anh
     */
    public void changeVa() {
        setTypeOfDictionary("va");
        listWord.clear();
        showlistview();
        listView.scrollTo(0);
        webView.getEngine().loadContent("");
        search.setText("");
        checkTypeDictionary = false;
    }

    /**
     * tắt chuong trình từ điển
     */
    public void closeWindows() {
        Stage stage = (Stage) search.getScene().getWindow();
        Alert alert = new Alert(Alert.AlertType.NONE, "Bạn có chắc là muốn thoát chương trình?", ButtonType.YES, ButtonType.NO);
        if (alert.showAndWait().orElse(ButtonType.NO) == ButtonType.YES) {
            stage.close();
            System.exit(0);
        }
    }

    /**
     * thông báo từ chưa nhập
     */
    public void alertNotEntered() {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle("THÔNG BÁO:");
        alert.setHeaderText("TỪ CHƯA ĐƯỢC NHẬP!");
        alert.setContentText("*WARNING: FBI");
        alert.show();
    }

    /**
     * thông báo không có internet
     */
    public void alertNotInternet() {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle("THÔNG BÁO:");
        alert.setHeaderText("KHÔNG CÓ KẾT NỐI INTERNET!");
        alert.setContentText("*WARNING: FBI");
        alert.showAndWait();
    }

    /**
     * thông báo từ chưa được chọn
     */
    public void alertNotSelected() {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("THÔNG BÁO:");
        alert.setHeaderText("TỪ CHƯA ĐƯỢC CHỌN!");
        alert.setContentText("*ERROR : 404");
        alert.show();
    }

    /**
     * hàm set ngày
     */
    private void setCalendarDisplay() {
        //TODO : Hàm thực hiện sự kiên cho Label CalenDarDisplay
        Calendar calendar = Calendar.getInstance();
        year = calendar.get(Calendar.YEAR);
        month = calendar.get(Calendar.MONTH);
        day = calendar.get(Calendar.DATE);
        CalendarDisplay.setText(String.valueOf(day) + "/" + String.valueOf(month + 1) + "/" + String.valueOf(year) + " ICT");
    }

    /**
     * hàm set giờ
     */
    private void setClockDisplay() {
        //TODO : Hàm thực hiện animation cho Label ClockDisplay
        Timeline clock;
        clock = new Timeline(new KeyFrame(Duration.ZERO, (ActionEvent e) -> {
            second = LocalDateTime.now().getSecond();
            minute = LocalDateTime.now().getMinute();
            hour = LocalDateTime.now().getHour();
            if (hour >= 0 && hour <= 12) {
                ClockDisplay.setText(String.valueOf(hour) + ":" + String.valueOf(minute) + ":" + String.valueOf(second) + " AM");
            } else {
                ClockDisplay.setText(String.valueOf(hour) + ":" + String.valueOf(minute) + ":" + String.valueOf(second) + " PM");
            }

        }),
                new KeyFrame(Duration.seconds(1))
        );
        clock.setCycleCount(Animation.INDEFINITE);
        clock.play();
    }
}
