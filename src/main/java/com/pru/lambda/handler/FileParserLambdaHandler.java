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
    private final AthenaService athenaService;

    public FileParserLambdaHandler() {
        parserService = new DefaultFileParserService(
                AmazonS3ClientBuilder.defaultClient(),
                new DefaultDatabaseService(),
                new JsonFileParser()
        );
        athenaService = new DefaultAthenaService();
    }

    public FileParserLambdaHandler(FileParserService parserService, AthenaService athenaService) {
        this.parserService = parserService;
        this.athenaService = athenaService;
    }

    @Override
    public Void handleRequest(SQSEvent event, Context context) {
        FilePath filePath = extractFilePathFromEvent(event);
        try {
            athenaService.runQuery();
        } catch (InterruptedException e) {
            e.printStackTrace();
            log.error(e.getMessage());
            //throw e; todo
        }
        //parserService.parseFile(filePath);
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