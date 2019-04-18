package pt.ulisboa.tecnico.cnv.loadbalancer;

import com.amazonaws.services.ec2.model.Instance;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import java.io.IOException;
import java.util.Set;

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

