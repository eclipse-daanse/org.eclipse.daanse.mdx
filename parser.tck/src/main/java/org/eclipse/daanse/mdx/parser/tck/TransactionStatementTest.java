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
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.eclipse.daanse.mdx.parser.tck.CubeTest.propertyWords;

import org.eclipse.daanse.mdx.model.api.MdxStatement;
import org.eclipse.daanse.mdx.model.api.TransactionKind;
import org.eclipse.daanse.mdx.model.api.TransactionStatement;
import org.eclipse.daanse.mdx.parser.api.MdxParserException;
import org.eclipse.daanse.mdx.parser.api.MdxParserProvider;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.osgi.service.component.annotations.RequireServiceComponentRuntime;
import org.osgi.test.common.annotation.InjectService;

/**
 * The bracket a cell writeback runs in. Excel, ADOMD and Flexmonster all send
 * these as the text of an XMLA Statement command, so failing to parse them
 * fails the writeback at its first step, before any UPDATE CUBE is seen.
 */
@RequireServiceComponentRuntime
class TransactionStatementTest {

    @ParameterizedTest
    @CsvSource({ "BEGIN TRANSACTION,BEGIN", "COMMIT TRANSACTION,COMMIT", "ROLLBACK TRANSACTION,ROLLBACK",
            "BEGIN TRAN,BEGIN", "COMMIT TRAN,COMMIT", "ROLLBACK TRAN,ROLLBACK",
            // Clients do not agree on case, and MDX keywords never depend on it.
            "begin transaction,BEGIN", "Commit Transaction,COMMIT" })
    void parses(String mdx, String expected, @InjectService MdxParserProvider mdxParserProvider)
            throws MdxParserException {
        MdxStatement statement = mdxParserProvider.newParser(mdx, propertyWords).parseMdxStatement();
        assertThat(statement).isInstanceOf(TransactionStatement.class);
        assertThat(((TransactionStatement) statement).kind()).isEqualTo(TransactionKind.valueOf(expected));
    }

    @Test
    void rejectsBareKeyword(@InjectService MdxParserProvider mdxParserProvider) {
        // Without this, BEGIN would swallow the start of anything that follows it.
        assertThatThrownBy(() -> mdxParserProvider.newParser("BEGIN", propertyWords).parseMdxStatement())
                .isInstanceOf(MdxParserException.class);
    }
}
