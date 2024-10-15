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

import org.eclipse.daanse.mdx.model.api.DrillthroughStatement;
import org.eclipse.daanse.mdx.model.api.expression.NameObjectIdentifier;
import org.eclipse.daanse.mdx.model.api.expression.ObjectIdentifier;
import org.eclipse.daanse.mdx.model.api.select.SelectSubcubeClauseName;
import org.eclipse.daanse.mdx.parser.api.MdxParserException;
import org.eclipse.daanse.mdx.parser.api.MdxParserProvider;
import org.junit.jupiter.api.Test;
import org.osgi.service.component.annotations.RequireServiceComponentRuntime;
import org.osgi.test.common.annotation.InjectService;

@RequireServiceComponentRuntime
class DrillthroughStatementTest {

    @Test
    void test1(@InjectService MdxParserProvider mdxParserProvider) throws MdxParserException {
        String mdx = """
                DRILLTHROUGH MAXROWS 10
                SELECT *
                FROM [Adventure Works]
                """;
        DrillthroughStatement clause = mdxParserProvider.newParser(mdx, propertyWords).parseDrillthroughStatement();
        assertThat(clause).isNotNull().isInstanceOf(DrillthroughStatement.class);
        assertThat(clause.firstRowSet()).isNotPresent();
        assertThat(clause.maxRows()).isPresent().contains(10);
        assertThat(clause.selectStatement()).isNotNull();
        assertThat(clause.selectStatement().selectSubcubeClause()).isNotNull()
                .isInstanceOf(SelectSubcubeClauseName.class);
        SelectSubcubeClauseName selectSubcubeClauseName = (SelectSubcubeClauseName) clause.selectStatement()
                .selectSubcubeClause();
        assertThat(selectSubcubeClauseName.cubeName().name()).isEqualTo("Adventure Works");
        assertThat(selectSubcubeClauseName.cubeName().quoting()).isEqualTo(ObjectIdentifier.Quoting.QUOTED);
        assertThat(clause.returnItems()).isNull();
    }

    @Test
    void test2(@InjectService MdxParserProvider mdxParserProvider) throws MdxParserException {
        String mdx = """
                DRILLTHROUGH MAXROWS 10
                FIRSTROWSET 1
                SELECT *
                FROM [Adventure Works]
                RETURN a
                """;
        DrillthroughStatement clause = mdxParserProvider.newParser(mdx, propertyWords).parseDrillthroughStatement();
        assertThat(clause).isNotNull().isInstanceOf(DrillthroughStatement.class);
        assertThat(clause.firstRowSet()).isPresent().contains(1);
        assertThat(clause.maxRows()).isPresent().contains(10);
        assertThat(clause.selectStatement()).isNotNull();
        assertThat(clause.selectStatement().selectSubcubeClause()).isNotNull()
                .isInstanceOf(SelectSubcubeClauseName.class);
        SelectSubcubeClauseName selectSubcubeClauseName = (SelectSubcubeClauseName) clause.selectStatement()
                .selectSubcubeClause();
        assertThat(selectSubcubeClauseName.cubeName().name()).isEqualTo("Adventure Works");
        assertThat(selectSubcubeClauseName.cubeName().quoting()).isEqualTo(ObjectIdentifier.Quoting.QUOTED);
        assertThat(clause.returnItems()).isNotNull().hasSize(1);
        assertThat(clause.returnItems().get(0).compoundId()).isNotNull();
        assertThat(clause.returnItems().get(0).compoundId().objectIdentifiers()).isNotNull().hasSize(1);
        assertThat(clause.returnItems().get(0).compoundId().objectIdentifiers().get(0))
                .isInstanceOf(NameObjectIdentifier.class);
        assertThat(((NameObjectIdentifier) clause.returnItems().get(0).compoundId().objectIdentifiers().get(0)).name())
                .isEqualTo("a");
        assertThat(clause.returnItems().get(0).compoundId().objectIdentifiers().get(0).quoting())
                .isEqualTo(ObjectIdentifier.Quoting.UNQUOTED);
    }

}
