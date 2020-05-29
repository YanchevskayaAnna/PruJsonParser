package com.pru.lambda.domain;

import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBAttribute;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBHashKey;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBRangeKey;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBTable;
import com.pru.lambda.domain.dynamodb.RangedDynamoDBEntity;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@SuperBuilder
@Data
@NoArgsConstructor
@AllArgsConstructor
@DynamoDBTable(tableName = "main-batchrow")
public class Row implements RangedDynamoDBEntity<String, String> {

    @DynamoDBHashKey()
    private String batchId;

    @DynamoDBRangeKey()
    private String id;

    @DynamoDBAttribute
    private Long timeToLive;

//    @DynamoDBAttribute
//    private Map<String, String> data;

    //todo create data column
    @DynamoDBAttribute
    private String employee;

    @DynamoDBAttribute
    private String salary;

    @DynamoDBAttribute
    private String experience;

    @Override
    public String getHashKey() {
        return batchId;
    }

    @Override
    public void setHashKey(String hashKey) {
        this.batchId = hashKey;
    }


    @Override
    public String getRangeKey() {
        return id;
    }

    @Override
    public void setRangeKey(String key) {
        this.id = key;
    }
}
