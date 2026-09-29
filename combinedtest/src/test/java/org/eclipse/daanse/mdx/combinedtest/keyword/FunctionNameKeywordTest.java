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
package org.eclipse.daanse.mdx.combinedtest.keyword;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Stream;

import org.eclipse.daanse.mdx.parser.api.MdxParserProvider;
import org.eclipse.daanse.mdx.parser.ccc.CCCMdxParserProvider;
import org.eclipse.daanse.mdx.parser.cccx.CCCXMdxParserProvider;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

/** Function names and sort orders are identifiers, not keywords, in both parsers. */
class FunctionNameKeywordTest {

    private static final List<MdxParserProvider> PROVIDERS = List.of(new CCCMdxParserProvider(),
            new CCCXMdxParserProvider());

    private static final List<String> STATEMENTS = List.of(
            "SELECT {Ancestor([Geo].[DE].[North], 1)} ON 0 FROM [Sales]",
            "SELECT {Ancestors([Geo].[DE].[North], 1)} ON 0 FROM [Sales]",
            "SELECT {AllMembers([Geo])} ON 0 FROM [Sales]",
            "SELECT {[Geo].AllMembers} ON 0 FROM [Sales]",
            "SELECT {[Geo].[DE].Ancestor(1)} ON 0 FROM [Sales]",
            "SELECT Order([Geo].Members, [Measures].[Amount], ASC) ON 0 FROM [Sales]",
            "SELECT Order([Geo].Members, [Measures].[Amount], DESC) ON 0 FROM [Sales]",
            "SELECT Order([Geo].Members, [Measures].[Amount], BASC) ON 0 FROM [Sales]",
            "SELECT Order([Geo].Members, [Measures].[Amount], BDESC) ON 0 FROM [Sales]",
            "SELECT {All([Geo])} ON 0 FROM [Sales]",
            "SELECT {CurrentCube([Geo])} ON 0 FROM [Sales]",
            "WITH MEMBER [Measures].[X] AS 'Ancestor([Geo].[DE].[North], 1).Name' "
                    + "SELECT {[Measures].[X]} ON 0 FROM [Sales]");

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
    void parses(String name, MdxParserProvider provider, String statement) throws Exception {
        assertThat(provider.newParser(statement, Set.of()).parseMdxStatement()).isNotNull();
    }
}
