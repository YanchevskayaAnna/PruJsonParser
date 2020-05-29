package com.pru.lambda.domain;

import com.amazonaws.services.dynamodbv2.datamodeling.*;
import com.pru.lambda.domain.dynamodb.DynamoDBEntity;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.util.Date;

@SuperBuilder
@Data
@NoArgsConstructor
@AllArgsConstructor
@DynamoDBTable(tableName = "main-batchinfo")
public class Batch implements DynamoDBEntity<String> {

    @DynamoDBHashKey
    @DynamoDBAutoGeneratedKey
    private String id;

    @DynamoDBAttribute
    private String description;

    @DynamoDBAttribute
    private String user;

    @DynamoDBAttribute
    private String fileKey;

    @DynamoDBAttribute
    private Integer rowCount;

    @DynamoDBAttribute
    private Date creationDate;

    @DynamoDBAttribute
    private String batchStatus;

    @DynamoDBAttribute
    private Double averageExperience; //todo

    @Override
    public String getHashKey() {
        return id;
    }

    @Override
    public void setHashKey(String hashKey) {
        this.id = hashKey;
    }

}
