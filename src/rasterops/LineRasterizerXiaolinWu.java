package rasterops;

import rasterdata.RasterImage;

/**
 * class for rasterization line using Xiaolin Wu's algorithm
 * done with help of pseudo-code on wiki -
 * https://en.wikipedia.org/wiki/Xiaolin_Wu%27s_line_algorithm
 */
public class LineRasterizerXiaolinWu {

    private RasterImage img;
    private int backGroundColor, drawingColor, color;
    double gradient;

    public LineRasterizerXiaolinWu(RasterImage img) {
        this.img = img;
    }

    /**
     *
     * @param x1 start point x
     * @param y1 start point y
     * @param x2 end point x
     * @param y2 end point y
     * @return Raster image "result" with rasterize points (line)
     */
    public RasterImage rasterizeLine(int x1, int y1,
                                     int x2, int y2) {

        RasterImage result = img;

        //chceking steepness of line for changing points
        boolean steep = Math.abs(y2-y1)> Math.abs(x2-x1);

        //swaping points due to direction
        if(steep){
            int c = y1;
            y1 = x1;
            x1 = c;
            c = x2;
            x2 = y2;
            y2 = c;
        }
        if(x2 < x1) {
            int d = x2;
            x2 = x1;
            x1 = d;
            d = y2;
            y2 = y1;
            y1 = d;
        }

        double dx = x2 - x1;
        double dy = y2 - y1;

        if (dx == 0.0){
            gradient =1;
        }else {
            gradient = dy / dx;
        }

        //treatment of first end point
        int xEnd = round(x1);
        int yEnd = (int)(y1+gradient*(xEnd-x1));
        int xGap = (int) rfpart(x1+0.5);

        int xPxl1 = xEnd;   //usage in main loop
        int yPxl1 = (int)ipart(yEnd);

        if (steep) {
            plot(yPxl1, xPxl1, rfpart(yEnd)*xGap);
            plot((yPxl1 + 1), xPxl1, fpart(yEnd)*xGap);
        } else {
            plot(xPxl1, yPxl1, rfpart(yEnd) * xGap);
            plot(xPxl1, yPxl1 + 1, fpart(yEnd) * xGap);
        }

        double intery = yEnd + gradient;

        //treatment of second end point
        xEnd = round(x2);
        yEnd = (int)(y2+gradient*(xEnd-x2));
        xGap = (int) rfpart(x2+0.5);

        int xPxl2 = xEnd;   //usage in main loop
        int yPxl2 = (int)ipart(yEnd);

        if (steep) {
            plot(yPxl2, xPxl2, rfpart(yEnd)*xGap);
            plot((yPxl2 + 1), xPxl2, fpart(yEnd)*xGap);
        } else {
            plot(xPxl2, yPxl2, rfpart(yEnd) * xGap);
            plot(xPxl2, yPxl2 + 1, fpart(yEnd) * xGap);
        }

        //main loop
        if(steep){
            for(int i = xPxl1+1; i<=xPxl2-1; i++){
                plot((int)ipart(intery),i,rfpart(intery));
                plot((int)ipart(intery)+1,i,fpart(intery));
                intery +=gradient;
            }
        }else {
            for(int i = xPxl1+1; i<=xPxl2-1; i++){
                plot(i,(int)ipart(intery),rfpart(intery));
                plot(i,(int)ipart(intery)+1,fpart(intery));
                intery +=gradient;
            }
        }
        return result;
    }

    /**
     *
     * @param x coordinate of point
     * @param y coordinate of point
     * @param intensity color intensity of the pixel
     */
    private void plot(int x, int y, double intensity){
        double alfaChanel = Math.round(intensity*100.0)/100.0;

        //init collors
        backGroundColor = 0xff2f2f2f;
        drawingColor = 0xffff0000;

        //mixing of colors due to alfaChanel value
        if (alfaChanel >= 0 && alfaChanel <= 0.2) {
            color = blend(backGroundColor,drawingColor,(float)alfaChanel);
        } else if (alfaChanel > 0.2 && alfaChanel <= 0.4) {
            color = blend(backGroundColor,drawingColor,(float)alfaChanel);
        } else if (alfaChanel > 0.4 && alfaChanel <= 0.6) {
            color = blend(backGroundColor,drawingColor,(float)alfaChanel);
        } else if (alfaChanel > 0.6 && alfaChanel <= 0.8) {
            color = blend(backGroundColor,drawingColor,(float)alfaChanel);
        } else if (alfaChanel > 0.8 && alfaChanel <= 1.0) {
            color = blend(backGroundColor,drawingColor,(float)alfaChanel);
        }

        img.withPixel(x,y,color);
    }

    /**
     * method for color blending
     * @param a first color
     * @param b second color
     * @param ratio intensity of the color
     * @return mixed color
     */
    private int blend (int a, int b, float ratio) {
        if (ratio > 1f) {
            ratio = 1f;
        } else if (ratio < 0f) {
            ratio = 0f;
        }

        float iRatio = 1.0f - ratio;

        int aA = (a >> 24 & 0xff);
        int aR = ((a & 0xff0000) >> 16);
        int aG = ((a & 0xff00) >> 8);
        int aB = (a & 0xff);

        int bA = (b >> 24 & 0xff);
        int bR = ((b & 0xff0000) >> 16);
        int bG = ((b & 0xff00) >> 8);
        int bB = (b & 0xff);

        int A = (int)((aA * iRatio) + (bA * ratio));
        int R = (int)((aR * iRatio) + (bR * ratio));
        int G = (int)((aG * iRatio) + (bG * ratio));
        int B = (int)((aB * iRatio) + (bB * ratio));

        return A << 24 | R << 16 | G << 8 | B;
    }

    //integer part of x
    private double ipart (double x){
        return Math.floor(x);
    }

    private int round (double x){
        return (int) Math.round(x+0.5);
    }

    //fractional part of x
    private double fpart (double x){
        return x-Math.floor(x);
    }

    private double rfpart (double x){
        return 1-fpart(x);
    }
}
