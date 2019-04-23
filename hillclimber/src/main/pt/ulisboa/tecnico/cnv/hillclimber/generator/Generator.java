package pt.ulisboa.tecnico.cnv.generator;

import pt.ulisboa.tecnico.cnv.util.ImageBuilder;

import java.awt.image.BufferedImage;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;


public class Generator {

    static public final String IMAGE_FILE_TYPE = ".png";
    private final GeneratorStrategy strategy;
    private final GeneratorArgumentParser ap;
    private final double[][] data;

    protected Generator(GeneratorArgumentParser ap, final GeneratorStrategy strategy) {
        this.ap = ap;
        this.strategy = strategy;
        this.data = new double[ap.getWidth()][ap.getHeight()];
    }

    public double[][] getData() {
        return data;
    }

    public Integer getWidth() {
        return this.ap.getWidth();
    }

    public Integer getHeight() {
        return this.ap.getHeight();
    }

    @Override
    public String toString() {
        final DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss");
        final Date date = new Date();

        final StringBuilder sb = new StringBuilder(this.strategy.toString())
                .append("_").append(this.ap.getWidth())
                .append("x")
                .append(this.ap.getHeight())
                .append("_").append(dateFormat.format(date))
                .append(IMAGE_FILE_TYPE);

        return sb.toString();
    }

    public BufferedImage buildImage() {
        this.strategy.generate(this);

        final BufferedImage bufferedImage = ImageBuilder.buildImage(data, ImageBuilder.colors);

        return bufferedImage;
    }
}
