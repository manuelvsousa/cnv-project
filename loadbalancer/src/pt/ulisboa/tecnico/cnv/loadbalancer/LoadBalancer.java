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
import pt.ulisboa.tecnico.cnv.lib.request.Point;
import pt.ulisboa.tecnico.cnv.lib.request.Request;
import pt.ulisboa.tecnico.cnv.mss.MSSClient;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.*;
import java.net.InetSocketAddress;
import java.net.URL;
import java.util.*;
import java.util.concurrent.Executors;

public class LoadBalancer {
	private static Map<Instance, List<Request>> runningRequests = new HashMap<>();
	private static InstanceManager instanceManager = new InstanceManager();
	private static boolean isTestingLocally = false;

	public static void main(final String[] args) throws Exception {
		final HttpServer server = HttpServer.create(new InetSocketAddress(8000), 0);

		server.createContext("/climb", new ClimbHandler());
		server.createContext("/requestStatus", new RequestStatusHandler());

		// be aware! infinite pool of threads!
		server.setExecutor(Executors.newCachedThreadPool());
		server.start();

		// if testing on single machine use localhost ip's
		if(args.length == 1 && args[0].equals("-localhost")){
			isTestingLocally = true;
			System.out.println("Running Loadbalancer on localhost.");
		}else{
			// clear previous tags and set loadbalancer tag for instance identification
			Instance instance = instanceManager.getInstanceById(EC2MetadataUtils.getInstanceId());
			instanceManager.clearInstanceTags(instance);
			instanceManager.tagInstanceAsLoadBalancer(instance);
		}
		System.out.println(server.getAddress().toString());
	}

	/**
	 * Request entry point for load balancing
	 */
	static class ClimbHandler implements HttpHandler {
		public void handle(final HttpExchange t) throws IOException {
			// create request object
			Request request = (new QueryParser(t.getRequestURI().getQuery())).getRequest();
			long estimatedComplexity = estimateComplexity(request);
			request.setEstimatedComplexity(estimatedComplexity);

			// select an instance to send this request to and get it's ip
			String ip = "";
			if(isTestingLocally){
				ip = "localhost";
			}else{
				Instance instance = selectInstanceForRequest(request);
				System.out.println("selected instance id = " + instance.getInstanceId());
				ip = instance.getPrivateIpAddress();
				System.out.println("ip=" + ip);

				// store request in the hashmap for this instance
				storeRequest(request, instance);
			}

			//String ip = "localhost";
			String redirectUrl = HttpUtil.buildUrl(ip, 8080);
			System.out.println("Redirecting to " + redirectUrl);

			BufferedImage bufferedImage = doGET(redirectUrl, t.getRequestURI().getQuery().toString()+
					"&estimatedComplexity="+request.getEstimatedComplexity());
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
	 * Handle progress reports from worker instances,
	 * for example instance X is aproximately halfway done on request Y
	 * also includes progress = 1 (request finished)
	 */
	static class RequestStatusHandler implements HttpHandler {

		@Override
		public void handle(final HttpExchange t) throws IOException {
			System.out.println("RequestStatusHandler ran");
			// Get the query.
			final String query = t.getRequestURI().getQuery();
			QueryParser queryParser = new QueryParser(query);
			Request request = queryParser.getRequest();

			InstanceManager instanceManager = new InstanceManager();
			Instance instance = instanceManager.getInstanceById(queryParser.getInstanceId());
			updateRequestById(request, instance);

			if(request.getProgress() == 1){
				// remove from running requests
				removeRequestById(request, instance);

				MSSClient.getInstance().addMetrics(
						request.getId(),
						request.getSearchAlgorithm().toString(),
						request.getDataset(),
						request.getStartingPoint(),
						request.getPoint0(),
						request.getPoint1(),
						Long.toString(request.getMeasuredComplexity())
				);
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
		 	if(requests == null){
				requests = new ArrayList<>();	
				runningRequests.put(instance, requests);
				continue;
			}	

			System.out.println("requests: " + requests);
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

		if(selectedInstance == null){
			return instances.iterator().next();
		}

		return selectedInstance;
	}


	/**
	 * Estimate the complexity that would be obtained after measuring the request execution with instrumentation
	 * in a web server instance.
	 */
	public static long estimateComplexity(Request request){
		// get metrics of similar requests with same search algo and dataset
		List<Request> recentRequests = MSSClient.getInstance().getMetrics(request.getSearchAlgorithm(),
				request.getDataset());

		if(recentRequests.size() != 0){
			// get the request for this search algo and dataset where starting point is the closest to the
			// starting point of the request currently being estimated.
			int minDist = Integer.MAX_VALUE;
			Request closestStartPointRequest = recentRequests.get(0);
			for(Request req : recentRequests){
				int dist = getDistanceBetweenPoints(request.getStartingPoint(), req.getStartingPoint());
				if(dist < minDist){
					closestStartPointRequest = req;
					minDist = dist;
				}
			}
			return closestStartPointRequest.getMeasuredComplexity();
		}
		return 0;
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
				requests.get(i).setProgress(request.getProgress());
				requests.get(i).setMeasuredComplexity(request.getMeasuredComplexity());
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
		} catch (Exception e) {
			e.printStackTrace();
		}
		return image;
	}

	private static int getDistanceBetweenPoints(Point a, Point b){
		int xDiff = b.getX() - a.getX();
		int yDiff = b.getY() - a.getY();

		return (int) Math.sqrt(Math.pow(xDiff,2) + Math.pow(yDiff, 2));
	}


}

