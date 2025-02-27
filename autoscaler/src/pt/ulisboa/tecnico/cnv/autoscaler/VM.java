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
    private int GRACE_PERIOD = 120; // 2 minutes
    private AmazonCloudWatch cloudWatch;
    private String SECURITY_GROUP = "novosec";
    private String KEY_NAME = "CNV-GERAL";
    private String AMI = "ami-051a4c858f850c907";
    private String INSTANCE_TYPE = "t2.micro";
    private String id;
    private Instance instance;
    private String ENDPOINT_REQUEST_COUNT = "/countRequests";
    private String ENDPOINT_HEALTH_CHECK = "/healthCheck";
    private long OFFSET_MILLISECONDS = 1000 * 60 * 4; // 5 minutes
    private int WEBSERVER_PORT = 8080;
    private double startRunningAt;

    private List<Boolean> healthRecords;
    private List<Integer> cpuRecords;
    private List<Integer> requestHistory;



    public VM(){
        try{
            this.healthRecords = new ArrayList<>();
            this.cpuRecords = new ArrayList<>();
            this.requestHistory = new ArrayList<>();
            this.id = new String();
            this.init();
        }catch (Exception e){
            e.printStackTrace();
        }
    }


    private void init() {

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
                .withMonitoring(true)
                .withSecurityGroups(this.SECURITY_GROUP);

        RunInstancesResult runInstancesResult =
                ec2Client.runInstances(runInstancesRequest);

        this.id = runInstancesResult.getReservation().getInstances()
                .get(0).getInstanceId();
        System.out.println("Machine ID: " + this.id);

        while(getInstanceStatus(getID()) != RUNNING){
            try {
                Thread.sleep(5000);
                System.out.print(".");
            } catch(InterruptedException e) {
                e.printStackTrace();
            }
        }
        System.out.println();
        this.instance = getInstance(this.getID());
        System.out.println("Instance: " + getDNS() + " Running!");

        while(!isWebServerRunning()){
            try {
                Thread.sleep(3000);
                System.out.print(".");
            } catch(InterruptedException e) {
                e.printStackTrace();
            }
        }
        System.out.println();
        System.out.println("Web Server Started Running!!");
        this.startRunningAt = (new Date().getTime()) / 1000.0; // in seconds
    }

    public boolean isInGracePeriod(){
        return (this.startRunningAt + GRACE_PERIOD) >= ((new Date().getTime()) / 1000.0);
    }

    private int getWebServerRequests(){
        int runningMachines = Integer.parseInt(HTTPRequest.doGET(getURL(ENDPOINT_REQUEST_COUNT)));
        return runningMachines == -1 ? 0 : runningMachines;
    }

    public boolean isBusy(){
        int runningMachines = Integer.parseInt(HTTPRequest.doGET(getURL(ENDPOINT_REQUEST_COUNT)));
        return runningMachines == -1 ? false : runningMachines != 0;
    }

    private String getURL(String urlPath){
        return "http://" + getDNS() + ":" + WEBSERVER_PORT + urlPath;
    }


    private boolean isWebServerRunning(){
        String status = HTTPRequest.doGET(getURL(ENDPOINT_HEALTH_CHECK)) ;
        if(status.equals("-1")){
            return false;
        }
        return true;
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

    public void tick(){
        this.healthRecords.add(isWebServerRunning());
//        System.out.println(getCPUUsage());
        if (getCPUUsage() > 0){ //dangerous
            this.cpuRecords.add(getCPUUsage());
        }
        this.requestHistory.add(this.getWebServerRequests());
        this.dataCleaner();
    }


    public List<Boolean> getHealthRecords(){
        return this.healthRecords;
    }

    public List<Integer> getCPURecords(){
        return this.cpuRecords;
    }

    public List<Integer> getRequestsHistory(){
        return this.requestHistory;
    }

    public int getLastRecordedCPU(){
        if(this.cpuRecords.size() > 0){
            return this.cpuRecords.get(this.cpuRecords.size() - 1);
        }
        return -1;
    }

    private void dataCleaner(){
        // yea we should be using queued lists
        if(this.requestHistory.size() == 4){
                this.requestHistory.remove(0);
        }
        if(this.healthRecords.size() ==  4){
                this.healthRecords.remove(0);
        }
        if (this.cpuRecords.size() == 4){
                this.cpuRecords.remove(0);
        }
    }


    public int getCPUUsage(){
        Dimension instanceDimension = new Dimension();
        instanceDimension.setName("InstanceId");
        List<Dimension> dims = new ArrayList<>();
        dims.add(instanceDimension);
        String name = this.instance.getInstanceId();
        int avg = 0;
        if (getInstanceStatus(getID()) == RUNNING) {
            instanceDimension.setValue(name);
            GetMetricStatisticsRequest request = new GetMetricStatisticsRequest()
                    .withStartTime(new Date(new Date().getTime() - OFFSET_MILLISECONDS))
                    .withNamespace("AWS/EC2")
                    .withPeriod(60)
                    .withMetricName("CPUUtilization")
                    .withStatistics("Average")
                    .withDimensions(instanceDimension)
                    .withEndTime(new Date());
            GetMetricStatisticsResult getMetricStatisticsResult =
                    cloudWatch.getMetricStatistics(request);
            List<Datapoint> datapoints = getMetricStatisticsResult.getDatapoints();
//            System.out.println(datapoints);
            for (Datapoint dp : datapoints) {
                avg += dp.getAverage();
            }
            if(datapoints.size() != 0){
                return avg / datapoints.size();
            }
        }
        return -1;
    }

}

