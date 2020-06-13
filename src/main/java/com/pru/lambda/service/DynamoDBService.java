package com.pru.lambda.service;

import com.amazonaws.client.builder.AwsClientBuilder;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClientBuilder;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapper;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapperConfig;
import com.pru.lambda.domain.Batch;
import lombok.extern.slf4j.Slf4j;

import java.util.Calendar;

@Slf4j
public class DynamoDBService implements DatabaseService {

    private static final String REGION = "eu-west-1";
    private static final String ENDPOINT = "https://dynamodb.eu-west-1.amazonaws.com";

    private DynamoDBMapper dynamoMapper;

    public DynamoDBService() {
        initDynamoMapperClient();
    }

    public DynamoDBService(AmazonDynamoDB client) {
        initDynamoMapper(client);
    }

    private void initDynamoMapperClient() {
        try {
            AmazonDynamoDB client = AmazonDynamoDBClientBuilder
                    .standard()
                    .withEndpointConfiguration(new AwsClientBuilder.EndpointConfiguration(ENDPOINT, REGION))
                    .build();
            initDynamoMapper(client);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
    }

    private void initDynamoMapper(AmazonDynamoDB client) {
        DynamoDBMapperConfig config = new DynamoDBMapperConfig.Builder().
                        withSaveBehavior(DynamoDBMapperConfig.SaveBehavior.UPDATE_SKIP_NULL_ATTRIBUTES).
                        build();
        this.dynamoMapper = new DynamoDBMapper(client, config);
    }

    @Override
    public void updateBatchInfo(Batch batch) {
        batch.setTimeToLive(getTimeToLive());
        dynamoMapper.save(batch);
    }

    @Override
    public Batch getBatchInfo(String batchId) {
        Batch batch = new Batch();
        batch.setId(batchId);
        return dynamoMapper.load(batch.getClass(), batch.getId());
    }

    public Long getTimeToLive() {
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.DATE, 10);
        return cal.getTimeInMillis() / 1000;
    }
}
