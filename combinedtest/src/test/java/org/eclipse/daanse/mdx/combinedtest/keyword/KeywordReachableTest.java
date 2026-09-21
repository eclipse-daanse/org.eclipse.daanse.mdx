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
import java.util.Set;
import java.util.TreeSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

/**
 * A word token that no production accepts turns every identifier of that name
 * into a parse error. Each one a grammar declares must be used.
 */
class KeywordReachableTest {

    private static final Pattern DECLARATION = Pattern.compile("<\\s*(\\w+)\\s*:\\s*\"\\w+\"\\s*>");
    private static final Pattern USE = Pattern.compile("<\\s*(\\w+)\\s*>");

    @ParameterizedTest
    @ValueSource(strings = { "../parser.ccc/src/main/ccc", "../parser.cccx/src/main/ccc" })
    void everyWordTokenIsUsed(String grammarDir) throws IOException {
        String grammar;
        try (Stream<Path> files = Files.list(Path.of(grammarDir))) {
            grammar = files.filter(f -> f.toString().endsWith(".ccc")).sorted().map(KeywordReachableTest::read)
                    .collect(Collectors.joining("\n")).replaceAll("(?m)//.*$", "");
        }

        Set<String> declared = names(DECLARATION, grammar);
        assertThat(declared).isNotEmpty();
        declared.removeAll(names(USE, grammar));
        assertThat(declared).as("word tokens used in no production").isEmpty();
    }

    private static Set<String> names(Pattern pattern, String grammar) {
        Set<String> names = new TreeSet<>();
        for (Matcher m = pattern.matcher(grammar); m.find();) {
            names.add(m.group(1));
        }
        return names;
    }

    private static String read(Path file) {
        try {
            return Files.readString(file);
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
    }
}
