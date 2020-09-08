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

package com.hazelcast.jet.sql;

import com.hazelcast.sql.SqlIntegrationTestSuite;
import com.hazelcast.sql.SqlTestInstanceFactory;
import org.junit.BeforeClass;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;

/**
 * A suite to run all the IMDG SQL tests with JetInstance.
 */
@RunWith(Suite.class)
@Suite.SuiteClasses(SqlIntegrationTestSuite.class)
public final class ImdgSqlTestSuite {

    private ImdgSqlTestSuite() {
    }

    @BeforeClass
    public static void beforeClass() {
        // set the factory to use Jet clusters
        SqlTestInstanceFactory.isJet = true;
    }
}