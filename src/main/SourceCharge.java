package main;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Paint;

public class SourceCharge {
    
    private double charge, x, y;
    private double drawRadius = 15;
    private boolean showCharge = true;
    private boolean showCircle = true;
    private boolean showText = true;
    private GraphicsContext gc;
    
    public SourceCharge(double charge, double x, double y, GraphicsContext gc) {
        super();
        this.charge = charge;
        this.x = x;
        this.y = y;
        this.gc = gc;
    }
    
    public SourceCharge(double charge, double x, double y, double drawRadius,
            boolean showCharge, boolean showCircle, boolean showText, GraphicsContext gc) {
        super();
        this.charge = charge;
        this.x = x;
        this.y = y;
        this.drawRadius = drawRadius;
        this.showCharge = showCharge;
        this.showCircle = showCircle;
        this.showText = showText;
        this.gc = gc;
    }

    public void draw() {
        gc.setFill(Paint.valueOf("#ffffff"));
        gc.fillOval(x - drawRadius, y - drawRadius, 2 * drawRadius, 2 * drawRadius);
        gc.strokeOval(x - drawRadius, y - drawRadius, 2 * drawRadius, 2 * drawRadius);
        if (showText) {
            String chargeString = (charge > 0) ? "+" + charge : "" + charge;
            gc.strokeText(chargeString, x - 12, y + 5);
        }
    }

    public double getCharge() {
        return charge;
    }

    public void setCharge(double charge) {
        this.charge = charge;
    }

    public double getX() {
        return x;
    }

    public void setX(double x) {
        this.x = x;
    }

    public double getY() {
        return y;
    }

    public void setY(double y) {
        this.y = y;
    }

    public double getDrawRadius() {
        return drawRadius;
    }

    public void setDrawRadius(double drawRadius) {
        this.drawRadius = drawRadius;
    }

    public boolean isShowCharge() {
        return showCharge;
    }

    public void setShowCharge(boolean showCharge) {
        this.showCharge = showCharge;
    }

    public boolean isShowCircle() {
        return showCircle;
    }

    public void setShowCircle(boolean showCircle) {
        this.showCircle = showCircle;
    }

    public boolean isShowText() {
        return showText;
    }

    public void setShowText(boolean showText) {
        this.showText = showText;
    }

    public GraphicsContext getGC() {
        return gc;
    }

    public void setGC(GraphicsContext gc) {
        this.gc = gc;
    }
}
