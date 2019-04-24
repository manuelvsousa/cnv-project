package pt.ulisboa.tecnico.cnv.loadbalancer;

import com.amazonaws.services.ec2.model.Instance;
import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import pt.ulisboa.tecnico.cnv.lib.request.Request;
import pt.ulisboa.tecnico.cnv.lib.request.RequestBuilder;

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

	public static void main(final String[] args) throws Exception {
		final HttpServer server = HttpServer.create(new InetSocketAddress("127.0.0.1", 8000), 0);
		//final HttpServer server = HttpServer.create(new InetSocketAddress(8000), 0);
		server.createContext("/climb", new ClimbHandler());
		server.createContext("/requestFinishedProcessing", new PostMetricDataHandler());

		// be aware! infinite pool of threads!
		server.setExecutor(Executors.newCachedThreadPool());
		server.start();

		System.out.println(server.getAddress().toString());
	}

	static class ClimbHandler implements HttpHandler {
		public void handle(final HttpExchange t) throws IOException {
			// create request object
			Request request = RequestBuilder.fromQuery(t.getRequestURI().getQuery());

			// store request in the hashmap
			//storeRequest(request, instance);

			// redirect and get buffered image response
			//Instance instance = selectInstanceForRequest(request);
			//String ip = instance.getPrivateIpAddress();

			// use localhost for testing
			String ip = "localhost";
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
	 * Select the instance that we want to send the request to (main algorithm)
	 * @param request request to be sent and processed at the instance
	 * @return instance
	 */
	private static Instance selectInstanceForRequest(Request request){
		// get metrics of similar requests and estimate complexity of this request

		// get total estimated complexity of each running instance

		// send request to the instance with least total estimated complexity


		return null;
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

