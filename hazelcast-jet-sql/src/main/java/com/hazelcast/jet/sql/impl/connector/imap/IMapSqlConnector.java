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

package com.hazelcast.jet.sql.impl.connector.imap;

import com.hazelcast.function.FunctionEx;
import com.hazelcast.jet.JetInstance;
import com.hazelcast.jet.core.DAG;
import com.hazelcast.jet.core.Vertex;
import com.hazelcast.jet.core.processor.Processors;
import com.hazelcast.jet.core.processor.SinkProcessors;
import com.hazelcast.jet.datamodel.Tuple2;
import com.hazelcast.jet.pipeline.ServiceFactories;
import com.hazelcast.jet.sql.SqlConnector;
import com.hazelcast.jet.sql.impl.connector.SqlWriters.EntryWriter;
import com.hazelcast.jet.sql.impl.expression.ExpressionUtil;
import com.hazelcast.jet.sql.impl.schema.JetTable;
import com.hazelcast.query.Predicates;
import com.hazelcast.sql.impl.expression.Expression;
import com.hazelcast.sql.impl.type.QueryDataType;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import static com.hazelcast.jet.core.Edge.between;
import static com.hazelcast.jet.core.processor.Processors.mapP;
import static com.hazelcast.jet.core.processor.SourceProcessors.readMapP;
import static com.hazelcast.jet.datamodel.Tuple2.tuple2;
import static com.hazelcast.jet.sql.impl.connector.SqlWriters.entryWriter;

public class IMapSqlConnector implements SqlConnector {

    /**
     * A key in the table options (TO).
     * <p>
     * Specifies the map name. If missing, the IMap name is assumed to be equal
     * to the table name.
     */
    public static final String TO_MAP_NAME = "mapName";

    /**
     * A key in the table options (TO).
     * <p>
     * Specifies the key class in the IMap entry. Can be omitted if "__key" is
     * one of the columns.
     */
    public static final String TO_KEY_CLASS = "keyClass";

    /**
     * A key in the table options (TO).
     * <p>
     * Specifies the value class in the IMap entry. Can be omitted if "this" is
     * one of the columns.
     */
    public static final String TO_VALUE_CLASS = "valueClass";

    @Override
    public boolean isStream() {
        return false;
    }

    @Nullable @Override
    public JetTable createTable(
            @Nonnull JetInstance jetInstance,
            @Nonnull String tableName,
            @Nonnull Map<String, String> serverOptions,
            @Nonnull Map<String, String> tableOptions
    ) {
        throw new UnsupportedOperationException("TODO field examination");
    }

    @Nullable @Override
    public JetTable createTable(
            @Nonnull JetInstance jetInstance,
            @Nonnull String tableName,
            @Nonnull Map<String, String> serverOptions,
            @Nonnull Map<String, String> tableOptions,
            @Nonnull List<Entry<String, QueryDataType>> fields
    ) {
        // TODO validate options
//        if (!serverOptions.isEmpty()) {
//            throw new JetException("Only local maps are supported for now");
//        }
        String mapName = tableOptions.getOrDefault(TO_MAP_NAME, tableName);
        EntryWriter writer = entryWriter(fields, tableOptions.get(TO_KEY_CLASS), tableOptions.get(TO_VALUE_CLASS));
        return new IMapTable(this, mapName, fields, writer);
    }

    @Nullable @Override
    public Vertex fullScanReader(
            @Nonnull DAG dag,
            @Nonnull JetTable jetTable,
            @Nullable String timestampField,
            @Nullable Expression<Boolean> predicate,
            @Nonnull List<Expression<?>> projections
    ) {
        IMapTable table = (IMapTable) jetTable;
        FunctionEx<Entry<Object, Object>, Object[]> mapProjection =
                ExpressionUtil.projectionFn(jetTable, predicate, projections);

        String mapName = table.getMapName();
        return dag.newVertex("map(" + mapName + ")",
                readMapP(mapName, Predicates.alwaysTrue(), entry -> mapProjection.apply(entry)));
    }

    @Nullable @Override
    public Tuple2<Vertex, Vertex> nestedLoopReader(
            @Nonnull DAG dag,
            @Nonnull JetTable jetTable,
            @Nonnull Expression<Boolean> predicateWithParams,
            @Nonnull List<Expression<?>> projection
    ) {
        IMapTable table = (IMapTable) jetTable;
        String mapName = table.getMapName();

        Vertex v = dag.newVertex("enrich", Processors.mapUsingServiceAsyncP(
                ServiceFactories.iMapService(mapName),
                1024,
                true,
                t -> {
                    throw new RuntimeException();
                }, // not needed for ordered
                (map, item) -> {
                    // TODO query the map based on the predicate&projection
                    return null;
                }
        ));
        return tuple2(v, v);
    }

    @Nullable @Override
    public Vertex sink(
            @Nonnull DAG dag,
            @Nonnull JetTable jetTable
    ) {
        IMapTable table = (IMapTable) jetTable;

        EntryWriter writer = table.getWriter();
        Vertex vStart = dag.newVertex("project", mapP(writer));

        String mapName = table.getMapName();
        Vertex vEnd = dag.newVertex("mapSink", SinkProcessors.writeMapP(mapName));

        dag.edge(between(vStart, vEnd));
        return vStart;
    }

    @Override
    public boolean supportsFullScanReader() {
        return true;
    }

    @Override
    public boolean supportsNestedLoopReader() {
        return true;
    }

    @Override
    public boolean supportsSink() {
        return true;
    }
}