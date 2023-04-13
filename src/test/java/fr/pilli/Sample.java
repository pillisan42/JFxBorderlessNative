package fr.pilli;

import javafx.application.Application;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

public class Sample extends Application {

    static   //static initializer code
    {
        System.loadLibrary("JFxBorderlessNative");
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
        Button maximizeButton = new Button();
        maximizeButton.setText("O");
        Button minimizeButton = new Button();
        minimizeButton.setText("_");
        minimizeButton.setOnAction(event -> primaryStage.setIconified(true));
        Button captionButton = new Button();
        captionButton.setText("Caption");
        closeButton.setOnAction(event -> primaryStage.close());

        StackPane root = new StackPane();

        root.setStyle("-fx-background-color: blue;");
        VBox vBox=new VBox();
        root.getChildren().add(vBox);
        HBox headerHBox=new HBox();
        headerHBox.setAlignment(Pos.CENTER_RIGHT);
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
        primaryStage.initStyle(StageStyle.UNDECORATED);
        primaryStage.show();
        BorderlessNative borderlessNative=activateSnap(primaryStage,headerHBox,maximizeButton);
        headerHBox.setOnMouseClicked(mouseEvent -> borderlessNative.maximizeOrRestore());
        maximizeButton.setOnAction(event -> borderlessNative.maximizeOrRestore());
    }

    public BorderlessNative activateSnap(Stage primaryStage,Node moveNode,Node maximizeNode) {
        BorderlessNative borderlessNative= new BorderlessNative(primaryStage);
        borderlessNative.setCaptionNode(moveNode);
        borderlessNative.setMaximizeNode(maximizeNode);
        borderlessNative.makeWindowsBorderless("Sample");
        return borderlessNative;
    }

}