package com.yourname.mmoitemseditor;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;

import java.io.IOException;
import java.net.URL;
import java.util.Locale;
import java.util.ResourceBundle;

public class Main extends Application {

    private static Stage primaryStage;

    @Override
    public void start(Stage stage) throws IOException {
        primaryStage = stage;
        // Load the FXML file with the default locale's resource bundle.
        loadScene(Locale.getDefault());
    }

    public static void loadScene(Locale locale) throws IOException {
        URL fxmlLocation = Main.class.getResource("Editor.fxml");
        if (fxmlLocation == null) {
            System.err.println("Error: FXML file 'Editor.fxml' not found.");
            return;
        }

        ResourceBundle bundle = ResourceBundle.getBundle("com.yourname.mmoitemseditor.messages", locale);
        FXMLLoader loader = new FXMLLoader(fxmlLocation, bundle);
        Parent root = loader.load();
        EditorController controller = loader.getController();

        // Pass the Stage instance to the controller
        controller.setStage(primaryStage);
        controller.setMainApp(Main.class);

        primaryStage.setTitle(bundle.getString("app.title"));
        primaryStage.setScene(new Scene(root, 1200, 750));

        // Set the close request handler
        primaryStage.setOnCloseRequest((WindowEvent event) -> {
            if (!controller.canClose()) {
                event.consume();
            }
        });

        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
