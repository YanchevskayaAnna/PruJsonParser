package com.pru.lambda.domain;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.util.List;
import java.util.Map;

@SuperBuilder
@Data
@NoArgsConstructor
@AllArgsConstructor
public class FileContent {

    private List<Map<String, String>> data;
    private List<Entity> entities;
    private Integer rowCount;

}
