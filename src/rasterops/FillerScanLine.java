package rasterops;

import rasterdata.RasterImage;

import java.util.ArrayList;
import java.util.Collections;

/**
 * class to implementation scan-line algorithm
 * @param <PixelType> a value of the pixel
 */
public class FillerScanLine<PixelType> {

    /**
     *
     * @param img Raster image with previous rasterized operations
     * @param value value of the pixel (color)
     * @param lines1 List of polygon's lines to fill
     * @return RasterImage with filled polygon
     */
    public RasterImage<PixelType> filler (RasterImage<PixelType> img, PixelType value, ArrayList<Line> lines1){

        RasterImage<PixelType>result = img;

        //creating tmp arrays
        ArrayList<Line> lines = lines1;
        ArrayList<Line> intersectionLines = new ArrayList<Line>();
        ArrayList<Integer> intersection = new ArrayList<>();

        for(int i =0; i<lines.size(); i++){
            //recount coordinates
            double X11 =lines.get(i).getX1()* (img.getWidth() - 1);
            double X21 =lines.get(i).getX2() * (img.getWidth() - 1);
            double Y11 =(1 - lines.get(i).getY1()) * (img.getHeight() - 1);
            double Y22 =(1 - lines.get(i).getY2()) * (img.getHeight() - 1);

            lines.get(i).setX1(X11);
            lines.get(i).setX2(X21);
            lines.get(i).setY1(Y11);
            lines.get(i).setY2(Y22);
        }

        //pre-preparation max/min
        int yMax = -1;
        int yMin = (int) lines.get(0).getY1();

        //checking guideline
        //adding, shortening, comparing, counting (k and q), changing points
        for (int i = 0; i<lines.size(); i++){
            if(!lines.get(i).isHorizontal()){
                Line intersectionLine = lines.get(i);

                intersectionLine.changePoints();
                intersectionLine.countParameters();
                intersectionLine.shorten();

                intersectionLines.add(intersectionLine);

                if(yMin>intersectionLine.getY1()){
                    yMin=(int)intersectionLine.getY1();
                }
                if(yMax<intersectionLine.getY2()){
                    yMax=(int)intersectionLine.getY2();
                }
            }
        }

        //main cycle
        for (int i = yMin; i<=yMax; i++){
            intersection.clear();

            for(Line line : intersectionLines){
                Line intersectionLine = line;
                if(intersectionLine.isIntersection(i)){
                    intersection.add(new Integer((int)((double)intersectionLine.countIntersection(i))));
                }
            }

            Collections.sort(intersection);

            for(int j = 0; j<intersection.size(); j=j+2){
                for (int j1 = intersection.get(j); j1<=intersection.get(j+1); j1++){
                    result.withPixel(j1,i,value);
                }
            }
        }
        return result;
    }
}
