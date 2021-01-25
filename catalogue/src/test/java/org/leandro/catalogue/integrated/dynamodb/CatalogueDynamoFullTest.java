package org.leandro.catalogue.integrated.dynamodb;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.micronaut.context.ApplicationContext;
import io.micronaut.context.annotation.Value;
import io.micronaut.http.HttpRequest;
import io.micronaut.http.HttpResponse;
import io.micronaut.http.HttpStatus;
import io.micronaut.http.MediaType;
import io.micronaut.http.client.RxHttpClient;
import io.micronaut.http.client.annotation.Client;
import io.micronaut.http.client.exceptions.HttpClientResponseException;
import io.micronaut.http.hateoas.JsonError;
import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.junit.jupiter.api.extension.ExtendWith;
import org.leandro.api.v1.model.ProductType;
import org.leandro.catalogue.Application;
import org.leandro.catalogue.integrated.controller.entity.CatalogueEntity;
import org.leandro.catalogue.service.aws.CatalogueConfigurationDynamoDB;
import org.leandro.catalogue.service.aws.DynamoDBService;
import org.leandro.catalogue.util.FriendlyUrl;
import software.amazon.awssdk.services.dynamodb.model.ListTablesResponse;

import javax.inject.Inject;
import java.nio.charset.Charset;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;

import static org.junit.jupiter.api.Assertions.*;

@TestMethodOrder(value=MethodOrderer.OrderAnnotation.class)
@MicronautTest(application = Application.class)
@ExtendWith(LocalDynamoDbExtension.class)
public class CatalogueDynamoFullTest {

    @Inject
    ApplicationContext applicationContext;

    @Inject
    @Client("/${catalogue.api.version}/catalogue")
    RxHttpClient rxHttpClient;

    @Value("${catalogue.api.version}")
    String apiVersion;

    @Inject
    ObjectMapper objectMapper;

    @Inject
    DynamoDBService dynamoDBService;

    @Inject
    CatalogueConfigurationDynamoDB configuration;

    @Order(0)
    @Test
    void configureTable() throws InterruptedException, ExecutionException {
        System.out.println("Init dropped table ? :" + dynamoDBService.dropTable(configuration.getTableName()).get());
        dynamoDBService.createTableIfNeeded().get();
        ListTablesResponse response = dynamoDBService.listTables().get();
        System.out.println(dynamoDBService.describeTable(response.tableNames().get(0)).get().table().toString());
    }

    @Test
    @Order(1)
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
    @Order(2)
    public void testNextFindByTitle() throws JsonProcessingException {

        final CatalogueEntity entity = new CatalogueEntity("Juan", "Ron", "photo-1442605527737-ed62b867591f.jpeg")
                .type(ProductType.UNDEFINED);

        final String requestBody = objectMapper.writeValueAsString(entity);
        final HttpRequest<String> request = HttpRequest.POST("/", requestBody);
        final String ronResponseBody =  rxHttpClient.toBlocking().retrieve(request);
        final CatalogueEntity ron = objectMapper.readValue(ronResponseBody, CatalogueEntity.class);

        assertNotNull(ron);

//        final HttpRequest<String> listRequest = HttpRequest.GET("/");
//
//
//        final HttpRequest<String> byTitleRequest = HttpRequest.GET("/title/Ron");
//        final String listResponseBody = rxHttpClient.toBlocking().retrieve(byTitleRequest);
//        CatalogueEntity[] catalogueEntities = objectMapper.readValue(listResponseBody, CatalogueEntity[].class);
//
//        assertEquals(1, catalogueEntities.length);
    }

    @Test
    @Order(3)
    public void testNextFindByVendor() throws JsonProcessingException {

        final HttpRequest<String> byVendorRequest = HttpRequest.GET("/vendor/Fred");
        final String listResponseBody = rxHttpClient.toBlocking().retrieve(byVendorRequest);
        CatalogueEntity[] catalogueEntities = objectMapper.readValue(listResponseBody, CatalogueEntity[].class);

        assertEquals(1, catalogueEntities.length);
    }
}
