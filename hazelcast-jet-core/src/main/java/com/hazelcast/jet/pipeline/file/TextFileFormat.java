package com.hazelcast.jet.pipeline.file;

import com.hazelcast.function.FunctionEx;
import com.hazelcast.jet.impl.util.IOUtil;

import java.io.InputStream;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.stream.Stream;

public class TextFileFormat implements FileFormat<String> {

    private final String charsetName;

    public TextFileFormat() {
        charsetName = StandardCharsets.UTF_8.name();
    }

    public TextFileFormat(Charset charset) {
        charsetName = charset.name();
    }

    @Override
    public FunctionEx<? super InputStream, Stream<String>> mapFn() {
        String localCharsetName = charsetName;
        return inputStream -> {
            byte[] bytes = IOUtil.readFully(inputStream);
            return Stream.of(new String(bytes, localCharsetName));
        };
    }
}