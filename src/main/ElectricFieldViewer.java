package main;

import java.awt.geom.Point2D;
import java.util.ArrayList;

import javafx.geometry.HPos;
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

@SuppressWarnings("FieldCanBeLocal")
class ElectricFieldViewer extends Stage {
    
    private double placedCharge = 1;

    private int drawLimit = 500;
    private double step = 10;

    private int scale = 50;
    private int dynamicScaleFactor = 4;

    private boolean useDynamicScale = false;
    
    private double zoomLevel = 1;
    private static final double MIN_ZOOM = 0.25;
    private static final double MAX_ZOOM = 4;
    private static final double ZOOM_CHANGE_FACTOR = 2;

    private double xTrans = 0;
    private double yTrans = 0;
    
    private static final double SOURCE_CHARGE_DRAW_RADIUS = 15;

    private static final double VECTOR_FIELD_SEPARATION = 20;
    
    private static final double TEST_CHARGE = 1;
    
    private static final int INITIAL_WIDTH = 1000;
    private static final int INITIAL_HEIGHT = 700;

    private static final int OPTIONS_PANE_WIDTH = 200;

    private final ArrayList<SourceCharge> sourceCharges;
    
    private final GraphicsContext gc;
    
    private Canvas canvas;
    private final Scene baseScene;
    private final BorderPane basePane;

    private GridPane gridPane;
    private Label label0, label1, label2, label3, label4, errorLabel,
            refreshTimeLabel1, refreshTimeLabel2;
    private TextField field0, field1, field2, field3, field4;
    private CheckBox checkBox;
    private Button refreshButton, clearAllButton, editOptionsButton, linesOrVectorsButton;

    private boolean areOptionsDisabled = false;

    // When true, lines will be drawn from the charges. When false, direction vectors will be drawn at equal distances
    private boolean drawLines = true;
    
