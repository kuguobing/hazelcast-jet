package com.hazelcast.jet.pipeline.file;

import com.hazelcast.jet.pipeline.BatchSource;

public interface FileSourceBuilder<T> {

    FileSourceBuilder<T> option(String key, Object value);

    <T_NEW> FileSourceBuilder<T_NEW> format(FileFormat<T_NEW> fileFormat);

    BatchSource<T> build();

}
