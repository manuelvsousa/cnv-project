package pt.ulisboa.tecnico.cnv.loadbalancer;

import com.amazonaws.services.ec2.model.Instance;
import com.amazonaws.util.EC2MetadataUtils;
import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import pt.ulisboa.tecnico.cnv.lib.ec2.InstanceManager;
import pt.ulisboa.tecnico.cnv.lib.request.Request;
import pt.ulisboa.tecnico.cnv.lib.request.RequestBuilder;
import pt.ulisboa.tecnico.cnv.mssclient.MSSClient;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.*;
import java.net.InetSocketAddress;
import java.net.URL;
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
		}

		// find mss server ip and create the client
		if(isTestingLocally){
			mssClient = new MSSClient("localhost", mssPort);
			System.out.println("Running Loadbalancer locally. Created MSS client talking to localhost:"+mssPort);
		}else{
			Instance mssInstance = instanceManager.getMSSInstance();
			String mssIp = mssInstance.getPrivateIpAddress();
			mssClient = new MSSClient(mssIp, mssPort);
			System.out.println("Created MSS client talking to server at: " + mssIp +":" + mssPort);
		}

		server.createContext("/climb", new ClimbHandler());
		server.createContext("/requestFinishedProcessing", new PostMetricDataHandler());

		// be aware! infinite pool of threads!
		server.setExecutor(Executors.newCachedThreadPool());
		server.start();

		// clear previous tags and set loadbalancer tag for instance identification
		Instance instance = instanceManager.getInstanceById(EC2MetadataUtils.getInstanceId());
		instanceManager.clearInstanceTags(instance);
		instanceManager.tagInstanceAsLoadBalancer(instance);

		System.out.println(server.getAddress().toString());
	}


	static class ClimbHandler implements HttpHandler {
		public void handle(final HttpExchange t) throws IOException {
			// create request object
			Request request = RequestBuilder.fromQuery(t.getRequestURI().getQuery());
			estimateRequestComplexity(request);

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
			String redirectUrl = buildRedirectUrl(ip, 8080);

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


		}
	}

	/**
	 * Select the instance that we want to send the request to
	 * @param request request to be sent and processed at the instance
	 * @return instance
	 */
	private static Instance selectInstanceForRequest(Request request){
		Instance instance = getInstanceLowestEstimatedTimeComplexity();

		return instance;
	}


	public static void estimateRequestComplexity(Request request){
		// get metrics of similar requests and estimate complexity of this request

		// testing
		try {
			mssClient.getMetrics(request.getSearchAlgorithm().toString(), request.getMapSize().getWidth());
		} catch (Exception e) {
			e.printStackTrace();
		}


		List<Object> metrics = null; // = mssClient.getMetrics(request.getSearchAlgorithm().toString(), request.getMapSize().getWidth());
		int estimatedTimeComplexity, estimatedSpaceComplexity;
		int timeSum=0, spaceSum=0;
		for(int i = 0; i < metrics.size(); i++){
		//	timeSum += metrics.get(i).getTimeComplexity();
		//	spaceSum += metrics.get(i).getSpaceComplexity();
		}
		estimatedTimeComplexity = timeSum/metrics.size();
		estimatedSpaceComplexity = spaceSum/metrics.size();

		request.setEstimatedTimeComplexity(estimatedTimeComplexity);
		request.setEstimatedSpaceComplexity(estimatedSpaceComplexity);
	}


	/**
	 * Find the instance with the lowest estimated time complexity
	 * @return
	 */
	private static Instance getInstanceLowestEstimatedTimeComplexity(){
		Instance selectedInstance = null;
		int lowestTimeComplexity = Integer.MAX_VALUE;
		int curSum = 0;
		Set<Instance> instances = instanceManager.getWorkerInstances();
		for(Instance instance: instances){
			List<Request> requests = runningRequests.get(instance);
			// sum estimated time complexities of all requests
			// any running request has estimated values, first thing loadbalancer will do is estimate given metric data in MSS
			for(Request req : requests){
				curSum += req.getEstimatedTimeComplexity();
			}

			if(curSum < lowestTimeComplexity){
				lowestTimeComplexity = curSum;
				selectedInstance = instance;
			}
			curSum = 0;
		}

		return selectedInstance;
	}


	static class PostMetricDataHandler implements HttpHandler{
		public void handle(final HttpExchange t) throws IOException{
			System.out.println(t.getRequestURI());

			final Headers hdrs = t.getResponseHeaders();
       		t.sendResponseHeaders(200, 0);
		}
	}


	private static void storeRequest(Request request, Instance instance){
		List<Request> requestList = runningRequests.get(instance);
		if(requestList == null){
			requestList = new ArrayList<Request>();
			runningRequests.put(instance, requestList);
		}
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


	/**
	 * Get the instance with the lowest number of runningRequests running concurrently
	 * @return instance
	 */
	private static Instance getInstanceLeastRunningRequests(){
		Set<Instance> instances = instanceManager.getInstances();
		Instance minInstance = null;
		int minCount = Integer.MAX_VALUE;
		for(Instance instance : instances){
			int runningCount = runningRequests.get(instance).size();
			if(runningCount < minCount){
				minCount = runningCount;
				minInstance = instance;
			}
		}
		return minInstance;
	}


	private static String buildRedirectUrl(String ip, int port){
		return "http://" + ip + ":" + port + "/climb";
	}




}

