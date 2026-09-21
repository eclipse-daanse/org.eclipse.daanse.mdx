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
package org.eclipse.daanse.mdx.combinedtest.roundtrip;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Stream;

import org.eclipse.daanse.mdx.combinedtest.integration.UnparseParsedTest;
import org.eclipse.daanse.mdx.model.api.MdxStatement;
import org.eclipse.daanse.mdx.model.api.SelectStatement;
import org.eclipse.daanse.mdx.model.api.expression.CallExpression;
import org.eclipse.daanse.mdx.model.api.expression.MdxExpression;
import org.eclipse.daanse.mdx.model.api.expression.StringLiteral;
import org.eclipse.daanse.mdx.model.api.select.CreateMemberBodyClause;
import org.eclipse.daanse.mdx.parser.api.MdxParserException;
import org.eclipse.daanse.mdx.parser.api.MdxParserProvider;
import org.eclipse.daanse.mdx.parser.ccc.CCCMdxParserProvider;
import org.eclipse.daanse.mdx.parser.cccx.CCCXMdxParserProvider;
import org.eclipse.daanse.mdx.unparser.simple.SimpleUnparser;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

/**
 * parse → unparse → parse must give the same tree. Comparing the text of two
 * unparse runs is not enough: a value that leaks out of its delimiters gives a
 * stable text and a different tree.
 */
class RoundTripTest {

    static final Set<String> PROPERTY_WORDS = UnparseParsedTest.reservedWords;

    private static final List<MdxParserProvider> PROVIDERS = List.of(new CCCMdxParserProvider(),
            new CCCXMdxParserProvider());

    static final List<String> STATEMENTS = List.of(
            UnparseParsedTest.MDX,
            "SELECT FROM [Sales]",
            "SELECT * FROM [Sales]",
            "SELECT [Measures].[Unit Sales] ON COLUMNS, NON EMPTY [Store].Members ON ROWS FROM [Sales]",
            "SELECT [a] ON 0, [b] ON 1, [c] ON PAGES FROM [Sales]",
            "SELECT [Store].Members DIMENSION PROPERTIES [Store].[Name], MEMBER_CAPTION ON 0 FROM [Sales]",
            "SELECT [a] ON 0 FROM (SELECT [b] ON 0 FROM [Sales] WHERE [Time].[1997]) WHERE ([c], [d])",
            "SELECT [a] ON 0 FROM [Sales] CELL PROPERTIES VALUE, FORMATTED_VALUE",
            "SELECT [a] ON 0 FROM [Cube ]] with [ brackets]",
            // string literals
            "WITH MEMBER [Measures].[X] AS 1, FORMAT_STRING = \"x, SOLVE_ORDER = 999\" SELECT [Measures].[X] ON 0 FROM [Public]",
            "WITH MEMBER [Measures].[X] AS 1, FORMAT_STRING = \"x SET Evil AS [Secret].Members\" SELECT [Measures].[X] ON 0 FROM [Public]",
            "WITH MEMBER [Measures].[X] AS 1, FORMAT_STRING = \"[Secret].Members\" SELECT [Measures].[X] ON 0 FROM [Public]",
            "WITH MEMBER [Measures].[X] AS 1, FORMAT_STRING = '#,#\\#0.00' SELECT [Measures].[X] ON 0 FROM [Public]",
            "WITH MEMBER [Measures].[X] AS \"a \"\"quoted\"\" 'text'\" SELECT [Measures].[X] ON 0 FROM [Public]",
            "WITH MEMBER [Measures].[X] AS \"--\" || 'It''s' || \"/* no comment */\" SELECT [Measures].[X] ON 0 FROM [Public]",
            "WITH SET [S] AS StrToSet(\"\"\"{[Secret].Members}\"\"\") SELECT [S] ON 0 FROM [Public]",
            "SELECT Filter([Store].Members, [Store].CurrentMember.Name = \"a\nb\") ON 0 FROM [Sales]",
            // legacy quoted formulas
            "WITH MEMBER [Measures].[X] AS '[Measures].[A] + 1' SET [S] AS '{[a], [b]}' SELECT [S] ON 0 FROM [Sales]",
            // identifiers, keys
            "SELECT [Product].[Category].&[1]&[a]]b]&c ON 0, [Time].&Q1 ON 1 FROM [Sales]",
            "SELECT [Select].[From] ON 0 FROM [Where]",
            "SELECT Foo(@param) ON 0 FROM [Sales]",
            // calls
            "SELECT {CAST(IIF(1 = 1, \"a,b\", 'c') AS STRING)} ON 0 FROM [Sales]",
            "SELECT {CAST(1 AS [INT]]EGER])} ON 0 FROM [Sales]",
            "SELECT {CASE [x] WHEN 1 THEN \"one\" WHEN 2 THEN \"two\" ELSE \"many\" END} ON 0 FROM [Sales]",
            "SELECT {(1 + 2) * 3 - -4, NOT ([a] IS NULL), [a] : [b]} ON 0 FROM [Sales]",
            "SELECT Crossjoin([a].Children, [b].Members).Item(0) ON 0 FROM [Sales]",
            "SELECT Foo().[Quoted ]] Property] ON 0, Foo().&[Key ]] Property] ON 1 FROM [Sales]",
            // other statements
            "DRILLTHROUGH MAXROWS 10 FIRSTROWSET 2 SELECT [a] ON 0 FROM [Sales] RETURN [Store].[Name], [Measures].[Unit Sales]",
            "EXPLAIN PLAN FOR SELECT [a] ON 0 FROM [Sales]",
            "REFRESH CUBE [Sa]]les]",
            "UPDATE CUBE [Sales] SET ([a], [b]) = 1 USE_EQUAL_ALLOCATION, [c] = \"x, [d] = 2\" USE_WEIGHTED_ALLOCATION BY [w]",
            "BEGIN TRANSACTION", "COMMIT TRANSACTION", "ROLLBACK TRANSACTION");

