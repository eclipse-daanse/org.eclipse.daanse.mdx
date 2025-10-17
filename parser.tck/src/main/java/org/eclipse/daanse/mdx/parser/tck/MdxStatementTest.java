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
import static org.eclipse.daanse.mdx.parser.tck.MdxTestUtils.checkSelectCubeClauseName;
import static org.eclipse.daanse.mdx.parser.tck.SelectQueryAxisClauseTest.checkSelectQueryAxisClause1;
import static org.eclipse.daanse.mdx.parser.tck.SelectQueryAxisClauseTest.checkSelectQueryAxisClause2;
import static org.eclipse.daanse.mdx.parser.tck.SelectSlicerAxisClauseTest.checkSelectSlicerAxisClause1;

import org.eclipse.daanse.mdx.model.api.MdxStatement;
import org.eclipse.daanse.mdx.model.api.SelectStatement;
import org.eclipse.daanse.mdx.model.api.expression.ObjectIdentifier;
import org.eclipse.daanse.mdx.model.api.select.SelectQueryAxesClause;
import org.eclipse.daanse.mdx.model.api.select.SelectQueryAxisClause;
import org.eclipse.daanse.mdx.model.api.select.SelectCubeClauseName;
import org.eclipse.daanse.mdx.parser.api.MdxParserException;
import org.eclipse.daanse.mdx.parser.api.MdxParserProvider;
import org.junit.jupiter.api.Test;
import org.osgi.service.component.annotations.RequireServiceComponentRuntime;
import org.osgi.test.common.annotation.InjectService;

@RequireServiceComponentRuntime
class MdxStatementTest {

    @Test
    void testTypeSelectStatement(@InjectService MdxParserProvider mdxParserProvider) throws MdxParserException {
        String mdx = """
                SELECT [Customer].[Gender].[Gender].Membmers ON COLUMNS,
                        {[Customer].[Customer].[Aaron A. Allen],
                        [Customer].[Customer].[Abigail Clark]} ON ROWS
                FROM [Adventure Works]
                WHERE [Measures].[Internet Sales Amount]
                """;

        MdxStatement clause = mdxParserProvider.newParser(mdx, propertyWords).parseMdxStatement();
        assertThat(clause).isNotNull().isInstanceOf(SelectStatement.class);
        SelectStatement selectStatement = (SelectStatement) clause;

        assertThat(selectStatement.selectWithClauses()).isNotNull().isEmpty();

        assertThat(selectStatement.selectQueryClause()).isNotNull().isInstanceOf(SelectQueryAxesClause.class);
        SelectQueryAxesClause selectQueryAxesClause = (SelectQueryAxesClause) selectStatement.selectQueryClause();
        assertThat(selectQueryAxesClause.selectQueryAxisClauses()).isNotNull().hasSize(2);
        SelectQueryAxisClause selectQueryAxisClause1 = selectQueryAxesClause.selectQueryAxisClauses().get(0);
        SelectQueryAxisClause selectQueryAxisClause2 = selectQueryAxesClause.selectQueryAxisClauses().get(1);

        checkSelectQueryAxisClause1(selectQueryAxisClause1);
        checkSelectQueryAxisClause2(selectQueryAxisClause2);

        assertThat(selectStatement.selectCubeClause()).isNotNull().isInstanceOf(SelectCubeClauseName.class);
        checkSelectCubeClauseName(selectStatement.selectCubeClause(), "Adventure Works",
                ObjectIdentifier.Quoting.QUOTED);

        assertThat(selectStatement.selectSlicerAxisClause()).isPresent();
        checkSelectSlicerAxisClause1(selectStatement.selectSlicerAxisClause().get());

        assertThat(selectStatement.selectCellPropertyListClause()).isNotPresent();
    }

    @Test
    void testCommentsAndChars(@InjectService MdxParserProvider mdxParserProvider) throws MdxParserException {
        String mdx = """
                SELECT [Customer].[Gender].[Gender].Membmers ON COLUMNS,\r
                        {[Customer].[Customer]/*multi
                        string comment */.[Aaron A. Allen],\n
                        [Customer].[Customer].[Abigail Clark]} ON ROWS\f
                        /* multi
                        string comment */
                FROM [Adventure Works]\t
                WHERE [Measures].[Internet Sales Amount]
                //comment test
                """;
        MdxStatement clause = mdxParserProvider.newParser(mdx, propertyWords).parseMdxStatement();
        assertThat(clause).isNotNull().isInstanceOf(SelectStatement.class);
        SelectStatement selectStatement = (SelectStatement) clause;

        assertThat(selectStatement.selectWithClauses()).isNotNull().isEmpty();

        assertThat(selectStatement.selectQueryClause()).isNotNull().isInstanceOf(SelectQueryAxesClause.class);
        SelectQueryAxesClause selectQueryAxesClause = (SelectQueryAxesClause) selectStatement.selectQueryClause();
        assertThat(selectQueryAxesClause.selectQueryAxisClauses()).isNotNull().hasSize(2);
        SelectQueryAxisClause selectQueryAxisClause1 = selectQueryAxesClause.selectQueryAxisClauses().get(0);
        SelectQueryAxisClause selectQueryAxisClause2 = selectQueryAxesClause.selectQueryAxisClauses().get(1);

        checkSelectQueryAxisClause1(selectQueryAxisClause1);
        checkSelectQueryAxisClause2(selectQueryAxisClause2);

        assertThat(selectStatement.selectCubeClause()).isNotNull().isInstanceOf(SelectCubeClauseName.class);
        checkSelectCubeClauseName(selectStatement.selectCubeClause(), "Adventure Works",
                ObjectIdentifier.Quoting.QUOTED);

        assertThat(selectStatement.selectSlicerAxisClause()).isPresent();
        checkSelectSlicerAxisClause1(selectStatement.selectSlicerAxisClause().get());

        assertThat(selectStatement.selectCellPropertyListClause()).isNotPresent();
    }

    @Test
    void testDrillThroughMdx(@InjectService MdxParserProvider mdxParserProvider) throws MdxParserException {
        MdxStatement clause = mdxParserProvider
                .newParser("DRILLTHROUGH SELECT [Foo] on 0, [Bar] on 1 FROM [Cube]", propertyWords).parseMdxStatement();
        assertThat(clause).isNotNull().isInstanceOf(MdxStatement.class);
    }

}