    ElectricFieldViewer() {
        this.setTitle(
                "ElectricFieldSim V" + ElectricFieldSimStart.getVersion() + " by Orion Forowycz");
        this.setWidth(INITIAL_WIDTH);
        this.setHeight(INITIAL_HEIGHT);
        
        this.setOnCloseRequest(we -> System.exit(0));
        
        basePane = new BorderPane();

        basePane.setStyle("-fx-background-color: #b3b3ff;");
        
        baseScene = new Scene(basePane);
        
        baseScene.widthProperty().addListener((observableValue, oldSceneWidth, newSceneWidth) -> canvas.setWidth(
                canvas.getWidth() + newSceneWidth.intValue() - oldSceneWidth.intValue()));

        baseScene.heightProperty().addListener((observableValue, oldSceneHeight, newSceneHeight) -> canvas.setHeight(
                canvas.getHeight() + newSceneHeight.intValue() - oldSceneHeight.intValue()));

        canvas = new Canvas(baseScene.getWidth() - OPTIONS_PANE_WIDTH, baseScene.getHeight());
        basePane.setCenter(canvas);

        gc = canvas.getGraphicsContext2D();
        
        sourceCharges = new ArrayList<>();
        
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
    @SuppressWarnings("SameParameterValue")
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
                    scaleToUse = (int) Math.ceil(dynamicScaleFactor * c.getCharge());
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

                        // Calculate which direction to draw
                        nextAngle = Math.atan2(force.y, force.x);

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
                        for (SourceCharge c2 : sourceCharges) {
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

        for (double x = getXMin(); x < getXMax(); x = x + VECTOR_FIELD_SEPARATION) {
            for (double y = getYMin(); y < getYMax(); y = y + VECTOR_FIELD_SEPARATION) {


                Point2D.Double force = forceAt(x, y);

                double nextAngle = Math.atan2(force.y, force.x);

                double nextX = x + xComp(step, nextAngle);
                double nextY = y + yComp(step, nextAngle);

                gc.strokeLine(x, y, nextX, nextY);
            }
        }

    }
    
    /**
     * Finds the electric force in component form at a point in this electric field
     * 
     * @param x position in x direction
     * @param y position in y direction
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

        return new Point2D.Double(netX, netY);
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

//    private double getNumberOfPositiveCharges() {
//        double n = 0;
//        for (SourceCharge c : sourceCharges) {
//            if (c.isPositive()) {
//                n++;
//            }
//        }
//        return n;
//    }

    private double getXMin() {
        return -xTrans / zoomLevel;
    }

    private double getXMax() {
        return canvas.getWidth() / zoomLevel;
    }

    private double getYMin() {
        return -yTrans / zoomLevel;
    }

    private double getYMax() {
        return canvas.getHeight() / zoomLevel;
    }
    
    private void clear() {
        gc.clearRect(getXMin(), getYMin(), getXMax(), getYMax());
    }

//    private boolean isInsideCanvas(double x, double y) {
//        return x >= getXMin() && x <= getXMax() && y >= getYMin() && y <= getYMax();
//    }
    
    private void refresh() {

        long iTime = System.nanoTime();

        gc.setTransform(zoomLevel, 0, 0, zoomLevel, xTrans, yTrans);
        clear();

        if(drawLines) {
            drawField(true);
        } else {
            drawVectorField();
        }

        drawSourceCharges();

        long fTime = System.nanoTime();
        refreshTimeLabel2.setText(String.valueOf((fTime - iTime)/1000000));

    }

    private double xComp(double mag, double angle) {
        return mag * Math.cos(angle);
    }

    private double yComp(double mag, double angle) {
        return mag * Math.sin(angle);
    }

    private void addMouseHandlers() {
        canvas.addEventFilter(MouseEvent.MOUSE_PRESSED, mouseEvent -> {
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
        });
    }
    
    private void addKeyHandlers() {
        this.addEventFilter(KeyEvent.KEY_PRESSED, event -> {
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
        });
        this.addEventFilter(KeyEvent.KEY_TYPED, event -> {
            if (event.getCharacter().equals("=") || event.getCharacter().equals("+")) {
                zoomIn();
            }
            if (event.getCharacter().equals("-") || event.getCharacter().equals("_")) {
                zoomOut();
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
        linesOrVectorsButton.setOnAction(event -> {
            drawLines = !drawLines;
            refresh();
        });

        editOptionsButton = new Button("Edit");
        editOptionsButton.setOnAction(event -> {
            areOptionsDisabled = !areOptionsDisabled;
            field0.setDisable(areOptionsDisabled);
            field1.setDisable(areOptionsDisabled);
            field2.setDisable(areOptionsDisabled);
            field3.setDisable(areOptionsDisabled);
            field4.setDisable(areOptionsDisabled);
        });

        label0 = new Label("Placed Charge:");
        label0.setTooltip(new Tooltip("Left-click for positive, right-click for negative"));
        GridPane.setHalignment(label0, HPos.RIGHT);

        field0 = new TextField(String.valueOf(placedCharge));

        label1 = new Label("Draw Limit:");
        label1.setTooltip(new Tooltip("The higher the number, the longer the lines go"));
        GridPane.setHalignment(label1, HPos.RIGHT);

        field1 = new TextField(String.valueOf(drawLimit));

        label2 = new Label("Step:");
        label2.setTooltip(new Tooltip("The lower the number, the smoother the lines"));
        GridPane.setHalignment(label2, HPos.RIGHT);

        field2 = new TextField(String.valueOf(step));

        label3 = new Label("Scale:");
        label3.setTooltip(new Tooltip("Lines that come out of + charge, ignoring magnitude"));
        GridPane.setHalignment(label3, HPos.RIGHT);

        field3 = new TextField(String.valueOf(scale));

        label4 = new Label("Dynamic Scale:");
        label4.setTooltip(new Tooltip("Lines that come out of + charge per unit charge"));
        GridPane.setHalignment(label4, HPos.RIGHT);

        field4 = new TextField(String.valueOf(dynamicScaleFactor));

        field0.setMaxWidth(70);
        field1.setMaxWidth(70);
        field2.setMaxWidth(70);
        field3.setMaxWidth(70);
        field4.setMaxWidth(70);

        checkBox = new CheckBox("Dynamic Scale");
        checkBox.setSelected(useDynamicScale);
        checkBox.setTooltip(new Tooltip("Change number of lines based on charge"));

        refreshButton = new Button("Refresh");
        refreshButton.setOnAction(event -> {
            setValues();
            refresh();
        });

        errorLabel = new Label();
        errorLabel.setStyle("-fx-background-color: #ff4040;");

        refreshTimeLabel1 = new Label("Refresh Time (ms):");
        GridPane.setHalignment(refreshTimeLabel1, HPos.RIGHT);

        refreshTimeLabel2 = new Label();

        clearAllButton = new Button("Clear All");
        clearAllButton.setOnAction(event -> {
            sourceCharges.clear();
            refresh();
        });

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
        gridPane.add(refreshTimeLabel1, 0, 7);
        gridPane.add(refreshTimeLabel2, 1, 7);
        gridPane.add(errorLabel, 0, 8);
        gridPane.add(clearAllButton, 0, 9);

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
