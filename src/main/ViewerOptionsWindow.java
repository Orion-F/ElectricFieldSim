package main;

import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.GridPane;
import javafx.stage.Stage;

public class ViewerOptionsWindow extends Stage {
    
    private GridPane gridPane;
    private Scene scene;
    private Label label0, label1, label2, label3, label4;
    private TextField field0, field1, field2, field3, field4;
    private CheckBox checkBox;
    private Button refreshButton, clearAllButton;
    
    public ViewerOptionsWindow() {
        this.setTitle("Viewer Options");
        this.setResizable(false);
        this.setWidth(300);
        this.setHeight(270);
        
        label0 = new Label("Placed Charge:");
        label0.setTooltip(new Tooltip("Left-click for positive, right-click for negative"));
        
        field0 = new TextField(String.valueOf(ElectricFieldViewer.placedCharge));
        
        label1 = new Label("Draw Limit:");
        label1.setTooltip(new Tooltip("The higher the number, the longer the lines go"));
        
        field1 = new TextField(String.valueOf(ElectricFieldViewer.drawLimit));
        
        label2 = new Label("Step:");
        label2.setTooltip(new Tooltip("The lower the number, the smoother the lines"));
        
        field2 = new TextField(String.valueOf(ElectricFieldViewer.step));
        
        label3 = new Label("Scale:");
        label3.setTooltip(new Tooltip("Lines that come out of + charge, ignoring magnitude"));
        
        field3 = new TextField(String.valueOf(ElectricFieldViewer.scale));
        
        label4 = new Label("Dynamic Scale:");
        label0.setTooltip(new Tooltip("Lines that come out of + charge per unit charge"));
        
        field4 = new TextField(String.valueOf(ElectricFieldViewer.dynamicScale));
        
        checkBox = new CheckBox("Dynamic Scale");
        checkBox.setSelected(ElectricFieldViewer.useDynamicScale);
        checkBox.setTooltip(new Tooltip("Change number of lines based on charge"));
        
        refreshButton = new Button("Refresh");
        refreshButton.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent arg0) {
                setValues();
                ElectricFieldSimStart.viewer.refresh();
            }
        });
        clearAllButton = new Button("Clear All");
        clearAllButton.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent arg0) {
                ElectricFieldSimStart.viewer.sourceCharges.clear();
                ElectricFieldSimStart.viewer.refresh();
            }
        });
        
        gridPane = new GridPane();
        gridPane.setHgap(10);
        gridPane.setVgap(10);
        gridPane.setPadding(new Insets(0, 10, 0, 10));
        
        gridPane.add(label0, 0, 0);
        gridPane.add(field0, 1, 0);
        gridPane.add(label1, 0, 1);
        gridPane.add(field1, 1, 1);
        gridPane.add(label2, 0, 2);
        gridPane.add(field2, 1, 2);
        gridPane.add(label3, 0, 3);
        gridPane.add(field3, 1, 3);
        gridPane.add(label4, 0, 4);
        gridPane.add(field4, 1, 4);
        gridPane.add(checkBox, 0, 5);
        gridPane.add(refreshButton, 1, 5);
        gridPane.add(clearAllButton, 0, 6);
        
        scene = new Scene(gridPane);
        this.setScene(scene);
        
        this.show();
    }
    
    public void setValues() {
        ElectricFieldViewer.placedCharge = Double.valueOf(field0.getText());
        ElectricFieldViewer.drawLimit = Integer.valueOf(field1.getText());
        ElectricFieldViewer.step = Double.valueOf(field2.getText());
        ElectricFieldViewer.scale = Integer.valueOf(field3.getText());
        ElectricFieldViewer.dynamicScale = Integer.valueOf(field4.getText());
        ElectricFieldViewer.useDynamicScale = checkBox.isSelected();
    }
}
