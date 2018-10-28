package rasterops;

import rasterdata.RasterImage;

public interface LineRasterizer<PixelType> {
    RasterImage<PixelType> rasterizeLine(RasterImage<PixelType> img,
                  double x1, double y1, double x2, double y2,
                  PixelType value);
}
