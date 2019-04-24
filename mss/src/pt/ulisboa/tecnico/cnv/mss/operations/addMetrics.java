package pt.ulisboa.tecnico.cnv.mss.operations;

import com.google.gson.JsonObject;
import com.sun.net.httpserver.HttpExchange;
import pt.ulisboa.tecnico.cnv.mss.MSSDynamo;

import java.io.IOException;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.Map;

public class addMetrics extends AbstractHandler {
    private MSSDynamo mssDynamo;

    public addMetrics(MSSDynamo dyn) throws Exception {
        this.mssDynamo = dyn;
    }

    @Override
    public void handle(HttpExchange t) throws IOException {
        Map<String, String> params = queryToMap(t.getRequestURI().getQuery());
        JsonObject innerObject = new JsonObject();
        Map<String, Object> map = new HashMap<>();
        if (!params.containsKey("ip") || !params.containsKey("algorithm") || !params.containsKey("mapwidth") || !params.containsKey("startx") || !params.containsKey("starty") || !params.containsKey("timecomplexity") || !params.containsKey("spacecomplexity")) {
            innerObject.addProperty("message", "ip or algorithm or mapwidth or startx or starty or timecomplexity or spacecomplexity are null");
            innerObject.addProperty("success", false);
        } else {
            mssDynamo.addItem(params.get("ip"),
                    params.get("algorithm"),
                    Integer.parseInt(params.get("mapwidth")),
                    Integer.parseInt(params.get("startx")),
                    Integer.parseInt(params.get("starty")),
                    Integer.parseInt(params.get("timecomplexity")),
                    Integer.parseInt(params.get("spacecomplexity")));
        }
        innerObject.addProperty("success", true);
        String response = innerObject.toString();
        t.sendResponseHeaders(200, response.length());
        OutputStream os = t.getResponseBody();
        os.write(response.getBytes());
        os.close();
    }
}