/*
* Copyright (c) 2024 Contributors to the Eclipse Foundation.
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
package org.eclipse.daanse.mdx.model.api.expression.operation;

import java.util.Objects;

/**
 * Defines a Case-Operation. The name tells the two forms apart; the parts are
 * one flat list in the call's expressions.
 *
 * <ul>
 * <li>{@link #CASE_MATCH}: {@code CASE <operand> WHEN <value> THEN <result> ... [ELSE <result>] END},
 * list {@code [operand, value1, result1, ..., (else)]}</li>
 * <li>{@link #CASE_TEST}: {@code CASE WHEN <condition> THEN <result> ... [ELSE <result>] END},
 * list {@code [condition1, result1, ..., (else)]}</li>
 * </ul>
 */
public record CaseOperationAtom(String name) implements OperationAtom {

    /** The simple form, with an operand after {@code CASE}. */
    public static final String CASE_MATCH = "_CaseMatch";

    /** The searched form, with a condition after each {@code WHEN}. */
    public static final String CASE_TEST = "_CaseTest";

    public CaseOperationAtom {
        Objects.requireNonNull(name, "name must not be null");
    }


}
