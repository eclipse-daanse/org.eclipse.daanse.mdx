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

import org.eclipse.daanse.mdx.model.api.select.CreateMemberBodyClause;

/**
 * CreateMemberStatement Statement
 * Syntax CREATE [SESSION] [CALCULATED] MEMBER Cube_Name.Member_Name
 *          AS Expression [, Property = Expression]...
 * <p>
 * The first segment after {@code MEMBER} is always the cube, it is kept apart in
 * {@link #cube()}; {@link #body()} holds the member name without it, the
 * expression and the properties, as a {@code WITH MEMBER} does.
 */
public non-sealed interface CreateMemberStatement extends MdxStatement {

    /** {@code SESSION} was written. Without it the scope is the session as well. */
    boolean session();

    /** {@code CALCULATED} was written. */
    boolean calculated();

    CubeReference cube();

    CreateMemberBodyClause body();
}
