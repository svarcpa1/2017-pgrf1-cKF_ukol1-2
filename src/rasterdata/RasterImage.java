package rasterdata;

import java.util.Optional;

public interface RasterImage<PixelType> {
    Optional<PixelType> getPixel(int c, int r);
    RasterImage<PixelType> withPixel(int c, int r, PixelType value);
    RasterImage<PixelType> cleared(PixelType pixel);
    int getWidth();
    int getHeight();
}
