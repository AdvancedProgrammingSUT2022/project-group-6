package clientapp.views;

import java.util.Arrays;
import java.util.List;

import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ScrollPane;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.Pane;
import clientapp.controllers.GameController;
import clientapp.models.Player;
import clientapp.models.maprelated.Hex;
import clientapp.models.units.Unit;

public class UnitsPanel {
    
    @FXML
    private AnchorPane anchorPane;
    public void initialize() {
        extractUnits();

    }
    private void extractUnits()
    {
        ScrollPane scrollPane=new ScrollPane();
        Pane demoPane=new Pane();
        
        int startSize=50;
        int height=50;
        int buttonHeight=30;
        int screenWidth=600;
        int screenHeight=400;

     
        List<String> Units=Arrays.asList(GameController.unitsPanel().split("\n"));
        for(String temp:Units)
        {
            Button newUnit=new Button();
            newUnit.setStyle("-fx-text-fill: goldenrod; -fx-background-color:#2f2f2f");
            newUnit.setText(temp);
            newUnit.setPrefWidth(screenWidth);
            newUnit.setPrefHeight(buttonHeight);
            newUnit.setLayoutY(startSize);

            newUnit.setOnMouseEntered(new EventHandler<MouseEvent>() {

                @Override
                public void handle(MouseEvent event) {
                    newUnit.setStyle("-fx-text-fill: goldenrod; -fx-background-color:#000000");
                    
                }
                
            });
            newUnit.setOnMouseExited(new EventHandler<MouseEvent>() {

                @Override
                public void handle(MouseEvent event) {
                    newUnit.setStyle("-fx-text-fill: goldenrod; -fx-background-color:#2f2f2f");
                    
                }
                
            });
            

            demoPane.getChildren().add(newUnit);
            demoPane.setStyle("-fx-background-color: #1a1a1a" );
            startSize+=height;
        }

        
        scrollPane.setMaxHeight(screenHeight);
        scrollPane.setPrefWidth(screenWidth);
        scrollPane.setLayoutY(27);
        scrollPane.setContent(demoPane);
        scrollPane.setPannable(true);

        anchorPane.getChildren().add(scrollPane);

    }

}
