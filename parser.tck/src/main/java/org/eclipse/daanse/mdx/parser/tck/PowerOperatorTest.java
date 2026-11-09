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
package org.eclipse.daanse.mdx.parser.tck;

import static org.assertj.core.api.Assertions.assertThat;
import static org.eclipse.daanse.mdx.parser.tck.CubeTest.propertyWords;

import java.math.BigDecimal;

import org.eclipse.daanse.mdx.model.api.MdxStatement;
import org.eclipse.daanse.mdx.model.api.SelectStatement;
import org.eclipse.daanse.mdx.model.api.expression.CallExpression;
import org.eclipse.daanse.mdx.model.api.expression.CompoundId;
import org.eclipse.daanse.mdx.model.api.expression.MdxExpression;
import org.eclipse.daanse.mdx.model.api.expression.NameObjectIdentifier;
import org.eclipse.daanse.mdx.model.api.expression.NullLiteral;
import org.eclipse.daanse.mdx.model.api.expression.NumericLiteral;
import org.eclipse.daanse.mdx.model.api.select.CreateMemberBodyClause;
import org.eclipse.daanse.mdx.model.api.select.SelectSlicerAxisClause;
import org.eclipse.daanse.mdx.parser.api.MdxParserException;
import org.eclipse.daanse.mdx.parser.api.MdxParserProvider;
import org.junit.jupiter.api.Test;
import org.osgi.service.component.annotations.RequireServiceComponentRuntime;
import org.osgi.test.common.annotation.InjectService;

/**
 * Analysis Services reads {@code ^} as the power operator: {@code 2 ^ 3} is 8. The parsed form is an infix call of the
 * operator {@code ^}, as {@code *} is.
 */
@RequireServiceComponentRuntime
class PowerOperatorTest {

    @Test
    void aPowerInACalculatedMember(@InjectService MdxParserProvider mdxParserProvider) throws MdxParserException {
        SelectStatement select = select(mdxParserProvider,
                "WITH MEMBER [Measures].[A] AS 2 ^ 3 SELECT {[Measures].[A]} ON 0 FROM [Sales]");
        CreateMemberBodyClause member = (CreateMemberBodyClause) select.selectWithClauses().get(0);

        CallExpression power = call(member.expression(), "^");
        number(arg(power, 0), 2);
        number(arg(power, 1), 3);
    }

    @Test
    void aPowerInTheSlicer(@InjectService MdxParserProvider mdxParserProvider) throws MdxParserException {
        SelectStatement select = select(mdxParserProvider,
                "SELECT {[Measures].[A]} ON 0 FROM [Sales] WHERE ([Measures].[Sales] / 1000) ^ 2");
        assertThat(select.selectSlicerAxisClause()).isPresent();
        SelectSlicerAxisClause slicer = select.selectSlicerAxisClause().get();

        CallExpression power = call(slicer.expression(), "^");
        CallExpression parentheses = call(arg(power, 0), "()");
        CallExpression division = call(arg(parentheses, 0), "/");
        name(arg(division, 0), "Sales");
        number(arg(division, 1), 1000);
        number(arg(power, 1), 2);
    }

    @Test
    void thePowerGroupsFromRightToLeft(@InjectService MdxParserProvider provider) throws MdxParserException {
        CallExpression outer = call(parse(provider, "2 ^ 3 ^ 2"), "^");
        number(arg(outer, 0), 2);
        CallExpression inner = call(arg(outer, 1), "^");
        number(arg(inner, 0), 3);
        number(arg(inner, 1), 2);
    }

    @Test
    void thePowerBindsMoreTightlyThanMultiplicationAndDivision(@InjectService MdxParserProvider provider)
            throws MdxParserException {
        CallExpression times = call(parse(provider, "2 ^ 2 * 3"), "*");
        power(arg(times, 0), 2, 2);
        number(arg(times, 1), 3);

        CallExpression timesRight = call(parse(provider, "2 * 3 ^ 2"), "*");
        number(arg(timesRight, 0), 2);
        power(arg(timesRight, 1), 3, 2);

        CallExpression division = call(parse(provider, "12 / 2 ^ 2"), "/");
        number(arg(division, 0), 12);
        power(arg(division, 1), 2, 2);
    }

    @Test
    void theBaseMayBeNull(@InjectService MdxParserProvider provider) throws MdxParserException {
        CallExpression power = call(parse(provider, "NULL ^ 2"), "^");
        assertThat(arg(power, 0)).isInstanceOf(NullLiteral.class);
        number(arg(power, 1), 2);
    }

    @Test
    void theLeadingSignAppliesToThePower(@InjectService MdxParserProvider provider) throws MdxParserException {
        CallExpression minus = call(parse(provider, "-2 ^ 2"), "-");
        assertThat(minus.expressions()).hasSize(1);
        CallExpression power = call(arg(minus, 0), "^");
        number(arg(power, 0), 2);
        number(arg(power, 1), 2);
    }

    @Test
    void theExponentMayCarryASign(@InjectService MdxParserProvider provider) throws MdxParserException {
        CallExpression power = call(parse(provider, "2 ^ -1"), "^");
        number(arg(power, 0), 2);
        CallExpression minus = call(arg(power, 1), "-");
        assertThat(minus.expressions()).hasSize(1);
        number(arg(minus, 0), 1);

        CallExpression root = call(parse(provider, "2 ^ 0.5"), "^");
        assertThat(arg(root, 1)).isInstanceOf(NumericLiteral.class);
        assertThat(((NumericLiteral) arg(root, 1)).value()).isEqualByComparingTo(new BigDecimal("0.5"));
    }

    private static MdxExpression parse(MdxParserProvider provider, String mdx) throws MdxParserException {
        return provider.newParser(mdx, propertyWords).parseExpression();
    }

    private static SelectStatement select(MdxParserProvider provider, String mdx) throws MdxParserException {
        MdxStatement statement = provider.newParser(mdx, propertyWords).parseMdxStatement();
        assertThat(statement).isInstanceOf(SelectStatement.class);
        return (SelectStatement) statement;
    }

    private static CallExpression call(MdxExpression expression, String operator) {
        assertThat(expression).isInstanceOf(CallExpression.class);
        CallExpression call = (CallExpression) expression;
        assertThat(call.operationAtom().name()).as("operator").isEqualTo(operator);
        return call;
    }

    private static MdxExpression arg(CallExpression call, int index) {
        assertThat(call.expressions()).as("arguments of " + call.operationAtom().name()).hasSizeGreaterThan(index);
        return call.expressions().get(index);
    }

    private static void power(MdxExpression expression, int base, int exponent) {
        CallExpression power = call(expression, "^");
        number(arg(power, 0), base);
        number(arg(power, 1), exponent);
    }

    private static void number(MdxExpression expression, int value) {
        assertThat(expression).isInstanceOf(NumericLiteral.class);
        assertThat(((NumericLiteral) expression).value()).isEqualByComparingTo(BigDecimal.valueOf(value));
    }

    private static void name(MdxExpression expression, String name) {
        assertThat(expression).isInstanceOf(CompoundId.class);
        var identifiers = ((CompoundId) expression).objectIdentifiers();
        assertThat(identifiers.getLast()).isInstanceOf(NameObjectIdentifier.class);
        assertThat(((NameObjectIdentifier) identifiers.getLast()).name()).isEqualTo(name);
    }
}
