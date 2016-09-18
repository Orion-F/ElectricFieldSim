package main;

public class SourceCharge {
    
    private double charge, x, y;
    
    public SourceCharge(double charge, double x, double y) {
        super();
        this.charge = charge;
        this.x = x;
        this.y = y;
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
}
