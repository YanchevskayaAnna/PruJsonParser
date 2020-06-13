package com.pru.lambda.util;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class ServiceConstants {
    public static final String ATHENA_OUTPUT_S3_FOLDER_PATH = "s3://athena-poc-pivoting/";
    public static final String PROCESSED_FILES_S3_FOLDER_PATH = "athena-processed-files";
    public static final String FAILURE_FILES_S3_FOLDER_PATH = "athena-failure-files";
}
