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

import java.util.Optional;

import org.eclipse.daanse.mdx.model.api.select.SelectQueryClause;
import org.eclipse.daanse.mdx.model.api.select.SelectSlicerAxisClause;
import org.eclipse.daanse.mdx.model.api.select.SelectCubeClause;
import org.eclipse.daanse.mdx.model.api.select.SelectCubeClauseSubStatement;

public record SelectCubeClauseSubStatementR(SelectQueryClause selectQueryClause,
                                            SelectCubeClause selectCubeClause,
                                            Optional<SelectSlicerAxisClause> selectSlicerAxisClause)
        implements SelectCubeClauseSubStatement {

}
