package pt.ulisboa.tecnico.cnv.mss;


import com.amazonaws.AmazonClientException;
import com.amazonaws.AmazonServiceException;
import com.amazonaws.auth.profile.ProfileCredentialsProvider;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClientBuilder;
import com.amazonaws.services.dynamodbv2.model.*;
import com.amazonaws.services.dynamodbv2.util.TableUtils;

import java.util.HashMap;
import java.util.Map;

public class MSSDynamo {


    private static final String TABLE_NAME = "mss-metrics";
    private static final String ATTRIBUTE_NAME = "ip";
    static AmazonDynamoDB dynamoDB;


    public MSSDynamo() throws Exception {
        this.init();
        this.create();
    }

    private static void init() throws Exception {
        ProfileCredentialsProvider credentialsProvider = new ProfileCredentialsProvider();
        try {
            credentialsProvider.getCredentials();
        } catch (Exception e) {
            throw new AmazonClientException(
                    "Cannot load the credentials from the credential profiles file. " +
                            "Please make sure that your credentials file is at the correct " +
                            "location (~/.aws/credentials), and is in valid format.",
                    e);
        }
        dynamoDB = AmazonDynamoDBClientBuilder.standard()
                .withCredentials(credentialsProvider)
                .withRegion("us-east-1")
                .build();
    }

    public static void main(String[] args) throws Exception {
        MSSDynamo mssD = new MSSDynamo();
        try {
            mssD.addItem("1.1.1.1", "olaola", 1, 1, 3, 4, 5);
            mssD.addItem("0.0.1.1", "adeusadeus", 2, 3, 3, 7, 9);
            System.out.println(mssD.search(4).toString());
            System.out.println(mssD.search(5).toString());

        } catch (AmazonServiceException ase) {
            System.out.println("Caught an AmazonServiceException, which means your request made it "
                    + "to AWS, but was rejected with an error response for some reason.");
            System.out.println("Error Message:    " + ase.getMessage());
            System.out.println("HTTP Status Code: " + ase.getStatusCode());
            System.out.println("AWS Error Code:   " + ase.getErrorCode());
            System.out.println("Error Type:       " + ase.getErrorType());
            System.out.println("Request ID:       " + ase.getRequestId());
        } catch (AmazonClientException ace) {
            System.out.println("Caught an AmazonClientException, which means the client encountered "
                    + "a serious internal problem while trying to communicate with AWS, "
                    + "such as not being able to access the network.");
            System.out.println("Error Message: " + ace.getMessage());
        }
    }

    public void addItem(String ip, String algorithm, int mapWidth, int startX, int startY, int timeComplexity, int spaceComplexity) {
        Map<String, AttributeValue> item = new HashMap<String, AttributeValue>();
        item.put("ip", new AttributeValue(ip));
        item.put("SearchAlgorithm", new AttributeValue(algorithm));
        item.put("MapWidth", new AttributeValue().withN(Integer.toString(mapWidth)));
        item.put("StartX", new AttributeValue().withN(Integer.toString(startX)));
        item.put("StartY", new AttributeValue().withN(Integer.toString(startY)));
        item.put("TimeComplexity", new AttributeValue().withN(Integer.toString(timeComplexity)));
        item.put("SpaceComplexity", new AttributeValue().withN(Integer.toString(spaceComplexity)));
        PutItemRequest putItemRequest = new PutItemRequest(TABLE_NAME, item);
        dynamoDB.putItem(putItemRequest);
    }

    private void create() throws Exception {
        CreateTableRequest createTableRequest = new CreateTableRequest().withTableName(TABLE_NAME)
                .withKeySchema(new KeySchemaElement().withAttributeName(ATTRIBUTE_NAME).withKeyType(KeyType.HASH))
                .withAttributeDefinitions(new AttributeDefinition().withAttributeName(ATTRIBUTE_NAME).withAttributeType(ScalarAttributeType.S))
                .withProvisionedThroughput(new ProvisionedThroughput().withReadCapacityUnits(1L).withWriteCapacityUnits(1L));

        TableUtils.createTableIfNotExists(dynamoDB, createTableRequest);
        TableUtils.waitUntilActive(dynamoDB, TABLE_NAME);
    }

    public ScanResult search(int timeComplexity) {
        HashMap<String, Condition> scanFilter = new HashMap<>();
        Condition condition = new Condition()
                .withComparisonOperator(ComparisonOperator.EQ.toString())
                .withAttributeValueList(new AttributeValue().withN(Integer.toString(timeComplexity)));
        scanFilter.put("TimeComplexity", condition);
        ScanRequest scanRequest = new ScanRequest(TABLE_NAME).withScanFilter(scanFilter);
        ScanResult scanResult = dynamoDB.scan(scanRequest);
        return scanResult;
    }

}
