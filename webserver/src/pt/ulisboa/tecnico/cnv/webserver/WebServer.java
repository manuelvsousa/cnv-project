package pt.ulisboa.tecnico.cnv.webserver;

import com.amazonaws.services.ec2.model.Instance;
import com.amazonaws.util.EC2MetadataUtils;
import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import pt.ulisboa.tecnico.cnv.lib.ec2.InstanceManager;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.util.concurrent.Executors;

public class WebServer {
	public static final InstanceManager instanceManager = new InstanceManager();
	public static int requestCount = 0;

	private static boolean isTestingLocally = false;

	public static void main(final String[] args) throws Exception {
		//final HttpServer server = HttpServer.create(new InetSocketAddress("127.0.0.1", 8080), 0);
		final HttpServer server = HttpServer.create(new InetSocketAddress(8080), 0);
		server.createContext("/climb", new WebServerHandler());
		server.createContext("/countRequests", new WebServerRequestCountHandler());

		// be aware! infinite pool of threads!
		server.setExecutor(Executors.newCachedThreadPool());
		server.start();

		if(args.length == 1 && args[0].equals("-localhost")){
			isTestingLocally = true;
			System.out.println("Running webserver locally");
		}else{
			// clear previous tags and set loadbalancer tag for instance identification
			Instance instance = instanceManager.getInstanceById(EC2MetadataUtils.getInstanceId());
			System.out.println("instance to clear: "+ instance);
			instanceManager.clearInstanceTags(instance);
			instanceManager.tagInstanceAsWorker(instance);
		}

		System.out.println(server.getAddress().toString());
	}

	static class WebServerRequestCountHandler implements HttpHandler {
		@Override
		public void handle(HttpExchange t) throws IOException {
			final Headers hdrs = t.getResponseHeaders();
			t.sendResponseHeaders(200, 4);
			hdrs.add("Content-Type", "image/png");
			hdrs.add("Access-Control-Allow-Origin", "*");
			hdrs.add("Access-Control-Allow-Credentials", "true");
			hdrs.add("Access-Control-Allow-Methods", "POST, GET, HEAD, OPTIONS");
			hdrs.add("Access-Control-Allow-Headers", "Origin, Accept, X-Requested-With, Content-Type, Access-Control-Request-Method, Access-Control-Request-Headers");
			final OutputStream os = t.getResponseBody();
			os.write(requestCount);
			os.close();
		}
	}


}

