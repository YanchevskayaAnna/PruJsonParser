package com.pru.lambda.service;

import com.pru.lambda.domain.Batch;

public interface DatabaseService {

    void updateBatchInfo(Batch batch);

    Batch getBatchInfo(String batchId);
}
