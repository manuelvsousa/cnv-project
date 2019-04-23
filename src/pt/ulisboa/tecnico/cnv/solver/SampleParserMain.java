package pt.ulisboa.tecnico.cnv.solver;

public class SampleParserMain {
    public static void main(final String[] args) {
        // Get user-provided flags.
        final SolverArgumentParser ap = new SolverArgumentParser(args);

        // Specified starting point.
        int startingX = ap.getStartX();
        int startingY = ap.getStartY();
        System.out.println(String.format("> Starting point:\t\t(%d,% d)", startingX, startingY));

        // Upper-left point coordinates.
        int upperLeftX = ap.getX0();
        int upperLeftY = ap.getY0();
        System.out.println(String.format("> Rectangle upper-left point:\t(%d,% d)", upperLeftX, upperLeftY));

        // Lower-right point coordinates.
        int lowerRightX = ap.getX1();
        int lowerRightY = ap.getY1();
        System.out.println(String.format("> Rectangle lower-right point:\t(%d,% d)", lowerRightX, lowerRightY));

        // Get the strategy type.
        String st = ap.getSolverStrategy().toString();
        System.out.println("> Search strategy:\t\t" + st);

        // Get input map.
        String input = ap.getInputImage();
        System.out.println("> Image:\t\t\t" + input);
    }
}
