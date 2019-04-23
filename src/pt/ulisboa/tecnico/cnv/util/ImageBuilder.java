package pt.ulisboa.tecnico.cnv.util;

import java.awt.*;
import java.awt.image.BufferedImage;

public class ImageBuilder {
    private ImageBuilder() {};


    static public Color[] colors = Gradient.RAINBOW;


    static public BufferedImage buildImage(final double[][] data, final Color[] colors) {
        /** updateDataColors();
         * This uses the current array of colors that make up the gradient, and
         * assigns a color index to each data point, stored in the dataColorIndices
         * array, which is used by the drawData() method to plot the points.
         */
        //We need to find the range of the data values,
        // in order to assign proper colors.
        double largest = Double.MIN_VALUE;
        double smallest = Double.MAX_VALUE;
        for (int x = 0; x < data.length; x++)
        {
            for (int y = 0; y < data[0].length; y++)
            {
                largest = Math.max(data[x][y], largest);
                smallest = Math.min(data[x][y], smallest);
            }
        }
        final double range = largest - smallest;

        // dataColorIndices is the same size as the data array
        // It stores an int index into the color array
        final int[][] dataColorIndices = new int[data.length][data[0].length];

        //assign a Color to each data point
        for (int x = 0; x < data.length; x++)
        {
            for (int y = 0; y < data[0].length; y++)
            {
                double norm = (data[x][y] - smallest) / range; // 0 < norm < 1
                int colorIndex = (int) Math.floor(norm * (colors.length - 1));
                dataColorIndices[x][y] = colorIndex;
            }
        }

        /** drawData();
         * Creates a BufferedImage of the actual data plot.
         *
         * After doing some profiling, it was discovered that 90% of the drawing
         * time was spend drawing the actual data (not on the axes or tick marks).
         * Since the Graphics2D has a drawImage method that can do scaling, we are
         * using that instead of scaling it ourselves. We only need to draw the
         * data into the bufferedImage on startup, or if the data or gradient
         * changes. This saves us an enormous amount of time. Thanks to
         * Josh Hayes-Sheen (grey@grevian.org) for the suggestion and initial code
         * to use the BufferedImage technique.
         *
         * Since the scaling of the data plot will be handled by the drawImage in
         * paintComponent, we take the easy way out and draw our bufferedImage with
         * 1 pixel per data point. Too bad there isn't a setPixel method in the
         * Graphics2D class, it seems a bit silly to fill a rectangle just to set a
         * single pixel...
         *
         * This function should be called whenever the data or the gradient changes.
         */
        //
        final BufferedImage bufferedImage = new BufferedImage(data.length,data[0].length, BufferedImage.TYPE_INT_ARGB);
        final Graphics2D bufferedGraphics = bufferedImage.createGraphics();

        for (int x = 0; x < data.length; x++)
        {
            for (int y = 0; y < data[0].length; y++)
            {
                bufferedGraphics.setColor(colors[dataColorIndices[x][y]]);
                bufferedGraphics.fillRect(x, y, 1, 1);
            }
        }

        return bufferedImage;
    }

}
