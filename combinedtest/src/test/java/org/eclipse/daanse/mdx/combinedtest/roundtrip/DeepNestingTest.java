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

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Stream;

import org.eclipse.daanse.mdx.model.api.MdxStatement;
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
 * The parsers are recursive. However deep the input is nested, a parse ends
 * with a statement or with a MdxParserException, never with an Error, whatever
 * the size of the stack of the calling thread.
 */
class DeepNestingTest {

    private static final List<MdxParserProvider> PROVIDERS = List.of(new CCCMdxParserProvider(),
            new CCCXMdxParserProvider());

    private static final List<String> KINDS = List.of("not", "paren", "brace", "function", "case", "subselect",
            "formula");

    private static final long[] STACKS = { 256 * 1024L, 1024 * 1024L, 16 * 1024 * 1024L };

    private static String statement(String kind, int depth) {
        String select = "SELECT %s ON 0 FROM [Sales]";
        return switch (kind) {
        case "not" -> select.formatted("NOT ".repeat(depth) + "1");
        case "paren" -> select.formatted("(".repeat(depth) + "1" + ")".repeat(depth));
        case "brace" -> select.formatted("{".repeat(depth) + "1" + "}".repeat(depth));
        case "function" -> select.formatted("f(".repeat(depth) + "1" + ")".repeat(depth));
        case "case" -> select.formatted("CASE WHEN 1 THEN ".repeat(depth) + "1" + " END".repeat(depth));
        case "subselect" -> "SELECT [a] ON 0 FROM " + "(SELECT [a] ON 0 FROM ".repeat(depth) + "[Sales]"
                + ")".repeat(depth);
        case "formula" -> "WITH MEMBER [Measures].[X] AS '" + "(".repeat(depth) + "1" + ")".repeat(depth)
                + "' SELECT [Measures].[X] ON 0 FROM [Sales]";
        default -> throw new IllegalArgumentException(kind);
        };
    }

    static Stream<Arguments> cases() {
        List<Arguments> arguments = new ArrayList<>();
        for (MdxParserProvider provider : PROVIDERS) {
            for (String kind : KINDS) {
                arguments.add(Arguments.of(provider.getClass().getSimpleName(), provider, kind));
            }
        }
        return arguments.stream();
    }

    @ParameterizedTest(name = "{0}: {2}")
    @MethodSource("cases")
    void hostileDepthIsAParserException(String name, MdxParserProvider provider, String kind) throws Exception {
        String mdx = statement(kind, 100_000);
        for (long stack : STACKS) {
            Throwable thrown = parse(provider, mdx, stack);
            assertThat(thrown).as("stack %d", stack).isInstanceOf(MdxParserException.class);
        }
    }

    @ParameterizedTest(name = "{0}: {2}")
    @MethodSource("cases")
    void usualDepthParses(String name, MdxParserProvider provider, String kind) throws Exception {
        assertThat(parse(provider, statement(kind, 25), 1024 * 1024L)).isNull();
    }

    @Test
    void theLimitCanBeSet() throws Exception {
        String mdx = statement("function", 25);
        for (MdxParserProvider strict : List.of(new CCCMdxParserProvider(50), new CCCXMdxParserProvider(50))) {
            assertThat(parse(strict, mdx, 1024 * 1024L)).isInstanceOf(MdxParserException.class)
                    .hasMessageContaining("nested too deep").hasMessageContaining("50");
        }

        // a higher limit takes what the default refuses, the caller has to bring the stack
        String deep = statement("function", 300);
        for (MdxParserProvider wide : List.of(new CCCMdxParserProvider(100_000), new CCCXMdxParserProvider(100_000))) {
            assertThat(parse(wide, deep, 64 * 1024 * 1024L)).isNull();
        }
        for (MdxParserProvider provider : PROVIDERS) {
            assertThat(parse(provider, deep, 64 * 1024 * 1024L)).isInstanceOf(MdxParserException.class)
                    .hasMessageContaining("nested too deep");
        }

        // below 1 is the default
        assertThat(parse(new CCCMdxParserProvider(0), mdx, 1024 * 1024L)).isNull();
        assertThat(parse(new CCCXMdxParserProvider(-1), mdx, 1024 * 1024L)).isNull();
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("providers")
    void aLongChainIsNoNesting(String name, MdxParserProvider provider) throws Exception {
        String mdx = "SELECT 1" + " + 1".repeat(100_000) + " ON 0 FROM [Sales]";
        MdxStatement statement = provider.newParser(mdx, Set.of()).parseMdxStatement();

        // the tree is as deep as the chain is long: text or a clean refusal
        AtomicReference<Throwable> thrown = new AtomicReference<>();
        Thread thread = new Thread(null, () -> {
            try {
                new SimpleUnparser().unparseMdxStatement(statement);
            } catch (Throwable t) {
                thrown.set(t);
            }
        }, "unparse", 256 * 1024L);
        thread.start();
        thread.join();
        if (thrown.get() != null) {
            assertThat(thrown.get()).isInstanceOf(IllegalArgumentException.class);
        }
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("providers")
    void aParseWritesNothingToStdout(String name, MdxParserProvider provider) throws Exception {
        PrintStream original = System.out;
        ByteArrayOutputStream captured = new ByteArrayOutputStream();
        System.setOut(new PrintStream(captured, true));
        try {
            provider.newParser(statement("function", 20), Set.of()).parseMdxStatement();
            parse(provider, statement("not", 100_000), 1024 * 1024L);
        } finally {
            System.setOut(original);
        }
        assertThat(captured.size()).isZero();
    }

    static Stream<Arguments> providers() {
        return PROVIDERS.stream().map(p -> Arguments.of(p.getClass().getSimpleName(), p));
    }

    /** Parses in a thread with a stack of the given size, gives what was thrown. */
    private static Throwable parse(MdxParserProvider provider, String mdx, long stack) throws InterruptedException {
        AtomicReference<Throwable> thrown = new AtomicReference<>();
        Thread thread = new Thread(null, () -> {
            try {
                provider.newParser(mdx, Set.of()).parseMdxStatement();
            } catch (Throwable t) {
                thrown.set(t);
            }
        }, "parse", stack);
        thread.start();
        thread.join();
        return thrown.get();
    }
}
