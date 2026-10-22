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

import java.util.List;

/**
 * DropMemberStatement Statement
 * Syntax DROP MEMBER Cube_Name.Member_Name [, Cube_Name.Member_Name]...
 */
public non-sealed interface DropMemberStatement extends MdxStatement {

    List<? extends QualifiedName> names();
}
