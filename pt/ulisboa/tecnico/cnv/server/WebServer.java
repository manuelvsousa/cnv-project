package pt.ulisboa.tecnico.cnv.server;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.Executors;

import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import pt.ulisboa.tecnico.cnv.solver.Solver;
import pt.ulisboa.tecnico.cnv.solver.SolverArgumentParser;
import pt.ulisboa.tecnico.cnv.solver.SolverFactory;

import javax.imageio.ImageIO;

public class WebServer {

	public static void main(final String[] args) throws Exception {
		final HttpServer server = HttpServer.create(new InetSocketAddress(8000), 0);
		server.createContext("/climb", new MyHandler());

		// be aware! infinite pool of threads!
		server.setExecutor(Executors.newCachedThreadPool());
		server.start();

		System.out.println(server.getAddress().toString());
	}

	static class MyHandler implements HttpHandler {
		@Override
		public void handle(final HttpExchange t) throws IOException {

			// Get the query.
			final String query = t.getRequestURI().getQuery();

			System.out.println("> Query:\t" + query);

			// Break it down into String[].
			final String[] params = query.split("&");

			/*
			for(String p: params) {
				System.out.println(p);
			}
			*/

			// Store as if it was a direct call to SolverMain.
			final ArrayList<String> newArgs = new ArrayList<>();
			for (final String p : params) {
				final String[] splitParam = p.split("=");
				newArgs.add("-" + splitParam[0]);
				newArgs.add(splitParam[1]);

				/*
				System.out.println("splitParam[0]: " + splitParam[0]);
				System.out.println("splitParam[1]: " + splitParam[1]);
				*/
			}

			newArgs.add("-d");

			// Store from ArrayList into regular String[].
			final String[] args = new String[newArgs.size()];
			int i = 0;
			for(String arg: newArgs) {
				args[i] = arg;
				i++;
			}

			/*
			for(String ar : args) {
				System.out.println("ar: " + ar);
			} */

			SolverArgumentParser ap = null;
			try {
				// Get user-provided flags.
				ap = new SolverArgumentParser(args);
			}
			catch(Exception e) {
				System.out.println(e);
				return;
			}

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

}
