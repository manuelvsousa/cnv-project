package pt.ulisboa.tecnico.cnv.mss;

import com.amazonaws.services.dynamodbv2.model.ScanResult;


public class MSSClient {
    private MSSDynamo mssDynamo;
    private static MSSClient instance;

    private MSSClient() throws Exception {
        mssDynamo = new MSSDynamo();
    }

    public static void main(String[] args)  {
        MSSClient.getInstance().addMetrics("1.1.1.1","BFS",123,123,123,123,123);
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
    public void addMetrics(String ip, String algorithm, int mapWidth, int startX, int startY, int timeComplexity, int spaceComplexity) {
        mssDynamo.addItem(ip,
                algorithm,
                mapWidth,
                startX,
                startY,
                timeComplexity,
                spaceComplexity);

    }

    public void getMetrics(int id) {
        ScanResult scanResult = mssDynamo.search(id);
        System.out.println(scanResult.getItems().toString());
    }


}