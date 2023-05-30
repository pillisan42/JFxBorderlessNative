package io.github.pillisan42;

import javafx.application.Application;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

public class Sample extends Application {



    static   //static initializer code
    {
        BorderlessNative.loadJarDll("deploy");
    }




    public static void main(String[] args) {
        launch(args);
    }
    
    @Override
    public void start(final Stage primaryStage) {
        //https://stackoverflow.com/questions/39731497/create-window-without-titlebar-with-resizable-border-and-without-bogus-6px-whit
        primaryStage.setTitle("Sample");
        Button closeButton = new Button();
        closeButton.setText("X");
        closeButton.setId("close");
        Button maximizeButton = new Button();
        maximizeButton.setId("maximize");
        maximizeButton.setText("O");
        Button minimizeButton = new Button();
        minimizeButton.setId("minimize");
        minimizeButton.setText("_");
        minimizeButton.setOnAction(event -> primaryStage.setIconified(true));
        Button captionButton = new Button();
        captionButton.setText("Caption");
        closeButton.setOnAction(event -> primaryStage.close());

        StackPane root = new StackPane();

        root.setStyle("-fx-background-color: #000044; -fx-border-color : black");
        VBox vBox=new VBox();
        root.getChildren().add(vBox);
        HBox headerHBox=new HBox();
        StackPane movePane=new StackPane();
        HBox.setHgrow(movePane,Priority.ALWAYS);
        headerHBox.setStyle("-fx-background-color: #001144; -fx-min-height: 40px; -fx-pref-height: 40px; -fx-max-height: 40px; -fx-border-color: #00000000 #00000000 grey #00000000;");
        movePane.setStyle("-fx-background-color: #001144; -fx-min-height: 40px; -fx-pref-height: 40px; -fx-max-height: 40px; -fx-border-color: #00000000 #00000000 grey #00000000;");
        minimizeButton.setStyle("-fx-text-fill: white; -fx-background-color: #001144; -fx-min-height: 40px; -fx-pref-height: 40px; -fx-max-height: 40px; -fx-border-color: #00000000 #00000000 grey #00000000;");
        maximizeButton.setStyle("-fx-text-fill: white; -fx-background-color: #001144; -fx-min-height: 40px; -fx-pref-height: 40px; -fx-max-height: 40px;  -fx-border-color: #00000000 #00000000 grey #00000000;");
        closeButton.setStyle("-fx-text-fill: white; -fx-background-color: #001144; -fx-min-height: 40px; -fx-pref-height: 40px; -fx-max-height: 40px; -fx-border-color: #00000000 #00000000 grey #00000000;");
        headerHBox.setAlignment(Pos.CENTER_RIGHT);
        headerHBox.setSpacing(10);
        headerHBox.getChildren().add(movePane);
        headerHBox.getChildren().add(minimizeButton);
        headerHBox.getChildren().add(maximizeButton);
        headerHBox.getChildren().add(closeButton);
        vBox.getChildren().add(headerHBox);
        StackPane stackPane=new StackPane();
        stackPane.getChildren().add(captionButton);
        VBox.setVgrow(stackPane, Priority.ALWAYS);
        vBox.getChildren().add(stackPane);
        Scene scene=new Scene(root, 300, 250, Color.RED);
        primaryStage.setScene(scene);
        BorderlessNative borderlessNative=showBorderlessAeroSnap(primaryStage,headerHBox,movePane);
        maximizeButton.setOnAction(event -> borderlessNative.maximizeOrRestore());
    }

    public BorderlessNative showBorderlessAeroSnap(Stage primaryStage,Node... moveNode) {
        primaryStage.initStyle(StageStyle.UNDECORATED);
        primaryStage.show();
        BorderlessNative borderlessNative= new BorderlessNative(primaryStage);
        borderlessNative.setCaptionNode(moveNode);
        borderlessNative.makeWindowsBorderless("Sample");
        return borderlessNative;
    }

}