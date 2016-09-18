package main;

import java.awt.geom.Point2D;
import java.util.ArrayList;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.EventHandler;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.paint.Paint;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;

public class ElectricFieldViewer extends Stage {
    
    public static double placedCharge = 1;
    
    public static int drawLimit = 500;
    public static double step = 10;
    
    public static int scale = 50;
    public static int dynamicScaleFactor = 4;
    
    public static boolean useDynamicScale = false;
    
    private double zoomLevel = 1;
    private final double MIN_ZOOM = 0.25;
    private final double MAX_ZOOM = 4;
    private final double ZOOM_CHANGE_FACTOR = 2;
    
    private double xTrans = 0;
    private double yTrans = 0;
    
    private final double SOURCE_CHARGE_DRAW_RADIUS = 15;
    
    private final double TEST_CHARGE = 1;
    
    private final int INITIAL_WIDTH = 1000;
    private final int INITIAL_HEIGHT = 700;
    
    public ArrayList<SourceCharge> sourceCharges;
    
    private GraphicsContext gc;
    
    private Canvas canvas;
    private Scene baseScene;
    private BorderPane basePane;
    
    public ElectricFieldViewer() {
        this.setTitle(
                "ElectricFieldSim V" + ElectricFieldSimStart.getVersion() + " by Orion Forowycz");
        this.setWidth(INITIAL_WIDTH);
        this.setHeight(INITIAL_HEIGHT);
        
        this.setOnCloseRequest(new EventHandler<WindowEvent>() {
            @Override
            public void handle(WindowEvent we) {
                System.exit(0);
            }
        });
        
        basePane = new BorderPane();
        
        baseScene = new Scene(basePane);
        
        baseScene.widthProperty().addListener(new ChangeListener<Number>() {
            @Override
            public void changed(ObservableValue<? extends Number> observableValue,
                    Number oldSceneWidth, Number newSceneWidth) {
                canvas.setWidth(
                        canvas.getWidth() + newSceneWidth.intValue() - oldSceneWidth.intValue());
            }
        });
        baseScene.heightProperty().addListener(new ChangeListener<Number>() {
            @Override
            public void changed(ObservableValue<? extends Number> observableValue,
                    Number oldSceneHeight, Number newSceneHeight) {
                canvas.setHeight(
                        canvas.getHeight() + newSceneHeight.intValue() - oldSceneHeight.intValue());
            }
        });
        
        canvas = new Canvas(baseScene.getWidth(), baseScene.getHeight());
        basePane.setCenter(canvas);
        
        gc = canvas.getGraphicsContext2D();
        
        sourceCharges = new ArrayList<SourceCharge>();
        
        addMouseHandlers();
        addKeyHandlers();
        
        this.setScene(baseScene);
        this.show();
        
        xTrans = canvas.getWidth() / 2;
        yTrans = canvas.getHeight() / 2;
        
        refresh();
    }
    
    /**
     * Draws electric field lines from every positive source charge, until they
     * reach a negative source charge or the draw limit. The number of lines per
     * positive source charge is equal to the scale or a dynamic scale of (
     */
    private void drawField() {
        double startAngle, preX, preY, nextX, nextY, nextAngle;
        boolean notReachedNegative;
        
        // The scale determines the number of lines draw around each positive
        // source charge.
        // By default it uses the number provided in the scale variable
        int scaleToUse = scale;
        
        for (SourceCharge c : sourceCharges) {
            
            if (c.getCharge() > 0) {
                
                // If dynamic scale is on, instead make the scale equal to 4 *
                // the charge of the positive source charge, rounds up
                if (useDynamicScale) {
                    scaleToUse = (int) Math.ceil(dynamicScaleFactor * new Double(c.getCharge()));
                }
                
                // Cycle through until every equal-distance point on the edge of
                // the source charge, up to the scale number, has been drawn to
                for (int i = 0; i < scaleToUse; i++) {
                    
                    // Find angle around the source charge
                    startAngle = 2 * Math.PI / scaleToUse * i;
                    
                    // Find starting point on the edge of the circle drawn for
                    // the source charge using the angle
                    preX = c.getX() + xComp(SOURCE_CHARGE_DRAW_RADIUS, startAngle);
                    preY = c.getY() + yComp(SOURCE_CHARGE_DRAW_RADIUS, startAngle);
                    
                    notReachedNegative = true;
                    
                    for (int j = 0; notReachedNegative && j < drawLimit; j++) {
                        
                        // Use nextAngle to calculate which direction to draw
                        nextAngle = nextAngle(preX, preY);
                        
                        // Calculate end point of line to draw
                        nextX = preX + xComp(step, nextAngle);
                        nextY = preY + yComp(step, nextAngle);
                        
                        // Draw line from old point to new point
                        gc.strokeLine(preX, preY, nextX, nextY);
                        
                        // Make new point the old point
                        preX = nextX;
                        preY = nextY;
                        
                        // Checks if new point is inside charge so that the
                        // drawing can stop
                        for (SourceCharge c2 : sourceCharges) {
                            
                            // Only negative source charges
                            if (c2.getCharge() < 0) {
                                
                                // If point is inside circle of source charge
                                if ((nextX - c2.getX()) * (nextX - c2.getX()) + (nextY - c2.getY())
                                        * (nextY - c2.getY()) < SOURCE_CHARGE_DRAW_RADIUS
                                                * SOURCE_CHARGE_DRAW_RADIUS) {
                                    notReachedNegative = false;
                                }
                            }
                        }
                    }
                }
            }
        }
    }
    
