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
import static org.eclipse.daanse.mdx.parser.tck.MdxTestUtils.checkAxis;
import static org.eclipse.daanse.mdx.parser.tck.MdxTestUtils.checkNameObjectIdentifiers;
import static org.eclipse.daanse.mdx.parser.tck.MdxTestUtils.checkSelectCubeClauseName;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.eclipse.daanse.mdx.model.api.expression.CallExpression;
import org.eclipse.daanse.mdx.model.api.expression.CompoundId;
import org.eclipse.daanse.mdx.model.api.expression.KeyObjectIdentifier;
import org.eclipse.daanse.mdx.model.api.expression.NameObjectIdentifier;
import org.eclipse.daanse.mdx.model.api.expression.ObjectIdentifier;
import org.eclipse.daanse.mdx.model.api.expression.operation.BracesOperationAtom;
import org.eclipse.daanse.mdx.model.api.select.SelectQueryAxesClause;
import org.eclipse.daanse.mdx.model.api.select.SelectQueryAxisClause;
import org.eclipse.daanse.mdx.model.api.select.SelectCubeClause;
import org.eclipse.daanse.mdx.model.api.select.SelectCubeClauseName;
import org.eclipse.daanse.mdx.model.api.select.SelectCubeClauseSubStatement;
import org.eclipse.daanse.mdx.parser.api.MdxParserException;
import org.eclipse.daanse.mdx.parser.api.MdxParserProvider;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.osgi.service.component.annotations.RequireServiceComponentRuntime;
import org.osgi.test.common.annotation.InjectService;

@RequireServiceComponentRuntime
class SelectCubeClauseTest {

    @Nested
    class SelectCubeClauseNameTest {

        @Test
        void testUnQuoted(@InjectService MdxParserProvider mdxParserProvider) throws MdxParserException {
            SelectCubeClause selectCubeClause = mdxParserProvider.newParser("subcube", propertyWords)
                    .parseSelectCubeClause();
            assertThat(selectCubeClause).isNotNull().isInstanceOf(SelectCubeClauseName.class);
            SelectCubeClauseName selectCubeClauseName = (SelectCubeClauseName) selectCubeClause;
            assertThat(selectCubeClauseName.cubeName()).isNotNull();
            assertThat(selectCubeClauseName.cubeName().name()).isNotNull().isEqualTo("subcube");
            assertThat(selectCubeClauseName.cubeName().quoting()).isNotNull()
                    .isEqualTo(ObjectIdentifier.Quoting.UNQUOTED);
        }

        @Test
        void testQuoted(@InjectService MdxParserProvider mdxParserProvider) throws MdxParserException {
            SelectCubeClause selectCubeClause = mdxParserProvider.newParser("[subcube]", propertyWords)
                    .parseSelectCubeClause();
            assertThat(selectCubeClause).isNotNull().isInstanceOf(SelectCubeClauseName.class);
            checkSelectCubeClauseName((SelectCubeClauseName) selectCubeClause, "subcube",
                    ObjectIdentifier.Quoting.QUOTED);
        }

        @Test
        void testEmpty(@InjectService MdxParserProvider mdxParserProvider) {
            assertThrows(MdxParserException.class, () -> mdxParserProvider.newParser("", propertyWords));
        }

    }

    @Nested
    class SelectCubeClauseSubStatementTest {

        @Test
        void testSingleCube(@InjectService MdxParserProvider mdxParserProvider) throws MdxParserException {
            SelectCubeClause selectCubeClause = mdxParserProvider
                    .newParser("(SELECT {[Date].[Calendar].[Calendar Year].&[2001]} ON 0 FROM [Adventure Works])",
                            propertyWords)
                    .parseSelectCubeClause();
            checkSelectCubeClauseSubStatement(selectCubeClause);
            assertThat(selectCubeClause).isNotNull().isInstanceOf(SelectCubeClauseSubStatement.class);
            SelectCubeClauseSubStatement selectCubeClauseSubStatement = (SelectCubeClauseSubStatement) selectCubeClause;
            checkSelectCubeClauseName(selectCubeClauseSubStatement.selectCubeClause(), "Adventure Works",
                    ObjectIdentifier.Quoting.QUOTED);
            assertThat(selectCubeClauseSubStatement.selectSlicerAxisClause()).isNotNull().isNotPresent();
        }

