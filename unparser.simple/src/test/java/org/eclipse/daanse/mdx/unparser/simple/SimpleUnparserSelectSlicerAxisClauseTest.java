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

import org.eclipse.daanse.mdx.model.api.expression.ObjectIdentifier;
import org.eclipse.daanse.mdx.model.api.select.SelectSlicerAxisClause;
import org.eclipse.daanse.mdx.model.record.expression.CompoundIdR;
import org.eclipse.daanse.mdx.model.record.expression.NameObjectIdentifierR;
import org.eclipse.daanse.mdx.model.record.select.SelectSlicerAxisClauseR;
import org.junit.jupiter.api.Test;

class SimpleUnparserSelectSlicerAxisClauseTest {

    private SimpleUnparser unparser = new SimpleUnparser();

    @Test
    void test1() {
        SelectSlicerAxisClause selectSlicerAxisClause = new SelectSlicerAxisClauseR(
                new CompoundIdR(List.of(new NameObjectIdentifierR("Measures", ObjectIdentifier.Quoting.QUOTED),
                        new NameObjectIdentifierR("Internet Sales Amount", ObjectIdentifier.Quoting.QUOTED))));

        assertThat(unparser.unparseSelectSlicerAxisClause(selectSlicerAxisClause)).asString()
                .isEqualTo("WHERE [Measures].[Internet Sales Amount]");
    }
}
