package org.leandro.catalogue.integrated.dynamodb;

import com.amazonaws.services.dynamodbv2.local.main.ServerRunner;
import com.amazonaws.services.dynamodbv2.local.server.DynamoDBProxyServer;
import org.junit.jupiter.api.extension.AfterAllCallback;
import org.junit.jupiter.api.extension.BeforeAllCallback;
import org.junit.jupiter.api.extension.ExtensionContext;

public class LocalDynamoDbExtension implements AfterAllCallback, BeforeAllCallback {

    protected DynamoDBProxyServer server;


    @Override
    public void afterAll(ExtensionContext context) {
        stopUnchecked(server);
    }

    @Override
    public void beforeAll(ExtensionContext context) throws Exception {
        System.setProperty("AWS_ACCESS_KEY_ID", "foo");
        System.setProperty("aws_secret_access_key", "foo");
        this.server = ServerRunner.createServerFromCommandLineArgs(new String[] {"-inMemory", "-port", "8000"});
    }

    protected void stopUnchecked(DynamoDBProxyServer dynamoDbServer) {
        try {
            dynamoDbServer.stop();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
