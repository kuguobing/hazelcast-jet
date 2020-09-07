package com.hazelcast.jet.pipeline.file;

import com.hazelcast.function.FunctionEx;
import com.hazelcast.jet.json.JsonUtil;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Iterator;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import static java.util.Spliterator.NONNULL;
import static java.util.Spliterator.ORDERED;

public class JsonFileFormat<T> implements FileFormat<T> {

    private final Class<T> type;
    private final String charsetName;

    public JsonFileFormat(Class<T> type) {
        this.type = type;
        this.charsetName = StandardCharsets.UTF_8.name();
    }

    public JsonFileFormat(Class<T> type, Charset charset) {
        this.type = type;
        this.charsetName = charset.name();
    }

    @Override
    public FunctionEx<? super InputStream, Stream<T>> mapFn() {
        String localCharsetName = charsetName;
        Class<T> localType = type;
        return inputStream -> {
            InputStreamReader reader = new InputStreamReader(inputStream, localCharsetName);
            Iterator<T> iterator = JsonUtil.beanSequenceFrom(reader, localType);
            Spliterator<T> spliterator = Spliterators.spliteratorUnknownSize(iterator, ORDERED | NONNULL);
            return StreamSupport.stream(spliterator, false);
        };
    }

}
