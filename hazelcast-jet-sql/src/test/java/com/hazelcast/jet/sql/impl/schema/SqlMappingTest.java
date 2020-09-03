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

package com.hazelcast.jet.sql.impl.schema;

import com.hazelcast.jet.sql.JetSqlTestSupport;
import com.hazelcast.jet.sql.impl.schema.model.IdentifiedPerson;
import com.hazelcast.jet.sql.impl.schema.model.Person;
import com.hazelcast.sql.HazelcastSqlException;
import com.hazelcast.sql.SqlResult;
import com.hazelcast.sql.SqlService;
import org.junit.BeforeClass;
import org.junit.Test;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

public class SqlMappingTest extends JetSqlTestSupport {

    private static final String MAPPING_NAME = "mapping_name";

    private static SqlService sqlService;

    @BeforeClass
    public static void setUpClass() {
        initialize(1, null);
        sqlService = instance().getSql();
    }

    @Test
    public void when_mappingIsDeclared_then_itIsAvailable() {
        // when
        SqlResult createResult = sqlService.execute(javaSerializableMapDdl(MAPPING_NAME, Integer.class, String.class));

        // then
        assertThat(createResult.isUpdateCount()).isTrue();
        assertThat(createResult.updateCount()).isEqualTo(-1);

        // when
        SqlResult updateResult = sqlService.execute("SELECT __key, this FROM public." + MAPPING_NAME);

        // then
        assertThat(updateResult.isUpdateCount()).isFalse();
        assertThat(updateResult.iterator()).isExhausted();
    }

    @Test
    public void when_mappingIsDeclared_then_itsDefinitionHasPrecedenceOverDiscoveredOne() {
        // given
        sqlService.execute(javaSerializableMapDdl(MAPPING_NAME, Integer.class, Person.class));

        Map<Integer, Person> map = instance().getMap(MAPPING_NAME);
        map.put(1, new IdentifiedPerson(2, "Alice"));

        // when
        // then
        assertThatThrownBy(() -> sqlService.execute("SELECT id FROM " + MAPPING_NAME))
                .isInstanceOf(HazelcastSqlException.class);
    }

    @Test
    public void when_mappingIsDropped_then_itIsNotAvailable() {
        // given
        sqlService.execute(javaSerializableMapDdl(MAPPING_NAME, Integer.class, Person.class));

        // when
        SqlResult updateResult = sqlService.execute("DROP EXTERNAL MAPPING " + MAPPING_NAME);

        // then
        assertThat(updateResult.isUpdateCount()).isTrue();
        assertThat(updateResult.updateCount()).isEqualTo(-1);
        assertThatThrownBy(() -> sqlService.execute("SELECT * FROM public." + MAPPING_NAME))
                .isInstanceOf(HazelcastSqlException.class);
    }

    @Test
    public void when_schemaNameUsed_then_rejected() {
        assertThatThrownBy(() ->
                sqlService.execute(javaSerializableMapDdl("schema.m", Long.class, Long.class)))
                .hasMessageContaining("Encountered \".\" at line 1, column 22");
    }

    @Test
    public void when_emptyColumnList_then_fail() {
        assertThatThrownBy(() -> sqlService.execute("CREATE MAPPING m() TYPE m"))
                .hasMessageContaining("Encountered \")\" at line 1");
    }

    @Test
    public void when_badType_then_fail() {
        assertThatThrownBy(() -> sqlService.execute("CREATE MAPPING m TYPE TooBad"))
                .hasMessageContaining("Unknown connector type: TooBad");
    }

    @Test
    public void test_caseInsensitiveType() {
        sqlService.execute("CREATE MAPPING t1 TYPE TestStream");
        sqlService.execute("CREATE MAPPING t2 TYPE teststream");
        sqlService.execute("CREATE MAPPING t3 TYPE TESTSTREAM");
        sqlService.execute("CREATE MAPPING t4 TYPE tEsTsTrEaM");
    }
}
