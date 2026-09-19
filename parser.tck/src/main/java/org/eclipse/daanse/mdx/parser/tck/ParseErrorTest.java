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
package org.eclipse.daanse.mdx.parser.tck;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.Set;

import org.eclipse.daanse.mdx.parser.api.MdxParserException;
import org.eclipse.daanse.mdx.parser.api.MdxParserProvider;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.osgi.service.component.annotations.RequireServiceComponentRuntime;
import org.osgi.test.common.annotation.InjectService;

/**
 * Contract for parse-error reporting, portable across parser
 * implementations: syntax errors surface as {@link MdxParserException} —
 * never as an implementation exception such as a NullPointerException —
 * with a source position and a token diagnostic. Formula bodies in
 * single-quoted member definitions ("WITH MEMBER x AS '...'") may be parsed
 * eagerly (nested parser) or lazily; either way no implementation exception
 * must leak.
 */
@RequireServiceComponentRuntime
class ParseErrorTest {

    @Test
    void syntaxErrorCarriesPositionInfo(@InjectService MdxParserProvider mdxParserProvider) {
        MdxParserException e = assertThrows(MdxParserException.class,
                () -> mdxParserProvider.newParser("select from from [Sales]", Set.of()).parseMdxStatement());

        assertThat(e.getMessage()).contains("Encountered an error at");
        assertThat(e.line()).isGreaterThan(0);
        assertThat(e.column()).isGreaterThan(0);
    }

    @ParameterizedTest
    @ValueSource(strings = {
            // "5" is not a type name: an eager implementation fails the inner parse
            "with member [Measures].[Foo] as 'Cast(1 AS 5)' select {[Measures].[Foo]} on columns from Sales",
            // single quote inside a double-quoted string ends the outer formula early
            "with member [Measures].[Foo] as ' \"quoted string with 'apostrophe' in it\" ' "
                    + "select {[Measures].[Foo]} on columns from [Sales]" })
    void nestedFormulaErrorNeverLeaksImplementationException(String mdx,
            @InjectService MdxParserProvider mdxParserProvider) {
        try {
            mdxParserProvider.newParser(mdx, Set.of()).parseMdxStatement();
            // lazily parsing implementations accept the statement; the formula
            // body is validated later — that is a legal outcome here
        } catch (MdxParserException e) {
            // eagerly parsing implementations must report the failure as the
            // API exception with usable diagnostics
            assertThat(e.getMessage()).isNotBlank();
        }
        // anything else (NullPointerException, ...) fails the test by escaping
    }

    @Test
    void emptyArgumentListParses(@InjectService MdxParserProvider mdxParserProvider) throws MdxParserException {
        // zero-argument function calls are valid MDX (e.g. OpeningPeriod())
        assertThat(mdxParserProvider.newParser("select {OpeningPeriod()} on columns from Sales", Set.of())
                .parseMdxStatement()).isNotNull();
    }

    /** A parser is recursive, a parse of deep input still ends with the declared exception. */
    @ParameterizedTest
    @ValueSource(strings = { "NOT ", "(", "{", "f(", "CASE WHEN 1 THEN " })
    void deepNestingIsAParserException(String open, @InjectService MdxParserProvider mdxParserProvider) {
        String mdx = open.repeat(100_000) + "1";
        assertThrows(MdxParserException.class, () -> mdxParserProvider.newParser(mdx, Set.of()).parseExpression());
    }
}
