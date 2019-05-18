package pt.ulisboa.tecnico.cnv.loadbalancer;

import com.amazonaws.services.ec2.model.Instance;
import com.amazonaws.util.EC2MetadataUtils;
import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import pt.ulisboa.tecnico.cnv.lib.ec2.InstanceManager;
import pt.ulisboa.tecnico.cnv.lib.http.HttpUtil;
import pt.ulisboa.tecnico.cnv.lib.query.QueryParser;
import pt.ulisboa.tecnico.cnv.lib.request.Request;
import pt.ulisboa.tecnico.cnv.mssclient.MSSClient;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.*;
import java.net.InetSocketAddress;
import java.net.URL;
import java.net.URLConnection;
import java.util.*;
import java.util.concurrent.Executors;

public class LoadBalancer {
	private static HashMap<Instance, List<Request>> runningRequests;
	private static InstanceManager instanceManager = new InstanceManager();
	private static MSSClient mssClient;
	private static int mssPort = 8001;
	private static boolean isTestingLocally = false;

	public static void main(final String[] args) throws Exception {
		final HttpServer server = HttpServer.create(new InetSocketAddress(8000), 0);

		// if testing on single machine use localhost ip's
		if(args.length == 1 && args[0].equals("-localhost")){
			isTestingLocally = true;
			System.out.println("Running Loadbalancer on localhost.");
			mssClient = new MSSClient("localhost", mssPort);
		}else{
			Instance mssInstance = instanceManager.getMSSInstance();
			String mssIp = mssInstance.getPrivateIpAddress();
			mssClient = new MSSClient(mssIp, mssPort);
			System.out.println("Created MSS client talking to server at: " + mssIp +":" + mssPort);
		}

		server.createContext("/climb", new ClimbHandler());
		server.createContext("/requestStatus", new RequestStatusHandler());

		// be aware! infinite pool of threads!
		server.setExecutor(Executors.newCachedThreadPool());
		server.start();

		// clear previous tags and set loadbalancer tag for instance identification
		Instance instance = instanceManager.getInstanceById(EC2MetadataUtils.getInstanceId());
		instanceManager.clearInstanceTags(instance);
		instanceManager.tagInstanceAsLoadBalancer(instance);

		System.out.println(server.getAddress().toString());
	}

	/**
	 * Request entry point for load balancing
	 */
	static class ClimbHandler implements HttpHandler {
		public void handle(final HttpExchange t) throws IOException {
			// create request object
			Request request = (new QueryParser(t.getRequestURI().getQuery())).getRequest();
			int estimatedComplexity = calculateComplexityEstimate(request);
			request.setEstimatedComplexity(estimatedComplexity);

			// select an instance to send this request to and get it's ip
			String ip = "";
			if(isTestingLocally){
				ip = "localhost";
			}else{
				Instance instance = selectInstanceForRequest(request);
				ip = instance.getPrivateIpAddress();
				System.out.println("ip=" + ip);

				// store request in the hashmap for this instance
				storeRequest(request, instance);
				System.out.println("storing request");
			}

			//String ip = "localhost";
			String redirectUrl = HttpUtil.buildUrl(ip, 8080);
			System.out.println("redirecting to " + redirectUrl);

			BufferedImage bufferedImage = doGET(redirectUrl, t.getRequestURI().getQuery().toString());
			int imageSize = getBufferedImageSize(bufferedImage);

			OutputStream os = t.getResponseBody();
			final Headers hdrs = t.getResponseHeaders();
			t.sendResponseHeaders(200, imageSize);
			hdrs.add("Content-Type", "image/png");
			hdrs.add("Access-Control-Allow-Origin", "*");
			hdrs.add("Access-Control-Allow-Credentials", "true");
			hdrs.add("Access-Control-Allow-Methods", "POST, GET, HEAD, OPTIONS");
			hdrs.add("Access-Control-Allow-Headers", "Origin, Accept, X-Requested-With, Content-Type, Access-Control-Request-Method, Access-Control-Request-Headers");
			ImageIO.write(bufferedImage, "png", os);
			os.close();

			System.out.println("> Sent response to " + t.getRemoteAddress().toString());
		}
	}

