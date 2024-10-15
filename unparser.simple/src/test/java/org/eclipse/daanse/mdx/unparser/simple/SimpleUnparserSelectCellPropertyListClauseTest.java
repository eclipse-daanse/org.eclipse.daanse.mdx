/*
 * Copyright (c) 2023 Contributors to the Eclipse Foundation.
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
package org.eclipse.daanse.mdx.unparser.simple;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

import org.eclipse.daanse.mdx.model.api.select.SelectCellPropertyListClause;
import org.eclipse.daanse.mdx.model.record.select.SelectCellPropertyListClauseR;
import org.junit.jupiter.api.Test;

class SimpleUnparserSelectCellPropertyListClauseTest {

    private SimpleUnparser unparser = new SimpleUnparser();

    @Test
    void test1() {
        SelectCellPropertyListClause selectCellPropertyListClause = new SelectCellPropertyListClauseR(
                List.of("BACK_COLOR", "FORE_COLOR"), true);

        assertThat(unparser.unparseSelectCellPropertyListClause(selectCellPropertyListClause)).asString()
                .isEqualTo("CELL PROPERTIES BACK_COLOR\r\n, FORE_COLOR");
    }

    @Test
    void test2() {
        SelectCellPropertyListClause selectCellPropertyListClause = new SelectCellPropertyListClauseR(
                List.of("BACK_COLOR", "FORE_COLOR"), false);

        assertThat(unparser.unparseSelectCellPropertyListClause(selectCellPropertyListClause)).asString()
                .isEqualTo("PROPERTIES BACK_COLOR\r\n, FORE_COLOR");
    }
}
