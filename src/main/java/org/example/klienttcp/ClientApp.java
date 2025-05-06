package org.example.klienttcp;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

public class ClientApp extends Application {
    @Override
    public void start(Stage stage) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(ClientApp.class.getResource("ClientWindow.fxml"));
        Scene scene = new Scene(fxmlLoader.load(), 800, 700);
        stage.setTitle("Client TCP!");
        stage.setScene(scene);
        stage.show();
        ClientWindowController controller = fxmlLoader.getController();
        controller.initialize();
    }

    public static void main(String[] args) {
        launch();
    }
}