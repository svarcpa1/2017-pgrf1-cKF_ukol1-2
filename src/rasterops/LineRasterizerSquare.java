package rasterops;

import rasterdata.RasterImage;

/**
 * classt for rasterization of square
 * @param <PixelType> value of pixel
 */
public class LineRasterizerSquare<PixelType> {

    private int clickCounter=0;
    private double centerX, centerY;
    private boolean squareBeingRasterize=false;


    /**
     *
     * @param img Raster image with previous rasterized operations
     *            preparation for future enlargement (now id canvas cleared before rasterizing)
     * @param centerX   center coordinate x
     * @param centerY   center coordinate y
     * @param size      size od a line of square
     * @param value     value of rasterized pixel
     * @return  RasterImage with pixel which form square
     */
    public RasterImage<PixelType> rasterizeSquare(RasterImage<PixelType> img,
                                                  double centerX, double centerY,
                                                  double size,
                                                  PixelType value) {

        RasterImage<PixelType> result = img;

        for (int x1 = 0; x1 < size+1; x1++) {
            result.withPixel((int) (centerX - x1), (int) (centerY - size), value);
            result.withPixel((int) (centerX + size), (int) (centerY - x1), value);
            result.withPixel((int) (centerX + x1), (int) (centerY + size), value);
            result.withPixel((int) (centerX - size), (int) (centerY + x1), value);
            result.withPixel((int) (centerX - x1), (int) (centerY + size), value);
            result.withPixel((int) (centerX - size), (int) (centerY - x1), value);
            result.withPixel((int) (centerX + x1), (int) (centerY - size), value);
            result.withPixel((int) (centerX + size), (int) (centerY + x1), value);
        }
        return result;
    }

    //getters and setters
    public int getClickCounter() {
        return clickCounter;
    }
    public void setClickCounter(int clickCounter) {
        this.clickCounter = clickCounter;
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
    public boolean isSquareBeingRasterize() {
        return squareBeingRasterize;
    }
    public void setSquareBeingRasterize(boolean squareBeingRasterize) {
        this.squareBeingRasterize = squareBeingRasterize;
    }
}
