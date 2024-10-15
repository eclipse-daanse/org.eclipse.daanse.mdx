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
package org.eclipse.daanse.mdx.parser.tck;

import static org.assertj.core.api.Assertions.assertThat;
import static org.eclipse.daanse.mdx.parser.tck.CubeTest.propertyWords;

import org.assertj.core.data.Index;
import org.eclipse.daanse.mdx.model.api.select.SelectCellPropertyListClause;
import org.eclipse.daanse.mdx.parser.api.MdxParserException;
import org.eclipse.daanse.mdx.parser.api.MdxParserProvider;
import org.junit.jupiter.api.Test;
import org.osgi.service.component.annotations.RequireServiceComponentRuntime;
import org.osgi.test.common.annotation.InjectService;

@RequireServiceComponentRuntime
class SelectCellPropertyListClauseTest {

    @Test
    void test1(@InjectService MdxParserProvider mdxParserProvider) throws MdxParserException {
        SelectCellPropertyListClause selectCellPropertyListClause = mdxParserProvider
                .newParser("CELL PROPERTIES BACK_COLOR, FORE_COLOR", propertyWords).parseSelectCellPropertyListClause();
        assertThat(selectCellPropertyListClause).isNotNull();
        assertThat(selectCellPropertyListClause.cell()).isTrue();
        assertThat(selectCellPropertyListClause.properties()).isNotNull().hasSize(2);
        assertThat(selectCellPropertyListClause.properties()).contains("BACK_COLOR", Index.atIndex(0));
        assertThat(selectCellPropertyListClause.properties()).contains("FORE_COLOR", Index.atIndex(1));
    }

    @Test
    void test2(@InjectService MdxParserProvider mdxParserProvider) throws MdxParserException {
        SelectCellPropertyListClause selectCellPropertyListClause = mdxParserProvider
                .newParser("PROPERTIES BACK_COLOR, FORE_COLOR, TEST", propertyWords)
                .parseSelectCellPropertyListClause();
        assertThat(selectCellPropertyListClause).isNotNull();
        assertThat(selectCellPropertyListClause.cell()).isFalse();
        assertThat(selectCellPropertyListClause.properties()).isNotNull().hasSize(3);
        assertThat(selectCellPropertyListClause.properties()).contains("BACK_COLOR", Index.atIndex(0));
        assertThat(selectCellPropertyListClause.properties()).contains("FORE_COLOR", Index.atIndex(1));
        assertThat(selectCellPropertyListClause.properties()).contains("TEST", Index.atIndex(2));
    }
}
