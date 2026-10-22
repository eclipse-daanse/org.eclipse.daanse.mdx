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
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.osgi.service.component.annotations.RequireServiceComponentRuntime;
import org.osgi.test.common.annotation.InjectService;

/**
 * Session statements SSAS refuses with a syntax error. The parser fails with a position: on the offending word or on
 * the token after it, where SSAS points as well ("The syntax for 'SET' is incorrect").
 */
@RequireServiceComponentRuntime
class SessionStatementErrorTest {

    @ParameterizedTest
    @CsvSource(delimiter = '|', value = {
            // GLOBAL is not a scope SSAS knows
            "CREATE GLOBAL SET [Adventure Works].[S] AS {[Product].[Category].&[1]}|8|15",
            // HIDDEN is for sets only
            "CREATE HIDDEN MEMBER [Adventure Works].[Measures].[M] AS 1|8|15",
            // one statement per command
            "CREATE SET [AW].[A] AS {[x]}; CREATE SET [AW].[B] AS {[y]}|29|29",
            // a CREATE is not a WITH: no SELECT after it
            "CREATE MEMBER [AW].[Measures].[M] AS 2 SELECT {[Measures].[M]} ON 0 FROM [AW]|40|40",
            // STATIC, DYNAMIC and HIDDEN are for sets only, CALCULATED is for members only
            "CREATE CALCULATED SET [AW].[x] AS {}|8|19",
            "CREATE STATIC MEMBER [AW].[Measures].[M] AS 1|8|15",
            // the name needs a cube in front of it
            "CREATE MEMBER [M] AS 2|19|19",
            "DROP SET [S]|13|13",
            "DROP CUBE [AW]|6|6" })
    void refused(String mdx, int from, int to, @InjectService MdxParserProvider mdxParserProvider) {
        MdxParserException e = assertThrows(MdxParserException.class,
                () -> mdxParserProvider.newParser(mdx, Set.of()).parseMdxStatement());
        assertThat(e.line()).isEqualTo(1);
        assertThat(e.column()).isBetween(from, to);
    }
}
