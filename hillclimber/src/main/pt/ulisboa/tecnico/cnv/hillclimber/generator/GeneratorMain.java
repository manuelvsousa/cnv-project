package pt.ulisboa.tecnico.cnv.generator;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.*;
import java.nio.file.Path;
import java.nio.file.Paths;

public class GeneratorMain {

    public static void main(final String[] args) {

        // Get user-provided flags.
        final GeneratorArgumentParser ap = new GeneratorArgumentParser(args);

        // Create generator instance from factory.
        final Generator gen = GeneratorFactory.getInstance().makeGenerator(ap);

        // Produce surface and generate heat-map buffer.
        final BufferedImage img = gen.buildImage();


        try
        {
            final String outPath = ap.getOutputDirectory();

            // Write figure file to disk.
            final String imageName = gen.toString();
            final Path imagePath = Paths.get(outPath, imageName);
            if(ap.isDebugging()) {
                System.out.println("> Writing image to file:\n\t" + imagePath.toString());
            }

            ImageIO.write(img, "png", imagePath.toFile());

            // Write serialized version of the data to .dat file.
            final String serializedDataFileName = imageName.replace(Generator.IMAGE_FILE_TYPE, ".dat");
            final Path serializedDataPath = Paths.get(outPath, serializedDataFileName);
            if(ap.isDebugging()) {
                System.out.println("> Writing serialized data matrix to file:\n\t" + serializedDataPath.toString());
            }
            final FileOutputStream fos = new FileOutputStream(serializedDataPath.toFile());
            final ObjectOutputStream oos = new ObjectOutputStream(fos);
            oos.writeObject(gen.getData());

        } catch (final FileNotFoundException e) {
            e.printStackTrace();
        } catch (final IOException e) {
            e.printStackTrace();
        }


    }
}
