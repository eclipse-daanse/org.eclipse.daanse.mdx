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

import java.math.BigDecimal;
import java.util.List;

import org.eclipse.daanse.mdx.model.api.expression.ObjectIdentifier;
import org.eclipse.daanse.mdx.model.api.select.MemberPropertyDefinition;
import org.eclipse.daanse.mdx.model.record.expression.CompoundIdR;
import org.eclipse.daanse.mdx.model.record.expression.NameObjectIdentifierR;
import org.eclipse.daanse.mdx.model.record.expression.NumericLiteralR;
import org.eclipse.daanse.mdx.model.record.expression.StringLiteralR;
import org.eclipse.daanse.mdx.model.record.select.MemberPropertyDefinitionR;
import org.junit.jupiter.api.Test;

class SimpleUnparserMemberPropertyDefinitionTest {

    private SimpleUnparser unparser = new SimpleUnparser();

    @Test
    void test1() {

        MemberPropertyDefinition memberPropertyDefinition = new MemberPropertyDefinitionR(
                new NameObjectIdentifierR("name", ObjectIdentifier.Quoting.UNQUOTED),
                new CompoundIdR(List.of(new NameObjectIdentifierR("test", ObjectIdentifier.Quoting.UNQUOTED))));
        assertThat(unparser.unparseMemberPropertyDefinition(memberPropertyDefinition)).asString()
                .isEqualTo("name = test");
    }

    @Test
    void test2() {

        MemberPropertyDefinition memberPropertyDefinition = new MemberPropertyDefinitionR(
                new NameObjectIdentifierR("name", ObjectIdentifier.Quoting.QUOTED),
                new NumericLiteralR(new BigDecimal(10)));
        assertThat(unparser.unparseMemberPropertyDefinition(memberPropertyDefinition)).asString()
                .isEqualTo("[name] = 10");
    }

    @Test
    void test3() {

        MemberPropertyDefinition memberPropertyDefinition = new MemberPropertyDefinitionR(
                new NameObjectIdentifierR("name", ObjectIdentifier.Quoting.QUOTED), new StringLiteralR("\"test\""));
        assertThat(unparser.unparseMemberPropertyDefinition(memberPropertyDefinition)).asString()
                .isEqualTo("[name] = \"test\"");
    }

}
