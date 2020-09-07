package com.hazelcast.jet.pipeline.file;

import com.hazelcast.jet.pipeline.BatchSource;
import com.hazelcast.jet.pipeline.PipelineTestSupport;
import org.junit.Test;

import java.io.File;
import java.io.Serializable;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class FileSourcesTest extends PipelineTestSupport {

    @Test
    public void localFile_textFormat() throws Exception {
        // Given
        File directory = createTempDirectory();
        File file1 = new File(directory, randomName());
        appendToFile(file1, "hello", "world");
        File file2 = new File(directory, randomName());
        appendToFile(file2, "hello2", "world2");

        // When
        BatchSource<String> source = FileSources.files(directory.getPath())
                .format(new TextFileFormat())
                //.option("foo", "bar")
                .build();

        // Then
        p.readFrom(source).writeTo(sink);
        execute();
        int nodeCount = jet().getCluster().getMembers().size();
        assertEquals(2 * nodeCount, sinkList.size());
    }

    @Test
    public void localFile_lineTextFormat() throws Exception {
        // Given
        File directory = createTempDirectory();
        File file1 = new File(directory, randomName());
        appendToFile(file1, "hello", "world");
        File file2 = new File(directory, randomName());
        appendToFile(file2, "hello2", "world2");

        // When
        BatchSource<String> source = FileSources.files(directory.getPath())
                .format(new LinesTextFileFormat())
                //.option("foo", "bar")
                .build();

        // Then
        p.readFrom(source).writeTo(sink);
        execute();
        int nodeCount = jet().getCluster().getMembers().size();
        assertEquals(4 * nodeCount, sinkList.size());
    }

    @Test
    public void localFile_jsonFormat() throws Exception {
        // Given
        File directory = createTempDirectory();
        File file1 = new File(directory, randomName());
        appendToFile(file1, "{\"name\":\"hello\"}", "{\"name\":\"world\"}");
        File file2 = new File(directory, randomName());
        appendToFile(file2, "{\"name\":\"hello2\"}", "{\"name\":\"world2\"}");

        // When
        BatchSource<JsonBean> source = FileSources.files(directory.getPath())
                .format(new JsonFileFormat<>(JsonBean.class))
                .build();

        // Then
        p.readFrom(source).writeTo(sink);
        execute();
        int nodeCount = jet().getCluster().getMembers().size();
        assertEquals(4 * nodeCount, sinkList.size());
        assertTrue(sinkList.get(0) instanceof JsonBean);
    }

    static class JsonBean implements Serializable {

        public String name;

    }

}
