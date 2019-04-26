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

    public ScanResult search(int id) {
        HashMap<String, Condition> scanFilter = new HashMap<>();
        Condition condition = new Condition()
                .withComparisonOperator(ComparisonOperator.EQ.toString())
                .withAttributeValueList(new AttributeValue().withN(Integer.toString(id)));
        scanFilter.put("id", condition);
        ScanRequest scanRequest = new ScanRequest(TABLE_NAME).withScanFilter(scanFilter);
        ScanResult scanResult = dynamoDB.scan(scanRequest);
        return scanResult;
    }

    public ScanResult search(String searchAlgorithm, int mapWidth){
        HashMap<String, Condition> scanFilter = new HashMap<>();
        Condition searchAlgorithmCondition = new Condition()
                .withComparisonOperator(ComparisonOperator.EQ.toString())
                .withAttributeValueList(new AttributeValue().withS(searchAlgorithm));
        Condition mapCondition = new Condition()
                .withComparisonOperator(ComparisonOperator.EQ.toString())
                .withAttributeValueList(new AttributeValue().withN(Integer.toString(mapWidth)));
        scanFilter.put("SearchAlgorithm", searchAlgorithmCondition);
        scanFilter.put("MapWidth", mapCondition);
        ScanRequest scanRequest = new ScanRequest(TABLE_NAME).withScanFilter(scanFilter);
        ScanResult scanResult = dynamoDB.scan(scanRequest);
        return scanResult;
    }

}
