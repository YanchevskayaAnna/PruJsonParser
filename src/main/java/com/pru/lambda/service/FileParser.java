package com.pru.lambda.service;

import com.amazonaws.services.s3.model.S3Object;
import com.pru.lambda.domain.FileContent;

public interface FileParser {
    FileContent parse(S3Object s3Object);
}
