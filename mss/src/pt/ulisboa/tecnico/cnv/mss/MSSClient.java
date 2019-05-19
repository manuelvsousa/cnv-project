package pt.ulisboa.tecnico.cnv.mss;

import com.amazonaws.services.dynamodbv2.model.AttributeValue;
import com.amazonaws.services.dynamodbv2.model.ScanResult;
import pt.ulisboa.tecnico.cnv.lib.request.Point;
import pt.ulisboa.tecnico.cnv.lib.request.Request;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class MSSClient {
    private MSSDynamo mssDynamo;
    private static MSSClient instance;

    private MSSClient() throws Exception {
        mssDynamo = new MSSDynamo();
    }

    public static void main(String[] args)  {
        //MSSClient.getInstance().addMetrics(44,"BFS",123,123,123);
        MSSClient.getInstance().getMetrics(44);
    }

    public static MSSClient getInstance() {
        try{
            if (instance == null){
                instance = new MSSClient();
            }
        } catch (Exception e){
            e.printStackTrace();
        }
        return instance;
    }

    public void addMetrics(int id, String algorithm, String dataset, Point start, Point upperLeft, Point lowerRight, String timeComplexity) {
        mssDynamo.addItem(id, algorithm, dataset, start.getX(), start.getY()
                , upperLeft.getX(), upperLeft.getY(), lowerRight.getX(), lowerRight.getY(), Long.parseLong(timeComplexity));
    }

    public String getMetrics(int id) {
        ScanResult scanResult = mssDynamo.search(id);
        return scanResult.getItems().toString();
    }

    public List<Request> getMetrics(Request.SearchAlgorithm searchAlgorithm) {
        ScanResult scanResult = mssDynamo.search(searchAlgorithm.toString());
        return buildRequestsFromDynamoItems(scanResult.getItems());

    }

    private List<Request> buildRequestsFromDynamoItems(List<Map<String, AttributeValue>> items){
        List<Request> requests = new ArrayList<>();
        for(Map<String, AttributeValue> item : items){
            requests.add(buildRequestFromDynamoItem(item));
        }
        return requests;
    }

    private Request buildRequestFromDynamoItem(Map<String, AttributeValue> item){
        Request request = new Request(Request.SearchAlgorithm.valueOf(item.get("SearchAlgorithm").getS()),
                item.get("Dataset").getS(),
                new Point(Integer.parseInt(item.get("StartX").getN()), Integer.parseInt(item.get("StartY").getN())),
                new Point(Integer.parseInt(item.get("X0").getN()), Integer.parseInt(item.get("Y0").getN())),
                new Point(Integer.parseInt(item.get("X1").getN()), Integer.parseInt(item.get("Y1").getN())),
                Long.parseLong(item.get("TimeComplexity").getN()));
        request.setId(Integer.parseInt(item.get("id").getN()));
        return request;
    }



}