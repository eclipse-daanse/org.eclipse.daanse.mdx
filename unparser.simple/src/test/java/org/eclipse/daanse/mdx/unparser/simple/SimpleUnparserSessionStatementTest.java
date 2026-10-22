/*
* Copyright (c) 2026 Contributors to the Eclipse Foundation.
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
package org.eclipse.daanse.mdx.unparser.simple;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import org.eclipse.daanse.mdx.model.api.CubeReference;
import org.eclipse.daanse.mdx.model.api.SetEvaluation;
import org.eclipse.daanse.mdx.model.api.expression.CompoundId;
import org.eclipse.daanse.mdx.model.api.expression.NameObjectIdentifier;
import org.eclipse.daanse.mdx.model.api.expression.ObjectIdentifier;
import org.eclipse.daanse.mdx.model.api.expression.ObjectIdentifier.Quoting;
import org.eclipse.daanse.mdx.model.api.expression.operation.BracesOperationAtom;
import org.eclipse.daanse.mdx.model.record.CreateMemberStatementR;
import org.eclipse.daanse.mdx.model.record.CreateSetStatementR;
import org.eclipse.daanse.mdx.model.record.DropMemberStatementR;
import org.eclipse.daanse.mdx.model.record.DropSetStatementR;
import org.eclipse.daanse.mdx.model.record.QualifiedNameR;
import org.eclipse.daanse.mdx.model.record.expression.CallExpressionR;
import org.eclipse.daanse.mdx.model.record.expression.CompoundIdR;
import org.eclipse.daanse.mdx.model.record.expression.NameObjectIdentifierR;
import org.eclipse.daanse.mdx.model.record.expression.NumericLiteralR;
import org.eclipse.daanse.mdx.model.record.expression.StringLiteralR;
import org.eclipse.daanse.mdx.model.record.select.CreateMemberBodyClauseR;
import org.eclipse.daanse.mdx.model.record.select.CreateSetBodyClauseR;
import org.eclipse.daanse.mdx.model.record.select.MemberPropertyDefinitionR;
import org.junit.jupiter.api.Test;

class SimpleUnparserSessionStatementTest {

    private SimpleUnparser unparser = new SimpleUnparser();

    private static NameObjectIdentifier quoted(String name) {
        return new NameObjectIdentifierR(name, Quoting.QUOTED);
    }

    private static CompoundId id(String... names) {
        return new CompoundIdR(List.of(names).stream().<ObjectIdentifier>map(SimpleUnparserSessionStatementTest::quoted).toList());
    }

    private static final CubeReference AW = new CubeReference.Named(quoted("Adventure Works"));

    @Test
    void createMember() {
        var statement = new CreateMemberStatementR(false, false, AW,
                new CreateMemberBodyClauseR(id("Measures", "X"), new NumericLiteralR(BigDecimal.ONE), List.of()));
        assertThat(unparser.unparseMdxStatement(statement)).asString()
                .isEqualTo("CREATE MEMBER [Adventure Works].[Measures].[X] AS 1");
    }

    @Test
    void createSessionCalculatedMemberWithProperty() {
        var statement = new CreateMemberStatementR(true, true, AW, new CreateMemberBodyClauseR(id("Measures", "X"),
                new NumericLiteralR(BigDecimal.ONE),
                List.of(new MemberPropertyDefinitionR(new StringLiteralR("Currency"),
                        new NameObjectIdentifierR("FORMAT_STRING", Quoting.UNQUOTED)))));
        assertThat(unparser.unparseMdxStatement(statement)).asString()
                .isEqualTo("CREATE SESSION CALCULATED MEMBER [Adventure Works].[Measures].[X] AS 1 ,\r\n"
                        + " FORMAT_STRING = \"Currency\"");
    }

    @Test
    void createSetKeepsOnlyTheWrittenWords() {
        var body = new CreateSetBodyClauseR(id("S"), new CallExpressionR(new BracesOperationAtom(), List.of()));
        assertThat(unparser.unparseMdxStatement(new CreateSetStatementR(false, Optional.empty(), false, AW, body)))
                .asString().isEqualTo("CREATE SET [Adventure Works].[S] AS {}");
        assertThat(unparser.unparseMdxStatement(
                new CreateSetStatementR(true, Optional.of(SetEvaluation.DYNAMIC), true, AW, body))).asString()
                .isEqualTo("CREATE SESSION DYNAMIC HIDDEN SET [Adventure Works].[S] AS {}");
    }

    @Test
    void createSetWithPropertyOnCurrentCube() {
        var body = new CreateSetBodyClauseR(id("S"), new CallExpressionR(new BracesOperationAtom(), List.of()),
                List.of(new MemberPropertyDefinitionR(new StringLiteralR("c"),
                        new NameObjectIdentifierR("CAPTION", Quoting.UNQUOTED))));
        assertThat(unparser.unparseMdxStatement(new CreateSetStatementR(false, Optional.of(SetEvaluation.STATIC),
                false, new CubeReference.Current(), body))).asString()
                .isEqualTo("CREATE STATIC SET CURRENTCUBE.[S] AS {} ,\r\n CAPTION = \"c\"");
    }

    @Test
    void cubeNamedCurrentCubeKeepsItsBrackets() {
        var body = new CreateSetBodyClauseR(id("S"), new CallExpressionR(new BracesOperationAtom(), List.of()));
        var cube = new CubeReference.Named(new NameObjectIdentifierR("CurrentCube", Quoting.UNQUOTED));
        assertThat(unparser.unparseMdxStatement(new CreateSetStatementR(false, Optional.empty(), false, cube, body)))
                .asString().isEqualTo("CREATE SET [CurrentCube].[S] AS {}");
    }

    @Test
    void drop() {
        assertThat(unparser.unparseMdxStatement(new DropMemberStatementR(List.of(new QualifiedNameR(AW,
                id("Measures", "X")))))).asString().isEqualTo("DROP MEMBER [Adventure Works].[Measures].[X]");
        assertThat(unparser.unparseMdxStatement(new DropSetStatementR(List.of(new QualifiedNameR(AW, id("A")),
                new QualifiedNameR(new CubeReference.Current(), id("B")))))).asString()
                .isEqualTo("DROP SET [Adventure Works].[A], CURRENTCUBE.[B]");
    }
}
