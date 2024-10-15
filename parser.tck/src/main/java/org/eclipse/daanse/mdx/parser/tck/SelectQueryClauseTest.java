/*
* Copyright (c) 2023 Contributors to the Eclipse Foundation.
*
* This program and the accompanying materials are made
* available under the terms of the Eclipse Public License 2.0
* which is available at https://www.eclipse.org/legal/epl-2.0/
*
* SPDX-License-Identifier: EPL-2.0
*
* Contributors:
*   SmartCity Jena - initial
*   Stefan Bischof (bipolis.org) - initial
*/
package org.eclipse.daanse.mdx.parser.tck;

import static org.assertj.core.api.Assertions.assertThat;
import static org.eclipse.daanse.mdx.parser.tck.CubeTest.propertyWords;

import org.eclipse.daanse.mdx.model.api.SelectStatement;
import org.eclipse.daanse.mdx.model.api.select.SelectQueryAsteriskClause;
import org.eclipse.daanse.mdx.model.api.select.SelectQueryAxesClause;
import org.eclipse.daanse.mdx.model.api.select.SelectQueryEmptyClause;
import org.eclipse.daanse.mdx.parser.api.MdxParserException;
import org.eclipse.daanse.mdx.parser.api.MdxParserProvider;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.osgi.service.component.annotations.RequireServiceComponentRuntime;
import org.osgi.test.common.annotation.InjectService;

@RequireServiceComponentRuntime
class SelectQueryClauseTest {
    @Nested
    class SelectQueryAxesClauseClauseTest {

        @Test
        void testInStatement(@InjectService MdxParserProvider mdxParserProvider) throws MdxParserException {
            String mdx = """
                    SELECT [Customer].[Gender].[Gender].Membmers ON COLUMNS,
                            {[Customer].[Customer].[Aaron A. Allen],
                            [Customer].[Customer].[Abigail Clark]} ON ROWS
                    FROM [c]
                    """;

            SelectStatement selectStatement = mdxParserProvider.newParser(mdx, propertyWords).parseSelectStatement();
            System.out.println(selectStatement);
            assertThat(selectStatement).isNotNull();

        }

        @Test
        void testInClauseMultiple(@InjectService MdxParserProvider mdxParserProvider) throws MdxParserException {
            String mdx = """
                    [Customer].[Gender].[Gender].Membmers ON COLUMNS,
                            {[Customer].[Customer].[Aaron A. Allen],
                            [Customer].[Customer].[Abigail Clark]} ON ROWS
                    """;
            SelectQueryAxesClause clause = mdxParserProvider.newParser(mdx, propertyWords).parseSelectQueryAxesClause();
            assertThat(clause).isNotNull().isInstanceOf(SelectQueryAxesClause.class);
        }

        @Test
        void testInClauseSingle(@InjectService MdxParserProvider mdxParserProvider) throws MdxParserException {
            String mdx = "[Customer] ON COLUMNS";
            SelectQueryAxesClause clause = mdxParserProvider.newParser(mdx, propertyWords).parseSelectQueryAxesClause();
            assertThat(clause).isNotNull().isInstanceOf(SelectQueryAxesClause.class);
        }
    }

    @Nested
    class SelectQueryEmptyClauseTest {
        @Test
        void testInStatement(@InjectService MdxParserProvider mdxParserProvider) throws MdxParserException {
            String mdx = "SELECT FROM [c]";

            SelectStatement selectStatement = mdxParserProvider.newParser(mdx, propertyWords).parseSelectStatement();
            assertThat(selectStatement).isNotNull();
            assertThat(selectStatement.selectQueryClause()).isNotNull().isInstanceOf(SelectQueryEmptyClause.class);

        }
    }

    @Nested
    class SelectQueryAsteriskClauseTest {

        @Test
        void testAsteriskInStatement(@InjectService MdxParserProvider mdxParserProvider) throws MdxParserException {
            String mdx = "SELECT * FROM [c]";

            SelectStatement selectStatement = mdxParserProvider.newParser(mdx, propertyWords).parseSelectStatement();
            assertThat(selectStatement).isNotNull();
            assertThat(selectStatement.selectQueryClause()).isNotNull().isInstanceOf(SelectQueryAsteriskClause.class);

        }

        @Test
        void testAsteriskInClause(@InjectService MdxParserProvider mdxParserProvider) throws MdxParserException {
            String mdx = "*";
            SelectQueryAsteriskClause clause = mdxParserProvider.newParser(mdx, propertyWords)
                    .parseSelectQueryAsteriskClause();
            assertThat(clause).isNotNull();
        }

    }
}
