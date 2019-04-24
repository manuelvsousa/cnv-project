package pt.ulisboa.tecnico.cnv.mssclient;

import com.amazonaws.AmazonServiceException;
import com.google.gson.Gson;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.util.EntityUtils;

import java.util.Map;


public class MSSClient {
    private String mssServerIp;
    private int mssServerPort;

    public MSSClient(String mssServerIp, int mssServerPort) {
        this.mssServerIp = mssServerIp;
        this.mssServerPort = mssServerPort;
    }

    public static void main(String[] args) throws Exception {
        // test code
        try {
            MSSClient msscli = new MSSClient("127.0.0.1", 8001);
            System.out.println(msscli.getMetrics(7));
            msscli.addMetrics("0.0.1.7", "caccacacacca", 6, 6, 3, 9, 9);
            System.out.println(msscli.getMetrics(9));
        } catch (AmazonServiceException ase) {

        }
    }

    public void addMetrics(String ip, String algorithm, int mapWidth, int startX, int startY, int timeComplexity, int spaceComplexity) throws Exception {
        HttpClient httpClient = HttpClientBuilder.create().build();
        HttpGet request = new HttpGet("http://" + this.mssServerIp + ":" + this.mssServerPort + "/addMetrics"
                + "?mssServerIp=" + ip
                + "&algorithm=" + algorithm
                + "&mapwidth=" + mapWidth
                + "&startx=" + startX
                + "&starty=" + startY
                + "&timecomplexity=" + timeComplexity
                + "&spacecomplexity=" + spaceComplexity);
        HttpResponse response = httpClient.execute(request);
        String json = EntityUtils.toString(response.getEntity(), "UTF-8");
        Gson gson = new Gson();
        Map<String, Object> output = gson.fromJson(json, Map.class);
        if(!(boolean) output.get("success")){
            throw new RuntimeException(output.get("message").toString());
        }
        System.out.println(response);
    }

    public Map<String, Object> getMetrics(int id) throws Exception {
        return getMetrics("http://" + this.mssServerIp + ":" + this.mssServerPort + "/getMetrics?id=" + id);
    }

    public Map<String, Object> getMetrics(String searchAlgorithm, int mapWidth) throws Exception{
        return getMetrics("http://" + this.mssServerIp + ":" + this.mssServerPort + "/getMetrics?searchAlgo=" + searchAlgorithm + "&mapWidth=" + mapWidth);
    }

    private Map<String, Object> getMetrics(String targetUrl) throws Exception{
        HttpClient httpClient = HttpClientBuilder.create().build();
        HttpGet request = new HttpGet(targetUrl);
        HttpResponse response = httpClient.execute(request);
        String json = EntityUtils.toString(response.getEntity(), "UTF-8");
        System.out.println("JSON: " + json);
        Gson gson = new Gson();
        Map<String, Object> output = gson.fromJson(json, Map.class);
        return output;
    }



}