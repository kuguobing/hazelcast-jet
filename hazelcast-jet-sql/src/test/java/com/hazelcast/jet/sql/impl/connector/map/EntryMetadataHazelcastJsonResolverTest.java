/*
 * Copyright (c) 2008-2020, Hazelcast, Inc. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.hazelcast.jet.sql.impl.connector.map;

import com.hazelcast.jet.sql.impl.connector.EntryMetadata;
import com.hazelcast.jet.sql.impl.inject.HazelcastJsonUpsertTargetDescriptor;
import com.hazelcast.jet.sql.impl.schema.MappingField;
import com.hazelcast.sql.impl.QueryException;
import com.hazelcast.sql.impl.extract.GenericQueryTargetDescriptor;
import com.hazelcast.sql.impl.extract.QueryPath;
import com.hazelcast.sql.impl.schema.map.MapTableField;
import com.hazelcast.sql.impl.type.QueryDataType;
import junitparams.JUnitParamsRunner;
import junitparams.Parameters;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.List;

import static com.hazelcast.jet.sql.impl.connector.map.EntryMetadataHazelcastJsonResolver.INSTANCE;
import static java.util.Collections.emptyMap;
import static java.util.Collections.singletonList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@RunWith(JUnitParamsRunner.class)
public class EntryMetadataHazelcastJsonResolverTest {

    @Test
    @Parameters({
            "true, __key",
            "false, this"
    })
    public void test_resolveFields(boolean key, String prefix) {
        List<MappingField> fields = INSTANCE.resolveFields(
                key,
                singletonList(field("field", QueryDataType.INT, prefix + ".field")),
                emptyMap(),
                null
        );

        assertThat(fields).containsExactly(field("field", QueryDataType.INT, prefix + ".field"));
    }

    @Test
    @Parameters({
            "true, __key",
            "false, this"
    })
    public void when_invalidExternalName_then_throws(boolean key, String prefix) {
        assertThatThrownBy(() -> INSTANCE.resolveFields(
                key,
                singletonList(field("field", QueryDataType.INT, prefix)),
                emptyMap(),
                null
        )).isInstanceOf(QueryException.class);
    }

    @Test
    @Parameters({
            "true, __key",
            "false, this"
    })
    public void test_resolveMetadata(boolean key, String prefix) {
        EntryMetadata metadata = INSTANCE.resolveMetadata(
                key,
                singletonList(field("field", QueryDataType.INT, prefix + ".field")),
                emptyMap(),
                null
        );

        assertThat(metadata.getFields()).containsExactly(
                new MapTableField("field", QueryDataType.INT, false, QueryPath.create(prefix + ".field"))
        );
        assertThat(metadata.getQueryTargetDescriptor()).isEqualTo(GenericQueryTargetDescriptor.DEFAULT);
        assertThat(metadata.getUpsertTargetDescriptor()).isEqualTo(HazelcastJsonUpsertTargetDescriptor.INSTANCE);
    }

    private static MappingField field(String name, QueryDataType type, String externalName) {
        return new MappingField(name, type, externalName);
    }
}
