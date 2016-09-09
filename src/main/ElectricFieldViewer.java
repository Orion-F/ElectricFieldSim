package main;

import java.awt.Point;
import java.util.ArrayList;

import gui.CustomStage;
import javafx.event.EventHandler;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import run.ProgramInfo;

public class ElectricFieldViewer extends CustomStage {
    
    public static double placedCharge = 1;
    
    public static int drawLimit = 500;
    public static double step = 10;
    
    public static int scale = 50;
    public static int dynamicScale = 4;
    
    public static boolean useDynamicScale = false;
    
//    private static final double DRAW_FACTOR = Math.pow(10, 3);
    private static final double FORCE_FACTOR = Math.pow(10, 3);
    private static final double TEST_CHARGE = 1;
    
    private static int WIDTH = 1000;
    private static int HEIGHT = 700;
    
    private Canvas canvas;
    private GraphicsContext gc;
    
    public ArrayList<SourceCharge> sourceCharges;
    
    public ElectricFieldViewer() {
        this.setCustomTitle(ProgramInfo.getDefaultTitle() + " by " + ProgramInfo.getAuthor());
        this.setResizable(false);
        this.setWidth(WIDTH);
        this.setHeight(HEIGHT);
        this.setTitleBarColor("#999999"); //Grey
        this.setTitleTextColor("#EEEEEE"); //Very light Grey
        
        canvas = new Canvas(WIDTH, HEIGHT - 50);
        gc = canvas.getGraphicsContext2D();
        
        sourceCharges = new ArrayList<SourceCharge>();
        
//        sourceCharges.add(new SourceCharge(-1, canvasCenterX, canvasCenterY + 100, gc));
//        sourceCharges.add(new SourceCharge(1, canvasCenterX, canvasCenterY - 100, gc));
//        sourceCharges.add(new SourceCharge(1, canvasCenterX + 100, canvasCenterY, gc));
        
        refresh();
        
        canvas.addEventFilter(MouseEvent.MOUSE_PRESSED, new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent mouseEvent) {
                if (mouseEvent.getButton() == MouseButton.PRIMARY) {
                    ElectricFieldSimStart.optionsWindow.setValues();
                    sourceCharges.add(new SourceCharge(placedCharge, mouseEvent.getX(), mouseEvent.getY(), gc));
                    refresh();
                } else if (mouseEvent.getButton() == MouseButton.SECONDARY) {
                    ElectricFieldSimStart.optionsWindow.setValues();
                    sourceCharges.add(new SourceCharge(-placedCharge, mouseEvent.getX(), mouseEvent.getY(), gc));
                    refresh();
                }
            }
        });
        
//        canvas.setOnKeyTyped(new EventHandler<KeyEvent>() {
//            @Override
//            public void handle(KeyEvent event) {
//                if (event.getCode() == KeyCode.UP) {
//                    for (SourceCharge c : sourceCharges) {
//                        c.setY(c.getY() + 100);
//                    }
//                    refresh();
//                } if (event.getCode() == KeyCode.DOWN) {
//                    for (SourceCharge c : sourceCharges) {
//                        c.setY(c.getY() - 100);
//                    }
//                    refresh();
//                } if (event.getCode() == KeyCode.LEFT) {
//                    for (SourceCharge c : sourceCharges) {
//                        c.setX(c.getX() + 100);
//                    }
//                    refresh();
//                }if (event.getCode() == KeyCode.RIGHT) {
//                    for (SourceCharge c : sourceCharges) {
//                        c.setX(c.getX() - 100);
//                    }
//                    refresh();
//                }
//            }
//        });
        
        this.setMajor(canvas);
        this.show();
    }
    
    public void drawSourceCharges() {
        for(SourceCharge c : sourceCharges) {
            c.draw();
        }
    }
    
    public void drawField() {
        double startAngle, preX, preY, nextX, nextY, nextAngle;
        int scaleToUse = scale;
        for(SourceCharge c : sourceCharges) {
            if (c.getCharge() > 0) {
                if (useDynamicScale) {
                    scaleToUse = dynamicScale * new Double(c.getCharge()).intValue();
                }
                for (int i = 0; i < scaleToUse; i++) {
                    startAngle = 2 * Math.PI / scaleToUse * i;
                    preX = c.getX() + xComp(c.getDrawRadius(), startAngle);
                    preY = c.getY() + yComp(c.getDrawRadius(), startAngle);
                    for (int j = 0; j < drawLimit; j++) {
                        nextAngle = nextAngle(TEST_CHARGE, preX, preY);
                        
//                        System.out.println(preX + " " + preY + " " + nextAngle);
                        
                        nextX = preX + xComp(step, nextAngle);
                        nextY = preY + yComp(step, nextAngle);
                        
                        gc.strokeLine(preX, preY, nextX, nextY);
                        
                        preX = nextX;
                        preY = nextY;
                    }
                }
            }
        }
    }
    
    public double nextAngle(double charge, double x, double y) {
        double netX = 0;
        double netY = 0;
        
        double dis, mag, angle;
        
        for (SourceCharge sourceCharge : sourceCharges) {
            dis = Point.distance(x, y, sourceCharge.getX(), sourceCharge.getY());
            mag = FORCE_FACTOR * charge * sourceCharge.getCharge() / dis / dis;
            angle = Math.atan2(y - sourceCharge.getY(), x - sourceCharge.getX());
            netX += xComp(mag, angle);
            netY += yComp(mag, angle);
        }
        
        return Math.atan2(netY, netX);
    }
    
    public void clear() {
        gc.clearRect(0, 0, WIDTH, HEIGHT - 50);
    }
    
    public void refresh() {
        clear();
        drawField();
        drawSourceCharges();
    }
    
    public double xComp(double mag, double angle) {
        return mag * Math.cos(angle);
    }
    
    public double yComp(double mag, double angle) {
        return mag * Math.sin(angle);
    }
}
