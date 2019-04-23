package pt.ulisboa.tecnico.cnv.mss.operations;

import com.amazonaws.services.dynamodbv2.model.AttributeValue;
import com.amazonaws.services.dynamodbv2.model.ScanResult;
import com.google.gson.JsonObject;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
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
        JsonObject innerObject = new JsonObject();

        if(!params.containsKey("id")){
            innerObject.addProperty("message", "id parameter does not exist");
            innerObject.addProperty("success", false);
        } else {
            ScanResult scanResult = mssDynamo.search(Integer.parseInt(params.get("id")));
            for (Map<String, AttributeValue> item : scanResult.getItems()) {
                innerObject.addProperty("ip", item.get("ip").getS());
                innerObject.addProperty("SearchAlgorithm", item.get("SearchAlgorithm").getS());
                innerObject.addProperty("MapWidth", Integer.parseInt(item.get("MapWidth").getN()));
                innerObject.addProperty("StartX", Integer.parseInt(item.get("StartX").getN()));
                innerObject.addProperty("StartY", Integer.parseInt(item.get("StartY").getN()));
                innerObject.addProperty("TimeComplexity", Integer.parseInt(item.get("TimeComplexity").getN()));
                innerObject.addProperty("SpaceComplexity", Integer.parseInt(item.get("SpaceComplexity").getN()));
            }
        }
        String response = innerObject.toString();
        t.sendResponseHeaders(200, response.length());
        OutputStream os = t.getResponseBody();
        os.write(response.getBytes());
        os.close();
    }
}