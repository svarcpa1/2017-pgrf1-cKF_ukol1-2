package rasterops;

import rasterdata.RasterImage;
import java.util.Optional;

/**
 * seedfill/flodfill algorithm implementation
 * @param <PixelType>
 */
public class FillerSeedFill<PixelType> {

    //set-up pattern
    private static final Integer [][] PATTERN= {    {0xff000000,0xff000000,0xffffffff,0xffffffff,0xff000000,0xff000000},
                                                    {0xff000000,0xff000000,0xffffffff,0xffffffff,0xff000000,0xff000000},
                                                    {0xffffffff,0xffffffff,0xff000000,0xff000000,0xffffffff,0xffffffff},
                                                    {0xffffffff,0xffffffff,0xff000000,0xff000000,0xffffffff,0xffffffff},
                                                    {0xff000000,0xff000000,0xffffffff,0xffffffff,0xff000000,0xff000000},
                                                    {0xff000000,0xff000000,0xffffffff,0xffffffff,0xff000000,0xff000000}
                                               };

    /**
     * method for filling (one color, seed fill)
     * @param img Raster image with previous rasterized operations
     * @param x   seed start point x coordinate
     * @param y   seed start point y coordinate
     * @param newPixel  value of pixel to rasterize (color)
     * @param borderPixel   value of border pixel (color)
     * @return RasterImage with filled polygon (filled with color)
     */
    public RasterImage<PixelType> filler (RasterImage<PixelType> img,
                                          int x, int y,
                                          PixelType newPixel,
                                          PixelType borderPixel){

        PixelType currentPixel= (PixelType) img.getPixel(x,y);

        if ( ((Optional.of(borderPixel).equals(currentPixel))) ||
                ((Optional.of(newPixel).equals(currentPixel))) ||
                ((x<0)||(y<0)||(x>img.getWidth())||(y>img.getHeight())) ){

            return img;
        }
        else {
            img.withPixel(x,y,newPixel);
            filler(img,x+1,y,newPixel,borderPixel);
            filler(img,x-1,y,newPixel,borderPixel);
            filler(img,x,y+1,newPixel,borderPixel);
            filler(img,x,y-1,newPixel,borderPixel);
            return img;
        }
    }

    /**
     * method for filling (one color, seed fill)
     * @param img Raster image with previous rasterized operations
     * @param x   seed start point x coordinate
     * @param y   seed start point y coordinate
     * @param pattern   value of new pixel ( in array (pattern))
     * @param borderPixel   value of border pixel (color)
     * @return RasterImage with filled polygon (filled with pattern)
     */
    public RasterImage<PixelType> fillerPattern (RasterImage<PixelType> img,
                                          int x, int y,
                                          PixelType[][] pattern,
                                          PixelType borderPixel){

        PixelType currentPixel= (PixelType) img.getPixel(x,y);
        PixelType newPixel = pattern[x%pattern.length][y%pattern.length];

        if ( (Optional.of(borderPixel).equals(currentPixel)) ||
                (Optional.of(newPixel).equals(currentPixel)) ||
                ((x<0)||(y<0)||(x>img.getWidth())||(y>img.getHeight())) ){
            return img;
        }
        else {
            img.withPixel(x,y,newPixel);
            fillerPattern(img,x+1,y,pattern,borderPixel);
            fillerPattern(img,x-1,y,pattern,borderPixel);
            fillerPattern(img,x,y+1,pattern,borderPixel);
            fillerPattern(img,x,y-1,pattern,borderPixel);
            return img;
        }
    }

    //getter
    public Integer[][] getPATTERN() {
        return PATTERN;
    }
}