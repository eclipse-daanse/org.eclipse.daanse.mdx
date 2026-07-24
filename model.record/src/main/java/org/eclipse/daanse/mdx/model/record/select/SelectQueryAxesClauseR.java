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
package org.eclipse.daanse.mdx.model.record.select;

import java.util.List;
import java.util.Objects;

import org.eclipse.daanse.mdx.model.api.select.SelectQueryAxesClause;
import org.eclipse.daanse.mdx.model.api.select.SelectQueryAxisClause;

public record SelectQueryAxesClauseR(List<SelectQueryAxisClause> selectQueryAxisClauses)
        implements SelectQueryAxesClause {

    public SelectQueryAxesClauseR{
        Objects.requireNonNull(selectQueryAxisClauses, "selectQueryAxisClauses must not be null");
        selectQueryAxisClauses = List.copyOf(selectQueryAxisClauses);
    }

}
