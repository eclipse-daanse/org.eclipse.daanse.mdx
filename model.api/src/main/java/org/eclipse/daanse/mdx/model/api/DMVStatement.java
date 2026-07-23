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

import org.eclipse.daanse.mdx.model.api.expression.CompoundId;
import org.eclipse.daanse.mdx.model.api.expression.MdxExpression;
import org.eclipse.daanse.mdx.model.api.expression.NameObjectIdentifier;

/**
 * DMVStatement Statement
 * Syntax SELECT [DISTINCT] [TOP n] select_list
 * FROM $System.schemaRowset
 * [WHERE condition_expression]
 * [ORDER BY expression [DESC | ASC]
 */
public non-sealed interface DMVStatement extends MdxStatement {

    List<? extends CompoundId> columns();

    NameObjectIdentifier table();

    MdxExpression where();
}
