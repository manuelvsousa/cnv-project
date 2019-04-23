package pt.ulisboa.tecnico.cnv.generator;

import pt.ulisboa.tecnico.cnv.util.Perlin2d;

public class RandomHillStrategy extends AbstractGeneratorStrategy {

    public RandomHillStrategy() {
        super.name = GeneratorFactory.GeneratorType.RANDOM_HILL.toString();
    }

    @Override
    public void generate(final Generator gen) {

        final float HighestHillHeight = 500.0f;
        final float LowestHillHeight = 0.0f;

        float hillHeight = (float)((float)HighestHillHeight - (float)LowestHillHeight) / ((float)gen.getWidth() / 2);
        float baseHeight = (float)LowestHillHeight / ((float)gen.getHeight() / 2);

        final int tileSize = 2;


        double p = 1.0 / 2;
        int seed = (int) (Integer.MAX_VALUE * Math.random());
        int n = 4;
        final Perlin2d perlin = new Perlin2d(p, n, seed);

        for (int i = 0; i < gen.getWidth(); i++) {
            for (int k = 0; k < gen.getHeight(); k++) {
                gen.getData()[i][k] = baseHeight + (perlin.perlinNoise2(((float)i / (float)gen.getWidth()) * tileSize, ((float)k / (float)gen.getHeight()) * tileSize) * (float)hillHeight);
            }
        }
    }
}
