package com.hazelcast.jet.pipeline.file;

import com.hazelcast.function.FunctionEx;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.stream.Stream;

public class LinesTextFileFormat implements FileFormat<String> {

    private final String charsetName;

    public LinesTextFileFormat() {
        charsetName = StandardCharsets.UTF_8.name();
    }

    public LinesTextFileFormat(Charset charset) {
        this.charsetName = charset.name();
    }

    @Override
    public FunctionEx<? super InputStream, Stream<String>> mapFn() {
        String localCharsetName = charsetName;
        return inputStream -> {
            BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, localCharsetName));
            return reader.lines();
        };
    }
}