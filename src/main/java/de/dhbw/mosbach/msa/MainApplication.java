package de.dhbw.mosbach.msa;

import de.dhbw.mosbach.msa.database.HSQLDB;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.input.KeyCode;
import javafx.stage.Stage;

public class MainApplication extends Application  {

    private static final int MIN_WIDTH = 400;
    private static final int MIN_HEIGHT = 300;
    private static final String TITLE = "Security Agency";

    private FXMLController controller;
    private Parent root;

    @Override
    public void start(Stage primaryStage) throws Exception {
        FXMLLoader loader = new FXMLLoader(getClass().getClassLoader().getResource("main.fxml"));
        root = loader.load();
        Scene scene = new Scene(root);
        primaryStage.setScene(scene);

        // Apply application configurations
        primaryStage.setMinWidth(MIN_WIDTH);
        primaryStage.setMinHeight(MIN_HEIGHT);
        primaryStage.setTitle(TITLE);
        primaryStage.getIcons().add(new Image("icon.png"));

        controller = loader.getController();
        doKeybindings();

        primaryStage.show();
    }

    private void doKeybindings() {
        root.setOnKeyPressed((event) -> {
            if (event.getCode() == KeyCode.F5) {
                controller.getBtnExecute().fire();
            }

            if (event.getCode() == KeyCode.F3) {
                controller.getBtnDebug().fire();
            }

            if (event.getCode() == KeyCode.F8) {
                controller.getBtnLoadLog().fire();
            }

            if (event.getCode() == KeyCode.F4) {
                controller.getBtnClear().fire();
            }
        });
    }

    public static void main(String[] args) {
        HSQLDB.instance.setupDatabase();
        launch(args);
        HSQLDB.instance.shutdown();
    }
}
