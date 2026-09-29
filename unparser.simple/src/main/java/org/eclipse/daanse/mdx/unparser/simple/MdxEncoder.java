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
package org.eclipse.daanse.mdx.unparser.simple;

import java.util.Locale;
import java.util.Set;

/**
 * Turns the decoded values of the model into MDX text. The model holds values
 * without delimiters and without doubled escape characters, this is the one
 * place that puts them back. A value that has no safe text form is rejected.
 */
final class MdxEncoder {

    private static final Set<String> INFIX_OPERATORS = Set.of("+", "-", "*", "/", "^", "||", ":", "=", "<>", "<", ">",
            "<=", ">=", "AND", "OR", "XOR", "IS", "IN", "MATCHES", "AS");

    private static final Set<String> PREFIX_OPERATORS = Set.of("-", "+", "NOT", "EXISTING");

    private static final Set<String> POSTFIX_OPERATORS = Set.of("IS NULL", "IS EMPTY");

    private MdxEncoder() {
    }

    /**
     * Always double quotes: a single quoted literal is a formula in the body of a
     * member or a set and would be parsed as an expression again.
     */
    static String string(String value) {
        requireValue(value, "string literal");
        return new StringBuilder(value.length() + 2).append('"').append(value.replace("\"", "\"\"")).append('"')
                .toString();
    }

    static String bracket(String name) {
        requireValue(name, "identifier");
        return new StringBuilder(name.length() + 2).append('[').append(name.replace("]", "]]")).append(']').toString();
    }

    /** Bare where the lexer reads it back as one ID token, in brackets otherwise. */
    static String identifier(String name) {
        requireValue(name, "identifier");
        return isBareIdentifier(name) ? name : bracket(name);
    }

    /** For the places where the grammar takes no quoted form. */
    static String bare(String name, String what) {
        requireValue(name, what);
        if (!isBareIdentifier(name)) {
            throw new IllegalArgumentException("not a valid " + what + ": '" + name + "'");
        }
        return name;
    }

    static String infixOperator(String name) {
        return operator(name, INFIX_OPERATORS, "infix operator");
    }

    static String prefixOperator(String name) {
        return operator(name, PREFIX_OPERATORS, "prefix operator");
    }

    static String postfixOperator(String name) {
        return operator(name, POSTFIX_OPERATORS, "postfix operator");
    }

    static boolean isBareIdentifier(String name) {
        if (name == null || name.isEmpty()) {
            return false;
        }
        int start = name.charAt(0) == '@' ? 1 : 0;
        if (start == name.length() || !Character.isJavaIdentifierStart(name.charAt(start))) {
            return false;
        }
        for (int i = start + 1; i < name.length(); i++) {
            char c = name.charAt(i);
            if (!Character.isJavaIdentifierPart(c) || Character.isIdentifierIgnorable(c)) {
                return false;
            }
        }
        return true;
    }

    private static String operator(String name, Set<String> allowed, String what) {
        requireValue(name, what);
        if (!allowed.contains(name.toUpperCase(Locale.ROOT))) {
            throw new IllegalArgumentException("not a valid " + what + ": '" + name + "'");
        }
        return name;
    }

    private static void requireValue(String value, String what) {
        if (value == null) {
            throw new IllegalArgumentException(what + " must not be null");
        }
    }
}