    static Stream<Arguments> cases() {
        List<Arguments> arguments = new ArrayList<>();
        for (MdxParserProvider provider : PROVIDERS) {
            for (String statement : STATEMENTS) {
                arguments.add(Arguments.of(provider.getClass().getSimpleName(), provider, statement));
            }
        }
        return arguments.stream();
    }

    @ParameterizedTest(name = "{0}: {2}")
    @MethodSource("cases")
    void sameTreeAfterRoundTrip(String name, MdxParserProvider provider, String mdx) throws MdxParserException {
        MdxStatement first = provider.newParser(mdx, PROPERTY_WORDS).parseMdxStatement();
        String text = new SimpleUnparser().unparseMdxStatement(first).toString();

        MdxStatement second;
        try {
            second = provider.newParser(text, PROPERTY_WORDS).parseMdxStatement();
        } catch (MdxParserException e) {
            throw new AssertionError("unparsed text does not parse:\n" + text, e);
        }
        assertThat(AstDump.dump(second)).as("unparsed text:\n%s", text).isEqualTo(AstDump.dump(first));
    }

    @Test
    void recordsAreEqualAfterRoundTrip() throws MdxParserException {
        MdxParserProvider provider = new CCCMdxParserProvider();
        for (String mdx : STATEMENTS) {
            MdxStatement first = provider.newParser(mdx, PROPERTY_WORDS).parseMdxStatement();
            String text = new SimpleUnparser().unparseMdxStatement(first).toString();
            assertThat(provider.newParser(text, PROPERTY_WORDS).parseMdxStatement()).as(text).isEqualTo(first);
        }
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("providers")
    void stringLiteralStaysOneProperty(String name, MdxParserProvider provider) throws MdxParserException {
        String mdx = "WITH MEMBER [Measures].[X] AS 1, FORMAT_STRING = \"x, SOLVE_ORDER = 999\" "
                + "SELECT [Measures].[X] ON 0 FROM [Public]";

        SelectStatement first = (SelectStatement) provider.newParser(mdx, PROPERTY_WORDS).parseMdxStatement();
        String text = new SimpleUnparser().unparseMdxStatement(first).toString();
        SelectStatement second = (SelectStatement) provider.newParser(text, PROPERTY_WORDS).parseMdxStatement();

        assertThat(text).contains("\"x, SOLVE_ORDER = 999\"");
        assertThat(second.selectWithClauses()).hasSize(1);
        CreateMemberBodyClause member = (CreateMemberBodyClause) second.selectWithClauses().get(0);
        assertThat(member.memberPropertyDefinitions()).hasSize(1);
        assertThat(member.memberPropertyDefinitions().get(0).expression()).isInstanceOf(StringLiteral.class);
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("providers")
    void stringLiteralIsNeverParsedAsExpression(String name, MdxParserProvider provider) throws MdxParserException {
        String mdx = "WITH MEMBER [Measures].[X] AS 1, FORMAT_STRING = \"[Secret].Members\" "
                + "SELECT [Measures].[X] ON 0 FROM [Public]";

        SelectStatement statement = (SelectStatement) provider.newParser(mdx, PROPERTY_WORDS).parseMdxStatement();
        CreateMemberBodyClause member = (CreateMemberBodyClause) statement.selectWithClauses().get(0);
        MdxExpression value = member.memberPropertyDefinitions().get(0).expression();

        assertThat(value).isInstanceOf(StringLiteral.class);
        assertThat(((StringLiteral) value).value()).isEqualTo("[Secret].Members");
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("providers")
    void doubleQuotedFormulaStaysString(String name, MdxParserProvider provider) throws MdxParserException {
        String mdx = "WITH SET [S] AS StrToSet(\"[Secret].Members\") SELECT [S] ON 0 FROM [Public]";

        SelectStatement first = (SelectStatement) provider.newParser(mdx, PROPERTY_WORDS).parseMdxStatement();
        String text = new SimpleUnparser().unparseMdxStatement(first).toString();
        SelectStatement second = (SelectStatement) provider.newParser(text, PROPERTY_WORDS).parseMdxStatement();

        CallExpression call = (CallExpression) ((org.eclipse.daanse.mdx.model.api.select.CreateSetBodyClause) second
                .selectWithClauses().get(0)).expression();
        assertThat(call.expressions()).hasSize(1);
        assertThat(call.expressions().get(0)).isInstanceOf(StringLiteral.class);
    }

    static Stream<Arguments> providers() {
        return PROVIDERS.stream().map(p -> Arguments.of(p.getClass().getSimpleName(), p));
    }
}
