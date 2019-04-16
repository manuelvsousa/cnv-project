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
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.ArrayList;
import java.util.Date;

import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import pt.ulisboa.tecnico.cnv.solver.Solver;
import pt.ulisboa.tecnico.cnv.solver.SolverArgumentParser;
import pt.ulisboa.tecnico.cnv.solver.SolverFactory;

import com.amazonaws.AmazonClientException;
import com.amazonaws.AmazonServiceException;
import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.profile.ProfileCredentialsProvider;
import com.amazonaws.services.ec2.AmazonEC2;
import com.amazonaws.services.ec2.AmazonEC2ClientBuilder;
import com.amazonaws.services.ec2.model.TerminateInstancesRequest;
import com.amazonaws.services.ec2.model.DescribeInstancesResult;
import com.amazonaws.services.ec2.model.RunInstancesRequest;
import com.amazonaws.services.ec2.model.RunInstancesResult;
import com.amazonaws.services.ec2.model.Instance;
import com.amazonaws.services.ec2.model.Reservation;
import com.amazonaws.services.cloudwatch.AmazonCloudWatch;
import com.amazonaws.services.cloudwatch.AmazonCloudWatchClientBuilder;
import com.amazonaws.services.cloudwatch.model.Dimension;
import com.amazonaws.services.cloudwatch.model.Datapoint;
import com.amazonaws.services.cloudwatch.model.GetMetricStatisticsRequest;
import com.amazonaws.services.cloudwatch.model.GetMetricStatisticsResult;

import javax.imageio.ImageIO;

public class LoadBalancer {
	private AmazonEC2      ec2;
	private AmazonCloudWatch cloudWatch;

	public LoadBalancer(){
		init();
	}

	private void init(){
		AWSCredentials credentials = null;
		try {
			credentials = new ProfileCredentialsProvider().getCredentials();
		} catch (Exception e) {
			throw new AmazonClientException(
				"Cannot load the credentials from the credential profiles file. " +
				"Please make sure that your credentials file is at the correct " +
				"location (~/.aws/credentials), and is in valid format.",
				e);
		}
		ec2 = AmazonEC2ClientBuilder.standard().withRegion("us-east-1")
		.withCredentials(new AWSStaticCredentialsProvider(credentials)).build();

		cloudWatch = AmazonCloudWatchClientBuilder.standard().withRegion("us-east-1")
		.withCredentials(new AWSStaticCredentialsProvider(credentials)).build();

	}


	public double getAverageCPUUsage(Instance instance){
		Dimension instanceDimension = new Dimension();
		instanceDimension.setName("InstanceId");
		List<Dimension> dims = new ArrayList<Dimension>();
		dims.add(instanceDimension);
		long offsetInMilliseconds = 1000 * 60 * 10;
		
		String name = instance.getInstanceId();
		String state = instance.getState().getName();
		if (state.equals("running")) {
			System.out.println("running instance id = " + name);
			instanceDimension.setValue(name);
			GetMetricStatisticsRequest request = new GetMetricStatisticsRequest()
				.withStartTime(new Date(new Date().getTime() - offsetInMilliseconds))
				.withNamespace("AWS/EC2")
				.withPeriod(60)
				.withMetricName("CPUUtilization")
				.withStatistics("Average")
				.withDimensions(instanceDimension)
				.withEndTime(new Date());
			GetMetricStatisticsResult getMetricStatisticsResult = cloudWatch.getMetricStatistics(request);

			List<Datapoint> datapoints = getMetricStatisticsResult.getDatapoints();
			return datapoints.get(0).getAverage();
		}else{
			return 0;
		}

	}

	public Set<Instance> getInstances(){
		Set<Instance> instances = new HashSet<Instance>();
		DescribeInstancesResult describeInstancesResult = ec2.describeInstances();
		List<Reservation> reservations = describeInstancesResult.getReservations();
		for (Reservation reservation : reservations) {
			instances.addAll(reservation.getInstances());
		}
		return instances;
	}

	public AmazonEC2 getEc2(){
		return ec2;
	}

	public AmazonCloudWatch getCloudWatch(){
		return cloudWatch;
	}



}
