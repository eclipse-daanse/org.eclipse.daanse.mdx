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

import java.util.List;

import org.eclipse.daanse.mdx.model.api.ReturnItem;
import org.eclipse.daanse.mdx.model.api.expression.CompoundId;
import org.eclipse.daanse.mdx.model.api.expression.NameObjectIdentifier;
import org.eclipse.daanse.mdx.model.api.expression.ObjectIdentifier;
import org.eclipse.daanse.mdx.parser.api.MdxParserException;
import org.eclipse.daanse.mdx.parser.api.MdxParserProvider;
import org.junit.jupiter.api.Test;
import org.osgi.service.component.annotations.RequireServiceComponentRuntime;
import org.osgi.test.common.annotation.InjectService;

@RequireServiceComponentRuntime
class ReturnItemTest {

    @Test
    void test(@InjectService MdxParserProvider mdxParserProvider) throws MdxParserException {
        List<? extends ReturnItem> clauseList = mdxParserProvider.newParser("[a].[b]", propertyWords)
                .parseReturnItems();
        assertThat(clauseList).isNotNull().hasSize(1);
        CompoundId compoundId = clauseList.get(0).compoundId();
        assertThat(compoundId.objectIdentifiers()).hasSize(2);
        assertThat(compoundId.objectIdentifiers().get(0)).isInstanceOf(NameObjectIdentifier.class);
        assertThat(compoundId.objectIdentifiers().get(1)).isInstanceOf(NameObjectIdentifier.class);
        NameObjectIdentifier nameObjectIdentifier1 = (NameObjectIdentifier) compoundId.objectIdentifiers().get(0);
        NameObjectIdentifier nameObjectIdentifier2 = (NameObjectIdentifier) compoundId.objectIdentifiers().get(1);
        assertThat(nameObjectIdentifier1.name()).isEqualTo("a");
        assertThat(nameObjectIdentifier1.quoting()).isEqualTo(ObjectIdentifier.Quoting.QUOTED);
        assertThat(nameObjectIdentifier2.name()).isEqualTo("b");
        assertThat(nameObjectIdentifier2.quoting()).isEqualTo(ObjectIdentifier.Quoting.QUOTED);
    }
}
