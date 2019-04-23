package pt.ulisboa.tecnico.cnv.generator;

import org.apache.commons.cli.*;
import pt.ulisboa.tecnico.cnv.util.AbstractArgumentParser;

public class GeneratorArgumentParser extends AbstractArgumentParser {
    public enum GeneratorParameters {
        /**
         * Set debug mode.
         */

        STRATEGY_SHORT("s"), STRATEGY("strategy");


        private final String text;
        GeneratorParameters(final String text) {
            this.text = text;
        }

        /* (non-Javadoc)
         * @see java.lang.Enum#toString()
         */
        @Override
        public String toString() {
            return this.text;
        }
    }

    @Override
    public void parseValues(final String[] args) {




        if(super.cmd.hasOption(GeneratorParameters.STRATEGY.toString())) {
            final String strategy = super.cmd.getOptionValue(GeneratorParameters.STRATEGY.toString());

            if( ! GeneratorFactory.GeneratorType.isValid(strategy)) {
                throw new IllegalArgumentException(strategy + " is an invalid generator strategy.");
            }

            super.argValues.put(GeneratorParameters.STRATEGY.toString(), GeneratorFactory.GeneratorType.valueOf(strategy));
        }
    }

    @Override
    public void setupCLIOptions() {
        final Option strategyOption = new Option(GeneratorParameters.STRATEGY_SHORT.toString(), GeneratorParameters.STRATEGY.toString(),
                true, "generator strategy can be one of: SINUSOIDAL, RAMP_TEST, PYRAMID, BLUE_NOISE or RANDOM_HILL.");
        strategyOption.setRequired(true);
        super.options.addOption(strategyOption);
    }

    public GeneratorArgumentParser(final String[] args) {
        this.setup(args);
    }

    public GeneratorFactory.GeneratorType getGeneratorStrategy() {
        return (GeneratorFactory.GeneratorType)super.argValues.get(GeneratorParameters.STRATEGY.toString());
    }
}
