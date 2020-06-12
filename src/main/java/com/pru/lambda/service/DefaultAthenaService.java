package com.pru.lambda.service;

import com.pru.lambda.athena.AthenaClientFactory;
import software.amazon.awssdk.services.athena.AthenaClient;
import software.amazon.awssdk.services.athena.model.*;
import lombok.extern.slf4j.Slf4j;
import software.amazon.awssdk.services.athena.paginators.GetQueryResultsIterable;

import java.util.ArrayList;
import java.util.List;

@Slf4j
public class DefaultAthenaService implements AthenaService {

    private static final String ATHENA_DATABASE = "sampledata";
    private static final String ATHENA_OUTPUT_S3_FOLDER_PATH = "s3://athena-poc-pivoting/";
    private static final String SIMPLE_ATHENA_QUERY = "SELECT\n" +
            "  uid,\n" +
            "  kv1['A'] AS A,\n" +
            "  kv1['B'] AS B\n" +
            "FROM (\n" +
            "  SELECT uid, map_agg(key, value1) kv1\n" +
            "  FROM json_table\n" +
            "  GROUP BY uid\n" +
            "); ";
    private static final long SLEEP_AMOUNT_IN_MS = 1000;

    private final AthenaClient athenaClient;

    public DefaultAthenaService() {
        AthenaClientFactory factory = new AthenaClientFactory();
        athenaClient = factory.createClient();
    }

    public DefaultAthenaService(AthenaClient athenaClient) {
        this.athenaClient = athenaClient;
    }

    @Override
    public void runQuery() throws InterruptedException {

        String queryExecutionId = submitAthenaQuery(athenaClient);

        log.info("Query submitted: " + System.currentTimeMillis());

        waitForQueryToComplete(athenaClient, queryExecutionId);

        log.info("Query finished: " + System.currentTimeMillis());

        processResultRows(athenaClient, queryExecutionId);

    }

    private static String submitAthenaQuery(AthenaClient athenaClient) {

        QueryExecutionContext queryExecutionContext = QueryExecutionContext.builder()
                .database(ATHENA_DATABASE).build();
        ResultConfiguration resultConfiguration = ResultConfiguration.builder()
                .outputLocation(ATHENA_OUTPUT_S3_FOLDER_PATH).build();
        StartQueryExecutionRequest startQueryExecutionRequest = StartQueryExecutionRequest.builder()
                .queryString(SIMPLE_ATHENA_QUERY)
                .queryExecutionContext(queryExecutionContext)
                .resultConfiguration(resultConfiguration).build();
        StartQueryExecutionResponse startQueryExecutionResponse = athenaClient.startQueryExecution(startQueryExecutionRequest);
        return startQueryExecutionResponse.queryExecutionId();
    }

    private static void waitForQueryToComplete(AthenaClient athenaClient, String queryExecutionId) throws InterruptedException {
        GetQueryExecutionRequest getQueryExecutionRequest = GetQueryExecutionRequest.builder()
                .queryExecutionId(queryExecutionId).build();
        GetQueryExecutionResponse getQueryExecutionResponse;
        boolean isQueryStillRunning = true;
        while (isQueryStillRunning) {
            getQueryExecutionResponse = athenaClient.getQueryExecution(getQueryExecutionRequest);
            String queryState = getQueryExecutionResponse.queryExecution().status().state().toString();
            if (queryState.equals(QueryExecutionState.FAILED.toString())) {
                throw new RuntimeException("Query Failed to run with Error Message: " + getQueryExecutionResponse
                        .queryExecution().status().stateChangeReason());
            } else if (queryState.equals(QueryExecutionState.CANCELLED.toString())) {
                throw new RuntimeException("Query was cancelled.");
            } else if (queryState.equals(QueryExecutionState.SUCCEEDED.toString())) {
                isQueryStillRunning = false;
            } else {
                Thread.sleep(SLEEP_AMOUNT_IN_MS);
            }
            log.info("Current Status is: " + queryState);
        }
    }

    private static void processResultRows(AthenaClient athenaClient, String queryExecutionId) {
        GetQueryResultsRequest getQueryResultsRequest = GetQueryResultsRequest.builder()
                .queryExecutionId(queryExecutionId).build();
        GetQueryResultsIterable getQueryResultsResults = athenaClient.getQueryResultsPaginator(getQueryResultsRequest);
        for (GetQueryResultsResponse Resultresult : getQueryResultsResults) {
            List<ColumnInfo> columnInfoList = Resultresult.resultSet().resultSetMetadata().columnInfo();
            int resultSize = Resultresult.resultSet().rows().size();
            log.info("Result size: " + resultSize);
            List<Row> results = Resultresult.resultSet().rows();
            processRow(results, columnInfoList);
        }
    }

    private static void processRow(List<Row> rowList, List<ColumnInfo> columnInfoList) {
        List<String> columns = new ArrayList<>();
        for (ColumnInfo columnInfo : columnInfoList) {
            columns.add(columnInfo.name());
        }
        for (Row row : rowList) {
            int index = 0;
            for (Datum datum : row.data()) {
                log.info(columns.get(index) + ": " + datum.varCharValue());
                index++;
            }
            log.info("===================================");
        }
    }


}
