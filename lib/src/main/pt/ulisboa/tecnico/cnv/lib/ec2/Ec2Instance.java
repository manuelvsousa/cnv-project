package pt.ulisboa.tecnico.cnv.lib.ec2;


import java.util.HashSet;
import java.util.List;
import java.util.Set;

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
import com.amazonaws.services.ec2.model.DescribeAvailabilityZonesResult;


public class Ec2Instance{

/*
<<<<<<< HEAD
=======
	private String _name;
>>>>>>> acfe89f7dfa24789f46ca1391fc3430f3ec353e9
	private String _imageId;

	public Ec2Instance(String name, String imageId){
		_name=name;
		_imageId=imageId;


	}

*/

	private String instanceId;
	private AmazonEC2      ec2;
	private AmazonCloudWatch cloudWatch;



	public Ec2Instance(){

	}

	private  void init(){


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
		ec2 = AmazonEC2ClientBuilder.standard().withRegion("us-east-1").withCredentials(new AWSStaticCredentialsProvider(credentials)).build();

		cloudWatch = AmazonCloudWatchClientBuilder.standard().withRegion("us-east-1").withCredentials(new AWSStaticCredentialsProvider(credentials)).build();


	}

	public  void lauchInstance(){
		System.out.println("Launching instance");
		init();

            DescribeInstancesResult describeInstancesRequest = ec2.describeInstances();
            List<Reservation> reservations = describeInstancesRequest.getReservations();
            Set<Instance> instances = new HashSet<Instance>();

		RunInstancesRequest runInstancesRequest =
               new RunInstancesRequest();

            runInstancesRequest.withImageId("ami-0596114656fa8bac1")
                               .withInstanceType("t2.micro")
                               .withMinCount(1)
                               .withMaxCount(1)
                               .withKeyName("cnv-proj")
                               .withSecurityGroups("test-secur");

							   
            RunInstancesResult runInstancesResult =
               ec2.runInstances(runInstancesRequest);
			   
            instanceId = runInstancesResult.getReservation().getInstances()
                                      .get(0).getInstanceId();

            describeInstancesRequest = ec2.describeInstances();
            reservations = describeInstancesRequest.getReservations();
            instances = new HashSet<Instance>();

            for (Reservation reservation : reservations) {
                instances.addAll(reservation.getInstances());
            }
						
	}

	public void terminateInstance(){

		System.out.println("Terminating the instance.");
            TerminateInstancesRequest termInstanceReq = new TerminateInstancesRequest();
            termInstanceReq.withInstanceIds(instanceId);
            ec2.terminateInstances(termInstanceReq);
	}
	/*

	public static void main(String[] args){
		System.out.println("Starting test");

		lauchInstance();

		try{
		    Thread.sleep(60000);
		}catch(InterruptedException ie){
		    System.out.println(ie.getMessage());
		}


		terminateInstance();

	} */


}