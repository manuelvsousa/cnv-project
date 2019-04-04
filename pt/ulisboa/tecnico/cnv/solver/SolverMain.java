package pt.ulisboa.tecnico.cnv.solver;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.*;
import java.nio.file.Path;
import java.nio.file.Paths;

public class SolverMain {

    public static void main(final String[] args) {

        // Get user-provided flags.
        final SolverArgumentParser ap = new SolverArgumentParser(args);

        // Create solver instance from factory.
        final Solver s = SolverFactory.getInstance().makeSolver(ap);

        // Write figure file to disk.
        try {

            final BufferedImage outputImg = s.solveImage();

            final String outPath = ap.getOutputDirectory();

            final String imageName = s.toString();

            if(ap.isDebugging()) {
                System.out.println("> Image name: " + imageName);
            }

            final Path imagePathPNG = Paths.get(outPath, imageName);
            ImageIO.write(outputImg, "png", imagePathPNG.toFile());

        } catch (final FileNotFoundException e) {
            e.printStackTrace();
        } catch (final IOException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }


    }
}
