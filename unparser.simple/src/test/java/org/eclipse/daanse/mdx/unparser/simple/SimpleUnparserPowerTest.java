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
*   dbulahov - initial
*/
package org.eclipse.daanse.mdx.unparser.simple;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import java.util.List;

import org.eclipse.daanse.mdx.model.api.expression.MdxExpression;
import org.eclipse.daanse.mdx.model.api.expression.ObjectIdentifier.Quoting;
import org.eclipse.daanse.mdx.model.api.expression.operation.InfixOperationAtom;
import org.eclipse.daanse.mdx.model.api.expression.operation.ParenthesesOperationAtom;
import org.eclipse.daanse.mdx.model.api.expression.operation.PrefixOperationAtom;
import org.eclipse.daanse.mdx.model.record.expression.CallExpressionR;
import org.eclipse.daanse.mdx.model.record.expression.CompoundIdR;
import org.eclipse.daanse.mdx.model.record.expression.NameObjectIdentifierR;
import org.eclipse.daanse.mdx.model.record.expression.NullLiteralR;
import org.eclipse.daanse.mdx.model.record.expression.NumericLiteralR;
import org.junit.jupiter.api.Test;

/**
 * The power operator {@code ^} is written back as an infix operator. The models are the ones the parsers build for the
 * SSAS cases: {@code ^} groups from right to left and binds more tightly than {@code * /} and a leading sign, so each
 * text reads back as the same model; a grouping against that order is held by an explicit parentheses node.
 */
class SimpleUnparserPowerTest {

    private final SimpleUnparser unparser = new SimpleUnparser();

    @Test
    void aPower() {
        assertUnparsed(power(n(2), n(3)), "2 ^ 3");
    }

    @Test
    void aPowerOfAPowerGroupsFromRightToLeft() {
        assertUnparsed(power(n(2), power(n(3), n(2))), "2 ^ 3 ^ 2");
    }

    @Test
    void aPowerOfAPowerFromLeftToRightKeepsItsParentheses() {
        assertUnparsed(power(parentheses(power(n(2), n(3))), n(2)), "(2 ^ 3) ^ 2");
    }

    @Test
    void aPowerTimesANumber() {
        assertUnparsed(infix("*", power(n(2), n(2)), n(3)), "2 ^ 2 * 3");
    }

    @Test
    void aNumberTimesAPower() {
        assertUnparsed(infix("*", n(2), power(n(3), n(2))), "2 * 3 ^ 2");
    }

    @Test
    void aNumberDividedByAPower() {
        assertUnparsed(infix("/", n(12), power(n(2), n(2))), "12 / 2 ^ 2");
    }

    @Test
    void theLeadingSignOfAPower() {
        assertUnparsed(minus(power(n(2), n(2))), "- 2 ^ 2");
    }

    @Test
    void aSignedExponent() {
        assertUnparsed(power(n(2), minus(n(1))), "2 ^ - 1");
    }

    @Test
    void aFractionalExponent() {
        assertUnparsed(power(n(2), new NumericLiteralR(new BigDecimal("0.5"))), "2 ^ 0.5");
    }

    @Test
    void aPowerOfNull() {
        assertUnparsed(power(new NullLiteralR(), n(2)), "NULL ^ 2");
    }

    @Test
    void aPowerOfAQuotientInParentheses() {
        MdxExpression sales = new CompoundIdR(List.of(new NameObjectIdentifierR("Measures", Quoting.QUOTED),
                new NameObjectIdentifierR("Sales", Quoting.QUOTED)));
        assertUnparsed(power(parentheses(infix("/", sales, n(1000))), n(2)), "([Measures].[Sales] / 1000) ^ 2");
    }

    private void assertUnparsed(MdxExpression expression, String mdx) {
        assertThat(unparser.unparseExpression(expression)).asString().isEqualTo(mdx);
    }

    private static MdxExpression power(MdxExpression base, MdxExpression exponent) {
        return infix("^", base, exponent);
    }

    private static MdxExpression infix(String operator, MdxExpression left, MdxExpression right) {
        return new CallExpressionR(new InfixOperationAtom(operator), List.of(left, right));
    }

    private static MdxExpression minus(MdxExpression operand) {
        return new CallExpressionR(new PrefixOperationAtom("-"), List.of(operand));
    }

    private static MdxExpression parentheses(MdxExpression expression) {
        return new CallExpressionR(new ParenthesesOperationAtom(), List.of(expression));
    }

    private static MdxExpression n(int value) {
        return new NumericLiteralR(BigDecimal.valueOf(value));
    }
}
