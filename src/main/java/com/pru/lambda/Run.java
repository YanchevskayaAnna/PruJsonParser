package com.pru.lambda;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.pru.lambda.domain.Entity;
import com.pru.lambda.handler.FileParserLambdaHandler;

import java.util.List;

public class Run {
    public static void main(String[] args) throws JsonProcessingException {
        System.out.println("Hello world");
        FileParserLambdaHandler fileParserLambdaHandler = new FileParserLambdaHandler();
        ObjectMapper mapper = new ObjectMapper();
        String s3Data = "[ {\"employee\":\"employee1\", \"salary\": \"1000\", \"experience\":\"10\"},\n" +
                "  {\"employee\":\"employee2\", \"salary\": \"2000\", \"experience\":\"15\"}\n" +
                "]";
        List<Entity> entities = mapper.readValue(s3Data, new TypeReference<List<Entity>>(){});
        System.out.println(entities);
    }
}
