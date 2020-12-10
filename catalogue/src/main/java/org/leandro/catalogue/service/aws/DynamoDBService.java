package org.leandro.catalogue.service.aws;

import software.amazon.awssdk.services.dynamodb.DynamoDbAsyncClient;
import software.amazon.awssdk.services.dynamodb.model.*;

import javax.annotation.PostConstruct;
import javax.inject.Singleton;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

@Singleton
public class DynamoDBService {
    public static final String TABLE_NAME = "events";
    public static final String ID_COLUMN = "id";
    public static final String BODY_COLUMN = "body";

    private final DynamoDbAsyncClient client;

    public DynamoDBService(DynamoDbAsyncClient client) {
        this.client = client;
    }

    @PostConstruct
    public void createTableIfNeeded() throws ExecutionException, InterruptedException {
        final ListTablesRequest request = ListTablesRequest.builder().exclusiveStartTableName(TABLE_NAME).build();
        final CompletableFuture<ListTablesResponse> listTableResponse = client.listTables(request);

        final CompletableFuture<CreateTableResponse> createTableRequest = listTableResponse
                .thenCompose(response -> {
                    boolean tableExist = response.tableNames().contains(TABLE_NAME);
                    if (!tableExist) {
                        return createTable();
                    } else {
                        return CompletableFuture.completedFuture(null);
                    }
                });

        //Wait in synchronous manner for table creation
        createTableRequest.get();
    }

    private CompletableFuture<CreateTableResponse> createTable() {

        final CreateTableRequest request = CreateTableRequest.builder()
                .tableName(TABLE_NAME)

                .keySchema(KeySchemaElement.builder().attributeName(ID_COLUMN).keyType(KeyType.HASH).build())
                .attributeDefinitions(AttributeDefinition.builder().attributeName(ID_COLUMN).attributeType(ScalarAttributeType.S).build())
                .billingMode(BillingMode.PAY_PER_REQUEST)
                .build();

        return client.createTable(request);
    }
}
