package org.leandro.catalogue.integrated;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mongodb.ConnectionString;
import com.mongodb.MongoClientSettings;
import com.mongodb.reactivestreams.client.MongoClient;
import com.mongodb.reactivestreams.client.MongoClients;
import io.micronaut.context.ApplicationContext;
import io.micronaut.context.annotation.Value;
import io.micronaut.http.*;
import io.micronaut.http.client.RxHttpClient;
import io.micronaut.http.client.annotation.Client;
import io.micronaut.http.client.exceptions.HttpClientResponseException;
import io.micronaut.http.hateoas.JsonError;
import io.micronaut.test.annotation.MicronautTest;
import io.reactivex.Flowable;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.leandro.api.v1.model.ProductType;
import org.leandro.catalogue.Application;
import org.leandro.catalogue.integrated.controller.entity.CatalogueEntity;
import org.leandro.catalogue.service.CatalogueConfiguration;
import org.leandro.catalogue.util.FriendlyUrl;

import javax.inject.Inject;
import java.nio.charset.Charset;

import static org.junit.jupiter.api.Assertions.*;

@TestMethodOrder(value=MethodOrderer.Alphanumeric.class)
@MicronautTest(application = Application.class)
public class CatalogueFullTest {

    @Inject
    ApplicationContext applicationContext;

    @Inject
    @Client("/${catalogue.api.version}/catalogue")
    RxHttpClient rxHttpClient;

    @Value("${catalogue.api.version}")
    String apiVersion;

    @Inject
    ObjectMapper objectMapper;

    static String connectionString = "mongodb://leandro:123@0.0.0.0:27017/catalogue_db";

    @AfterEach
    public void cleanupData() {

        final ConnectionString connString = new ConnectionString(connectionString);
        final MongoClientSettings settings = MongoClientSettings.builder()
                .applyConnectionString(connString)
                .retryWrites(true)
                .build();
        final MongoClient mongoClient = MongoClients.create(settings);

        final CatalogueConfiguration config = applicationContext.getBean(CatalogueConfiguration.class);
        // drop the data
        Flowable.fromPublisher(
                mongoClient.getDatabase(config.getDatabaseName()).getCollection(config.getCollectionName()).drop())
                .blockingSubscribe();
    }

    @Test
    public void testListProducts() throws JsonProcessingException {

        final HttpRequest<String> listRequest = HttpRequest.GET("/");
        String listResponseBody = rxHttpClient.toBlocking().retrieve(listRequest);
        assertNotNull(listResponseBody);  // with  HttpClientResponseException

        CatalogueEntity[] catalogueEntities = objectMapper.readValue(listResponseBody, CatalogueEntity[].class);

        assertEquals(0, catalogueEntities.length);

        try {
            final String requestBody = objectMapper.writeValueAsString(new CatalogueEntity("", "", ""));
            final HttpRequest<String> request = HttpRequest.POST("/", requestBody);
            rxHttpClient.toBlocking().retrieve(request);

            fail("Should have thrown a client response exception");
        } catch (HttpClientResponseException e) {
            HttpResponse<?> response = e.getResponse();
            assertEquals(response.getStatus(), HttpStatus.BAD_REQUEST);
            assertEquals(response.getContentType().get().getName(), MediaType.APPLICATION_JSON);
            assertEquals(response.getCharacterEncoding(), Charset.defaultCharset());
            assertEquals(response.getBody(JsonError.TYPE).get().getClass(), JsonError.class);
            final JsonError jsonError  = response.getBody(JsonError.TYPE).get();
            assertEquals(jsonError.getLinks().getFirst("self").get().getHref(), "/"+apiVersion+"/catalogue");
        }

        final CatalogueEntity entity = new CatalogueEntity("Fred", "Harry","photo-1457914109735-ce8aba3b7a79.jpeg").type(ProductType.UNDEFINED);
        final String requestBody = objectMapper.writeValueAsString(entity);
        final HttpRequest<String> request = HttpRequest.POST("/", requestBody);
        String harryResponseBody = rxHttpClient.toBlocking().retrieve(request);
        assertNotNull(harryResponseBody);  // with  HttpClientResponseException
        CatalogueEntity harryResponse = objectMapper.readValue(harryResponseBody, CatalogueEntity.class);


        assertEquals(harryResponse.getDescription(), entity.getDescription());
        assertEquals(harryResponse.getTitle(), FriendlyUrl.sanitizeWithDashes(entity.getTitle()));
        assertEquals(harryResponse.getVendor(), entity.getVendor());
        assertEquals(harryResponse.getType(), entity.getType());

        listResponseBody = rxHttpClient.toBlocking().retrieve(listRequest);
        assertNotNull(listResponseBody);  // with  HttpClientResponseException

        final CatalogueEntity[] newCatalogueEntities = objectMapper.readValue(listResponseBody, CatalogueEntity[].class);

        assertEquals(newCatalogueEntities.length, 1);
        assertEquals(newCatalogueEntities[0].getTitle(), harryResponse.getTitle());

    }

    @Test
    public void testNextFindByVendor() throws JsonProcessingException {

        final CatalogueEntity entity = new CatalogueEntity("Fred", "Ron", "photo-1442605527737-ed62b867591f.jpeg")
                .type(ProductType.UNDEFINED);

        final String requestBody = objectMapper.writeValueAsString(entity);
        final HttpRequest<String> request = HttpRequest.POST("/", requestBody);
        final String ronResponseBody =  rxHttpClient.toBlocking().retrieve(request);
        final CatalogueEntity ron = objectMapper.readValue(ronResponseBody, CatalogueEntity.class);

        assertNotNull(ron);

        final HttpRequest<String> byVendorRequest = HttpRequest.GET("/vendor/Fred");
        final String listResponseBody = rxHttpClient.toBlocking().retrieve(byVendorRequest);
        CatalogueEntity[] catalogueEntities = objectMapper.readValue(listResponseBody, CatalogueEntity[].class);

        assertEquals(1, catalogueEntities.length);
    }
}
