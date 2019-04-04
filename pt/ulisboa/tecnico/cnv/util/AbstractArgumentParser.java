package pt.ulisboa.tecnico.cnv.util;

import org.apache.commons.cli.*;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

public abstract class AbstractArgumentParser implements ArgumentParser {
    protected final Options options = new Options();
    protected final Map<String, Object> argValues = new HashMap<>();

    static protected final Integer DEFAULT_WIDTH = 512;
    static protected final Integer DEFAULT_HEIGHT = 512;

    protected final CommandLineParser parser = new DefaultParser();
    protected final HelpFormatter formatter = new HelpFormatter();
    protected CommandLine cmd = null;

    public enum GenericParameters {
        /**
         * Set debug mode.
         */
        DEBUG_SHORT("d"), DEBUG("debug"),
        OUTPUT_DIR_SHORT("o"), OUTPUT_DIR("output-directory"),
        WIDTH_SHORT("w"), WIDTH("width"),
        HEIGHT_SHORT("h"), HEIGHT("height"),
        GRADIENT_SHORT("g"), GRADIENT("gradient");

        private final String text;
        GenericParameters(final String text) {
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

    protected final void setup(final String[] args) {
        

        final Option outputDirOption = new Option(GenericParameters.OUTPUT_DIR_SHORT.toString(), GenericParameters.OUTPUT_DIR.toString(),
                true, "output directory for generated images. By omission it is the system's temp directory.");
        outputDirOption.setRequired(false);
        this.options.addOption(outputDirOption);

        final Option debugOption = new Option(GenericParameters.DEBUG_SHORT.toString(), GenericParameters.DEBUG.toString(),
                false, "set debug mode.");
        debugOption.setRequired(false);
        this.options.addOption(debugOption);

        final Option widthOption = new Option(GenericParameters.WIDTH_SHORT.toString(), GenericParameters.WIDTH.toString(),
                true, "generated image width (default 512 pixels).");
        widthOption.setRequired(false);
        this.options.addOption(widthOption);

        final Option heightOption = new Option(GenericParameters.HEIGHT_SHORT.toString(), GenericParameters.HEIGHT.toString(),
                true, "generated image height (default 512 pixels).");
        heightOption.setRequired(false);
        this.options.addOption(heightOption);

        final Option gradientOption = new Option(GenericParameters.GRADIENT_SHORT.toString(), GenericParameters.GRADIENT.toString(),
                true, "output image's color gradient.");
        gradientOption.setRequired(false);
        this.options.addOption(gradientOption);

        // Prepare program-specific options provided in a subclass of this one.
        setupCLIOptions();

        System.out.println("> [AbstractArgumentParser]: setupCLIOptions() DONE.");

        // Set values
        try {
            this.cmd = this.parser.parse(this.options, args);
        } catch (ParseException e) {
            System.out.println("> [AbstractArgumentParser]: ParseException.");
            System.out.println(e.getMessage());
            this.formatter.printHelp("utility-name", this.options);
            System.exit(1);
        }

        // Process output directory argument.
        String outDirPath = System.getProperty("java.io.tmpdir");
        if(this.cmd.hasOption(GenericParameters.OUTPUT_DIR.toString())) {
            outDirPath = this.cmd.getOptionValue(GenericParameters.OUTPUT_DIR.toString()).replace("'", "");
            File outDirHandle = new File(outDirPath).getAbsoluteFile();
            if(!outDirHandle.exists()) {
                boolean result = false;
                try{
                    result = outDirHandle.mkdir();
                }
                catch(SecurityException se){
                    System.out.println("Error creating output directory:" + outDirPath);
                    se.printStackTrace();
                    System.exit(1);
                }
                if(result) {
                    System.out.println("Created output directory:\t" + outDirPath);
                }
                else if (outDirHandle.exists()) {
                    System.out.println("Output directory already existed:\t" + outDirPath);
                }
                else {
                    System.out.println("Create directory failed:\t " + outDirPath);
                    System.exit(1);
                }
                argValues.put(GenericParameters.OUTPUT_DIR.toString(), outDirPath);
            }
            else if(!outDirHandle.isDirectory()) {
                System.out.println("The given output directory path was a file but should be a directory:");
                System.out.println(outDirPath);
                System.out.println("Exiting.");
                System.exit(1);
            }
            else {
                this.argValues.put(GenericParameters.OUTPUT_DIR.toString(), outDirPath);
            }
        }
        else {
            final String tempDir = System.getProperty("java.io.tmpdir");
            argValues.put(GenericParameters.OUTPUT_DIR.toString(), tempDir);
        }

        // Process width and height parameters.
        if(this.cmd.hasOption(GenericParameters.WIDTH.toString())) {
            final Integer w = Integer.parseInt(this.cmd.getOptionValue(GenericParameters.WIDTH.toString()));

            if(w <= 0)
                throw new IllegalArgumentException(GenericParameters.WIDTH.toString() + " must be a positive integer.");

            this.argValues.put(GenericParameters.WIDTH.toString(), w);
        }
        else {
            this.argValues.put(GenericParameters.WIDTH.toString(), AbstractArgumentParser.DEFAULT_WIDTH);
        }

        if(this.cmd.hasOption(GenericParameters.HEIGHT.toString())) {
            final Integer h = Integer.parseInt(this.cmd.getOptionValue(GenericParameters.HEIGHT.toString()));

            if(h <= 0)
                throw new IllegalArgumentException(GenericParameters.HEIGHT.toString() + " must be a positive integer.");

            this.argValues.put(GenericParameters.HEIGHT.toString(), h);
        }
        else {
            this.argValues.put(GenericParameters.HEIGHT.toString(), AbstractArgumentParser.DEFAULT_HEIGHT);
        }

        if(this.cmd.hasOption(GenericParameters.GRADIENT.toString())) {
            final String gradient = this.cmd.getOptionValue(GenericParameters.GRADIENT.toString());

            if( ! Gradient.GradientType.isValid(gradient) ) {
                throw new IllegalArgumentException(gradient + " is an invalid gradient.");
            }

            this.argValues.put(GenericParameters.GRADIENT.toString(), Gradient.GradientType.valueOf(gradient));
            ImageBuilder.colors = Gradient.getGradientFromEnum(Gradient.GradientType.valueOf(gradient));
        }
        else {
            this.argValues.put(GenericParameters.GRADIENT.toString(), Gradient.GradientType.RAINBOW);
            ImageBuilder.colors = Gradient.RAINBOW;
        }

        // Parse program-specific arguments provided in a subclass of this one.
        parseValues(args);

        this.argValues.put(GenericParameters.DEBUG.toString(), cmd.hasOption(GenericParameters.DEBUG.toString()));
        if(this.cmd.hasOption(GenericParameters.DEBUG.toString())) {
            for(Map.Entry<String, Object> param : this.argValues.entrySet()) {
                System.out.println(param.getKey() + "\t" + param.getValue().toString());
            }
            System.out.println("\n");
        }
    }

    public abstract void parseValues(final String[] args);
    public abstract void setupCLIOptions();


    public Boolean isDebugging() {
        return (Boolean)this.argValues.get(GenericParameters.DEBUG.toString());
    }

    public Integer getWidth() {
        return (Integer)this.argValues.get(GenericParameters.WIDTH.toString());
    }

    public Integer getHeight() {
        return (Integer)this.argValues.get(GenericParameters.HEIGHT.toString());
    }

    public String getOutputDirectory() {
        return (String)this.argValues.get(GenericParameters.OUTPUT_DIR.toString());
    }
}
