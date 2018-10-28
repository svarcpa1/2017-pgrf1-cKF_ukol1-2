package rasterops;

import rasterdata.RasterImage;

/**
 * class for implementation trivial algorithm
 * not completed
 * @param <PixelType> value of the pixel
 */
public class LineRasterizerNaive<PixelType> implements LineRasterizer<PixelType> {

    /**
     *
     * @param img Raster image with previous rasterized operations
     * @param x1    start point x
     * @param y1    start point y
     * @param x2    end point x
     * @param y2    end point y
     * @param value value of the pixel
     * @return
     */
    @Override
    public RasterImage<PixelType> rasterizeLine(
            final RasterImage<PixelType> img,
             double x1,  double y1,  double x2,  double y2,
            PixelType value) {

        //recounting points
        double rx1 = x1 * (img.getWidth() - 1);
        double ry1 = (1 - y1) * (img.getHeight() - 1);
        double rx2 = x2 * (img.getWidth() - 1);
        double ry2 = (1 - y2) * (img.getHeight() - 1);
        double k = (ry2 - ry1) / (rx2 - rx1);
        double q = ry1 - k * rx1;

        if(rx2 < rx1) {
            double c = rx2;
            rx2 = rx1;
            rx1 = c;
            c = ry2;
            ry2 = ry1;
            ry1 = c;
        }

        RasterImage<PixelType> result = img;

        if(k<=1.0){
            for (int c = (int) rx1; c <= rx2; c++) {
                final double ry = k * c + q;
                result = result.withPixel(c, (int) ry, value);
            }
        }else{
            for (int c = (int) ry1; c <= ry2; c++) {
                final double rx = (c-q)/k;
                result = result.withPixel((int) rx,c, value);
            }
        }
        return result;
    }
}
