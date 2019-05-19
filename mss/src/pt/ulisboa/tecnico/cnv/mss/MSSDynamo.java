package pt.ulisboa.tecnico.cnv.mss;


import com.amazonaws.AmazonClientException;
import com.amazonaws.auth.profile.ProfileCredentialsProvider;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClientBuilder;
import com.amazonaws.services.dynamodbv2.model.*;
import com.amazonaws.services.dynamodbv2.util.TableUtils;

import java.util.HashMap;
import java.util.Map;

public class MSSDynamo {
    private static final String TABLE_NAME = "mss-metrics";
    private static final String ATTRIBUTE_NAME = "id";
    static AmazonDynamoDB dynamoDB;


    public MSSDynamo() throws Exception {
        this.init();
        this.create();
    }

    private static void init() {
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


    public void addItem(int id,String algorithm, String dataset, int startX, int startY, int x0, int y0, int x1, int y1, long timeComplexity) {
        Map<String, AttributeValue> item = new HashMap<String, AttributeValue>();
        item.put("id", new AttributeValue().withN(Integer.toString(id)));
        item.put("SearchAlgorithm", new AttributeValue(algorithm));
        item.put("Dataset", new AttributeValue(dataset));
        item.put("StartX", new AttributeValue().withN(Integer.toString(startX)));
        item.put("StartY", new AttributeValue().withN(Integer.toString(startY)));
        item.put("X0", new AttributeValue().withN(Integer.toString(x0)));
        item.put("Y0", new AttributeValue().withN(Integer.toString(y0)));
        item.put("X1", new AttributeValue().withN(Integer.toString(x1)));
        item.put("Y1", new AttributeValue().withN(Integer.toString(y1)));
        item.put("TimeComplexity", new AttributeValue().withN(Long.toString(timeComplexity)));
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

    public ScanResult search(int id) {
        HashMap<String, Condition> scanFilter = new HashMap<>();
        scanFilter.put("id", createScanFilterConditionInt(id));
        ScanRequest scanRequest = new ScanRequest(TABLE_NAME).withScanFilter(scanFilter);
        ScanResult scanResult = dynamoDB.scan(scanRequest);
        return scanResult;
    }

    public ScanResult search(String searchAlgorithm) {
        HashMap<String, Condition> scanFilter = new HashMap<>();
        scanFilter.put("SearchAlgorithm", createScanFilterConditionStr(searchAlgorithm));
        ScanRequest scanRequest = new ScanRequest(TABLE_NAME).withScanFilter(scanFilter);
        ScanResult scanResult = dynamoDB.scan(scanRequest);
        return scanResult;
    }

    public ScanResult search(String searchAlgorithm, String dataset) {
        HashMap<String, Condition> scanFilter = new HashMap<>();
        scanFilter.put("SearchAlgorithm", createScanFilterConditionStr(searchAlgorithm));
        scanFilter.put("Dataset", createScanFilterConditionStr(dataset));
        ScanRequest scanRequest = new ScanRequest(TABLE_NAME).withScanFilter(scanFilter);
        ScanResult scanResult = dynamoDB.scan(scanRequest);
        return scanResult;
    }

    /**
     * Create a Condition object for string to be used as a filter in a scan of the dynamo db
     */
    private Condition createScanFilterConditionStr(String attributeValue){
        return new Condition()
                .withComparisonOperator(ComparisonOperator.EQ.toString())
                .withAttributeValueList(new AttributeValue().withS(attributeValue));
    }

    /**
     * Create a Condition object for string to be used as a filter in a scan of the dynamo db
     */
    private Condition createScanFilterConditionInt(int attributeValue){
        return new Condition()
                .withComparisonOperator(ComparisonOperator.EQ.toString())
                .withAttributeValueList(new AttributeValue().withN(Integer.toString(attributeValue)));
    }

}
