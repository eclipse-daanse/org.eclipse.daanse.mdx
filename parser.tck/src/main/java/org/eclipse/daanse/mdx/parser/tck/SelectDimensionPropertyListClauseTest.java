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
import static org.eclipse.daanse.mdx.parser.tck.MdxTestUtils.checkNameObjectIdentifiers;

import org.eclipse.daanse.mdx.model.api.expression.NameObjectIdentifier;
import org.eclipse.daanse.mdx.model.api.expression.ObjectIdentifier;
import org.eclipse.daanse.mdx.model.api.select.SelectDimensionPropertyListClause;
import org.eclipse.daanse.mdx.parser.api.MdxParserException;
import org.eclipse.daanse.mdx.parser.api.MdxParserProvider;
import org.junit.jupiter.api.Test;
import org.osgi.service.component.annotations.RequireServiceComponentRuntime;
import org.osgi.test.common.annotation.InjectService;

@RequireServiceComponentRuntime
class SelectDimensionPropertyListClauseTest {
    @Test
    void test1(@InjectService MdxParserProvider mdxParserProvider) throws MdxParserException {
        SelectDimensionPropertyListClause selectDimensionPropertyListClause = mdxParserProvider
                .newParser("DIMENSION PROPERTIES BACK_COLOR, FORE_COLOR", propertyWords)
                .parseSelectDimensionPropertyListClause();
        assertThat(selectDimensionPropertyListClause).isNotNull();
        assertThat(selectDimensionPropertyListClause.properties()).isNotNull().hasSize(2);

        assertThat(selectDimensionPropertyListClause.properties().get(0)).isNotNull();
        assertThat(selectDimensionPropertyListClause.properties().get(0).objectIdentifiers()).isNotNull().hasSize(1);
        checkNameObjectIdentifiers(selectDimensionPropertyListClause.properties().get(0).objectIdentifiers(), 0,
                "BACK_COLOR", ObjectIdentifier.Quoting.UNQUOTED);

        checkNameObjectIdentifiers(selectDimensionPropertyListClause.properties().get(1).objectIdentifiers(), 0,
                "FORE_COLOR", ObjectIdentifier.Quoting.UNQUOTED);
    }

    @Test
    void test2(@InjectService MdxParserProvider mdxParserProvider) throws MdxParserException {
        SelectDimensionPropertyListClause selectDimensionPropertyListClause = mdxParserProvider
                .newParser("DIMENSION PROPERTIES [Store].[Store].[Store Name].[Store Type]", propertyWords)
                .parseSelectDimensionPropertyListClause();
        assertThat(selectDimensionPropertyListClause).isNotNull();
        assertThat(selectDimensionPropertyListClause.properties()).isNotNull().hasSize(1);
        assertThat(selectDimensionPropertyListClause.properties().get(0).objectIdentifiers()).isNotNull().hasSize(4);
        assertThat(selectDimensionPropertyListClause.properties().get(0).objectIdentifiers().get(0))
                .isInstanceOf(NameObjectIdentifier.class);
        assertThat(selectDimensionPropertyListClause.properties().get(0).objectIdentifiers().get(1))
                .isInstanceOf(NameObjectIdentifier.class);
        assertThat(selectDimensionPropertyListClause.properties().get(0).objectIdentifiers().get(2))
                .isInstanceOf(NameObjectIdentifier.class);
        assertThat(selectDimensionPropertyListClause.properties().get(0).objectIdentifiers().get(3))
                .isInstanceOf(NameObjectIdentifier.class);
        NameObjectIdentifier nameObjectIdentifier1 = (NameObjectIdentifier) selectDimensionPropertyListClause
                .properties().get(0).objectIdentifiers().get(0);
        NameObjectIdentifier nameObjectIdentifier2 = (NameObjectIdentifier) selectDimensionPropertyListClause
                .properties().get(0).objectIdentifiers().get(1);
        NameObjectIdentifier nameObjectIdentifier3 = (NameObjectIdentifier) selectDimensionPropertyListClause
                .properties().get(0).objectIdentifiers().get(2);
        NameObjectIdentifier nameObjectIdentifier4 = (NameObjectIdentifier) selectDimensionPropertyListClause
                .properties().get(0).objectIdentifiers().get(3);
        assertThat(nameObjectIdentifier1.name()).isEqualTo("Store");
        assertThat(nameObjectIdentifier1.quoting()).isEqualTo(ObjectIdentifier.Quoting.QUOTED);
        assertThat(nameObjectIdentifier2.name()).isEqualTo("Store");
        assertThat(nameObjectIdentifier2.quoting()).isEqualTo(ObjectIdentifier.Quoting.QUOTED);
        assertThat(nameObjectIdentifier3.name()).isEqualTo("Store Name");
        assertThat(nameObjectIdentifier3.quoting()).isEqualTo(ObjectIdentifier.Quoting.QUOTED);
        assertThat(nameObjectIdentifier4.name()).isEqualTo("Store Type");
        assertThat(nameObjectIdentifier4.quoting()).isEqualTo(ObjectIdentifier.Quoting.QUOTED);
    }

}
