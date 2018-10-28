package rasterops;

import rasterdata.RasterImage;

/**
 * class for line rasterization (DDA algorithm)
 * @param <PixelType> value of the pixel
 */
public class LineRasterizerDDA<PixelType> implements LineRasterizer<PixelType> {

    /**
     *
     * @param img Raster image with previous rasterized operations
     * @param x1 start point x
     * @param y1 start point y
     * @param x2 end point x
     * @param y2 edn point y
     * @param value value of the pixel
     * @return RasterImage with rasterizerd points (line)
     */
    @Override
    public RasterImage<PixelType> rasterizeLine(RasterImage<PixelType> img,
                                                double x1, double y1,
                                                double x2, double y2,
                                                PixelType value) {

        double rx1 = x1 * (img.getWidth() - 1);
        double ry1 = (1 - y1) * (img.getHeight() - 1);
        double rx2 = x2 * (img.getWidth() - 1);
        double ry2 = (1 - y2) * (img.getHeight() - 1);
        double error=0;

        RasterImage<PixelType> result = img;

        //handling one point
        if ((rx1 == rx2) && (ry1 == ry2)) {
            result = result.withPixel((int) rx1, (int) ry2, value);
        }

        // switching points in case of steep or bad direction
        if ((Math.abs(ry2 - ry1)) < (Math.abs(rx2 - rx1))) {
            if(rx2 < rx1) {
                double c = rx2;
                rx2 = rx1;
                rx1 = c;
                c = ry2;
                ry2 = ry1;
                ry1 = c;
            }
            final double dx = rx2 - rx1;
            final double dy = ry2 - ry1;
            final double direction = dy / dx;
            int integer_y;
            double y = ry1;
            int x = (int) Math.round(rx1);
            while ( x <= rx2){
                integer_y = (int) Math.round(y);
                result = result.withPixel(x, integer_y, value);
                y += direction;
                if (error >= 0.5)
                {
                    y++;
                    error -= 1.0;
                }
                x++;
            }
        } else {
            if (ry2 < ry1) {
                double c = rx2;
                rx2 = rx1;
                rx1 = c;
                c = ry2;
                ry2 = ry1;
                ry1 = c;
            }
            final double dx = rx2 - rx1;
            final double dy = ry2 - ry1;
            final double direction2 = dx / dy;
            int integer_x;
            double x = rx1;
            int y = (int) Math.round(ry1);
            while ( y <= ry2){
                integer_x = (int) Math.round(x);
                result = result.withPixel(integer_x, y, value);
                x += direction2;
                if (error >= 0.5)
                {
                    x++;
                    error -= 1.0;
                }
                y++;
            }
        }
        return result;
    }
}


