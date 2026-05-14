package com.jtjmpm.api.game.ClassesForMath;

public class Point2D {
    private double x;
    private double y;

    public Point2D(){
        this.x = 0;
        this.y = 0;
    }
    public Point2D(double x, double y){
        this.x = x;
        this.y = y;
    }

    public double getX(){
        return x;
    }

    public double getY(){
        return y;
    }

    public void setX(double x) {
        this.x = x;
    }

    public void setY(double y) {
        this.y = y;
    }

    public double distanceTo(Point2D other){
        return Math.sqrt(Math.pow(this.x - other.x, 2) + Math.pow(this.y - other.y, 2));
    }
}
