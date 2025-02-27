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
			String query = t.getRequestURI().getQuery();
			System.out.println("Query: " + query);
			// create request object
			Request request = (new QueryParser(query)).getRequest();
			long estimatedComplexity = estimateComplexity(request);
			request.setEstimatedComplexity(estimatedComplexity);

			Random random = new Random();
			request.setId(random.nextInt());

			// select an instance to send this request to and get it's ip
			String ip = "";
			if(isTestingLocally){
				ip = "localhost";
			}else{
				Instance instance = selectInstanceForRequest(request);
				ip = instance.getPrivateIpAddress();

				// store request in the hashmap for this instance
				storeRequest(request, instance);
			}

			//String ip = "localhost";
			String redirectUrl = HttpUtil.buildUrl(ip, 8080) + "/climb";
			System.out.println("Redirecting to " + redirectUrl);

			BufferedImage bufferedImage = doGET(redirectUrl, t.getRequestURI().getQuery().toString()+
					"&estimatedComplexity="+request.getEstimatedComplexity()+"&reqid="+request.getId());
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
			System.out.println("REQUESTSTATUSHANDLER parsed the query progress update from webserver.");
			// Get the query.
			final String query = t.getRequestURI().getQuery();
			QueryParser queryParser = new QueryParser(query);
			Request request = queryParser.getRequest();

			InstanceManager instanceManager = new InstanceManager();
			Instance instance = instanceManager.getInstanceById("i-"+queryParser.getInstanceId());
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

			final Headers hdrs = t.getResponseHeaders();
			String response ="";
			t.sendResponseHeaders(200, response.length());
			hdrs.add("Content-Typpe", "text/plain");
			hdrs.add("Access-Control-Allow-Origin", "*");
			hdrs.add("Access-Control-Allow-Credentials", "true");
			hdrs.add("Access-Control-Allow-Methods", "POST, GET, HEAD, OPTIONS");
			hdrs.add("Access-Control-Allow-Headers", "Origin, Accept, X-Requested-With, Content-Type, Access-Control-Request-Method, Access-Control-Request-Headers");

			OutputStream os = t.getResponseBody();
			os.write(response.getBytes());
			os.close();
		}
	}

	/**
	 * Select the instance that we want to send the request to
	 */
	private static synchronized Instance selectInstanceForRequest(Request request){
		Instance selectedInstance = null;
		long lowestComplexity = Long.MAX_VALUE;
		long curSum = 0;
		Set<Instance> instances = instanceManager.getWorkerInstances();
        System.out.println("Selecting an instance for request with estimated complexity = " + request.getEstimatedComplexity());
		for(Instance instance: instances){  
            System.out.println("Checking instance " + instance.getInstanceId() + " for loadbalancing");
            // only proceed if instance is in "running" state 
            if(instance.getState().getCode() != 16){
                continue;
            }
            // does the instance have a request entry in the map? if not, add a new one
			List<Request> requests = runningRequests.get(instance);
		 	if(requests == null){
				requests = new ArrayList<>();	
				runningRequests.put(instance, requests);
			}	

			// sum estimated time complexities of all requests
			// any running request has estimated values, except 0 if no recent data exists
            System.out.println("instance : " + instance.getInstanceId() + " has " + requests.size() + " requests");
			for(Request req : requests){
                System.out.println("instance : " + instance.getInstanceId() + " has request: " + req);
				// Sum estimated complexities taking also into account the progress of the request
				// progress is a double value ranging from 0 to 1
				double leftOverPercentage = 1-req.getProgress();
				curSum += (long) (req.getEstimatedComplexity() * leftOverPercentage);
				System.out.println("leftover: "+leftOverPercentage);
				System.out.println("estimated: " + req.getEstimatedComplexity());
				System.out.println("instance: " + instance.getInstanceId() + " , aproximate total complexity = " + curSum);
			}

            // pick the instance with the lowest estimated complexity
            System.out.println("test: instanceid: "+ instance.getInstanceId() + " curSum=" + curSum + " < lowestComplexity ="+lowestComplexity);
            
			if(curSum < lowestComplexity){
                System.out.println("instance : " + instance.getInstanceId() + " is temporarily selected as lowest with complexity " + curSum);
				lowestComplexity = curSum;
				selectedInstance = instance;
			}else if(curSum == lowestComplexity){
				System.out.println("Same request complexity, choosing one with least # of requests running");
				// choose the one with lowest # of requests running
				int selectedInstanceRequestCount = runningRequests.get(selectedInstance).size();
				int currInstanceRequestCount = runningRequests.get(instance).size();
				selectedInstance = currInstanceRequestCount < selectedInstanceRequestCount ? instance : selectedInstance;
			}	
			curSum = 0;
		}

		// if all else fails
		if(selectedInstance == null){
            Iterator<Instance> it = instances.iterator();
            if( it.hasNext() ){
               selectedInstance = it.next();
            } 
		}

        System.out.println("instance : " + selectedInstance.getInstanceId() + " has been selected with complexity: " + lowestComplexity);
		System.out.println("redirecting request to instance with id: " + selectedInstance.getInstanceId());

		return selectedInstance;
	}


	/**
	 * Estimate the complexity that would be obtained after measuring the request execution with instrumentation
	 * in a web server instance.
	 */
	public static long estimateComplexity(Request request){
		// get metrics of similar requests with same search algo and dataset
		List<Request> recentRequests = new ArrayList<>();
		try{
			recentRequests = MSSClient.getInstance().getMetrics(request.getSearchAlgorithm(),
				request.getDataset());
		}catch(Exception e){
			e.printStackTrace();
		}

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
			System.out.println("Doing get url: " + url);
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

