package pt.ulisboa.tecnico.cnv.autoscaler;

import com.amazonaws.AmazonClientException;
import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.profile.ProfileCredentialsProvider;
import com.amazonaws.services.cloudwatch.AmazonCloudWatch;
import com.amazonaws.services.cloudwatch.AmazonCloudWatchClientBuilder;
import com.amazonaws.services.cloudwatch.model.Datapoint;
import com.amazonaws.services.cloudwatch.model.Dimension;
import com.amazonaws.services.cloudwatch.model.GetMetricStatisticsRequest;
import com.amazonaws.services.cloudwatch.model.GetMetricStatisticsResult;
import com.amazonaws.services.ec2.AmazonEC2;
import com.amazonaws.services.ec2.AmazonEC2ClientBuilder;
import com.amazonaws.services.ec2.model.*;
import pt.ulisboa.tecnico.cnv.autoscaler.util.HTTPRequest;

import java.util.*;
import java.util.List;

public class VM {
    private int RUNNING = 16;

    private String REGION = "us-east-1";
    private AmazonEC2 ec2Client;
    private AmazonCloudWatch cloudWatch;
    private String SECURITY_GROUP = "CVN-ssh+http";
    private String KEY_NAME = "mvs-aws";
    private String AMI = "ami-0a79624df0814a698";
    private String INSTANCE_TYPE = "t2.micro";
    private String id = new String();
    private Instance instance;
    private String ENDPOINT_REQUEST_COUNT = "/countRequests";


    public VM(){
        try{
            this.init();
        }catch (Exception e){
            e.printStackTrace();
        }
    }


    private void init() throws Exception {

        /*
         * The ProfileCredentialsProvider will return your [default]
         * credential profile by reading from the credentials file located at
         * (~/.aws/credentials).
         */
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
        ec2Client = AmazonEC2ClientBuilder.standard().withRegion(this.REGION).withCredentials(new AWSStaticCredentialsProvider(credentials)).build();
        cloudWatch = AmazonCloudWatchClientBuilder.standard().withRegion(this.REGION).withCredentials(new AWSStaticCredentialsProvider(credentials)).build();
    }

    private Instance getInstance(String instanceId) {
        DescribeInstancesResult describeInstancesRequest = ec2Client.describeInstances();
        List<Reservation> reservations = describeInstancesRequest.getReservations();
        for (Reservation reservation : reservations) {
            for (Instance instance : reservation.getInstances()) {
                if (instance.getInstanceId().equals(instanceId)){
                    return instance.getPublicDnsName().equals("") ? getInstance(instanceId) : instance;
                }
            }
        }
        return getInstance(instanceId);
    }

    private String getDNS(){
        return this.instance.getPublicDnsName();
    }

    public String getID(){
        return this.id;
    }

    /*
    0 : pending
    16 : running
    32 : shutting-down
    48 : terminated
    64 : stopping
    80 : stopped
     */
    private int getInstanceStatus(String instanceId) {
        DescribeInstancesRequest describeInstanceRequest = new DescribeInstancesRequest().withInstanceIds(instanceId);
        DescribeInstancesResult describeInstanceResult = ec2Client.describeInstances(describeInstanceRequest);
        InstanceState state = describeInstanceResult.getReservations().get(0).getInstances().get(0).getState();
        return state.getCode();
    }

    public void launchVM() {
        RunInstancesRequest runInstancesRequest =
                new RunInstancesRequest();
        runInstancesRequest.withImageId(this.AMI)
                .withInstanceType(this.INSTANCE_TYPE)
                .withMinCount(1)
                .withMaxCount(1)
                .withKeyName(this.KEY_NAME)
                .withSecurityGroups(this.SECURITY_GROUP);

        RunInstancesResult runInstancesResult =
                ec2Client.runInstances(runInstancesRequest);

        this.id = runInstancesResult.getReservation().getInstances()
                .get(0).getInstanceId();
        System.out.println(this.id);

        while(getInstanceStatus(getID()) != RUNNING){
            try {
                Thread.sleep(5000);
                System.out.print(".");
            } catch(InterruptedException e) {}
        }
        System.out.print("");
        this.instance = getInstance(this.getID());
        System.out.println("Instance: " + getDNS() + " Running!");
    }

    private int getWebServerRequests(){
        int runningMachines = Integer.parseInt(HTTPRequest.doGET("http://" + getDNS() + "/" + ENDPOINT_REQUEST_COUNT));
        return runningMachines;
    }


    public boolean terminate(){
        if(getWebServerRequests() == 0) {
            TerminateInstancesRequest termInstanceReq = new TerminateInstancesRequest();
            termInstanceReq.withInstanceIds(getID());
            ec2Client.terminateInstances(termInstanceReq);
            System.out.println("Killing machine: " + getDNS());
            return true;
        }
        return false;
    }


    public void getStatistics(){
        long offsetInMilliseconds = 1000 * 60 * 10;
        Dimension instanceDimension = new Dimension();
        instanceDimension.setName("InstanceId");
        List<Dimension> dims = new ArrayList<>();
        dims.add(instanceDimension);
        String name = instance.getInstanceId();
        if (getInstanceStatus(getID()) == RUNNING) {
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
            GetMetricStatisticsResult getMetricStatisticsResult =
                    cloudWatch.getMetricStatistics(request);
            List<Datapoint> datapoints = getMetricStatisticsResult.getDatapoints();
            System.out.println(datapoints.toString());
            for (Datapoint dp : datapoints) {
                System.out.println(" CPU utilization for instance " + name +
                        " = " + dp.getAverage());
            }
        }
    }

}

