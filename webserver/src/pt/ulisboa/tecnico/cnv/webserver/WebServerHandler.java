package pt.ulisboa.tecnico.cnv.webserver;

import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import pt.ulisboa.tecnico.cnv.hillclimber.solver.Solver;
import pt.ulisboa.tecnico.cnv.hillclimber.solver.SolverArgumentParser;
import pt.ulisboa.tecnico.cnv.hillclimber.solver.SolverFactory;
import pt.ulisboa.tecnico.cnv.lib.query.QueryParser;
import pt.ulisboa.tecnico.cnv.lib.request.Request;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class WebServerHandler implements HttpHandler {
    public static ThreadLocal<Request> request = new ThreadLocal<Request>();
    public void handle(final HttpExchange t) throws IOException {

        // Get the query.
        final String query = t.getRequestURI().getQuery();
        QueryParser queryParser = new QueryParser(query);
        SolverArgumentParser ap = queryParser.getSolverArgumentParser();
        request.set(queryParser.getRequest());
        System.out.println("> Finished parsing args.");


        // Create solver instance from factory.
        final Solver s = SolverFactory.getInstance().makeSolver(ap);

        // Write figure file to disk.
        File responseFile = null;
        try {
            final BufferedImage outputImg = s.solveImage();
            final String outPath = ap.getOutputDirectory();
            final String imageName = s.toString();
            if(ap.isDebugging()) {
                System.out.println("> Image name: " + imageName);
            }
            final Path imagePathPNG = Paths.get(outPath, imageName);
            ImageIO.write(outputImg, "png", imagePathPNG.toFile());
            responseFile = imagePathPNG.toFile();
        } catch (final FileNotFoundException e) {
            e.printStackTrace();
        } catch (final IOException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }

        // Send response to browser.
        final Headers hdrs = t.getResponseHeaders();
        t.sendResponseHeaders(200, responseFile.length());
        hdrs.add("Content-Type", "image/png");
        hdrs.add("Access-Control-Allow-Origin", "*");
        hdrs.add("Access-Control-Allow-Credentials", "true");
        hdrs.add("Access-Control-Allow-Methods", "POST, GET, HEAD, OPTIONS");
        hdrs.add("Access-Control-Allow-Headers", "Origin, Accept, X-Requested-With, Content-Type, Access-Control-Request-Method, Access-Control-Request-Headers");
        final OutputStream os = t.getResponseBody();
        Files.copy(responseFile.toPath(), os);

        os.close();

        System.out.println("> Sent response to " + t.getRemoteAddress().toString());
    }

}