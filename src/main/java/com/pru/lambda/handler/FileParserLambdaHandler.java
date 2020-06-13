package com.pru.lambda.handler;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.SQSEvent;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.event.S3EventNotification;
import com.pru.lambda.domain.FilePath;
import com.pru.lambda.service.*;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class FileParserLambdaHandler implements RequestHandler<SQSEvent, Void> {

    private final FileParserService parserService;

    public FileParserLambdaHandler() {
        parserService = new DefaultFileParserService(
                AmazonS3ClientBuilder.defaultClient(),
                new DynamoDBService(),
                new AthenaQueryService()
        );
    }

    public FileParserLambdaHandler(FileParserService parserService) {
        this.parserService = parserService;
    }

    @Override
    public Void handleRequest(SQSEvent event, Context context) {
        FilePath filePath = extractFilePathFromEvent(event);
        try {
            parserService.parseFile(filePath);
        } catch (Exception e) {
            e.printStackTrace();
            log.error(e.getMessage());
            throw e;
        }
        return null;
    }

    private FilePath extractFilePathFromEvent(SQSEvent event) {
        log.info("Event recieved: {}", event);
        S3EventNotification notification = S3EventNotification.parseJson(event.getRecords().get(0).getBody());
        S3EventNotification.S3Entity s3Entity = notification.getRecords().get(0).getS3();
        log.info("S3 entity {}", s3Entity);
        String bucketName = s3Entity.getBucket().getName();
        String fileName = s3Entity.getObject().getKey();
        return new FilePath(bucketName, fileName);
    }

}