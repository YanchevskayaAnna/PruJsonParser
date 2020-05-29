package com.pru.lambda.domain.dynamodb;

public interface RangedDynamoDBEntity<K, R> extends DynamoDBEntity<K> {
    R getRangeKey();
    void setRangeKey(R key);
}
