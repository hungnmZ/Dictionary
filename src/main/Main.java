package main;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.stage.StageStyle;


public class Main extends Application {
    private static FXMLLoader mLoader;

    public static FXMLLoader getLoader() {
        return mLoader;
    }

    public static void setLoader(FXMLLoader tempLoader) {
        mLoader = tempLoader;
    }

    @Override
    public void start(Stage primaryStage) throws Exception {
        FXMLLoader fxmlLoader = new FXMLLoader();
        fxmlLoader.setLocation(getClass().getResource("/fxml/MainSceneBuilder.fxml"));
        fxmlLoader.load();
        Parent root = fxmlLoader.getRoot();

        setLoader(fxmlLoader);

        primaryStage.setTitle("Dictionary Demo");
        Scene scene = new Scene(root);
        scene.getStylesheets().add(getClass().getResource("/style/StyleBuilder.css").toExternalForm());
        primaryStage.setScene(scene);
        primaryStage.setResizable(false);
        primaryStage.initStyle(StageStyle.UNDECORATED);
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}