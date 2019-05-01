package pt.ulisboa.tecnico.cnv.webserver;

import com.amazonaws.services.ec2.model.Instance;
import com.amazonaws.util.EC2MetadataUtils;
import com.sun.net.httpserver.HttpServer;
import pt.ulisboa.tecnico.cnv.lib.ec2.InstanceManager;

import java.net.InetSocketAddress;
import java.util.concurrent.Executors;

public class WebServer {
	private static InstanceManager instanceManager = new InstanceManager();

	public static void main(final String[] args) throws Exception {
		//final HttpServer server = HttpServer.create(new InetSocketAddress("127.0.0.1", 8080), 0);
		final HttpServer server = HttpServer.create(new InetSocketAddress(8080), 0);
		server.createContext("/climb", new WebServerHandler());

		// be aware! infinite pool of threads!
		server.setExecutor(Executors.newCachedThreadPool());
		server.start();

		// clear previous tags and set loadbalancer tag for instance identification
		Instance instance = instanceManager.getInstanceById(EC2MetadataUtils.getInstanceId());
		instanceManager.clearInstanceTags(instance);
		instanceManager.tagInstanceAsWorker(instance);

		System.out.println(server.getAddress().toString());
	}

}

