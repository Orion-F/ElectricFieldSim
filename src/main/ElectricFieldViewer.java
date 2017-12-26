package main;

import java.awt.geom.Point2D;
import java.util.ArrayList;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.*;
import javafx.scene.paint.Paint;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;

import javax.xml.transform.Source;

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

    private final double VECTOR_FIELD_SEPARATION = 20;
    
    private final double TEST_CHARGE = 1;
    
    private final int INITIAL_WIDTH = 1000;
    private final int INITIAL_HEIGHT = 700;

    private final int OPTIONS_PANE_WIDTH = 200;
    
    public ArrayList<SourceCharge> sourceCharges;
    
    private GraphicsContext gc;
    
    private Canvas canvas;
    private Scene baseScene;
    private BorderPane basePane;

    private GridPane gridPane;
    private Label label0, label1, label2, label3, label4, errorLabel;
    private TextField field0, field1, field2, field3, field4;
    private CheckBox checkBox;
    private Button refreshButton, clearAllButton, editOptionsButton, linesOrVectorsButton;

    private boolean areOptionsDisabled = false;

    // When true, lines will be drawn from the charges. When false, direction vectors will be drawn at equal distances
    private boolean drawLines = true;
    
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

        basePane.setStyle("-fx-background-color: #b3b3ff;");
        
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

        canvas = new Canvas(baseScene.getWidth() - OPTIONS_PANE_WIDTH, baseScene.getHeight());
        basePane.setCenter(canvas);

        gc = canvas.getGraphicsContext2D();
        
        sourceCharges = new ArrayList<SourceCharge>();
        
        addMouseHandlers();
        addKeyHandlers();

        createOptionsArea();
        
        this.setScene(baseScene);
        this.show();
        this.setMaximized(true);
        
        xTrans = canvas.getWidth() / 2;
        yTrans = canvas.getHeight() / 2;
        
        refresh();
    }
    
    /**
     * Draws electric field lines from every positive source charge, until they
     * reach a negative source charge or the draw limit. The number of lines per
     * positive source charge is equal to the scale or dynamic scale
     */
    private void drawField(boolean isFromPositive) {
        double startAngle, preX, preY, nextX, nextY, nextAngle;
        boolean notReachedOpposite;
        
        // The scale determines the number of lines draw around each positive
        // source charge.
        // By default it uses the number provided in the scale variable
        int scaleToUse = scale;

        // Cycles through initial charges
        for (SourceCharge c : sourceCharges) {
            
            if (isFromPositive && c.isPositive() || !isFromPositive && c.isNegative()) {
                
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

                    notReachedOpposite = true;

                    for (int j = 0; notReachedOpposite && j < drawLimit; j++) {

                        Point2D.Double force = forceAt(preX, preY);

                        // Calculate which direction to draw by finding the angle perpendicular to the force
                        nextAngle = Math.atan2(force.x, force.y);

                        // If going from negative source charge, then use the opposite angle
                        if(c.isNegative()) {
                            nextAngle = nextAngle + Math.PI;
                        }

                        // Calculate end point of line to draw
                        nextX = preX + xComp(step, nextAngle);
                        nextY = preY + yComp(step, nextAngle);

                        // Draw line from old point to new point
                        gc.strokeLine(preX, preY, nextX, nextY);

                        // Make new point the old point
                        preX = nextX;
                        preY = nextY;

                        // Checks if new point is inside charge so that the drawing can stop
                        // Cycles through possible final charges
                        for (int k = 0; k < sourceCharges.size(); k++) {
                            SourceCharge c2 = sourceCharges.get(k);

                            // Look for only charges that are opposite of the charges where the lines came from
                            if (c.isPositive() && c2.isNegative() || c.isNegative() && c2.isPositive()) {

                                // If point is inside circle of source charge
                                if ((nextX - c2.getX()) * (nextX - c2.getX()) + (nextY - c2.getY())
                                        * (nextY - c2.getY()) < SOURCE_CHARGE_DRAW_RADIUS
                                        * SOURCE_CHARGE_DRAW_RADIUS) {
                                    notReachedOpposite = false;
                                }
                            }
                        }

                    }

                }
            }
        }
    }
    
    private void drawVectorField() {
        double xMin = (-canvas.getWidth() / 2 - xTrans + canvas.getWidth() / 2) / zoomLevel;
        double xMax = (canvas.getWidth() / 2 - xTrans + canvas.getWidth() / 2) / zoomLevel;
        double yMin = (-canvas.getHeight() / 2 - yTrans + canvas.getHeight() / 2) / zoomLevel;
        double yMax = (canvas.getHeight() / 2 - yTrans + canvas.getHeight() / 2) / zoomLevel;

        for (double x = xMin; x < xMax; x = x + VECTOR_FIELD_SEPARATION) {
            for (double y = yMin; y < yMax; y = y + VECTOR_FIELD_SEPARATION) {

                Point2D.Double force = forceAt(x, y);

                double nextAngle = Math.atan2(force.x, force.y);

                double nextX = x + xComp(step, nextAngle);
                double nextY = y + yComp(step, nextAngle);

                gc.strokeLine(x, y, nextX, nextY);
            }
        }
    }
    
    /**
     * Finds the electric force in component form at a point in this electric field
     * 
     * @param x
     * @param y
     * @return a Point2D.Double for the force in component form
     */
    private Point2D.Double forceAt(double x, double y) {
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
        return new Point2D.Double(netY, netX);
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

//        long iTime = System.nanoTime();

        gc.setTransform(zoomLevel, 0, 0, zoomLevel, xTrans, yTrans);
        clear();

        if(drawLines) {
            drawField(true);
        } else {
            drawVectorField();
        }

        drawSourceCharges();

//        long fTime = System.nanoTime();
//        errorLabel.setText(String.valueOf(fTime - iTime));

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
                    setValues();
                    sourceCharges.add(
                            new SourceCharge(placedCharge, (mouseEvent.getX() - xTrans) / zoomLevel,
                                    (mouseEvent.getY() - yTrans) / zoomLevel));
                    refresh();
                } else if (mouseEvent.getButton() == MouseButton.SECONDARY) {
                    setValues();
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

    private void createOptionsArea() {

        linesOrVectorsButton = new Button("Lines/Vectors");
        linesOrVectorsButton.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent arg0) {
                drawLines = !drawLines;
                refresh();
            }
        });

        editOptionsButton = new Button("Edit");
        editOptionsButton.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent arg0) {
                areOptionsDisabled = !areOptionsDisabled;
                field0.setDisable(areOptionsDisabled);
                field1.setDisable(areOptionsDisabled);
                field2.setDisable(areOptionsDisabled);
                field3.setDisable(areOptionsDisabled);
                field4.setDisable(areOptionsDisabled);
            }
        });

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

        field4 = new TextField(String.valueOf(ElectricFieldViewer.dynamicScaleFactor));

        field0.setMaxWidth(70);
        field1.setMaxWidth(70);
        field2.setMaxWidth(70);
        field3.setMaxWidth(70);
        field4.setMaxWidth(70);

        checkBox = new CheckBox("Dynamic Scale");
        checkBox.setSelected(ElectricFieldViewer.useDynamicScale);
        checkBox.setTooltip(new Tooltip("Change number of lines based on charge"));

        refreshButton = new Button("Refresh");
        refreshButton.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent arg0) {
                setValues();
                refresh();
            }
        });

        clearAllButton = new Button("Clear All");
        clearAllButton.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent arg0) {
                sourceCharges.clear();
                refresh();
            }
        });

        errorLabel = new Label();
        errorLabel.setStyle("-fx-background-color: #ff4040;");

        gridPane = new GridPane();
        gridPane.setHgap(10);
        gridPane.setVgap(10);
        gridPane.setPadding(new Insets(20, 10, 0, 10));
        gridPane.setStyle("-fx-background-color: #8080ff;");

        gridPane.add(linesOrVectorsButton, 0, 0);
        gridPane.add(editOptionsButton, 1, 0);
        gridPane.add(label0, 0, 1);
        gridPane.add(field0, 1, 1);
        gridPane.add(label1, 0, 2);
        gridPane.add(field1, 1, 2);
        gridPane.add(label2, 0, 3);
        gridPane.add(field2, 1, 3);
        gridPane.add(label3, 0, 4);
        gridPane.add(field3, 1, 4);
        gridPane.add(label4, 0, 5);
        gridPane.add(field4, 1, 5);
        gridPane.add(checkBox, 0, 6);
        gridPane.add(refreshButton, 1, 6);
        gridPane.add(errorLabel, 0, 7);
        gridPane.add(clearAllButton, 0, 8);

        basePane.setRight(gridPane);

        editOptionsButton.fire();
    }

    private void setValues() {
        try {
            placedCharge = Double.valueOf(field0.getText());
            drawLimit = Integer.valueOf(field1.getText());
            step = Double.valueOf(field2.getText());
            scale = Integer.valueOf(field3.getText());
            dynamicScaleFactor = Integer.valueOf(field4.getText());
            useDynamicScale = checkBox.isSelected();
            errorLabel.setText("");
        } catch (NumberFormatException e) {
            errorLabel.setText("INPUT ERROR");
        }
    }
}
