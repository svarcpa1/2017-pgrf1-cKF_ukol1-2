package rasterops;

import java.awt.*;
import java.util.ArrayList;

/**
 * class for resterizing polygon
 * @param <PixelType> value of the pixel
 */
public class LineRasterizerPolygon<PixelType> {

    private static final int DEVIATION = 10;
    private boolean beingRasterized= false;
    ArrayList<Point> apexList = new ArrayList<Point>();
    ArrayList<Point> apexListTrimmed = new ArrayList<Point>();

    //getters and setters
    public ArrayList<Point> getApexListTrimmed() {
        return apexListTrimmed;
    }
    public void setApexListTrimmed(ArrayList<Point> apexListTrimmed) {
        this.apexListTrimmed = apexListTrimmed;
    }
    public int getDeviation(){
        return DEVIATION;
    }
    public boolean getbeingRasterized(){
        return beingRasterized;
    }
    public void setbeingRasterized(boolean beingRasterized){
        this.beingRasterized=beingRasterized;
    }
    public void setApexList(ArrayList<Point> apexList){
        this.apexList=apexList;
    }
    public ArrayList<Point> getApexList(){
        return apexList;
    }
}
