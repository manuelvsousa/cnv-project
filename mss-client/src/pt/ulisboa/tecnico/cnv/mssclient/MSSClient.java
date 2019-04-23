package pt.ulisboa.tecnico.cnv.mssclient;

import com.amazonaws.AmazonServiceException;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.util.EntityUtils;

import java.util.Map;


public class MSSClient {
    private String ip;
    private int port;

    public MSSClient(String ip, int port) {
        this.ip = ip;
        this.port = port;
    }

    public static void main(String[] args) throws Exception {
        try {
            MSSClient msscli = new MSSClient("127.0.0.1", 8000);
            System.out.println(msscli.getMetrics(7));
            msscli.addMetrics("0.0.1.7", "caccacacacca", 6, 6, 3, 9, 9);
            System.out.println(msscli.getMetrics(9));
        } catch (AmazonServiceException ase) {

        }
    }

    public void addMetrics(String ip, String algorithm, int mapWidth, int startX, int startY, int timeComplexity, int spaceComplexity) throws Exception {
        JsonObject innerObject = new JsonObject();

        innerObject.addProperty("ip", ip);
        innerObject.addProperty("SearchAlgorithm", algorithm);
        innerObject.addProperty("MapWidth", mapWidth);
        innerObject.addProperty("StartX", mapWidth);
        innerObject.addProperty("StartY", startX);
        innerObject.addProperty("TimeComplexity", timeComplexity);
        innerObject.addProperty("SpaceComplexity", spaceComplexity);
        HttpClient httpClient = HttpClientBuilder.create().build();
        HttpGet request = new HttpGet("http://" + this.ip + ":" + this.port + "/addMetrics?ip=" + ip + "&algorithm=" + algorithm + "&mapwidth=" + mapWidth + "&startx=" + startX + "&starty=" + startY + "&timecomplexity=" + timeComplexity + "&spacecomplexity=" + spaceComplexity);
        HttpResponse response = httpClient.execute(request);
        System.out.println(response);
    }


    public Map<String, Object> getMetrics(int id) throws Exception {
        HttpClient httpClient = HttpClientBuilder.create().build();
        HttpGet request = new HttpGet("http://" + this.ip + ":" + this.port + "/getMetrics?id=" + id);
        HttpResponse response = httpClient.execute(request);
        String json = EntityUtils.toString(response.getEntity(), "UTF-8");
        Gson gson = new Gson(); // Or use new GsonBuilder().create();
        Map<String, Object> output = gson.fromJson(json, Map.class);
        return output;

    }

}