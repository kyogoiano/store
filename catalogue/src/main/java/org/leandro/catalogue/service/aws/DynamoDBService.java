package org.leandro.catalogue.service.aws;

import lombok.extern.slf4j.Slf4j;
import software.amazon.awssdk.services.dynamodb.DynamoDbAsyncClient;
import software.amazon.awssdk.services.dynamodb.model.*;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;
import java.util.function.Supplier;

@Singleton
@Slf4j
public class DynamoDBService {

    @Inject
    private CatalogueConfigurationDynamoDB configuration;

    @Inject
    private DynamoDbAsyncClient client;

    private static final int DEFAULT_WAIT_TIMEOUT = 10 * 60 * 1000; //10 minutes

    private static final int DEFAULT_WAIT_INTERVAL = 5 * 1000; //5 seconds

    public CompletableFuture<Boolean> createTableIfNeeded() {
        final ListTablesRequest request = ListTablesRequest.builder().exclusiveStartTableName(configuration.getTableName()).build();
        final CompletableFuture<ListTablesResponse> listTableResponse = client.listTables(request);

        final CompletableFuture<CreateTableResponse> createTableRequest = listTableResponse
                .thenCompose(response -> {
                    boolean tableExist = response.tableNames().contains(configuration.getTableName());
                    if (!tableExist) {
                        return createTable();
                    } else {
                        return CompletableFuture.completedFuture(null);
                    }
                });

        return createTableRequest.thenCompose( createTableResponse -> waitUntilTableActiveAsync(client, configuration.getTableName()))
                .exceptionally( throwable -> {
                    if (throwable.getCause() instanceof ResourceInUseException) {
                        log.info("Reused existing table");
                        return true;
                    } else {
                        log.error("Failed table creation", throwable);
                        return false;
                    }
        });
    }

    public CompletableFuture<Boolean> dropTable(final String tableName) {
        final DeleteTableRequest request = DeleteTableRequest.builder().tableName(tableName).build();

        return client.deleteTable(request).thenComposeAsync(result -> CompletableFuture.completedFuture(true)).exceptionally( throwable -> {
            if (throwable.getCause() instanceof ResourceNotFoundException) {
                log.info("Non existing table, already droped");
                return true;
            } else {
                log.error("Failed dropping table", throwable);
                return false;
            }
        });
    }

    public CompletableFuture<ListTablesResponse> listTables(){
        final ListTablesRequest request = ListTablesRequest.builder().build();
        return client.listTables(request);
    }

    public CompletableFuture<DescribeTableResponse> describeTable(final String tableName){
        return client.describeTable(DescribeTableRequest.builder().tableName(tableName).build());
    }

    private CompletableFuture<CreateTableResponse> createTable() {

        final CreateTableRequest request = CreateTableRequest.builder()
                .tableName(configuration.getTableName())
                .attributeDefinitions(AttributeDefinition.builder()
                        .attributeName(configuration.getCatalogueId())
                        .attributeType(ScalarAttributeType.S).build())
                .attributeDefinitions(AttributeDefinition.builder()
                        .attributeName(configuration.getCatalogueTitle())
                        .attributeType(ScalarAttributeType.S).build())
                .attributeDefinitions(AttributeDefinition.builder()
                        .attributeName(configuration.getCatalogueVendor())
                        .attributeType(ScalarAttributeType.S).build())
                .keySchema(KeySchemaElement.builder()
                        .keyType(KeyType.HASH)
                        .attributeName(configuration.getCatalogueId()).build())
                .keySchema(KeySchemaElement.builder()
                        .keyType(KeyType.HASH)
                        .attributeName(configuration.getCatalogueTitle()).build())
                .keySchema(KeySchemaElement.builder()
                        .keyType(KeyType.HASH)
                        .attributeName(configuration.getCatalogueVendor()).build())
                .provisionedThroughput(ProvisionedThroughput.builder()
                        .readCapacityUnits(10L)
                        .writeCapacityUnits(10L)
                        .build())
                .build();

        return client.createTable(request);
    }

    private static CompletableFuture<Boolean> waitUntilTableActiveAsync(final DynamoDbAsyncClient asyncClient,
                                                                        final String tableName) {
        final long startTime = System.currentTimeMillis();
        final long endTime = startTime + DEFAULT_WAIT_TIMEOUT;

        return retryAsync(() -> asyncClient.describeTable(DescribeTableRequest.builder().tableName(tableName).build()), endTime);
    }

    private static CompletableFuture<Boolean> retryAsync(final Supplier<CompletableFuture<DescribeTableResponse>> action,
                                                         final long endTime) {

        return action.get()
                .thenComposeAsync(result -> {
                    if (result.table().tableStatus() == TableStatus.ACTIVE) {
                        return CompletableFuture.completedFuture(true);
                    } else {
                        try {
                            Thread.sleep(DEFAULT_WAIT_INTERVAL);
                        } catch (InterruptedException e) {
                            Thread.currentThread().interrupt();
                        }
                        log.info("Async table - Retry table created status");
                        if (System.currentTimeMillis() < endTime) {
                            return retryAsync(action, endTime);
                        } else {
                            return CompletableFuture.completedFuture(false);
                        }
                    }
                });
    }
}
