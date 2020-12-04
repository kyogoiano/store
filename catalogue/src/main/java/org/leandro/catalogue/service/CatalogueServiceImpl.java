package org.leandro.catalogue.service;

import com.mongodb.reactivestreams.client.FindPublisher;
import com.mongodb.reactivestreams.client.MongoClient;
import com.mongodb.reactivestreams.client.MongoCollection;
import io.reactivex.Single;
import io.reactivex.SingleSource;
import org.leandro.catalogue.integrated.controller.entity.CatalogueEntity;

import javax.inject.Inject;
import javax.inject.Singleton;

import static com.mongodb.client.model.Filters.eq;

@Singleton
public class CatalogueServiceImpl implements CatalogueService<CatalogueEntity> {

    @Inject
    private MongoClient mongoClient;

    @Inject
    private CatalogueConfiguration configuration;

    @Override
    public FindPublisher<CatalogueEntity> findAll(){
        return getCollection().find();
    }


    @Override
    public FindPublisher<CatalogueEntity> findByVendorName(final String vendorName){
        return getCollection()
                .find(eq("vendor", vendorName));
    }

    @Override
    public FindPublisher<CatalogueEntity> findByType(final String name){
        return getCollection()
                .find(eq("type", name));
    }

    @Override
    public FindPublisher<CatalogueEntity> find(final String title){
        return getCollection()
                .find(eq("title", title)).limit(1);
    }

    @Override
    public SingleSource<CatalogueEntity> save(final CatalogueEntity catalogue){
        return Single.fromPublisher(getCollection().insertOne(catalogue))
                .map(success -> catalogue);
    }



    private MongoCollection<CatalogueEntity> getCollection() {
        return mongoClient
                .getDatabase(configuration.getDatabaseName())
                .getCollection(configuration.getCollectionName(), CatalogueEntity.class);
    }


}
