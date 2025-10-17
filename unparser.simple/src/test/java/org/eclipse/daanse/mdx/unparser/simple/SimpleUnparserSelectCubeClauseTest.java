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
package org.eclipse.daanse.mdx.unparser.simple;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import java.util.Optional;

import org.eclipse.daanse.mdx.model.api.expression.NameObjectIdentifier;
import org.eclipse.daanse.mdx.model.api.expression.ObjectIdentifier;
import org.eclipse.daanse.mdx.model.api.expression.operation.BracesOperationAtom;
import org.eclipse.daanse.mdx.model.api.select.SelectCubeClause;
import org.eclipse.daanse.mdx.model.api.select.SelectCubeClauseSubStatement;
import org.eclipse.daanse.mdx.model.record.expression.CallExpressionR;
import org.eclipse.daanse.mdx.model.record.expression.KeyObjectIdentifierR;
import org.eclipse.daanse.mdx.model.record.expression.NameObjectIdentifierR;
import org.eclipse.daanse.mdx.model.record.select.AxisR;
import org.eclipse.daanse.mdx.model.record.select.SelectQueryAxesClauseR;
import org.eclipse.daanse.mdx.model.record.select.SelectQueryAxisClauseR;
import org.eclipse.daanse.mdx.model.record.select.SelectCubeClauseNameR;
import org.eclipse.daanse.mdx.model.record.select.SelectCubeClauseSubStatementR;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

class SimpleUnparserSelectCubeClauseTest {

    private SimpleUnparser unparser = new SimpleUnparser();

    @Nested
    class SelectCubeClauseNameTestTest {

        @Test
        void testUnQuoted() {
            NameObjectIdentifier cubeName = new NameObjectIdentifierR("subCube", ObjectIdentifier.Quoting.UNQUOTED);
            SelectCubeClause selectCubeClauseName = new SelectCubeClauseNameR(cubeName);
            assertThat(unparser.unparseSelectCubeClause(selectCubeClauseName)).asString().isEqualTo("subCube");
        }

        @Test
        void testQuoted() {
            NameObjectIdentifier cubeName = new NameObjectIdentifierR("subCube", ObjectIdentifier.Quoting.QUOTED);
            SelectCubeClause selectCubeClauseName = new SelectCubeClauseNameR(cubeName);
            assertThat(unparser.unparseSelectCubeClause(selectCubeClauseName)).asString().isEqualTo("[subCube]");
        }
    }

    @Nested
    class SelectCubeClauseStatementTest {

        @Test
        void testSingleCube() {
            String mdx = " ( \r\n  SELECT \r\n{[Date],[Calendar],[Calendar Year],&[2001]} ON COLUMNS FROM \r\n[Adventure Works]\r\n ) \r\n";
            SelectCubeClauseSubStatement selectCubeClauseSubStatement = new SelectCubeClauseSubStatementR(
                    new SelectQueryAxesClauseR(List.of(new SelectQueryAxisClauseR(false, new CallExpressionR(
                            new BracesOperationAtom(),
                            List.of(new NameObjectIdentifierR("Date", ObjectIdentifier.Quoting.QUOTED),
                                    new NameObjectIdentifierR("Calendar", ObjectIdentifier.Quoting.QUOTED),
                                    new NameObjectIdentifierR("Calendar Year", ObjectIdentifier.Quoting.QUOTED),
                                    new KeyObjectIdentifierR(List
                                            .of(new NameObjectIdentifierR("2001", ObjectIdentifier.Quoting.QUOTED))))),
                            new AxisR(0, true), null))),
                    new SelectCubeClauseNameR(
                            new NameObjectIdentifierR("Adventure Works", ObjectIdentifier.Quoting.QUOTED)),
                    Optional.ofNullable(null));

            assertThat(unparser.unparseSelectCubeClause(selectCubeClauseSubStatement)).hasToString(mdx);
        }

        @Test
        void testMultiCube() {
            String mdx = " ( \r\n  SELECT \r\n{[Date],[Calendar],[Calendar Year],&[2001]} ON COLUMNS FROM \r\n ( \r\n  SELECT \r\n{test} ON COLUMNS FROM \r\n[cube]\r\n ) \r\n\r\n ) \r\n";
            SelectCubeClauseSubStatement selectCubeClauseSubStatement = new SelectCubeClauseSubStatementR(
                    new SelectQueryAxesClauseR(List.of(new SelectQueryAxisClauseR(false, new CallExpressionR(
                            new BracesOperationAtom(),
                            List.of(new NameObjectIdentifierR("Date", ObjectIdentifier.Quoting.QUOTED),
                                    new NameObjectIdentifierR("Calendar", ObjectIdentifier.Quoting.QUOTED),
                                    new NameObjectIdentifierR("Calendar Year", ObjectIdentifier.Quoting.QUOTED),
                                    new KeyObjectIdentifierR(List
                                            .of(new NameObjectIdentifierR("2001", ObjectIdentifier.Quoting.QUOTED))))),
                            new AxisR(0, true), null))),

                    new SelectCubeClauseSubStatementR(
                            new SelectQueryAxesClauseR(List.of(new SelectQueryAxisClauseR(false,
                                    new CallExpressionR(new BracesOperationAtom(),
                                            List.of(new NameObjectIdentifierR("test",
                                                    ObjectIdentifier.Quoting.UNQUOTED))),
                                    new AxisR(0, true), null))),
                            new SelectCubeClauseNameR(
                                    new NameObjectIdentifierR("cube", ObjectIdentifier.Quoting.QUOTED)),
                            Optional.ofNullable(null)),
                    Optional.ofNullable(null));

            assertThat(unparser.unparseSelectCubeClause(selectCubeClauseSubStatement)).hasToString(mdx);
        }
    }
}
