package org.leandro.inventory.service;

import com.mongodb.reactivestreams.client.FindPublisher;
import com.mongodb.reactivestreams.client.MongoClient;
import com.mongodb.reactivestreams.client.MongoCollection;
import org.leandro.inventory.entity.ProductInventory;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.Optional;

import static com.mongodb.client.model.Filters.eq;

@Singleton
public class InventoryServiceImpl implements InventoryService {


    @Inject
    private MongoClient mongoClient;

    @Inject
    private InventoryConfiguration configuration;

    @Override
    public FindPublisher<ProductInventory> findByBarCode(final String barCode){
        return getCollection()
                .find(eq("barCode", barCode));
    }

    private MongoCollection<ProductInventory> getCollection() {
        return mongoClient
                .getDatabase(configuration.getDatabaseName())
                .getCollection(configuration.getCollectionName(), ProductInventory.class);
    }
}
