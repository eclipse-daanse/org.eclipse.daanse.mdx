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

import org.eclipse.daanse.mdx.model.api.SelectStatement;
import org.eclipse.daanse.mdx.model.api.expression.CallExpression;
import org.eclipse.daanse.mdx.model.api.expression.CompoundId;
import org.eclipse.daanse.mdx.model.api.expression.MdxExpression;
import org.eclipse.daanse.mdx.model.api.expression.NameObjectIdentifier;
import org.eclipse.daanse.mdx.parser.api.MdxParserException;
import org.eclipse.daanse.mdx.parser.api.MdxParserProvider;
import org.junit.jupiter.api.Test;
import org.osgi.service.component.annotations.RequireServiceComponentRuntime;
import org.osgi.test.common.annotation.InjectService;

/**
 * The inline set alias {@code <set> AS <name>} binds more tightly than the binary set operators {@code *}, {@code +},
 * {@code -} and {@code :}: it names the operand directly to its left, and the expression goes on after the name.
 * That is how SSAS reads it, on both public Adventure Works servers: {@code Max({Categories} * {Years} AS S, S.Count)}
 * is the number of years, not of the cross join; {@code {Y0} + {Y1, Y2} AS S} names two years, {@code {Years} - {Y0} AS S}
 * one; {@code Y0 : Y2 AS S} is refused because the range gets a set; {@code {Categories} AS S * {Years}} and
 * {@code {C} * {Y} AS S * {G}} are whole cross joins with S naming one operand. Mondrian's grammar bound AS weakest
 * (MONDRIAN-648, never fixed). Inside {@code Cast(x AS type)} the AS belongs to the cast.
 */
@RequireServiceComponentRuntime
class AliasPrecedenceTest {

    @Test
    void theAliasNamesTheRightOperandOfACrossJoin(@InjectService MdxParserProvider provider) throws MdxParserException {
        CallExpression crossJoin = call(parse(provider, "[A] * [B] AS S"), "*");
        name(arg(crossJoin, 0), "A");
        alias(arg(crossJoin, 1), "B", "S");
    }

    @Test
    void theAliasNamesTheEndOfARange(@InjectService MdxParserProvider provider) throws MdxParserException {
        CallExpression range = call(parse(provider, "[A] : [B] AS S"), ":");
        name(arg(range, 0), "A");
        alias(arg(range, 1), "B", "S");
    }

    @Test
    void theAliasNamesTheRightOperandOfAUnionAndAnExcept(@InjectService MdxParserProvider provider)
            throws MdxParserException {
        CallExpression union = call(parse(provider, "[A] + [B] AS S"), "+");
        alias(arg(union, 1), "B", "S");
        CallExpression except = call(parse(provider, "[A] - [B] AS S"), "-");
        alias(arg(except, 1), "B", "S");
    }

    @Test
    void theExpressionGoesOnAfterTheAlias(@InjectService MdxParserProvider provider) throws MdxParserException {
        CallExpression crossJoin = call(parse(provider, "[A] AS S * [B]"), "*");
        alias(arg(crossJoin, 0), "A", "S");
        name(arg(crossJoin, 1), "B");

        CallExpression outer = call(parse(provider, "[A] * [B] AS S * [C]"), "*");
        CallExpression inner = call(arg(outer, 0), "*");
        name(arg(inner, 0), "A");
        alias(arg(inner, 1), "B", "S");
        name(arg(outer, 1), "C");
    }

    @Test
    void parenthesesNameTheWholeExpression(@InjectService MdxParserProvider provider) throws MdxParserException {
        CallExpression as = call(parse(provider, "([A] * [B]) AS S"), "AS");
        call(call(arg(as, 0), "()").expressions().get(0), "*");
        name(arg(as, 1), "S");
    }

    @Test
    void anAliasedFunctionArgument(@InjectService MdxParserProvider provider) throws MdxParserException {
        CallExpression filter = call(parse(provider, "Filter([A] AS S, S.Count > 0)"), "Filter");
        alias(arg(filter, 0), "A", "S");
    }

    @Test
    void theAsOfACastBelongsToTheCast(@InjectService MdxParserProvider provider) throws MdxParserException {
        CallExpression cast = call(parse(provider, "Cast(1 + 2 AS String)"), "Cast");
        call(arg(cast, 0), "+");

        CallExpression castOfCount = call(parse(provider, "Cast(Count([A] * [B] AS S) AS String)"), "Cast");
        CallExpression crossJoin = call(arg(call(arg(castOfCount, 0), "Count"), 0), "*");
        alias(arg(crossJoin, 1), "B", "S");
    }

    @Test
    void anAliasInTheMiddleOfAnAxisAndOfAFormula(@InjectService MdxParserProvider provider)
            throws MdxParserException {
        SelectStatement axis = provider.newParser("SELECT {[A]} AS S * {[B]} ON 0 FROM [C]", propertyWords)
                .parseSelectStatement();
        assertThat(axis).isNotNull();
        SelectStatement formula = provider.newParser(
                "WITH MEMBER [Measures].[c] AS Max({[A]} AS S * {[B]}, S.Count) SELECT {[Measures].[c]} ON 0 FROM [C]",
                propertyWords).parseSelectStatement();
        assertThat(formula).isNotNull();
    }

    private static MdxExpression parse(MdxParserProvider provider, String mdx) throws MdxParserException {
        return provider.newParser(mdx, propertyWords).parseExpression();
    }

    private static CallExpression call(MdxExpression expression, String operator) {
        assertThat(expression).isInstanceOf(CallExpression.class);
        CallExpression call = (CallExpression) expression;
        assertThat(call.operationAtom().name()).as("operator").isEqualToIgnoringCase(operator);
        return call;
    }

    private static MdxExpression arg(CallExpression call, int index) {
        assertThat(call.expressions()).as("arguments of " + call.operationAtom().name()).hasSizeGreaterThan(index);
        return call.expressions().get(index);
    }

    private static void name(MdxExpression expression, String name) {
        assertThat(expression).isInstanceOf(CompoundId.class);
        var identifiers = ((CompoundId) expression).objectIdentifiers();
        assertThat(identifiers.getLast()).isInstanceOf(NameObjectIdentifier.class);
        assertThat(((NameObjectIdentifier) identifiers.getLast()).name()).isEqualTo(name);
    }

    private static void alias(MdxExpression expression, String named, String alias) {
        CallExpression as = call(expression, "AS");
        name(arg(as, 0), named);
        name(arg(as, 1), alias);
    }
}
