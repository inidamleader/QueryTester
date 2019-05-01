package com.inidamleader.querytester;

import com.inidamleader.querytester.model.ConnectionManager;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class QueryTesterApplication extends Application {

    public static final String CONNECTION_CONFIG_FILE_NAME = "connection.config";
    private static Stage sPrimaryStage;

    public static void main(String[] args) {
        launch(args);
    }

    public static Stage getPrimaryStage() {
        return sPrimaryStage;
    }

    @Override
    public void start(Stage primaryStage) throws Exception {
        sPrimaryStage = primaryStage;
        Parent root = FXMLLoader.load(getClass().getResource("view/QueryTesterView.fxml"));
        primaryStage.setTitle("Query Tester");
        primaryStage.setScene(new Scene(root));
        primaryStage.show();
        ConnectionManager.setConnectionConfigFileName(CONNECTION_CONFIG_FILE_NAME);
        ConnectionManager.loadParameters();
    }
}