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
	public static final String instanceId = EC2MetadataUtils.getInstanceId();
	public static final InstanceManager instanceManager = new InstanceManager();
	public static int requestCount = 0;

	private static boolean isTestingLocally = false;

	public static void main(final String[] args) throws Exception {
		//final HttpServer server = HttpServer.create(new InetSocketAddress("127.0.0.1", 8080), 0);
		final HttpServer server = HttpServer.create(new InetSocketAddress(8080), 0);
		server.createContext("/climb", new WebServerHandler());
		server.createContext("/countRequests", new WebServerRequestCountHandler());
		server.createContext("/healthCheck", new WebServerHealthCheckHandler());

		// be aware! infinite pool of threads!
		server.setExecutor(Executors.newCachedThreadPool());
		server.start();

		if(args.length == 1 && args[0].equals("-localhost")){
			isTestingLocally = true;
			System.out.println("Running webserver locally");
		}else{
			// clear previous tags and set loadbalancer tag for instance identification
			Instance instance = instanceManager.getInstanceById(EC2MetadataUtils.getInstanceId());
			instanceManager.clearInstanceTags(instance);
			instanceManager.tagInstanceAsWorker(instance);
		}

		System.out.println(server.getAddress().toString());
	}

	static class WebServerRequestCountHandler implements HttpHandler {
		@Override
		public void handle(HttpExchange t) throws IOException {
			final Headers hdrs = t.getResponseHeaders();
			String toSend = Integer.toString(requestCount);
			t.sendResponseHeaders(200, toSend.length());
			hdrs.add("Content-Type", "text/plain");
			final OutputStream os = t.getResponseBody();
			os.write(toSend.getBytes());
			os.close();
		}
	}

	static class WebServerHealthCheckHandler implements HttpHandler {
		@Override
		public void handle(HttpExchange t) throws IOException {
			final Headers hdrs = t.getResponseHeaders();
			String toSend = "I am alive!";
			t.sendResponseHeaders(200, toSend.length());
			hdrs.add("Content-Type", "text/plain");
			final OutputStream os = t.getResponseBody();
			os.write(toSend.getBytes());
			os.close();
		}
	}

}