    /**
     * Finds the next angle for drawing the electric field line by finding the
     * angle of the net force at the point given
     * 
     * @param x
     * @param y
     * @return a double value for the next angle in radians
     */
    private double nextAngle(double x, double y) {
        double netX = 0;
        double netY = 0;
        double dis, mag, angle;
        
        // Cycle through each source charge
        for (SourceCharge sourceCharge : sourceCharges) {
            
            // Find distance between this point and the source charge
            dis = Point2D.distance(x, y, sourceCharge.getX(), sourceCharge.getY());
            
            // Find magnitude and sign of the electric force between this point
            // and the source charge
            mag = TEST_CHARGE * sourceCharge.getCharge() / dis / dis;
            
            // Find angle between this point and the source charge
            angle = Math.atan2(y - sourceCharge.getY(), x - sourceCharge.getX());
            
            // Add x and y components of the electric force to the net x and net
            // y components, respectively
            netX += xComp(mag, angle);
            netY += yComp(mag, angle);
        }
        
        // Return the angle of the net force
        return Math.atan2(netY, netX);
    }
    
    private void drawSourceCharges() {
        for (SourceCharge c : sourceCharges) {
            String chargeColorString = (c.getCharge() > 0) ? "#ff3333" : "#3366ff";
            gc.setFill(Paint.valueOf(chargeColorString));
            gc.fillOval(c.getX() - SOURCE_CHARGE_DRAW_RADIUS, c.getY() - SOURCE_CHARGE_DRAW_RADIUS,
                    2 * SOURCE_CHARGE_DRAW_RADIUS, 2 * SOURCE_CHARGE_DRAW_RADIUS);
            gc.strokeOval(c.getX() - SOURCE_CHARGE_DRAW_RADIUS,
                    c.getY() - SOURCE_CHARGE_DRAW_RADIUS, 2 * SOURCE_CHARGE_DRAW_RADIUS,
                    2 * SOURCE_CHARGE_DRAW_RADIUS);
            String chargeString = (c.getCharge() > 0) ? "+" + c.getCharge() : "" + c.getCharge();
            gc.strokeText(chargeString, c.getX() - 12, c.getY() + 5);
        }
    }
    
    private void clear() {
        gc.clearRect(-xTrans / zoomLevel, -yTrans / zoomLevel, canvas.getWidth() / zoomLevel,
                canvas.getHeight() / zoomLevel);
    }
    
    public void refresh() {
        gc.setTransform(zoomLevel, 0, 0, zoomLevel, xTrans, yTrans);
        clear();
        drawField();
        drawSourceCharges();
    }
    
    public ArrayList<SourceCharge> getCharges() {
        return sourceCharges;
    }
    
    private double xComp(double mag, double angle) {
        return mag * Math.cos(angle);
    }
    
    private double yComp(double mag, double angle) {
        return mag * Math.sin(angle);
    }
    
    private void addMouseHandlers() {
        canvas.addEventFilter(MouseEvent.MOUSE_PRESSED, new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent mouseEvent) {
                if (mouseEvent.getButton() == MouseButton.PRIMARY) {
                    ElectricFieldSimStart.optionsWindow.setValues();
                    sourceCharges.add(
                            new SourceCharge(placedCharge, (mouseEvent.getX() - xTrans) / zoomLevel,
                                    (mouseEvent.getY() - yTrans) / zoomLevel));
                    refresh();
                } else if (mouseEvent.getButton() == MouseButton.SECONDARY) {
                    ElectricFieldSimStart.optionsWindow.setValues();
                    sourceCharges.add(new SourceCharge(-placedCharge,
                            (mouseEvent.getX() - xTrans) / zoomLevel,
                            (mouseEvent.getY() - yTrans) / zoomLevel));
                    refresh();
                }
            }
        });
    }
    
    private void addKeyHandlers() {
        this.addEventFilter(KeyEvent.KEY_PRESSED, new EventHandler<KeyEvent>() {
            @Override
            public void handle(KeyEvent event) {
                if (event.getCode() == KeyCode.UP || event.getCode() == KeyCode.W) {
                    yTrans += 100;
                    refresh();
                }
                if (event.getCode() == KeyCode.DOWN || event.getCode() == KeyCode.S) {
                    yTrans -= 100;
                    refresh();
                }
                if (event.getCode() == KeyCode.LEFT || event.getCode() == KeyCode.A) {
                    xTrans += 100;
                    refresh();
                }
                if (event.getCode() == KeyCode.RIGHT || event.getCode() == KeyCode.D) {
                    xTrans -= 100;
                    refresh();
                }
                if (event.getCode() == KeyCode.Q) {
                    zoomIn();
                }
                if (event.getCode() == KeyCode.E) {
                    zoomOut();
                }
            }
        });
        this.addEventFilter(KeyEvent.KEY_TYPED, new EventHandler<KeyEvent>() {
            @Override
            public void handle(KeyEvent event) {
                if (event.getCharacter().equals("=") || event.getCharacter().equals("+")) {
                    zoomIn();
                }
                if (event.getCharacter().equals("-") || event.getCharacter().equals("_")) {
                    zoomOut();
                }
            }
        });
    }
    
    private void zoomIn() {
        if (zoomLevel < MAX_ZOOM) {
            zoomLevel *= ZOOM_CHANGE_FACTOR;
            
            // xTrans -= canvas.getWidth() / 2;
            
            refresh();
        }
    }
    
    private void zoomOut() {
        if (zoomLevel > MIN_ZOOM) {
            zoomLevel /= ZOOM_CHANGE_FACTOR;
            
            // yTrans += canvas.getHeight() / 2;
            
            refresh();
        }
    }
}
