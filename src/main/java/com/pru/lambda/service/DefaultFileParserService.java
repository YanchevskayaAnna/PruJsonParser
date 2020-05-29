package com.pru.lambda.service;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.S3Object;
import com.pru.lambda.domain.*;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static com.pru.lambda.domain.Status.*;

@Slf4j
@NoArgsConstructor
public class DefaultFileParserService implements FileParserService {

    private AmazonS3 client;
    private DatabaseService dbService;
    private FileParser fileParser;

    public DefaultFileParserService(AmazonS3 client, DatabaseService dbService, FileParser fileParser) {
        this.client = client;
        this.dbService = dbService;
        this.fileParser = fileParser;
    }

    public void parseFile(FilePath filePath) {
        String batchId = "";
        try {
            long startTime = System.currentTimeMillis();
            batchId = getBatchIdFromFileName(filePath.getFileName());
            log.info("Successfully retrieved batchId {} from event", batchId);
            moveBatchToInProgress(batchId);
            S3Object s3Object = extractObjectFromS3ByFilePath(filePath);
            FileContent fileContent = fileParser.parse(s3Object);
            List<Row> rows = insertRowsInBatch(batchId, fileContent);
            moveBatchToParsed(batchId, fileContent);
            log.info("Validation passed, Execution Time-> " + (System.currentTimeMillis() - startTime) / (1000) + " Seconds");
        } catch (Exception ex) {
            log.error("Error - Failure Parsing file:", ex);
            if (!batchId.isEmpty()) {
                moveBatchToFailed(batchId);
            } else {
                log.error("Error - could not update batch info because batchId was not populated");
            }
        }
    }

    private S3Object extractObjectFromS3ByFilePath(FilePath filePath) {
        String bucketName = filePath.getBucketName();
        String fileName = filePath.getFileName();
        return client.getObject(bucketName, fileName);
    }

    public String getBatchIdFromFileName(String fileName) {
        return fileName.substring(fileName.lastIndexOf("/") + 1, fileName.lastIndexOf("."));
    }

    public void moveBatchToInProgress(String batchId) {
        Batch batch = new Batch();
        batch.setId(batchId);
        batch.setBatchStatus(IN_PROGRESS.name());
        dbService.updateBatchInfo(batch);
    }

    public void moveBatchToFailed(String batchId) {
        Batch batch = new Batch();
        batch.setId(batchId);
        batch.setBatchStatus(FAILURE.name());
        dbService.updateBatchInfo(batch);
    }

    public void moveBatchToParsed(String batchId, FileContent fileContent) {
        Batch batch = new Batch();
        batch.setId(batchId);
        batch.setRowCount(fileContent.getRowCount());
        batch.setBatchStatus(COMPLETE.name());
        dbService.updateBatchInfo(batch);
    }

    private List<Row> insertRowsInBatch(String batchId, FileContent fileContent) {
        //List<Map<String, String>> fileRows = fileContent.getData(); todo
        List<Entity> entities = fileContent.getEntities();
        List<Row> rows = new ArrayList<>();
        int rowId = 0;
        for (Entity entity: entities){
            Row row = new Row();
            row.setBatchId(batchId);
            row.setId("" + rowId++);
            row.setEmployee(entity.getEmployee());
            row.setSalary(entity.getSalary());
            row.setExperience(entity.getExperience());
            rows.add(row);
        }
        if (!rows.isEmpty()) {
            log.info("Submitting final request with : {} records", rows.size());
            dbService.insertBatchRows(rows);
        }
        return rows;
    }
}
