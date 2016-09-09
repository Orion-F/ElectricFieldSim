package main;

import javafx.application.Application;
import javafx.stage.Stage;
import run.ProgramInfo;
import run.VersionReader;

public class ElectricFieldSimStart extends Application{
    
    public static ElectricFieldViewer viewer;
    public static ViewerOptionsWindow optionsWindow;

    public static void main(String[] args) {
        ProgramInfo.setName("Electric Field Sim");
        ProgramInfo.setAuthor("Orion Forowycz");
        VersionReader.useDefault(ElectricFieldSimStart.class);
        launch(args);
    }

    @Override
    public void start(Stage arg0) throws Exception {
        viewer = new ElectricFieldViewer();
        optionsWindow = new ViewerOptionsWindow();
    }

}
