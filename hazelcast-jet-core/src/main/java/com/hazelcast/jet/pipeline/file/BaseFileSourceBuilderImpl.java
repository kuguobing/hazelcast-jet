package com.hazelcast.jet.pipeline.file;

import java.util.Properties;

abstract class BaseFileSourceBuilderImpl<T> implements FileSourceBuilder<T> {

    String path;
    Properties options = new Properties();
    FileFormat<T> fileFormat;

    public BaseFileSourceBuilderImpl(String path) {
        this.path = path;
    }

    @Override
    public FileSourceBuilder<T> option(String key, Object value) {
        options.put(key, value);
        return this;
    }

    @Override
    public <T_NEW> FileSourceBuilder<T_NEW> format(FileFormat<T_NEW> fileFormat) {
        BaseFileSourceBuilderImpl<T_NEW> newThis = (BaseFileSourceBuilderImpl<T_NEW>) this;
        newThis.fileFormat = fileFormat;
        return newThis;
    }
}