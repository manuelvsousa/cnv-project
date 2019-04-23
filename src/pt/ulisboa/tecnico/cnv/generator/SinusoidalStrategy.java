package pt.ulisboa.tecnico.cnv.generator;

public class SinusoidalStrategy extends AbstractGeneratorStrategy {
    public SinusoidalStrategy() {
        super.name = GeneratorFactory.GeneratorType.SINUSOIDAL.toString();
    }


    @Override
    public void generate(final Generator gen) {


        /**
         * This function generates an appropriate data array for display. It uses
         * the function: z = sin(x)*cos(y). The parameter specifies the number
         * of data points in each direction, producing a square matrix.
         * @param dimension Size of each side of the returned array
         * @return double[][] calculated values of z = sin(x)*cos(y)
         */
        double sX, sY; //s for 'Scaled'

        for (int x = 0; x < gen.getWidth(); x++)
        {
            // Iterating the horizontal coordinates is represented by iterating the
            // angle from 0º to 360º in a circle.
            sX = 2 * Math.PI * (x / (double) gen.getWidth()); // 0 < sX < 2 * Pi
            for (int y = 0; y < gen.getHeight(); y++)
            {
                // Iterating the vertical coordinates is represented by iterating the
                // angle from 0º to 360º in a circle.
                sY = 2 * Math.PI * (y / (double) gen.getHeight()); // 0 < sY < 2 * Pi
                gen.getData()[x][y] = Math.sin(sX) * Math.cos(sY);
            }
        }
    }
}
