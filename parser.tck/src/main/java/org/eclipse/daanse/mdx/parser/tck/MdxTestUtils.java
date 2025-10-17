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

import java.util.List;

import org.eclipse.daanse.mdx.model.api.expression.NameObjectIdentifier;
import org.eclipse.daanse.mdx.model.api.expression.ObjectIdentifier;
import org.eclipse.daanse.mdx.model.api.select.Axis;
import org.eclipse.daanse.mdx.model.api.select.SelectCubeClause;
import org.eclipse.daanse.mdx.model.api.select.SelectCubeClauseName;
import org.osgi.service.component.annotations.RequireServiceComponentRuntime;

@RequireServiceComponentRuntime
public class MdxTestUtils {

    public static void checkNameObjectIdentifiers(List<? extends ObjectIdentifier> objectIdentifiers, int i,
            String name, ObjectIdentifier.Quoting quoted) {
        assertThat(objectIdentifiers.get(i).quoting()).isEqualTo(quoted);
        assertThat(objectIdentifiers.get(i)).isInstanceOf(NameObjectIdentifier.class);
        NameObjectIdentifier noi1 = (NameObjectIdentifier) objectIdentifiers.get(i);
        assertThat(noi1.name()).isEqualTo(name);
    }

    public static void checkSelectCubeClauseName(SelectCubeClause selectCubeClause, String name,
            ObjectIdentifier.Quoting quoted) {
        assertThat(selectCubeClause).isNotNull().isInstanceOf(SelectCubeClauseName.class);
        SelectCubeClauseName selectCubeClauseName = (SelectCubeClauseName) selectCubeClause;
        assertThat(selectCubeClauseName.cubeName()).isInstanceOf(NameObjectIdentifier.class);
        NameObjectIdentifier nameObjectIdentifier = selectCubeClauseName.cubeName();
        assertThat(nameObjectIdentifier.name()).isEqualTo(name);
        assertThat(nameObjectIdentifier.quoting()).isEqualTo(quoted);
    }

    public static void checkAxis(Axis axis, int ordinal, boolean named) {
        assertThat(axis).isNotNull();
        assertThat(axis.ordinal()).isEqualTo(ordinal);
        assertThat(axis.named()).isEqualTo(named);
    }
}