	/**
	 * Handle progress reports from worker instances
	 */
	static class RequestStatusHandler implements HttpHandler {

		@Override
		public void handle(final HttpExchange t) throws IOException {
			// Get the query.
			final String query = t.getRequestURI().getQuery();
			QueryParser queryParser = new QueryParser(query);
			Request request = queryParser.getRequest();

			InstanceManager instanceManager = new InstanceManager();
			Instance instance = instanceManager.getInstanceById(queryParser.getInstanceId());

			if(request.getProgress() == 1){
				// remove from runing requests
				removeRequestById(request, instance);

				// TODO send to MSS


			}else{
				// replace current request to have progress updated for loadbalancing decisions
				updateRequestById(request, instance);
			}
		}
	}

	/**
	 * Select the instance that we want to send the request to
	 */
	private static Instance selectInstanceForRequest(Request request){
		Instance selectedInstance = null;
		int lowestComplexity = Integer.MAX_VALUE;
		int curSum = 0;
		Set<Instance> instances = instanceManager.getWorkerInstances();
		for(Instance instance: instances){
			List<Request> requests = runningRequests.get(instance);
			// sum estimated time complexities of all requests
			// any running request has estimated values, except 0 if no recent data exists
			for(Request req : requests){
				// Sum estimated complexities taking also into account the progress of the request
				// progress is a double value ranging from 0 to 1
				curSum += (int) (req.getEstimatedComplexity() * (1-req.getProgress()));
			}

			if(curSum < lowestComplexity){
				lowestComplexity = curSum;
				selectedInstance = instance;
			}
			curSum = 0;
		}

		return selectedInstance;
	}


	public static int calculateComplexityEstimate(Request request){
		// get metrics of similar requests and estimate complexity of this request
		// TODO get recently ran requests through MSS with same algorithm and dataset
		List<Request> recentRequests = new ArrayList<>();
		if(recentRequests.size() == 0) return 0;
		int complexitySum = 0;
		for(Request req : recentRequests){
			complexitySum += req.getMeasuredComplexity();
		}
		// average out
		return complexitySum / recentRequests.size();
	}

	private static void storeRequest(Request request, Instance instance){
		List<Request> requestList = runningRequests.get(instance);
		if(requestList == null){
			requestList = new ArrayList<Request>();
		}
		requestList.add(request);
		runningRequests.put(instance, requestList);

	}

	private static void updateRequestById(Request request, Instance instance){
		List<Request> requests = runningRequests.get(instance);
		for(int i = 0; i < requests.size(); i++){
			if(requests.get(i).getId() == request.getId()){
				requests.remove(i);
				requests.add(request);
			}
		}
		runningRequests.remove(instance);
		runningRequests.put(instance, requests);
	}

	private static void removeRequestById(Request request, Instance instance){
		List<Request> requests = runningRequests.get(instance);
		for(int i = 0; i < requests.size(); i++){
			if(requests.get(i).getId() == request.getId()){
				requests.remove(i);
			}
		}
		runningRequests.remove(instance);
		runningRequests.put(instance, requests);
	}


	private static int getBufferedImageSize(BufferedImage bufferedImage){
		try{
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			ImageIO.write(bufferedImage, "png" ,baos);
			return baos.toByteArray().length;
		}catch(IOException ioe){
			ioe.printStackTrace();
		}
		return 0;
	}

	private static BufferedImage doGET(String targetUrl, String urlParameters) {
		BufferedImage image = null;
		try {
			URL url = new URL(targetUrl + "?" + urlParameters);
			image = ImageIO.read(url);

			// TESTING
			URLConnection connection = url.openConnection();
			connection.connect();;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return image;
	}


}

