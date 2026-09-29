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

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import org.eclipse.daanse.mdx.parser.api.MdxParserException;
import org.eclipse.daanse.mdx.parser.api.MdxParserProvider;
import org.eclipse.daanse.mdx.parser.ccc.CCCMdxParserProvider;
import org.eclipse.daanse.mdx.parser.cccx.CCCXMdxParserProvider;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

/** Both parsers accept the same keywords as a name. */
class KeywordAsNameTest {

    private static final Set<String> NAMES = Set.of("DIMENSION", "PROPERTIES");

    private static final List<String> FORMS = List.of(
            "SELECT {[Geo].[DE].%s} ON 0 FROM [Sales]",
            "SELECT {[Geo].%s.Members} ON 0 FROM [Sales]",
            "SELECT {[Geo].[DE].%s(1)} ON 0 FROM [Sales]",
            "SELECT {Foo(1).%s} ON 0 FROM [Sales]");

    static Stream<String> keywords() throws IOException {
        Set<String> words = new TreeSet<>();
        for (String grammar : List.of("../parser.ccc/src/main/ccc/Grammer.ccc",
                "../parser.cccx/src/main/ccc/Lexer.inc.ccc")) {
            Matcher m = Pattern.compile("<\\s*\\w+\\s*:\\s*\"(\\w+)\"\\s*>").matcher(Files.readString(Path.of(grammar)));
            while (m.find()) {
                words.add(m.group(1));
            }
        }
        assertThat(words).containsAll(NAMES);
        return words.stream();
    }

    @ParameterizedTest
    @MethodSource("keywords")
    void keywordAsName(String keyword) {
        for (String form : FORMS) {
            String mdx = form.formatted(keyword);
            assertThat(parses(new CCCMdxParserProvider(), mdx)).as("ccc: %s", mdx).isEqualTo(NAMES.contains(keyword));
            assertThat(parses(new CCCXMdxParserProvider(), mdx)).as("cccx: %s", mdx).isEqualTo(NAMES.contains(keyword));
        }
    }

    private static boolean parses(MdxParserProvider provider, String mdx) {
        try {
            return provider.newParser(mdx, Set.of()).parseMdxStatement() != null;
        } catch (MdxParserException e) {
            return false;
        }
    }
}
