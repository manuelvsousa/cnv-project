package pt.ulisboa.tecnico.cnv.Ec2;

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
		ec2 = AmazonEC2ClientBuilder.standard().withRegion("eu-east-1").withCredentials(new AWSStaticCredentialsProvider(credentials)).build();

		cloudWatch = AmazonCloudWatchClientBuilder.standard().withRegion("eu-east-1").withCredentials(new AWSStaticCredentialsProvider(credentials)).build();


	}

	public void lauchInstance(){
		System.out.println("Launching instance");
		init();

		RunInstancesRequest runInstancesRequest =
               new RunInstancesRequest();

            runInstancesRequest.withImageId("ami-0ff7a9830fa0714bb")
                               .withInstanceType("t2.micro")
                               .withMinCount(1)
                               .withMaxCount(1)
                               .withKeyName("cnv-proj")
                               .withSecurityGroups("test-secur");
							   
            RunInstancesResult runInstancesResult =
               ec2.runInstances(runInstancesRequest);
			   
            instanceId = runInstancesResult.getReservation().getInstances()
                                      .get(0).getInstanceId();
						
	}

	public void terminateInstance(){

		System.out.println("Terminating the instance.");
            TerminateInstancesRequest termInstanceReq = new TerminateInstancesRequest();
            termInstanceReq.withInstanceIds(instanceId);
            ec2.terminateInstances(termInstanceReq);
	}

	public void main(String[] args){
		System.out.println("Starting test");

		lauchInstance();
		try{
		    Thread.sleep(60000);
		}catch(InterruptedException ie){
		    System.out.println(ie.getMessage());
		}

		terminateInstance();

	}


}
