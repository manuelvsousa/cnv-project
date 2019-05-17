package pt.ulisboa.tecnico.cnv.mssclient;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.internal.LinkedTreeMap;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.util.EntityUtils;

import javax.swing.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;


public class MSSClient {
    private String mssServerIp;
    private int mssServerPort;

    public MSSClient(String mssServerIp, int mssServerPort) {
        this.mssServerIp = mssServerIp;
        this.mssServerPort = mssServerPort;
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

    public List<JsonObject>  getMetrics(int id) throws Exception {
        return getMetrics("http://" + this.mssServerIp + ":" + this.mssServerPort + "/getMetrics?id=" + id);
    }

    public List<JsonObject>  getMetrics(String searchAlgorithm, int mapWidth) throws Exception{
        return getMetrics("http://" + this.mssServerIp + ":" + this.mssServerPort + "/getMetrics?searchAlgo=" + searchAlgorithm + "&mapWidth=" + mapWidth);
    }

    private List<JsonObject> getMetrics(String targetUrl) throws Exception{
        HttpClient httpClient = HttpClientBuilder.create().build();
        HttpGet request = new HttpGet(targetUrl);
        HttpResponse response = httpClient.execute(request);
        String json = EntityUtils.toString(response.getEntity(), "UTF-8");
        System.out.println("JSON: " + json);
        Gson gson = new Gson();
        List<LinkedTreeMap<String, Object>> output = gson.fromJson(json, List.class);

        return linkedTreeMapsToJsonObjects(output);
    }

    /**
     * Convert a list of linkedtreemaps to list of gson jsonobjects
     * @param linkedTreeMaps
     * @return
     */
    private List<JsonObject> linkedTreeMapsToJsonObjects(List<LinkedTreeMap<String, Object>> linkedTreeMaps){
        List<JsonObject> jsonObjects = new ArrayList<>();
        for(LinkedTreeMap linkedTreeMap : linkedTreeMaps){
            jsonObjects.add(linkedTreeMaptoJsonObject(linkedTreeMap));
        }
        return jsonObjects;
    }

    /**
     * Convert linked tree map to GSON JsonObject
     * @param linkedTreeMap
     * @return
     */
    private JsonObject linkedTreeMaptoJsonObject(LinkedTreeMap linkedTreeMap){
        return (new Gson()).toJsonTree(linkedTreeMap).getAsJsonObject();
    }



}