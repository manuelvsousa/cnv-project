package pt.ulisboa.tecnico.cnv.lib.ec2;

import com.amazonaws.AmazonClientException;
import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.profile.ProfileCredentialsProvider;
import com.amazonaws.services.cloudtrail.model.RemoveTagsRequest;
import com.amazonaws.services.cloudwatch.AmazonCloudWatch;
import com.amazonaws.services.cloudwatch.AmazonCloudWatchClientBuilder;
import com.amazonaws.services.cloudwatch.model.Datapoint;
import com.amazonaws.services.cloudwatch.model.Dimension;
import com.amazonaws.services.cloudwatch.model.GetMetricStatisticsRequest;
import com.amazonaws.services.cloudwatch.model.GetMetricStatisticsResult;
import com.amazonaws.services.ec2.AmazonEC2;
import com.amazonaws.services.ec2.AmazonEC2ClientBuilder;
import com.amazonaws.services.ec2.model.*;
import com.amazonaws.services.s3.model.PutObjectRequest;

import java.util.*;

public class InstanceManager {
	private static final String MSS_INSTANCE_NAME = "mss";
	private static final String LOADBALANCER_INSTANCE_NAME = "loadbalancer";
	private static final String WORKER_INSTANCE_NAME = "worker";
	private AmazonEC2      ec2;
	private AmazonCloudWatch cloudWatch;

	public InstanceManager(){
		init();
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

	public Instance getInstanceById(String instanceId){
		Set<Instance> instances = getInstances();
		for(Instance instance : instances){
			if(instance.getInstanceId().equals(instanceId)){
				return instance;
			}
		}
		return null;
	}

	public String getTagNameOfInstance(Instance instance){
		List<Tag> tags = instance.getTags();
		if(tags.size() > 0){
			for(Tag tag : tags){
				if(tag.getKey().equals("Name")){
					return tag.getValue();
				}
			}
		}
		return "";
	}

	public void tagInstanceAsMSS(Instance instance){
		setInstanceName(instance, MSS_INSTANCE_NAME);
	}

	public void tagInstanceAsLoadBalancer(Instance instance){
		setInstanceName(instance, LOADBALANCER_INSTANCE_NAME);
	}

	public void tagInstanceAsWorker(Instance instance){
		setInstanceName(instance, WORKER_INSTANCE_NAME);
	}

	private void setInstanceName(Instance instance, String name){
		addTagToInstance(instance, "Name", name);
	}

	public void addTagToInstance(Instance instance, String key, String value){
		long offsetInMilliseconds = 1000 * 60 * 10;
		List<Tag> tags = instance.getTags();
		tags.add(new Tag(key, value));
		instance.setTags(tags);

		CreateTagsRequest createTagsRequest = new CreateTagsRequest().withTags(tags)
				.withResources(instance.getInstanceId());
		ec2.createTags(createTagsRequest);
	}

	public void clearInstanceTags(Instance instance){
		DeleteTagsRequest deleteTagsRequest = new DeleteTagsRequest()
				.withResources(instance.getInstanceId());

		ec2.deleteTags(deleteTagsRequest);
	}



	/**
	 * Find the instance with the tag mss
	 * @return
	 */
	public Instance getMSSInstance(){
		return findInstanceByTag(MSS_INSTANCE_NAME);
	}

	/**
	 * Find instance with tag loadbalancer
	 * @return
	 */
	public Instance getLoadBalancerInstance(){
		return findInstanceByTag(LOADBALANCER_INSTANCE_NAME);
	}


	/**
	 * Find an instance by a tag name, indicating for example if it is the mss
	 * @return
	 */
	private Instance findInstanceByTag(String tagValue){
		Set<Instance> instances = getInstances();
		for(Instance instance : instances){
			List<Tag> tags = instance.getTags();

			// does instance have tags?
			if(tags.size() > 0 ){

				// does instance have a tag with value tagValue?
				for(Tag tag : tags){
					if(tag.getValue().equals(tagValue)){
						return instance;
					}
				}
			}
		}
		return null;
	}


	public AmazonEC2 getEc2(){
		return ec2;
	}

	public AmazonCloudWatch getCloudWatch(){
		return cloudWatch;
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

}
