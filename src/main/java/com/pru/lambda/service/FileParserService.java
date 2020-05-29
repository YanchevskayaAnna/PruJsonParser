package com.pru.lambda.service;

import com.pru.lambda.domain.FileContent;
import com.pru.lambda.domain.FilePath;

public interface FileParserService {

     void parseFile(FilePath filePath);

     String getBatchIdFromFileName(String fileName);

     void moveBatchToFailed(String batchId);
     void moveBatchToInProgress(String batchId);
     void moveBatchToParsed(String batchId, FileContent fileContent);
}