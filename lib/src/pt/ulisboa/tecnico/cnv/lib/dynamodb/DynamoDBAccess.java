package pt.ulisboa.tecnico.cnv.lib.dynamodb;

import java.util.concurrent.ThreadLocalRandom;
import java.lang.Integer;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClientBuilder;
import com.amazonaws.services.dynamodbv2.document.DynamoDB;
import com.amazonaws.services.dynamodbv2.document.Item;
import com.amazonaws.services.dynamodbv2.document.PutItemOutcome;
import com.amazonaws.services.dynamodbv2.document.Table;
import com.amazonaws.services.dynamodbv2.model.*;
import pt.ulisboa.tecnico.cnv.lib.request.Point;
import pt.ulisboa.tecnico.cnv.lib.request.Request;
import pt.ulisboa.tecnico.cnv.lib.request.RequestMetricData;
import pt.ulisboa.tecnico.cnv.lib.request.Size;

import java.util.ArrayList;

// Code based on:
// https://docs.aws.amazon.com/amazondynamodb/latest/developerguide/AppendixSampleDataCodeJava.html

/**
 * Represents a client object from which the dynamodb can be accessed to store and retrieve request metric data
 */
public class DynamoDBAccess {
    private AmazonDynamoDB client = AmazonDynamoDBClientBuilder.standard().withRegion("us-east-1").build();
    private DynamoDB dynamoDB = new DynamoDB(client);

    private static String requestMetricTableName = "RequestMetricData";
    private static String threadTableName = "Thread";
    private static String replyTableName = "Reply";

    public static void main(String args[]){
        DynamoDBAccess dynamoDBAccess = new DynamoDBAccess();
        try {
            Table table = dynamoDBAccess.dynamoDB.getTable(requestMetricTableName);
            if(table == null){
                dynamoDBAccess.createTable(requestMetricTableName, 10L, 5L, "Id", "N");
            }

            // sample code
            dynamoDBAccess.loadSampleData();

        }
        catch (Exception e) {
            System.err.println("Program failed:");
            System.err.println(e.getMessage());
        }
        System.out.println("Success.");
    }

    public void loadSampleData(){
        Point startingPoint = new Point(25,25);
        Size mapSize = new Size(500,500);
        Request request = new Request(Request.SearchAlgorithm.ASTAR, mapSize, startingPoint);
        RequestMetricData requestMetricData = new RequestMetricData(request, 42, 41);
        insertRequestMetricData(requestMetricData);
    }

    public void insertRequestMetricData(RequestMetricData data) {
        Table table = dynamoDB.getTable(requestMetricTableName);
        Request req = data.getRequest();

        try {
            Item item = new Item().withPrimaryKey("Id", ThreadLocalRandom.current().nextInt(0, Integer.MAX_VALUE))
                    .withString("SearchAlgorithm", req.getSearchAlgorithm().toString())
                    .withInt("MapWidth", req.getMapSize().getWidth())
                    .withInt("MapHeight", req.getMapSize().getHeight())
                    .withInt("StartX", req.getStartingPoint().getX())
                    .withInt("StartY", req.getStartingPoint().getY())
                    .withLong("TimeComplexity", data.getTimeComplexity())
                    .withLong("SpaceComplexity", data.getSpaceComplexity());
            table.putItem(item);
        }catch(Exception e){
            System.err.println("Failed to create item in " + requestMetricTableName);
            System.err.println(e.getMessage());
        }
    }

    private void deleteTable(String tableName) {
        Table table = dynamoDB.getTable(tableName);
        try {
            System.out.println("Issuing DeleteTable request for " + tableName);
            table.delete();
            System.out.println("Waiting for " + tableName + " to be deleted...this may take a while...");
            table.waitForDelete();

        }
        catch (Exception e) {
            System.err.println("DeleteTable request failed for " + tableName);
            System.err.println(e.getMessage());
        }
    }

    private void createTable(String tableName, long readCapacityUnits, long writeCapacityUnits,
                                    String partitionKeyName, String partitionKeyType) {
        createTable(tableName, readCapacityUnits, writeCapacityUnits, partitionKeyName, partitionKeyType, null, null);
    }

    private void createTable(String tableName, long readCapacityUnits, long writeCapacityUnits,
                                    String partitionKeyName, String partitionKeyType, String sortKeyName, String sortKeyType) {

        try {

            ArrayList<KeySchemaElement> keySchema = new ArrayList<KeySchemaElement>();
            keySchema.add(new KeySchemaElement().withAttributeName(partitionKeyName).withKeyType(KeyType.HASH)); // Partition key
            ArrayList<AttributeDefinition> attributeDefinitions = new ArrayList<AttributeDefinition>();
            attributeDefinitions.add(new AttributeDefinition().withAttributeName(partitionKeyName).withAttributeType(partitionKeyType));

            if (sortKeyName != null) {
                keySchema.add(new KeySchemaElement().withAttributeName(sortKeyName).withKeyType(KeyType.RANGE)); // Sort key
                attributeDefinitions.add(new AttributeDefinition().withAttributeName(sortKeyName).withAttributeType(sortKeyType));
            }

            CreateTableRequest request = new CreateTableRequest().withTableName(tableName).withKeySchema(keySchema)
                    .withProvisionedThroughput(new ProvisionedThroughput().withReadCapacityUnits(readCapacityUnits)
                            .withWriteCapacityUnits(writeCapacityUnits));


            // If this is the Reply table, define a local secondary index
            if (replyTableName.equals(tableName)) {
                attributeDefinitions
                        .add(new AttributeDefinition().withAttributeName("PostedBy").withAttributeType("S"));

                ArrayList<LocalSecondaryIndex> localSecondaryIndexes = new ArrayList<LocalSecondaryIndex>();
                localSecondaryIndexes.add(new LocalSecondaryIndex().withIndexName("PostedBy-Index")
                        .withKeySchema(new KeySchemaElement().withAttributeName(partitionKeyName).withKeyType(KeyType.HASH), // Partition key
                                new KeySchemaElement().withAttributeName("PostedBy").withKeyType(KeyType.RANGE)) // Sort key
                        .withProjection(new Projection().withProjectionType(ProjectionType.KEYS_ONLY)));

                request.setLocalSecondaryIndexes(localSecondaryIndexes);
            }

            request.setAttributeDefinitions(attributeDefinitions);

            System.out.println("Issuing CreateTable request for " + tableName);
            Table table = dynamoDB.createTable(request);
            System.out.println("Waiting for " + tableName + " to be created...this may take a while...");
            table.waitForActive();

        }
        catch (Exception e) {
            System.err.println("CreateTable request failed for " + tableName);
            System.err.println(e.getMessage());
        }
    }

}
