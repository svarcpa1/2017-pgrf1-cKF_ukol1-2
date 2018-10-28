package rasterops;

/**
 * Line class used for representation a line. Used in polygon list and mostly
 * scan line algorithm
 */
public class Line{

    public Line(double x1, double y1, double x2, double y2, int color) {
        this.x1 = x1;
        this.y1 = y1;
        this.x2 = x2;
        this.y2 = y2;
        this.color=color;
    }

    private double x1,y1,x2,y2;
    private int color;
    private double k;
    private double q;

    //method for counting parameters k, q (guideline and intersection with Y)
    public void countParameters(){
        k=(x2-x1)/(y2-y1);
        q= x1-k*y1;
    }

    //changing points in case of wrong direction
    public void changePoints(){
        if(y1>y2){
            double tmp = y1;
            y1=y2;
            y2=tmp;
            double tmp2 = x1;
            x1=x2;
            x2=tmp2;
        }
    }

    //is line horizontal???
    public boolean isHorizontal(){
        if (y1==y2){
            return true;
        }
        return false;
    }

    //is there any intersection???
    public boolean isIntersection(double e){
        if(e>=Math.round(y1) && e<=Math.round(y2)){
            return true;
        }else {
            return false;
        }
    }

    //return number of intersection
    public double countIntersection(double e){
        double intersection = k*e+q;
        return intersection;
    }

    //method to shorten line (used in scan-line)
    public void shorten(){
        y2=y2-1;
    }

    //getters and setters
    public void setX1(double x1) {
        this.x1 = x1;
    }
    public void setY1(double y1) {
        this.y1 = y1;
    }
    public void setX2(double x2) {
        this.x2 = x2;
    }
    public void setY2(double y2) {
        this.y2 = y2;
    }
    public double getX1() {
        return x1;
    }
    public int getColor() {
        return color;
    }
    public double getY1() {
        return y1;
    }
    public double getX2() {
        return x2;
    }
    public double getY2() {
        return y2;
    }
}
