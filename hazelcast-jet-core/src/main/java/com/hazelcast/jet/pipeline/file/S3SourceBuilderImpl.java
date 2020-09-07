package com.hazelcast.jet.pipeline.file;

import com.hazelcast.jet.pipeline.BatchSource;

class S3SourceBuilderImpl<T> extends BaseFileSourceBuilderImpl<T> {

    String bucket;
    String prefix;

    public S3SourceBuilderImpl(String path) {
        super(path);
    }

    @Override
    public BatchSource<T> build() {
        return null;
    }
}