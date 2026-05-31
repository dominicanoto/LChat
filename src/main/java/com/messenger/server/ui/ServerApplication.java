package com.messenger.server.ui;

import com.messenger.server.Server;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class ServerApplication extends Application {

    public static void main(String[] args) {

        launch(args);
    }

    @Override
    public void start(Stage stage) throws Exception {

        FXMLLoader loader =
                new FXMLLoader(
                        getClass().getResource(
                                "/fxml/server.fxml"
                        )
                );

        Scene scene =
                new Scene(
                        loader.load()
                );

        stage.setTitle(
                "LChat Server"
        );

        stage.setOnCloseRequest(
                event -> Server.stop()
        );

        stage.setScene(scene);
        stage.show();
    }
}