        // (SELECT {[Date].[Calendar].[Calendar Year].&[2001]} ON 0 FROM
        static void checkSelectCubeClauseSubStatement(SelectCubeClause selectCubeClause) {
            assertThat(selectCubeClause).isNotNull().isInstanceOf(SelectCubeClauseSubStatement.class);
            SelectCubeClauseSubStatement selectCubeClauseSubStatement = (SelectCubeClauseSubStatement) selectCubeClause;

            assertThat(selectCubeClauseSubStatement.selectQueryClause()).isNotNull();
            assertThat(selectCubeClauseSubStatement.selectQueryClause()).isInstanceOf(SelectQueryAxesClause.class);
            SelectQueryAxesClause selectQueryAxesClause = (SelectQueryAxesClause) selectCubeClauseSubStatement
                    .selectQueryClause();
            assertThat(selectQueryAxesClause.selectQueryAxisClauses()).hasSize(1);
            SelectQueryAxisClause selectQueryAxisClause = selectQueryAxesClause.selectQueryAxisClauses().get(0);
            assertThat(selectQueryAxisClause.nonEmpty()).isFalse();
            assertThat(selectQueryAxisClause.expression()).isNotNull();
            assertThat(selectQueryAxisClause.selectDimensionPropertyListClause()).isNull();
            checkAxis(selectQueryAxisClause.axis(), 0, true);
            assertThat(selectQueryAxisClause.expression()).isInstanceOf(CallExpression.class);
            CallExpression callExpression = (CallExpression) selectQueryAxisClause.expression();
            assertThat(callExpression.operationAtom()).isEqualTo(new BracesOperationAtom());
            assertThat(callExpression.expressions()).isNotNull().hasSize(1);
            assertThat(callExpression.expressions().get(0)).isInstanceOf(CompoundId.class);
            CompoundId compoundId = (CompoundId) callExpression.expressions().get(0);
            assertThat(compoundId.objectIdentifiers()).isNotNull().hasSize(4);
            assertThat(compoundId.objectIdentifiers().get(0)).isInstanceOf(NameObjectIdentifier.class);
            assertThat(compoundId.objectIdentifiers().get(1)).isInstanceOf(NameObjectIdentifier.class);
            assertThat(compoundId.objectIdentifiers().get(2)).isInstanceOf(NameObjectIdentifier.class);
            assertThat(compoundId.objectIdentifiers().get(3)).isInstanceOf(KeyObjectIdentifier.class);
            checkNameObjectIdentifiers(compoundId.objectIdentifiers(), 0, "Date", ObjectIdentifier.Quoting.QUOTED);
            checkNameObjectIdentifiers(compoundId.objectIdentifiers(), 1, "Calendar", ObjectIdentifier.Quoting.QUOTED);
            checkNameObjectIdentifiers(compoundId.objectIdentifiers(), 2, "Calendar Year",
                    ObjectIdentifier.Quoting.QUOTED);
            KeyObjectIdentifier keyObjectIdentifier = (KeyObjectIdentifier) compoundId.objectIdentifiers().get(3);
            assertThat(keyObjectIdentifier.nameObjectIdentifiers()).hasSize(1);
            checkNameObjectIdentifiers(keyObjectIdentifier.nameObjectIdentifiers(), 0, "2001",
                    ObjectIdentifier.Quoting.QUOTED);
        }
    }

    @Test
    void testMultiCube(@InjectService MdxParserProvider mdxParserProvider) throws MdxParserException {

        SelectCubeClause selectCubeClause = mdxParserProvider.newParser(
                "(SELECT {[Date].[Calendar].[Calendar Year].&[2001]} ON 0 FROM (SELECT {test} ON 0 FROM [cube]))",
                propertyWords).parseSelectCubeClause();
        assertThat(selectCubeClause).isNotNull().isInstanceOf(SelectCubeClauseSubStatement.class);
        SelectCubeClauseSubStatement selectCubeClauseSubStatement = (SelectCubeClauseSubStatement) selectCubeClause;
        SelectCubeClauseSubStatementTest.checkSelectCubeClauseSubStatement(selectCubeClause);

        assertThat(selectCubeClauseSubStatement.selectCubeClause()).isNotNull()
                .isInstanceOf(SelectCubeClauseSubStatement.class);
        SelectCubeClauseSubStatement selectCubeClauseSubStatementInner = (SelectCubeClauseSubStatement) selectCubeClauseSubStatement
                .selectCubeClause();
        assertThat(selectCubeClauseSubStatementInner.selectCubeClause()).isNotNull();

        SelectQueryAxesClause selectQueryAxesClauseInner = (SelectQueryAxesClause) selectCubeClauseSubStatementInner
                .selectQueryClause();
        assertThat(selectQueryAxesClauseInner.selectQueryAxisClauses()).hasSize(1);
        SelectQueryAxisClause selectQueryAxisClauseInner = selectQueryAxesClauseInner.selectQueryAxisClauses().get(0);
        assertThat(selectQueryAxisClauseInner.nonEmpty()).isFalse();
        checkAxis(selectQueryAxisClauseInner.axis(), 0, true);
        assertThat(selectQueryAxisClauseInner.expression()).isNotNull();
        assertThat(selectQueryAxisClauseInner.selectDimensionPropertyListClause()).isNull();

        CallExpression callExpressionInner = (CallExpression) selectQueryAxisClauseInner.expression();
        assertThat(callExpressionInner.operationAtom()).isEqualTo(new BracesOperationAtom());
        assertThat(callExpressionInner.expressions()).isNotNull().hasSize(1);
        assertThat(callExpressionInner.expressions().get(0)).isInstanceOf(CompoundId.class);
        CompoundId compoundIdInner = (CompoundId) callExpressionInner.expressions().get(0);
        assertThat(compoundIdInner.objectIdentifiers()).isNotNull().hasSize(1);
        checkNameObjectIdentifiers(compoundIdInner.objectIdentifiers(), 0, "test", ObjectIdentifier.Quoting.UNQUOTED);
        assertThat(selectCubeClauseSubStatementInner.selectSlicerAxisClause()).isNotNull().isNotPresent();

        assertThat(selectCubeClauseSubStatementInner.selectCubeClause()).isNotNull()
                .isInstanceOf(SelectCubeClauseName.class);
        checkSelectCubeClauseName((SelectCubeClauseName) selectCubeClauseSubStatementInner.selectCubeClause(),
                "cube", ObjectIdentifier.Quoting.QUOTED);
    }
}
