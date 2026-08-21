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

/**
 * TransactionStatement Statement
 * Syntax BEGIN {TRANSACTION | TRAN}
 *      | COMMIT {TRANSACTION | TRAN}
 *      | ROLLBACK {TRANSACTION | TRAN}
 * <p>
 * The bracket around a cell writeback: a client opens one, sends its
 * {@code UPDATE CUBE} statements, and then makes them permanent or throws them
 * away. It is not MDX in the sense the other statements are - it queries
 * nothing and names no cube - but it arrives the same way, as the text of an
 * XMLA {@code Statement} command, which is why it is parsed here.
 */
public non-sealed interface TransactionStatement extends MdxStatement {

    TransactionKind kind();
}
