package pt.ulisboa.tecnico.cnv.loadbalancer;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.Executors;
import java.util.Set;

import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;

import com.amazonaws.services.ec2.model.Instance;

public class LoadBalancerServer{
	
	public static void main(final String[] args) throws Exception {
		LoadBalancer loadBalancer = new LoadBalancer();
		
		Set<Instance> instances = loadBalancer.getInstances();
		Instance instance = instances.iterator().next();
		if(instances.size() > 0 && instance != null ){
			System.out.println("Average CPU usage: " + loadBalancer.getAverageCPUUsage(instance));
		}
	}

	static class MyHandler implements HttpHandler {
		@Override
		public void handle(final HttpExchange t) throws IOException {

		}
	}
}

