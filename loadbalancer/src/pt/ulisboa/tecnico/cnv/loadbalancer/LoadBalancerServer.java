package pt.ulisboa.tecnico.cnv.loadbalancer;

import com.amazonaws.services.ec2.model.Instance;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import pt.ulisboa.tecnico.cnv.dto.Request;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Executors;

public class LoadBalancerServer{
	private static HashMap<Instance, List<Request>> requests;
	private static InstanceManager instanceManager = new InstanceManager();

	public static void main(final String[] args) throws Exception {
		final HttpServer server = HttpServer.create(new InetSocketAddress("127.0.0.1", 8000), 0);
		//final HttpServer server = HttpServer.create(new InetSocketAddress(8000), 0);
		server.createContext("/climb", new MyHandler());

		// be aware! infinite pool of threads!
		server.setExecutor(Executors.newCachedThreadPool());
		server.start();

		System.out.println(server.getAddress().toString());
	}

	static class MyHandler implements HttpHandler {
		@Override
		public void handle(final HttpExchange t) throws IOException {

		}
	}

	/**
	 * Select the instance that we want to send the request to (main algorithm)
	 * @param request request to be sent and processed at the instance
	 * @return instance
	 */
	private static Instance selectInstanceForRequest(Request request){
		// temporary -> select instance with least # of requests running
		return getInstanceLeastRunningRequests();
	}

	/**
	 * Get the instance with the lowest number of requests running concurrently
	 * @return instance
	 */
	private static Instance getInstanceLeastRunningRequests(){
		Set<Instance> instances = instanceManager.getInstances();
		Instance minInstance = null;
		int minCount = Integer.MAX_VALUE;
		for(Instance instance : instances){
			int runningCount = countRunningRequestsAtInstance(instance);
			if(runningCount < minCount){
				minCount = runningCount;
				minInstance = instance;
			}
		}
		return minInstance;
	}

	/**
	 * Count number of running requests at a specific ec2 instance
	 * @param instance ec2 instance
	 * @return number of running requests
	 */
	private static int countRunningRequestsAtInstance(Instance instance){
		int count = 0;
		List<Request> instanceRequests = requests.get(instance);
		if(instanceRequests != null){
			for(Request req : instanceRequests){
				if(req.getRequestStatus() == Request.Status.RUNNING){
					count++;
				}
			}
		}
		return count;

	}




}

