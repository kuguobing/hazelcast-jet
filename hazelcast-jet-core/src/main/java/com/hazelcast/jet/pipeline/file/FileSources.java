package com.hazelcast.jet.pipeline.file;

public class FileSources {

    static FileSourceBuilder<Void> files(String path) {
        return new LocalFileSourceBuilderImpl<>(path);
    }

    static <T> FileSourceBuilder<T> s3(String path) {
        return new S3SourceBuilderImpl<>(path);
    }

}
