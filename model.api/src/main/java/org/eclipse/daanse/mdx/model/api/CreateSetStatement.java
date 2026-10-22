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
package org.eclipse.daanse.mdx.model.api;

import java.util.Optional;

import org.eclipse.daanse.mdx.model.api.select.CreateSetBodyClause;

/**
 * CreateSetStatement Statement
 * Syntax CREATE [SESSION] [STATIC | DYNAMIC] [HIDDEN] SET Cube_Name.Set_Name
 *          AS Expression [, Property = Expression]...
 * <p>
 * The first segment after {@code SET} is always the cube, it is kept apart in
 * {@link #cube()}; {@link #body()} holds the set name without it, the
 * expression and the properties, as a {@code WITH SET} does.
 */
public non-sealed interface CreateSetStatement extends MdxStatement {

    /** {@code SESSION} was written. Without it the scope is the session as well. */
    boolean session();

    /** {@code STATIC} or {@code DYNAMIC}, empty when neither was written. */
    Optional<SetEvaluation> evaluation();

    /** {@code HIDDEN} was written. */
    boolean hidden();

    CubeReference cube();

    CreateSetBodyClause body();
}
