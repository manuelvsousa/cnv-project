package pt.ulisboa.tecnico.cnv.generator;

import java.util.Random;

public class BlueNoiseStrategy extends AbstractGeneratorStrategy {

    final Random rng = new Random();

    public BlueNoiseStrategy() {
        super.name = GeneratorFactory.GeneratorType.BLUE_NOISE.toString();
    }

    @Override
    public void generate(final Generator gen) {
// Generate 3D points of a surface.
        for(int i = 0; i < gen.getWidth(); i++) {
            for(int j = 0; j < gen.getHeight(); j++) {
                //planeCoords[i][j] = 2*sin(2*Math.PI*i/0.064)+(randomGaussian()*2);
                gen.getData()[i][j] = 2 * Math.sin(2 * Math.PI * i / 0.064) + (rng.nextGaussian() * 128);

                // Adapting the mean and standard deviation of randomGaussian():
                // https://processing.org/examples/randomgaussian.html
                //gen.getData()[i][j] = (rng.nextGaussian() * 128) + 128;
            }
        }
    }
}
