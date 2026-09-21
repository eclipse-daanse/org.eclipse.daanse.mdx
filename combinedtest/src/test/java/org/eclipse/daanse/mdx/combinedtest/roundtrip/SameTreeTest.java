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

import java.util.List;
import java.util.stream.Stream;

import org.eclipse.daanse.mdx.model.api.MdxStatement;
import org.eclipse.daanse.mdx.parser.api.MdxParserException;
import org.eclipse.daanse.mdx.parser.ccc.CCCMdxParserProvider;
import org.eclipse.daanse.mdx.parser.cccx.CCCXMdxParserProvider;
import org.eclipse.daanse.mdx.unparser.simple.SimpleUnparser;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

/** Both parsers give the same tree. Where they differ, one of them is wrong. */
class SameTreeTest {

    private static final List<String> METHOD_CALLS = List.of(
            "SELECT {[Geo].[DE].Lag(1)} ON 0 FROM [Sales]",
            "SELECT {[Geo].[DE].Lead(1)} ON 0 FROM [Sales]",
            "SELECT {[Geo].Lag(1)} ON 0 FROM [Sales]",
            "SELECT {[Geo].[DE].&[1].Lag(1)} ON 0 FROM [Sales]",
            "SELECT {[Time].[Time].Members.Item(0)} ON 0 FROM [Sales]",
            "SELECT {[Time].[Time].Members.Item(0).Item(1, 2)} ON 0 FROM [Sales]",
            "SELECT {[Geo].CurrentMember.Lag(1)} ON 0 FROM [Sales]",
            "SELECT {[Geo].Levels(1).Members} ON 0 FROM [Sales]",
            "SELECT {[Geo].[DE].Children()} ON 0 FROM [Sales]",
            "SELECT {[Time].Members.Item([a].[b], [c])} ON 0 FROM [Sales]",
            "SELECT {Foo([a].[b]).Item([c].[d])} ON 0 FROM [Sales]",
            "WITH MEMBER [Measures].[X] AS '[Store].CurrentMember.CalculatedChild(\"A\").Name' "
                    + "SELECT {[Measures].[X]} ON 0 FROM [Sales]");

    // DIMENSION and PROPERTIES are keywords and names
    private static final List<String> KEYWORD_NAMES = List.of(
            "SELECT {[Geo].[All Geo].[North].Dimension} ON 0 FROM [Sales]",
            "SELECT {[Geo].Dimension.Members} ON 0 FROM [Sales]",
            "SELECT {[Geo].CurrentMember.Properties(\"Caption\")} ON 0 FROM [Sales]",
            "SELECT {Foo([Geo]).Dimension, Foo([Geo]).Properties(\"Caption\")} ON 0 FROM [Sales]",
            "SELECT [Geo].Dimension.Members DIMENSION PROPERTIES [Geo].[Name] ON 0 FROM [Sales]",
            "SELECT [Geo].Dimension PROPERTIES [Geo].Dimension, Properties ON 0 FROM [Sales] CELL PROPERTIES VALUE",
            "SELECT {[Geo].[All Geo].[North].Level, [Geo].[All Geo].[North].Hierarchy} ON 0 FROM [Sales]");

    static Stream<String> statements() {
        return Stream.of(RoundTripTest.STATEMENTS, METHOD_CALLS, KEYWORD_NAMES).flatMap(List::stream);
    }

    @ParameterizedTest
    @MethodSource("statements")
    void sameTree(String mdx) throws MdxParserException {
        MdxStatement ccc = new CCCMdxParserProvider().newParser(mdx, RoundTripTest.PROPERTY_WORDS)
                .parseMdxStatement();
        MdxStatement cccx = new CCCXMdxParserProvider().newParser(mdx, RoundTripTest.PROPERTY_WORDS)
                .parseMdxStatement();

        assertThat(AstDump.dump(cccx)).isEqualTo(AstDump.dump(ccc));
        assertThat(new SimpleUnparser().unparseMdxStatement(cccx).toString())
                .isEqualTo(new SimpleUnparser().unparseMdxStatement(ccc).toString());
    }
}
