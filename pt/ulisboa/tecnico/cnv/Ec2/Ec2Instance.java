package pt.ulisboa.tecnico.cnv.Ec2;

import com.amazonaws.services.ec2.AmazonEC2;
import com.amazonaws.services.ec2.AmazonEC2ClientBuilder;
import com.amazonaws.services.ec2.model.InstanceType;
import com.amazonaws.services.ec2.model.RunInstancesRequest;
import com.amazonaws.services.ec2.model.RunInstancesResult;
import com.amazonaws.services.ec2.model.Tag;
import com.amazonaws.services.ec2.model.CreateTagsRequest;
import com.amazonaws.services.ec2.model.CreateTagsResult;

public class Ec2Instance{

/*
	private String _name;
	private String _imageId;

	public Ec2Instance(String name, String imageId){
		_name=name;
		_imageId=imageId;


	}

*/

	public Ec2Instance(){

	}

	private void init(){

	     AmazonEC2      ec2;
	     AmazonCloudWatch cloudWatch;


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

            runInstancesRequest.withImageId("ami-a0e9d7c6")
                               .withInstanceType("t2.micro")
                               .withMinCount(1)
                               .withMaxCount(1)
                               .withKeyName("jog-aws")
                               .withSecurityGroups("ssh+http8000");
							   
            RunInstancesResult runInstancesResult =
               ec2.runInstances(runInstancesRequest);
			   
            String newInstanceId = runInstancesResult.getReservation().getInstances()
                                      .get(0).getInstanceId();
						
	}


}