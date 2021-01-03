package org.leandro.catalogue.service.aws;

import io.micronaut.context.annotation.Bean;
import io.micronaut.context.annotation.Factory;
import software.amazon.awssdk.auth.credentials.AwsCredentials;
import software.amazon.awssdk.auth.credentials.AwsCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.dynamodb.DynamoDbAsyncClient;
import software.amazon.awssdk.services.dynamodb.DynamoDbAsyncClientBuilder;

import java.net.URI;

@Factory
public class Config {

    private static final String TABLE_NAME = "entities";
    private static final Region region = Region.US_EAST_1;

    public static final AwsCredentials CREDENTIALS = new AwsCredentials() {
        @Override
        public String accessKeyId() {
            return "foo";
        }

        @Override
        public String secretAccessKey() {
            return "foo";
        }
    };

    AwsCredentialsProvider provider = () -> CREDENTIALS;

    @Bean
    DynamoDbAsyncClient dynamoDbAsyncClient(){
        DynamoDbAsyncClientBuilder clientBuilder = DynamoDbAsyncClient.builder();

        clientBuilder.credentialsProvider(provider).endpointOverride(URI.create("http://localhost:4566"));

        return clientBuilder.region(region).build();
    }
}
