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

import java.util.Objects;
import java.util.Optional;

import org.eclipse.daanse.mdx.model.api.CreateSetStatement;
import org.eclipse.daanse.mdx.model.api.CubeReference;
import org.eclipse.daanse.mdx.model.api.SetEvaluation;
import org.eclipse.daanse.mdx.model.api.select.CreateSetBodyClause;

public record CreateSetStatementR(boolean session, Optional<SetEvaluation> evaluation, boolean hidden,
        CubeReference cube, CreateSetBodyClause body) implements CreateSetStatement {

    public CreateSetStatementR {
        Objects.requireNonNull(evaluation, "evaluation must not be null");
    }

}
