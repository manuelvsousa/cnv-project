package pt.ulisboa.tecnico.cnv.dto;

public class RequestMetricData {
    private Request request;

    // instrumented metrics
    private long timeComplexity;
    private long spaceComplexity;

    // TODO
    // duration
    // ec2 instance that processed the request
    // cpu load
    // mem load?

    public RequestMetricData(Request request, long timeComplexity, long spaceComplexity) {
        this.request = request;
        this.timeComplexity = timeComplexity;
        this.spaceComplexity = spaceComplexity;
    }

    public long getTimeComplexity() {
        return timeComplexity;
    }

    public void setTimeComplexity(long timeComplexity) {
        this.timeComplexity = timeComplexity;
    }

    public long getSpaceComplexity() {
        return spaceComplexity;
    }

    public void setSpaceComplexity(long spaceComplexity) {
        this.spaceComplexity = spaceComplexity;
    }

    public Request getRequest() {
        return request;
    }

    public void setRequest(Request request) {
        this.request = request;
    }
}


