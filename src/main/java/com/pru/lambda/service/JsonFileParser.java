package com.pru.lambda.service;

import com.amazonaws.services.s3.model.S3Object;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.pru.lambda.domain.Entity;
import com.pru.lambda.domain.FileContent;
import lombok.extern.slf4j.Slf4j;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
public class JsonFileParser implements FileParser {
    private ObjectMapper mapper;

    public JsonFileParser(ObjectMapper mapper) {
        this.mapper = mapper;
    }

    public JsonFileParser() {
        mapper = new ObjectMapper();
    }

    @Override
    public FileContent parse(S3Object s3Object) {
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(s3Object.getObjectContent(), StandardCharsets.UTF_8))) {
            String s3Data = reader.lines().collect(Collectors.joining("\n"));
            log.info("s3Data: {}", s3Data);
            List<Entity> entities = mapper.readValue(s3Data.trim(), new TypeReference<List<Entity>>() {
            });
            log.info("entities: {}", entities);
            FileContent fileContent = new FileContent();
            fileContent.setEntities(entities);
            fileContent.setRowCount(entities.size());
            return fileContent;
        } catch (Exception e) {
            log.error("Error - Failure Parsing file:", e);
        }
        return null;
    }
}
