package rasterdata;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.Optional;
import java.util.function.Function;

public class RasterImageBuffered<PixelType> implements RasterImage<PixelType> {
    private final BufferedImage img;
    private final Function<PixelType, Integer> toInteger;
    private final Function<Integer, PixelType> toPixelType;
    public RasterImageBuffered(final BufferedImage img,
                               final Function<PixelType, Integer> toInteger,
                               final Function<Integer, PixelType> toPixelType) {
        this.img = img;
        this.toInteger = toInteger;
        this.toPixelType = toPixelType;
    }

    @Override
    public Optional<PixelType> getPixel(final int c, final int r) {
        if (c < 0 || c >= img.getWidth() ||
                r < 0 || r >= img.getHeight())
            return Optional.empty();
        return Optional.of(toPixelType.apply(img.getRGB(c, r)));
    }

    @Override
    public RasterImage<PixelType> withPixel(final int c, final int r, final PixelType value) {
        if (!(c < 0 || c >= img.getWidth() ||
                r < 0 || r >= img.getHeight()))
            img.setRGB(c, r, toInteger.apply(value));
        return this;
    }

    @Override
    public RasterImage<PixelType> cleared(PixelType pixel) {
        final Graphics gr = img.getGraphics();
        gr.setColor(new Color(toInteger.apply(pixel)));
        gr.fillRect(0, 0, img.getWidth(), img.getHeight());
        return this;
    }

    @Override
    public int getWidth() {
        return img.getWidth();
    }

    @Override
    public int getHeight() {
        return img.getHeight();
    }
}
