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
*   Stefan Bischof (bipolis.org) - initial
*/
package org.eclipse.daanse.mdx.parser.tck;

import static org.assertj.core.api.Assertions.assertThat;
import static org.eclipse.daanse.mdx.parser.tck.CubeTest.propertyWords;

import org.eclipse.daanse.mdx.model.api.expression.CallExpression;
import org.eclipse.daanse.mdx.model.api.expression.MdxExpression;
import org.eclipse.daanse.mdx.model.api.expression.operation.FunctionOperationAtom;
import org.eclipse.daanse.mdx.model.api.expression.operation.PostfixOperationAtom;
import org.eclipse.daanse.mdx.model.api.select.SelectCellPropertyListClause;
import org.eclipse.daanse.mdx.parser.api.MdxParserException;
import org.eclipse.daanse.mdx.parser.api.MdxParserProvider;
import org.junit.jupiter.api.Test;
import org.osgi.service.component.annotations.RequireServiceComponentRuntime;
import org.osgi.test.common.annotation.InjectService;

/**
 * Forms of MDX that neither or only one of the two parsers reads as written, found while testing the olap
 * engine on 06.10.2026 against c310e37. Each test states the shape the tree should have. Today ccc fails all four;
 * cccx reads IS EMPTY right and fails the other three. The two Axis tests describe a feature (an SSAS function),
 * not a defect against Mondrian.
 */
@RequireServiceComponentRuntime
class OpenParserGapsTest {

    private static MdxExpression expression(MdxParserProvider parsers, String mdx) throws MdxParserException {
        return parsers.newParser(mdx, propertyWords).parseExpression();
    }

    /**
     * Feature request: Axis(n) as an SSAS function returning the set on that axis; both parsers stop at the token
     * AXIS. Mondrian reads AXIS(n) only as an axis name (ON AXIS(0)), which both parsers already accept.
     */
    @Test
    void axisIsAFunctionOfTheAxisNumber(@InjectService MdxParserProvider parsers) throws MdxParserException {
        MdxExpression axis = expression(parsers, "Axis(1)");
        assertThat(axis).isInstanceOf(CallExpression.class);
        CallExpression call = (CallExpression) axis;
        assertThat(call.operationAtom()).isInstanceOf(FunctionOperationAtom.class);
        assertThat(call.operationAtom().name()).isEqualToIgnoringCase("Axis");
        assertThat(call.expressions()).hasSize(1);
    }

    /** Feature request: Axis(n) also stands inside another call and as the set of another axis. */
    @Test
    void axisStandsInsideACallAndOnAnAxis(@InjectService MdxParserProvider parsers) throws MdxParserException {
        assertThat(expression(parsers, "Count(Axis(0))")).isInstanceOf(CallExpression.class);
        assertThat(parsers.newParser("SELECT {[Measures].[A]} ON 0, Axis(0) ON 1 FROM [C]", propertyWords)
                .parseMdxStatement()).isNotNull();
    }

    /**
     * IS EMPTY is its own operator (the cell has no value), not IS NULL (the member is the null member).
     * cccx keeps it; ccc turns it into IS NULL, so the two parsers give different trees for the same text.
     */
    @Test
    void isEmptyStaysIsEmpty(@InjectService MdxParserProvider parsers) throws MdxParserException {
        for (String mdx : new String[] { "[Measures].[A] IS EMPTY", "([Measures].[A], [P].[P].[p]) IS EMPTY" }) {
            CallExpression call = (CallExpression) expression(parsers, mdx);
            assertThat(call.operationAtom()).as(mdx).isInstanceOf(PostfixOperationAtom.class);
            assertThat(call.operationAtom().name()).as(mdx).isEqualTo("IS EMPTY");
        }
        CallExpression isNull = (CallExpression) expression(parsers, "[P].[P].[p] IS NULL");
        assertThat(isNull.operationAtom().name()).isEqualTo("IS NULL");
    }

    /** A cell property may be written as a bracketed name, as every other name in MDX may. */
    @Test
    void aCellPropertyMayBeBracketed(@InjectService MdxParserProvider parsers) throws MdxParserException {
        SelectCellPropertyListClause clause = parsers
                .newParser("CELL PROPERTIES [FORMATTED_VALUE], VALUE", propertyWords).parseSelectCellPropertyListClause();
        assertThat(clause.properties()).containsExactly("FORMATTED_VALUE", "VALUE");
    }
}
