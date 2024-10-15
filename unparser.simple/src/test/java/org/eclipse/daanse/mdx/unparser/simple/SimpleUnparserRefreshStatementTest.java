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

import org.eclipse.daanse.mdx.model.api.RefreshStatement;
import org.eclipse.daanse.mdx.model.api.expression.ObjectIdentifier;
import org.eclipse.daanse.mdx.model.record.RefreshStatementR;
import org.eclipse.daanse.mdx.model.record.expression.NameObjectIdentifierR;
import org.junit.jupiter.api.Test;

class SimpleUnparserRefreshStatementTest {

    private SimpleUnparser unparser = new SimpleUnparser();

    @Test
    void test1() {
        RefreshStatement refreshStatement = new RefreshStatementR(
                new NameObjectIdentifierR("cubeName", ObjectIdentifier.Quoting.UNQUOTED));

        assertThat(unparser.unparseRefreshStatement(refreshStatement)).asString().isEqualTo("REFRESH CUBE cubeName");
    }

    @Test
    void test2() {
        RefreshStatement refreshStatement = new RefreshStatementR(
                new NameObjectIdentifierR("cubeName", ObjectIdentifier.Quoting.QUOTED));

        assertThat(unparser.unparseRefreshStatement(refreshStatement)).asString().isEqualTo("REFRESH CUBE [cubeName]");
    }
}
