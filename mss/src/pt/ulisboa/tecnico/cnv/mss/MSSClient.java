package pt.ulisboa.tecnico.cnv.mss;

import com.amazonaws.services.dynamodbv2.model.ScanResult;
import pt.ulisboa.tecnico.cnv.lib.request.Request;


public class MSSClient {
    private MSSDynamo mssDynamo;
    private static MSSClient instance;

    private MSSClient() throws Exception {
        mssDynamo = new MSSDynamo();
    }

    public static void main(String[] args)  {
        MSSClient.getInstance().addMetrics("BFS",123,123,123);
        MSSClient.getInstance().getMetrics(123);
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

    public void addMetrics(String algorithm, int startX, int startY, long timeComplexity) {
        mssDynamo.addItem(algorithm, startX, startY, timeComplexity);
    }

    public String getMetrics(int id) {
        ScanResult scanResult = mssDynamo.search(id);
        return scanResult.getItems().toString();
    }

    public String getMetrics(Request.SearchAlgorithm searchAlgorithm) {
        ScanResult scanResult = mssDynamo.search(searchAlgorithm.toString());
        return scanResult.getItems().toString();
    }


}