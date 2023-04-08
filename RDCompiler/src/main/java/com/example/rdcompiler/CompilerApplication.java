package com.example.rdcompiler;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

public class CompilerApplication extends Application {

    @Override
    public void start(Stage stage) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(CompilerApplication.class.getResource("main-scene.fxml"));
        //Scene scene = new Scene(fxmlLoader.load(), 800, 600); // width, height
        //scene.getStylesheets().add("../../resorces/css/edit.css");
        Parent root = fxmlLoader.load();
        stage.setScene(new Scene(root));
        stage.setTitle("RDCompiler");
        stage.show();

        //stage.setScene(scene);
        //stage.show();
    }

    public static void main(String[] args) {
        launch();
    }
}
