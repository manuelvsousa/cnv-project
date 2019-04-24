package pt.ulisboa.tecnico.cnv.mss.operations;

import com.amazonaws.services.dynamodbv2.model.AttributeValue;
import com.amazonaws.services.dynamodbv2.model.ScanResult;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.sun.net.httpserver.HttpExchange;
import pt.ulisboa.tecnico.cnv.mss.MSSDynamo;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Map;

public class getMetrics extends AbstractHandler {
    private MSSDynamo mssDynamo;

    public getMetrics(MSSDynamo dyn) throws Exception {
        this.mssDynamo = dyn;
    }

    @Override
    public void handle(HttpExchange t) throws IOException {
        Map<String, String> params = queryToMap(t.getRequestURI().getQuery());
        JsonElement innerObject = null;

        if(params.containsKey("id")){
            ScanResult scanResult = mssDynamo.search(Integer.parseInt(params.get("id")));
            if(scanResult.getItems().size() > 0 ){
                System.out.println("test2)");
                innerObject = scanResultItemToJsonObject(scanResult.getItems().get(0));
            }
        }else if(params.containsKey("searchAlgo") && params.containsKey("mapWidth")){
            ScanResult scanResult = mssDynamo.search(params.get("searchAlgorithm"), Integer.parseInt("mapWidth"));
            innerObject = new JsonArray();
            for (Map<String, AttributeValue> item : scanResult.getItems()) {
                ((JsonArray) innerObject).add(scanResultItemToJsonObject(item));
            }

        }

        if(innerObject == null){
            innerObject = new JsonObject();
            ((JsonObject) innerObject).addProperty("message", "Invalid Request or no results found.");
            ((JsonObject) innerObject).addProperty("success", false);
        }

        String response = innerObject.toString();
        t.sendResponseHeaders(200, response.length());
        OutputStream os = t.getResponseBody();
        os.write(response.getBytes());
        os.close();
    }

    /**
     * Create a JsonObject from a map retrieved from a mssDynamo query ScanResult
     * @param item Map<String, AttributeValue>
     * @return JsonObject
     */
    private JsonObject scanResultItemToJsonObject(Map<String, AttributeValue> item){
        JsonObject jsonObject = new JsonObject();

        jsonObject.addProperty("ip", item.get("ip").getS());
        jsonObject.addProperty("SearchAlgorithm", item.get("SearchAlgorithm").getS());
        jsonObject.addProperty("MapWidth", Integer.parseInt(item.get("MapWidth").getN()));
        jsonObject.addProperty("StartX", Integer.parseInt(item.get("StartX").getN()));
        jsonObject.addProperty("StartY", Integer.parseInt(item.get("StartY").getN()));
        jsonObject.addProperty("TimeComplexity", Integer.parseInt(item.get("TimeComplexity").getN()));
        jsonObject.addProperty("SpaceComplexity", Integer.parseInt(item.get("SpaceComplexity").getN()));

        return jsonObject;
    }
}