package org.leandro.catalogue.service.aws;

import io.reactivex.rxjava3.core.Flowable;
import io.reactivex.rxjava3.core.Single;
import lombok.extern.slf4j.Slf4j;
import org.leandro.catalogue.integrated.controller.entity.CatalogueEntity;
import org.leandro.catalogue.service.CatalogueService;
import software.amazon.awssdk.services.dynamodb.DynamoDbAsyncClient;
import software.amazon.awssdk.services.dynamodb.model.*;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

@Slf4j
@Singleton
public class CatalogueServiceDynamoDBImpl implements CatalogueService<CatalogueEntity> {

    @Inject
    private DynamoDbAsyncClient client;

    @Inject
    private DynamoDBService dynamoDBService;

    @Inject
    private CatalogueConfigurationDynamoDB configuration;

    @Override
    public Flowable<List<CatalogueEntity>> findAll(){

        return Flowable.fromCompletionStage(processScanRequest(this.getCollectionRequest().build())).onTerminateDetach();
    }

    @Override
    public Flowable<List<CatalogueEntity>> findByVendorName(final String vendor){
        final Map<String,String> expressionAttributesNames = new HashMap<>(1);
        expressionAttributesNames.put("#vendor",configuration.getCatalogueVendor());
        final Map<String,AttributeValue> expressionAttributeValues = new HashMap<>(1);
        expressionAttributeValues.put(":vendor", AttributeValue.builder().s(vendor).build());

        final QueryRequest queryRequest = this.getQueryRequest()
                .keyConditionExpression("#vendor = :vendor")
                .expressionAttributeNames(expressionAttributesNames)
                .expressionAttributeValues(expressionAttributeValues).build();

        return Flowable.fromCompletionStage(processQueryRequest(queryRequest)).onTerminateDetach();
    }


    @Override
    public Flowable<List<CatalogueEntity>> findByTitle(final String title){

        final Map<String,String> expressionAttributesNames = new HashMap<>(1);
        expressionAttributesNames.put("#title",configuration.getCatalogueTitle());
        final Map<String,AttributeValue> expressionAttributeValues = new HashMap<>(1);
        expressionAttributeValues.put(":title", AttributeValue.builder().s(title).build());

        final QueryRequest queryRequest = this.getQueryRequest()
                .keyConditionExpression("#title = :title")
                .expressionAttributeNames(expressionAttributesNames)
                .expressionAttributeValues(expressionAttributeValues).build();

        return Flowable.fromCompletionStage(processQueryRequest(queryRequest)).onTerminateDetach();
    }

    @Override
    public Single<CatalogueEntity> save(final CatalogueEntity catalogue){
        try {
            final PutItemResponse putItemResponse = saveEvent(catalogue).get();
            log.info("Succeeded saved entity on dynamodb, put item response as follow: {}", putItemResponse.toString());
            return Single.just(catalogue);
        } catch (InterruptedException | ExecutionException e) {
            log.error("Error saving catalogue entity on dynamodb with the current cause", e.getCause());
            return Single.error(e);
        }
    }

    private CompletableFuture<PutItemResponse> saveEvent(final CatalogueEntity catalogue) {
        final Map<String, AttributeValue> item = new HashMap<>();
        item.put(configuration.getCatalogueId(), AttributeValue.builder().s(catalogue.getProductId().toString()).build());
        item.put(configuration.getCatalogueVendor(), AttributeValue.builder().s(catalogue.getVendor()).build());
        item.put(configuration.getCatalogueTitle(), AttributeValue.builder().s(catalogue.getTitle()).build());
        item.put(configuration.getCatalogueDescription(), AttributeValue.builder().s(catalogue.getDescription()).build());
        final PutItemRequest putItemRequest = PutItemRequest.builder()
                .tableName(configuration.getTableName())
                .item(item).conditionExpression("attribute_not_exists(catalogue_id)")
                .build();

        return client.putItem(putItemRequest);
    }


    private ScanRequest.Builder getCollectionRequest() {
        return ScanRequest.builder().tableName(configuration.getTableName());
    }

    private QueryRequest.Builder getQueryRequest() {
        return QueryRequest.builder().tableName(configuration.getTableName());
    }


    private GetItemRequest.Builder getItemRequest() {
        return GetItemRequest.builder().tableName(configuration.getTableName());
    }

    private CompletableFuture<List<CatalogueEntity>> processScanRequest(final ScanRequest scanRequest){
        return
            client.scan(scanRequest).thenApply(response -> {
                if (!response.hasItems()) {
                    return null;
                } else {
                    final List<CatalogueEntity> catalogueEntities = new ArrayList<>(response.items().size());
                    response.items().forEach( item -> {
                        String vendor = item.get(configuration.getCatalogueVendor()).s();
                        String title = item.get(configuration.getCatalogueTitle()).s();
                        String description = item.get(configuration.getCatalogueDescription()).s();
                        catalogueEntities.add(new CatalogueEntity(vendor, title, description));
                    });

                    return catalogueEntities;
                }
            });
    }

    private CompletableFuture<List<CatalogueEntity>> processQueryRequest(final QueryRequest queryRequest){
        return
                client.query(queryRequest).thenApply(response -> {
                    if (!response.hasItems()) {
                        return null;
                    } else {
                        final List<Map<String, AttributeValue>> itemsMap = response.items();
                        final List<CatalogueEntity> catalogueEntities = new ArrayList<>(itemsMap.size());
                        itemsMap.forEach( itemMap -> {
                            final String title = itemMap.get(configuration.getCatalogueTitle()).s();
                            final String description = itemMap.get(configuration.getCatalogueDescription()).s();
                            final String vendor = itemMap.get(configuration.getCatalogueVendor()).s();
                            catalogueEntities.add(new CatalogueEntity(vendor, title, description));
                        });
                        return catalogueEntities;
                    }
                });
    }

}
