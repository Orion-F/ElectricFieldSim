package main;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import javafx.application.Application;
import javafx.stage.Stage;

public class ElectricFieldSimStart extends Application {
    
    private static String version;
    
    public static void main(String[] args) {
        initVersion();
        launch(args);
    }
    
    @Override
    public void start(Stage arg0) throws Exception {
        new ElectricFieldViewer();
    }
    
    private static void initVersion() {
        InputStream versionStream = ElectricFieldSimStart.class
                .getResourceAsStream("/main/VERSION.txt");
        if (versionStream != null) {
            BufferedReader versionReader = new BufferedReader(new InputStreamReader(versionStream));
            try {
                version = versionReader.readLine();
            } catch (IOException e) {
                // Do nothing
            }
        }
    }
    
    public static String getVersion() {
        return version;
    }
}
