package com.pru.lambda.service;

import com.pru.lambda.domain.Batch;
import com.pru.lambda.domain.Row;

import java.util.List;

public interface DatabaseService {

    void insertBatchRows(List<Row> rows);

    void updateBatchInfo(Batch batch);

    Batch getBatchInfo(String batchId);
}
