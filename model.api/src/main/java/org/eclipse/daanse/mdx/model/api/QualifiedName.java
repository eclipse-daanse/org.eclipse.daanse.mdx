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

import org.eclipse.daanse.mdx.model.api.expression.CompoundId;

/**
 * A member or set name as {@code DROP MEMBER} and {@code DROP SET} take it: the
 * cube, and the name without the cube segment.
 */
public interface QualifiedName {

    CubeReference cube();

    CompoundId compoundId();
}
