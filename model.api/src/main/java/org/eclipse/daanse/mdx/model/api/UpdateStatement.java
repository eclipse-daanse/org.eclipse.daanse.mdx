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
package org.eclipse.daanse.mdx.model.api;

import java.util.List;

import org.eclipse.daanse.mdx.model.api.expression.NameObjectIdentifier;
import org.eclipse.daanse.mdx.model.api.select.UpdateClause;

/**
 * UpdateStatement Statement
 * Syntax UPDATE MEMBER Cube_Name.Member_Name
 * AS MDX_Expression
 * [, Property_Name = Property_Value, ...n]
 */
public non-sealed interface UpdateStatement extends MdxStatement {

    NameObjectIdentifier cubeName();

    List<? extends UpdateClause> updateClauses();
}
