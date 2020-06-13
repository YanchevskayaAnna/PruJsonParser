package com.pru.lambda.service;

import com.amazonaws.services.s3.AmazonS3;
import com.pru.lambda.domain.Batch;
import com.pru.lambda.domain.FilePath;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import static com.pru.lambda.domain.Status.*;
import static com.pru.lambda.util.ServiceConstants.*;

@Slf4j
@NoArgsConstructor
public class DefaultFileParserService implements FileParserService {

    private AmazonS3 s3Client;
    private DatabaseService dbService;
    private QueryService queryService;

    public DefaultFileParserService(AmazonS3 client, DatabaseService dbService, QueryService queryService) {
        this.s3Client = client;
        this.dbService = dbService;
        this.queryService = queryService;
    }

    public void parseFile(FilePath filePath) {
        String batchId = "";
        try {
            long startTime = System.currentTimeMillis();
            batchId = getBatchIdFromFileName(filePath.getFileName());
            log.info("Successfully retrieved batchId {} from event", batchId);
            moveBatchToInProgress(batchId);
            String queryExecutionId = queryService.runQuery();
            moveBatchToParsed(batchId, queryExecutionId);
            log.info("Pivoting passed, Execution Time-> " + (System.currentTimeMillis() - startTime) / (1000) + " Seconds");
            moveFileToProcessedFolder(filePath);
            log.info("Move S3 file: {} to processed folder", filePath);
        } catch (Exception ex) {
            log.error("Error - Failure Parsing file:", ex);
            if (!batchId.isEmpty()) {
                moveBatchToFailed(batchId, ex.getMessage());
            } else {
                log.error("Error - could not update batch info because batchId was not populated");
            }
            moveFileToFailureFolder(filePath);
            log.info("Move S3 file: {} to failure folder", filePath);
        }
    }

    private void moveFileToProcessedFolder(FilePath filePath) {
        moveFileToS3Bucket(filePath, PROCESSED_FILES_S3_FOLDER_PATH);
    }

    private void moveFileToFailureFolder(FilePath filePath) {
        moveFileToS3Bucket(filePath, FAILURE_FILES_S3_FOLDER_PATH);
    }

    private void moveFileToS3Bucket(FilePath filePath, String targetBucket) {
        s3Client.copyObject(filePath.getBucketName(), filePath.getFileName(), targetBucket, filePath.getFileName());
        s3Client.deleteObject(filePath.getBucketName(), filePath.getFileName());
    }

    public String getBatchIdFromFileName(String fileName) {
        return fileName.substring(fileName.lastIndexOf("/") + 1, fileName.lastIndexOf("."));
    }

    @Override
    public void moveBatchToInProgress(String batchId) {
        Batch batch = new Batch();
        batch.setId(batchId);
        batch.setBatchStatus(IN_PROGRESS.name());
        batch.setFailureReason("");
        batch.setQueryExecutionId("");
        batch.setS3Path("");
        dbService.updateBatchInfo(batch);
    }

    @Override
    public void moveBatchToFailed(String batchId, String failureReason) {
        Batch batch = new Batch();
        batch.setId(batchId);
        batch.setBatchStatus(FAILURE.name());
        batch.setFailureReason(failureReason);
        dbService.updateBatchInfo(batch);
    }

    @Override
    public void moveBatchToParsed(String batchId, String queryExecutionId) {
        Batch batch = new Batch();
        batch.setId(batchId);
        batch.setBatchStatus(COMPLETE.name());
        batch.setQueryExecutionId(queryExecutionId);
        batch.setS3Path(ATHENA_OUTPUT_S3_FOLDER_PATH + queryExecutionId + ".csv");
        batch.setFailureReason("");
        dbService.updateBatchInfo(batch);
    }

}


