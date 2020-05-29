package com.pru.lambda.domain.dynamodb;

import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBTable;

import java.io.Serializable;

@DynamoDBTable(tableName = "")
public interface DynamoDBEntity<K> extends Serializable {
    K getHashKey();
    void setHashKey(K hashKey);
}
