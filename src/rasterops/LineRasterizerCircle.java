package rasterops;

import rasterdata.RasterImage;

import static java.lang.Math.atan2;
import static java.lang.StrictMath.toDegrees;

/**
 * class for circle and sector circle rasterization
 * @param <PixelType> value of the pixel
 */
public class LineRasterizerCircle<PixelType> {

    private boolean beingCircleRasterized= false;
    private boolean beingSectorRasterized= false;
    private int clickCounter=0;
    private double centerX, centerY;

    /**
     *
     * @param img Raster image with previous rasterized operations
     * @param centerX   center x coordinate
     * @param centerY   center y coordinate
     * @param radius    circle's radius
     * @param value     value of the pixel
     * @return  Raster image with rasterized circle
     */
    public RasterImage<PixelType> rasterizeCircle(RasterImage<PixelType> img,
                                                  double centerX, double centerY,
                                                  double radius,
                                                  PixelType value){

        RasterImage<PixelType> result = img;

        double u = 1;
        double v=2*radius-1;
        double error =0;

        for(int x=0; x<radius;x++ ){
            img.withPixel((int)centerX-(int)radius,(int)centerY+x,value);
            img.withPixel((int)centerX-x,(int)centerY-(int)radius,value);
            img.withPixel((int)centerX+(int)radius,(int)centerY-x,value);
            img.withPixel((int)centerX+x,(int)centerY+(int)radius,value);

            error+=u;
            u+=2;
            if(v<2*error){
                radius--;
                error-=v;
                v-=2;
            }
            if(x<=radius && x!=0) {
                img.withPixel((int)centerX-x, (int)centerY+(int)radius, value);
                img.withPixel((int)centerX-(int)radius, (int)centerY-x, value);
                img.withPixel((int)centerX+x, (int)centerY-(int)radius, value);
                img.withPixel((int)centerX+(int)radius, (int)centerY+x, value);
            }
        }
        return result;
    }

    /**
     *
     * @param img Raster image with previous rasterized operations
     * @param centerX   center x coordinate
     * @param centerY   center y coordinate
     * @param radius    circle's radius
     * @param startAngle start angle of sector
     * @param endAngle  end angle of sector
     * @param value     value of the pixel
     * @return RasterImage with rasterized sector
     */
    public RasterImage<PixelType> rasterizeCircleSector(RasterImage<PixelType> img,
                                                        double centerX, double centerY,
                                                        double radius,
                                                        int startAngle, int endAngle,
                                                        PixelType value){

        RasterImage<PixelType> result = img;
        int angle;
        double u = 1;
        double v=2*radius-1;
        double error =0;

        for(int x1 = 0;  x1<radius;x1++) {

            if(centerX<centerX) {
                angle = (int) toDegrees(atan2((centerX - x1), (centerY - radius)));
            }else{
                angle = (int) toDegrees(atan2((centerX - x1), (centerX -radius)));
            }


            if (angle >= startAngle && angle <= endAngle) {
                result.withPixel((int) (centerX - x1), (int) (centerY - radius), value);
            }
            if (angle + 90 >= startAngle && angle + 90 < endAngle) {
                result.withPixel((int) (centerX + radius), (int) (centerY - x1), value);
            }
            if (angle + 180 >= startAngle && angle + 180 < endAngle) {
                result.withPixel((int) (centerX + x1), (int) (centerY + radius), value);
            }
            if (angle + 270 >= startAngle && angle + 270 < endAngle) {
                result.withPixel((int) (centerX - radius), (int) (centerY + x1), value);
            }

            error+=u;
            u+=2;
            if(v<2*error){
                radius--;
                error-=v;
                v-=2;
            }

            if(x1<=radius && x1!=0) {
                if (360 - angle >= startAngle && 360 - angle < endAngle) {
                    result.withPixel((int) (centerX - x1), (int) (centerY + radius), value);
                }
                if (90 - angle >= startAngle && 90 - angle < endAngle) {
                    result.withPixel((int) (centerX - radius), (int) (centerY - x1), value);
                }
                if (180 - angle >= startAngle && 180 - angle < endAngle) {
                    result.withPixel((int) (centerX + x1), (int) (centerY - radius), value);
                }
                if (270 - angle >= startAngle && 270 - angle < endAngle) {
                    result.withPixel((int) (centerX + radius), (int) (centerY + x1), value);
                }
            }
        }
        return result;
    }

    //gettersr and setters
    public boolean isBeingCircleRasterized() {
        return beingCircleRasterized;
    }
    public void setBeingCircleRasterized(boolean beingCircleRasterized) {
        this.beingCircleRasterized = beingCircleRasterized;
    }
    public boolean isBeingSectorRasterized() {
        return beingSectorRasterized;
    }
    public void setBeingSectorRasterized(boolean beingSectorRasterized) {
        this.beingSectorRasterized = beingSectorRasterized;
    }
    public double getCenterX() {
        return centerX;
    }
    public void setCenterX(double centerX) {
        this.centerX = centerX;
    }
    public double getCenterY() {
        return centerY;
    }
    public void setCenterY(double centerY) {
        this.centerY = centerY;
    }
    public int getClickCounter() {
        return clickCounter;
    }
    public void setClickCounter(int clickCounter) {
        this.clickCounter = clickCounter;
    }
}