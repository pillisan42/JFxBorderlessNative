package io.github.pillisan42;

import javafx.application.Application;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

public class Sample extends Application {

    public static final String DEFAULT_HEADER_TITLE_STYLE = "-fx-text-fill: white; -fx-background-color: #001144; -fx-min-height: 25px; -fx-pref-height: 25px; -fx-max-height: 25px; -fx-border-radius: 25; -fx-background-radius: 25;";

    public static final String DEFAULT_HEADER_BUTTON_STYLE = "-fx-text-fill: white; -fx-background-color: #001144; -fx-min-height: 25px; -fx-pref-height: 25px; -fx-max-height: 25px; -fx-border-color: grey ; -fx-border-radius: 25; -fx-background-radius: 25;";
    public static final String DEFAULT_HEADER_STYLE = "-fx-background-color: #001144; -fx-min-height: 40px; -fx-pref-height: 40px; -fx-max-height: 40px; -fx-border-color: #00000000 #00000000 grey #00000000; -fx-border-radius: 10 10 0 0; -fx-background-radius: 10 10 0 0;";

    static   //static initializer code
    {
        BorderlessNative.loadJarDll("deploy");
    }




    public static void main(String[] args) {
        launch(args);
    }
    
    @Override
    public void start(final Stage primaryStage) {
        buildAndShowStage(primaryStage, "Stage1");
        Stage secondaryStage=new Stage();
        buildAndShowStage(secondaryStage, "Stage2");
        secondaryStage.setX(0D);
        secondaryStage.setY(0D);
    }

    private void buildAndShowStage(Stage stage, String windowTitle) {
        stage.setTitle(windowTitle);
        Button closeButton = new Button();
        closeButton.setText("X");
        closeButton.setId("close");
        Button maximizeButton = new Button();
        maximizeButton.setId("maximize");
        maximizeButton.setText("O");
        Button minimizeButton = new Button();
        minimizeButton.setId("minimize");
        minimizeButton.setText("_");
        minimizeButton.setOnAction(event -> stage.setIconified(true));
        Button captionButton = new Button();
        captionButton.setText("Caption");
        closeButton.setOnAction(event -> stage.close());

        StackPane root = new StackPane();

        root.setStyle("-fx-background-color: #000044; -fx-border-color : black; -fx-border-radius: 10 10 10 10; -fx-background-radius: 10 10 10 10;");
        VBox vBox=new VBox();
        root.getChildren().add(vBox);
        HBox headerHBox=new HBox();
        StackPane movePane=new StackPane();
        HBox.setHgrow(movePane,Priority.ALWAYS);
        headerHBox.setStyle(DEFAULT_HEADER_STYLE);
        movePane.setStyle(DEFAULT_HEADER_STYLE);
        minimizeButton.setStyle(DEFAULT_HEADER_BUTTON_STYLE);
        maximizeButton.setStyle(DEFAULT_HEADER_BUTTON_STYLE);
        closeButton.setStyle(DEFAULT_HEADER_BUTTON_STYLE);
        headerHBox.setAlignment(Pos.CENTER_RIGHT);
        headerHBox.setSpacing(10);
        Label title = new Label(stage.getTitle());
        title.setStyle(DEFAULT_HEADER_TITLE_STYLE);
        headerHBox.getChildren().add(title);
        headerHBox.getChildren().add(movePane);
        headerHBox.getChildren().add(minimizeButton);
        headerHBox.getChildren().add(maximizeButton);
        headerHBox.getChildren().add(closeButton);
        vBox.getChildren().add(headerHBox);
        StackPane stackPane=new StackPane();
        stackPane.getChildren().add(captionButton);
        VBox.setVgrow(stackPane, Priority.ALWAYS);
        vBox.getChildren().add(stackPane);
        Scene scene=new Scene(root, 300, 250, Color.TRANSPARENT);
        stage.setScene(scene);
        BorderlessNative borderlessNative=showBorderlessAeroSnap(stage,maximizeButton,headerHBox,movePane);
        maximizeButton.setOnAction(event -> borderlessNative.maximizeOrRestore());
    }

    public BorderlessNative showBorderlessAeroSnap(Stage primaryStage,Node maximizeNode,Node... moveNode) {
        primaryStage.initStyle(StageStyle.TRANSPARENT);
        primaryStage.setOpacity(0.8);
        primaryStage.show();
        BorderlessNative borderlessNative= new BorderlessNative(primaryStage);
        borderlessNative.setCaptionNode(moveNode);
        //borderlessNative.setMaximizeNode(maximizeNode);
        borderlessNative.makeWindowsBorderless(primaryStage.getTitle());
        return borderlessNative;
    }

}