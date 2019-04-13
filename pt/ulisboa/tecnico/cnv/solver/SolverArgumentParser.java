package pt.ulisboa.tecnico.cnv.solver;

import org.apache.commons.cli.Option;
import pt.ulisboa.tecnico.cnv.util.AbstractArgumentParser;

import java.io.File;


public class SolverArgumentParser extends AbstractArgumentParser {
    public enum SolverParameters {
        /**
         * Set debug mode.
         */

        PATH_DASHED("dashed"),

        INPUT_IMG_SHORT("i"), INPUT_IMG("input"),

        STRATEGY_SHORT("s"), STRATEGY("strategy"),

        UPPER_LEFT_X("x0"),
        UPPER_LEFT_Y("y0"),
        LOWER_RIGHT_X("x1"),
        LOWER_RIGHT_Y("y1"),

        START_X("xS"),
        START_Y("yS");

        private final String text;
        SolverParameters(final String text) {
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
    public void parseValues(String[] args) {

        // If none are given, set them to (0, 0) and (image width, image height)
        // If all four are given, check x0 < x1 and y0 < y1.

        // Process input image argument.
        final String inputImgPath = cmd.getOptionValue(SolverParameters.INPUT_IMG.toString());
        final File file = new File(inputImgPath);
        if (! file.exists())
            throw new IllegalArgumentException(SolverParameters.INPUT_IMG.toString() + " does not exist: " + inputImgPath);
        if( file.isDirectory())
            throw new IllegalArgumentException(SolverParameters.INPUT_IMG.toString() + " was a directory but should be a file: " + inputImgPath);
        if( ! inputImgPath.endsWith(".png") && ! inputImgPath.endsWith(".dat"))
            throw new IllegalArgumentException(SolverParameters.INPUT_IMG.toString() + " must be either a .png or .dat file.");
        super.argValues.put(SolverParameters.INPUT_IMG.toString(), inputImgPath);

        ///// XX

        // Validate the upper-left X coordinate of the selected rectangle.
        if(cmd.hasOption(SolverParameters.UPPER_LEFT_X.toString())) {
            final Integer x0 = Integer.parseInt(cmd.getOptionValue(SolverParameters.UPPER_LEFT_X.toString()));

            if(x0 < 0 || x0 > this.getWidth())
                throw new IllegalArgumentException(SolverParameters.UPPER_LEFT_X.toString() + " must be a non-negative integer lower than -" +  GenericParameters.WIDTH.toString() + ".");

            super.argValues.put(SolverParameters.UPPER_LEFT_X.toString(), x0);
        }
        else {
            super.argValues.put(SolverParameters.UPPER_LEFT_X.toString(), 0);
        }

        // Validate the lower-right X coordinate of the selected rectangle.
        if(cmd.hasOption(SolverParameters.LOWER_RIGHT_X.toString())) {
            final Integer x1 = Integer.parseInt(cmd.getOptionValue(SolverParameters.LOWER_RIGHT_X.toString()));

            if(x1 > this.getWidth() || x1 <= this.getX0())
                throw new IllegalArgumentException(SolverParameters.LOWER_RIGHT_X.toString() + " must be a positive integer above -" + SolverParameters.UPPER_LEFT_X.toString() + " and below " + GenericParameters.WIDTH.toString() + ".");

            super.argValues.put(SolverParameters.LOWER_RIGHT_X.toString(), x1);
        }
        else {
            super.argValues.put(SolverParameters.LOWER_RIGHT_X.toString(), this.getWidth());
        }

        ///// YY

        // Validate the lower-left Y coordinate of the selected rectangle.
        if(cmd.hasOption(SolverParameters.UPPER_LEFT_Y.toString())) {
            final Integer y0 = Integer.parseInt(cmd.getOptionValue(SolverParameters.UPPER_LEFT_Y.toString()));

            if(y0 < 0 || y0 > this.getHeight())
                throw new IllegalArgumentException(SolverParameters.UPPER_LEFT_Y.toString() + " must be a non-negative integer lower than -" +  GenericParameters.HEIGHT.toString() + ".");

            super.argValues.put(SolverParameters.UPPER_LEFT_Y.toString(), y0);
        }
        else {
            super.argValues.put(SolverParameters.UPPER_LEFT_Y.toString(), 0);
        }

        // Validate the upper-right Y coordinate of the selected rectangle.
        if(cmd.hasOption(SolverParameters.LOWER_RIGHT_Y.toString())) {
            final Integer y1 = Integer.parseInt(cmd.getOptionValue(SolverParameters.LOWER_RIGHT_Y.toString()));

            if(y1 > this.getHeight() || y1 <= this.getY0())
                throw new IllegalArgumentException(SolverParameters.LOWER_RIGHT_Y.toString() + " must be a positive integer above -" + SolverParameters.UPPER_LEFT_Y.toString() + " and below " + GenericParameters.HEIGHT.toString() + ".");

            super.argValues.put(SolverParameters.LOWER_RIGHT_Y.toString(), y1);
        }
        else {
            super.argValues.put(SolverParameters.LOWER_RIGHT_Y.toString(), this.getHeight());
        }


        // Validate the chosen solver strategy.
        if(cmd.hasOption(SolverArgumentParser.SolverParameters.STRATEGY.toString())) {
            final String strategy = cmd.getOptionValue(SolverArgumentParser.SolverParameters.STRATEGY.toString());

            if( ! SolverFactory.SolverType.isValid(strategy)) {
                throw new IllegalArgumentException(strategy + " is an invalid generator strategy.");
            }

            super.argValues.put(SolverArgumentParser.SolverParameters.STRATEGY.toString(), SolverFactory.SolverType.valueOf(strategy));
        }


        // Check coordinates of starting position.
        if(cmd.hasOption(SolverParameters.START_X.toString())) {
            final Integer xS = Integer.parseInt(cmd.getOptionValue(SolverParameters.START_X.toString()));

            if(xS < 0 || xS >= this.getX1() || xS < this.getX0())
                throw new IllegalArgumentException(SolverParameters.START_X.toString() + " must be a positive integer above -" + SolverParameters.UPPER_LEFT_X.toString() + " and below " + SolverParameters.LOWER_RIGHT_X.toString() + ".");

            super.argValues.put(SolverParameters.START_X.toString(), xS);
        }
        else {
            super.argValues.put(SolverParameters.START_X.toString(), this.getX0());
        }


        if(cmd.hasOption(SolverParameters.START_Y.toString())) {
            final Integer yS = Integer.parseInt(cmd.getOptionValue(SolverParameters.START_Y.toString()));

            if(yS < 0 || yS >= this.getY1() || yS < this.getY0())
                throw new IllegalArgumentException(SolverParameters.START_Y.toString() + " must be a positive integer above -" + SolverParameters.UPPER_LEFT_Y.toString() + " and below " + SolverParameters.LOWER_RIGHT_Y.toString() + ".");

            super.argValues.put(SolverParameters.START_Y.toString(), yS);
        }
        else {
            super.argValues.put(SolverParameters.START_Y.toString(), this.getY0());
        }

        super.argValues.put(SolverParameters.PATH_DASHED.toString(), cmd.hasOption(SolverParameters.PATH_DASHED.toString()));
    }

    @Override
    public void setupCLIOptions() {

        // Mandatory arguments.


        final Option inputImgOption = new Option(SolverParameters.INPUT_IMG_SHORT.toString(), SolverParameters.INPUT_IMG.toString(),true, "path to input gradient image to solve.");
        inputImgOption.setRequired(true);
        super.options.addOption(inputImgOption);

        final Option strategyOption = new Option(SolverParameters.STRATEGY_SHORT.toString(), SolverParameters.STRATEGY.toString(),
                true, "solver strategy can be one of: BFS, DFS or ASTAR.");
        strategyOption.setRequired(true);
        super.options.addOption(strategyOption);


        //// Starting coordinates.

        final Option xSOption = new Option(SolverParameters.START_X.toString(),true, "starting x coordinate (default 0).");
        xSOption.setRequired(false);
        super.options.addOption(xSOption);

        final Option ySOption = new Option(SolverParameters.START_Y.toString(),true, "starting y coordinate (default 0).");
        ySOption.setRequired(false);
        super.options.addOption(ySOption);

        //// Bounding rectangle coordinates.

        final Option x0Option = new Option(SolverParameters.UPPER_LEFT_X.toString(),true, "upper-left x coordinate (default 0).");
        x0Option.setRequired(false);
        super.options.addOption(x0Option);

        final Option y0Option = new Option(SolverParameters.UPPER_LEFT_Y.toString(),true, "upper-left y coordinate (default 0).");
        y0Option.setRequired(false);
        super.options.addOption(y0Option);

        final Option x1Option = new Option(SolverParameters.LOWER_RIGHT_X.toString(),true, "lower-right x coordinate (default equal to image width).");
        x1Option.setRequired(false);
        super.options.addOption(x1Option);

        final Option y1Option = new Option(SolverParameters.LOWER_RIGHT_Y.toString(),true, "lower-right y coordinate (default equal to image height).");
        y1Option.setRequired(false);
        super.options.addOption(y1Option);


        final Option dashOption = new Option(SolverParameters.PATH_DASHED.toString(),
                false, "should solution path be drawn with dashes?");
        dashOption.setRequired(false);
        super.options.addOption(dashOption);


    }

    public SolverArgumentParser(final String[] args) {
        super.setup(args);
    }



    public Integer getX0() {
        return (Integer)super.argValues.get(SolverParameters.UPPER_LEFT_X.toString());
    }

    public Integer getY0() {
        return (Integer)super.argValues.get(SolverParameters.UPPER_LEFT_Y.toString());
    }

    public Integer getX1() {
        return (Integer)super.argValues.get(SolverParameters.LOWER_RIGHT_X.toString());
    }

    public Integer getY1() {
        return (Integer)super.argValues.get(SolverParameters.LOWER_RIGHT_Y.toString());
    }

    public Integer getStartX() {
        return (Integer)super.argValues.get(SolverParameters.START_X.toString());
    }

    public Integer getStartY() {
        return (Integer)super.argValues.get(SolverParameters.START_Y.toString());
    }

    public Boolean drawPathDashed() { return (Boolean)super.argValues.get(SolverParameters.PATH_DASHED.toString()); }

    public String getInputImage() {
        return (String)super.argValues.get(SolverParameters.INPUT_IMG.toString());
    }

    public SolverFactory.SolverType getSolverStrategy() {
        return (SolverFactory.SolverType)super.argValues.get(SolverArgumentParser.SolverParameters.STRATEGY.toString());
    }
}
