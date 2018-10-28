package rasterops;

/**
 * this class holds coordinates of 2D point in double.
 * used mainly in Sutherland–Hodgman algorithm
 */
public class PointInDouble {
    private double x;
    private double y;

    public PointInDouble(double x, double y) {
        this.x = x;
        this.y = y;
    }

    public double getX() {
        return x;
    }
    public double getY() {
        return y;
    }
}
