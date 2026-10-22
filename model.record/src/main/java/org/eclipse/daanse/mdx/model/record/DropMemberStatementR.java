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
package org.eclipse.daanse.mdx.model.record;

import java.util.List;
import java.util.Objects;

import org.eclipse.daanse.mdx.model.api.DropMemberStatement;
import org.eclipse.daanse.mdx.model.api.QualifiedName;

public record DropMemberStatementR(List<QualifiedName> names) implements DropMemberStatement {

    public DropMemberStatementR {
        Objects.requireNonNull(names, "names must not be null");
        names = List.copyOf(names);
    }

}
