package main;

class SourceCharge {
    
    private double charge, x, y;
    
    SourceCharge(double charge, double x, double y) {
        super();
        this.charge = charge;
        this.x = x;
        this.y = y;
    }
    
    double getCharge() {
        return charge;
    }
    
    double getX() {
        return x;
    }
    
    double getY() {
        return y;
    }

    boolean isPositive() {
        return charge > 0;
    }

    boolean isNegative() {
        return charge < 0;
    }
}
