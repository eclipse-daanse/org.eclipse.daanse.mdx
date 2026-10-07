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
import static org.assertj.core.api.Assertions.catchThrowableOfType;
import static org.eclipse.daanse.mdx.parser.tck.CubeTest.propertyWords;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.daanse.mdx.parser.api.MdxParserException;
import org.eclipse.daanse.mdx.parser.api.MdxParserProvider;
import org.junit.jupiter.api.Test;
import org.osgi.service.component.annotations.RequireServiceComponentRuntime;
import org.osgi.test.common.annotation.InjectService;

/**
 * A statement that ends too early is reported where it ends: on the line of its last token, at or right
 * after that token. An error inside the text is already reported at the offending token.
 */
@RequireServiceComponentRuntime
class ErrorPositionAtEndOfInputTest {

    private static final Pattern POSITION = Pattern.compile("input:(\\d+):(\\d+)");

    /** Line and column the parser names for the given text, as {line, column}. */
    private static int[] reportedPosition(MdxParserProvider parsers, String mdx) {
        MdxParserException e = catchThrowableOfType(MdxParserException.class,
                () -> parsers.newParser(mdx, propertyWords).parseMdxStatement());
        assertThat(e).as("%s is refused", mdx).isNotNull();
        Matcher m = POSITION.matcher(e.getMessage());
        assertThat(m.find()).as(e.getMessage()).isTrue();
        return new int[] { Integer.parseInt(m.group(1)), Integer.parseInt(m.group(2)) };
    }

    /** The last token is the one at {@code lastTokenColumn}; the end lies at it or right after it. */
    private static void assertReportedAtEnd(MdxParserProvider parsers, String mdx, int line, int lastTokenColumn) {
        int[] at = reportedPosition(parsers, mdx);
        assertThat(at[0]).as("line reported for %s", mdx).isEqualTo(line);
        assertThat(at[1]).as("column reported for %s", mdx).isBetween(lastTokenColumn, lastTokenColumn + 1);
    }

    @Test
    void anOpenTupleOnOneLineIsReportedAfterTheBracket(@InjectService MdxParserProvider parsers) {
        assertReportedAtEnd(parsers, "select {[A]} on 0 from [C] where (", 1, 34);
    }

    @Test
    void aMissingCubeIsReportedAfterFrom(@InjectService MdxParserProvider parsers) {
        assertReportedAtEnd(parsers, "select {[A]} on 0 from", 1, 22);
    }

    @Test
    void anOpenTupleOnTheSecondLineIsReportedOnThatLine(@InjectService MdxParserProvider parsers) {
        assertReportedAtEnd(parsers, "select {[A]} on 0\nfrom [C] where (", 2, 16);
    }

    @Test
    void anErrorInsideTheTextIsReportedAtTheToken(@InjectService MdxParserProvider parsers) {
        // green today in both parsers: the position of "from" where ON is expected
        int[] at = reportedPosition(parsers, "select [Time].Members\nfrom [Warehouse and Sales]");
        assertThat(at).containsExactly(2, 1);
    }
}
