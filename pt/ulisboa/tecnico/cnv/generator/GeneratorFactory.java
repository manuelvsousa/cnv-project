package pt.ulisboa.tecnico.cnv.generator;

public class GeneratorFactory {

    public enum GeneratorType {
        SINUSOIDAL("SINUSOIDAL"),
        RAMP_TEST("RAMP_TEST"),
        PYRAMID("PYRAMID"),
        BLUE_NOISE("BLUE_NOISE"),
        RANDOM_HILL("RANDOM_HILL");

        private final String text;
        GeneratorType(final String text) {
            this.text = text;
        }

        /* (non-Javadoc)
         * @see java.lang.Enum#toString()
         */
        @Override
        public String toString() {
            return text;
        }

        static public Boolean isValid(final String strategyString) {

            for (GeneratorType enumStrat : GeneratorType.values()) {
                if(enumStrat.toString().equals(strategyString)) {
                    return true;
                }
            }
            return false;
        }
    }

    // Singleton.
    static private GeneratorFactory instance = null;
    private GeneratorFactory(){}
    static public GeneratorFactory getInstance() {
        if(instance == null) {
            instance = new GeneratorFactory();
        }
        return instance;
    };

    public Generator makeGenerator(final GeneratorArgumentParser ap) {
        final GeneratorType t = ap.getGeneratorStrategy();

        if(t == GeneratorType.SINUSOIDAL) {
            return new Generator(ap, new SinusoidalStrategy());
        }
        else if(t == GeneratorType.RAMP_TEST) {
            return new Generator(ap, new RampTestStrategy());
        }
        else if(t == GeneratorType.PYRAMID) {
            return new Generator(ap, new PyramidStrategy());
        }
        else if(t == GeneratorType.BLUE_NOISE) {
            return new Generator(ap, new BlueNoiseStrategy());
        }
        else if(t == GeneratorType.RANDOM_HILL) {
            return new Generator(ap, new RandomHillStrategy());
        }
        else {
            throw new IllegalArgumentException();
        }
    }
}
