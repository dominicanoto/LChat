package com.messenger;

import com.messenger.database.Database;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class Main extends Application {
    @Override
    public void start(Stage stage) throws Exception {

        Database.initialize();

        FXMLLoader loader = new FXMLLoader(
                getClass().getResource("/fxml/login.fxml")
        );

        Scene scene = new Scene(loader.load());

        stage.setTitle("LChat");
        stage.setScene(scene);
        stage.show();
    }
}
