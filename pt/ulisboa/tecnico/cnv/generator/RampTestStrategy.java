package pt.ulisboa.tecnico.cnv.generator;

public class RampTestStrategy  extends AbstractGeneratorStrategy {

    public RampTestStrategy() {
        super.name = GeneratorFactory.GeneratorType.RAMP_TEST.toString();
    }

    @Override
    public void generate(final Generator gen) {



        /**
         * This function generates data that is not vertically-symmetric, which
         * makes it very useful for testing which type of vertical axis is being
         * used to plot the data. If the graphics Y-axis is used, then the lowest
         * values should be displayed at the top of the frame. If the non-graphics
         * (mathematical coordinate-system) Y-axis is used, then the lowest values
         * should be displayed at the bottom of the frame.
         * @return double[][] data values of a simple vertical ramp
         */
        for (int x = 0; x < gen.getWidth(); x++)
        {
            for (int y = 0; y < gen.getHeight(); y++)
            {
                gen.getData()[x][y] = y;
            }
        }
    }
}
