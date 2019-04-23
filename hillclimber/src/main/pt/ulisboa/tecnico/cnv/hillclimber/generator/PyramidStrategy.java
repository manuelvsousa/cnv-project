package pt.ulisboa.tecnico.cnv.generator;

public class PyramidStrategy extends AbstractGeneratorStrategy {
    public PyramidStrategy() {
        super.name = GeneratorFactory.GeneratorType.PYRAMID.toString();
    }


    @Override
    public void generate(final Generator gen) {


        /**
         * This function generates an appropriate data array for display. It uses
         * the function: z = Math.cos(Math.abs(sX) + Math.abs(sY)). The parameter
         * specifies the number of data points in each direction, producing a
         * square matrix.
         * @param dimension Size of each side of the returned array
         * @return double[][] calculated values of z = Math.cos(Math.abs(sX) + Math.abs(sY));
         */


        for (int x = 0; x < gen.getWidth(); x++)
        {
            double sX = 6 * (x / (double) gen.getWidth()); // 0 < sX < 6
            sX = sX - 3; // -3 < sX < 3
            for (int y = 0; y < gen.getHeight(); y++)
            {
                double sY = 6 * (y / (double) gen.getHeight()); // 0 < sY < 6
                sY = sY - 3; // -3 < sY < 3
                gen.getData()[x][y] = Math.cos(Math.abs(sX) + Math.abs(sY));
            }
        }
    }
}
