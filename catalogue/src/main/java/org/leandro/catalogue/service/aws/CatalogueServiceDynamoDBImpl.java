package org.leandro.catalogue.service.aws;

import com.mongodb.reactivestreams.client.FindPublisher;
import io.reactivex.Flowable;
import io.reactivex.Single;
import io.reactivex.SingleSource;
import org.leandro.catalogue.integrated.controller.entity.CatalogueEntity;
import org.leandro.catalogue.service.CatalogueService;
import software.amazon.awssdk.services.dynamodb.DynamoDbAsyncClient;
import software.amazon.awssdk.services.dynamodb.model.*;

import javax.inject.Inject;
import javax.inject.Singleton;

import java.util.*;
import java.util.concurrent.CompletableFuture;

import static com.mongodb.client.model.Filters.eq;

@Singleton
public class CatalogueServiceDynamoDBImpl implements CatalogueService<CatalogueEntity> {

    @Inject
    private DynamoDbAsyncClient client;

    @Inject
    private CatalogueConfigurationDynamoDB configuration;

    @Override
    public Flowable<List<CatalogueEntity>> findAll(){

        return Flowable.fromFuture(getFutureCollection(this.getCollectionRequest().build())).onTerminateDetach();
    }


    @Override
    public Flowable<List<CatalogueEntity>> findByVendorName(final String vendorName){
        final Map<String, Condition> filter = new HashMap<>();
        final AttributeValue attributeValue = AttributeValue.builder().s(vendorName).build();
        filter.put(configuration.getCatalogueVendor(), Condition.builder().attributeValueList(attributeValue).build());

        final ScanRequest scanRequest = this.getCollectionRequest().scanFilter(filter).scanFilter(filter).build();

        return Flowable.fromFuture(getFutureCollection(scanRequest)).onTerminateDetach();
    }


    @Override
    public Flowable<CatalogueEntity> find(final String title){
        final Map<String, AttributeValue> key = new HashMap<>(1);
        final AttributeValue attributeValue = AttributeValue.builder().s(title).build();
        key.put(configuration.getCatalogueTitle(), attributeValue);

        final GetItemRequest itemRequest = this.getItemRequest().key(key).build();

        return Flowable.fromFuture(getFuture(itemRequest)).onTerminateDetach();
    }

    @Override
    public SingleSource<CatalogueEntity> save(final CatalogueEntity catalogue){
        return Single.fromFuture(saveEvent(catalogue))
                .map(success -> catalogue);
    }

    private CompletableFuture<PutItemResponse> saveEvent(final CatalogueEntity catalogue) {
        final Map<String, AttributeValue> item = new HashMap<>();
        item.put(configuration.getCatalogueVendor(), AttributeValue.builder().s(catalogue.getVendor()).build());
        item.put(configuration.getCatalogueTitle(), AttributeValue.builder().s(catalogue.getTitle()).build());
        item.put(configuration.getCatalogueDescription(), AttributeValue.builder().s(catalogue.getDescription()).build());
        final PutItemRequest putItemRequest = PutItemRequest.builder()
                .tableName(configuration.getTableName())
                .item(item)
                .build();

        return client.putItem(putItemRequest);
    }


    private ScanRequest.Builder getCollectionRequest() {
        return ScanRequest.builder().tableName(configuration.getTableName());
    }

    private GetItemRequest.Builder getItemRequest() {
        return GetItemRequest.builder().tableName(configuration.getTableName());
    }

    private CompletableFuture<List<CatalogueEntity>> getFutureCollection(final ScanRequest scanRequest){
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

    private CompletableFuture<CatalogueEntity> getFuture(final GetItemRequest itemRequest){
        return
                client.getItem(itemRequest).thenApply(response -> {
                    if (!response.hasItem()) {
                        return null;
                    } else {
                        final Map<String, AttributeValue> itemAttr = response.item();
                        final String title = itemAttr.get(configuration.getCatalogueTitle()).s();
                        final String description = itemAttr.get(configuration.getCatalogueDescription()).s();
                        final String vendor = itemAttr.get(configuration.getCatalogueVendor()).s();
                        return new CatalogueEntity(vendor, title, description);
                    }
                });
    }

}
