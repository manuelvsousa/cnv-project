package pt.ulisboa.tecnico.cnv.webserver;

import com.sun.net.httpserver.HttpServer;

import java.net.InetSocketAddress;
import java.util.concurrent.Executors;

public class WebServer {
	public static void main(final String[] args) throws Exception {
		//final HttpServer server = HttpServer.create(new InetSocketAddress("127.0.0.1", 8080), 0);
		final HttpServer server = HttpServer.create(new InetSocketAddress(8080), 0);
		server.createContext("/climb", new WebServerHandler());

		// be aware! infinite pool of threads!
		server.setExecutor(Executors.newCachedThreadPool());
		server.start();

		System.out.println(server.getAddress().toString());
	}

}

