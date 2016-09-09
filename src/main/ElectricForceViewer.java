package main;

import java.awt.Point;
import java.util.ArrayList;

import gui.CustomStage;
import javafx.event.EventHandler;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.input.MouseEvent;

@Deprecated
public class ElectricForceViewer extends CustomStage {
    
    private static int WIDTH = 700;
    private static int HEIGHT = 450;

    private Canvas canvas;
    private GraphicsContext gc;
    
    private ArrayList<SourceCharge> sourceCharges;
    
    public ElectricForceViewer() {
        this.setCustomTitle("Electric Force Viewer");
        this.setResizable(false);
        this.setWidth(WIDTH);
        this.setHeight(HEIGHT);
        this.setTitleBarColor("#999999"); //Grey
        this.setTitleTextColor("#EEEEEE"); //Very light Grey
        
        canvas = new Canvas(WIDTH, HEIGHT - 50);
        gc = canvas.getGraphicsContext2D();
        
        sourceCharges = new ArrayList<SourceCharge>();
        sourceCharges.add(new SourceCharge(-1, 500, 150, gc));
        sourceCharges.add(new SourceCharge(1, 150, 150, gc));
        
        drawSourceCharges();
        
        canvas.addEventFilter(MouseEvent.MOUSE_PRESSED, new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent mouseEvent) {
                drawElectricForce(1, mouseEvent.getX(), mouseEvent.getY());
            }
        });
        
        this.setMajor(canvas);
        this.show();
    }
    
    public void drawSourceCharges() {
        for(SourceCharge c : sourceCharges) {
            c.draw();
        }
    }
    
    public void drawElectricForce(double charge, double x, double y) {
        
        double drawFactor = 100000;
        
        double netX = 0;
        double netY = 0;
        //double netAngle = 0;
        
        double dis, mag, angle;
        
        for (SourceCharge sourceCharge : sourceCharges) {
            dis = Point.distance(x, y, sourceCharge.getX(), sourceCharge.getY());
            mag = drawFactor * charge * sourceCharge.getCharge() / dis / dis;
            angle = Math.atan2(y - sourceCharge.getY(), x - sourceCharge.getX());
            netX += mag * Math.cos(angle);
            netY += mag * Math.sin(angle);
        }
        
        //netAngle = Math.atan2(y - netY, x - netX);
        
        gc.strokeLine(x, y, x + netX, y + netY);
    }
    
    public void clear() {
        gc.clearRect(0, 0, WIDTH, HEIGHT - 50);
    }
    
    public void refresh() {
        clear();
        drawSourceCharges();
    }
    
    
}
