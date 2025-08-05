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
package org.eclipse.daanse.mdx.parser.cccx;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.eclipse.daanse.mdx.model.api.expression.CompoundId;
import org.eclipse.daanse.mdx.model.api.expression.MdxExpression;
import org.eclipse.daanse.mdx.model.api.expression.NameObjectIdentifier;
import org.eclipse.daanse.mdx.model.api.expression.ObjectIdentifier;
import org.eclipse.daanse.mdx.model.api.expression.operation.PlainPropertyOperationAtom;
import org.eclipse.daanse.mdx.parser.cccx.tree.Expression;

public class MdxParserUtil {

    private static final Logger logger = LoggerFactory.getLogger(MdxParserUtil.class);

    private MdxParserUtil() {
    }

    public static String stripQuotes(String s, String prefix, String suffix, String quoted) {
        logger.debug("Stripping quotes from string: '{}' with prefix: '{}', suffix: '{}'", s, prefix, suffix);
        if (s.startsWith(prefix) && s.endsWith(suffix)) {
            s = s.substring(prefix.length(), s.length() - suffix.length());
            s = s.replace(quoted, suffix);
            logger.debug("Stripped quotes result: '{}'", s);
        } else {
            logger.debug("String '{}' does not match expected prefix/suffix pattern", s);
        }
        return s;
    }

    public static Node createCall(org.eclipse.daanse.mdx.parser.cccx.tree.CompoundId compoundId,
            Set<String> propertyWords) {
        logger.debug("Creating call for compound ID with size: {}, propertyWords size: {}", compoundId.size(),
                propertyWords != null ? propertyWords.size() : 0);

        // Member syntax: "foo.bar"
        // or property syntax: "foo.RESERVED_WORD.RESERVED_WORD"
        if (compoundId.size() > 1) {
            ObjectIdentifier objectIdentifier = (ObjectIdentifier) compoundId.get(compoundId.size() - 1);
            final String name = objectIdentifier instanceof NameObjectIdentifier nameObjectIdentifier
                    ? nameObjectIdentifier.name()
                    : null;
            logger.debug("Processing compound ID with last element: '{}', quoting: {}", name,
                    objectIdentifier.quoting());
            if (objectIdentifier.quoting().equals(ObjectIdentifier.Quoting.UNQUOTED) && name != null
                    && propertyWords.contains(name.toUpperCase())) {
                logger.debug("Property '{}' is a reserved word, creating function call", name);
                List<ObjectIdentifier> list = new ArrayList<>();
                for (int i = 0; i < compoundId.size() - 1; i++) {
                    if (compoundId.get(i) instanceof ObjectIdentifier mdxExpression) {
                        list.add(mdxExpression);
                    }
                }
                List<MdxExpression> l = getObjectIdentifierList(list, propertyWords);
                return new RightFunctionCall(new PlainPropertyOperationAtom(name), l);
            }
        }
        logger.debug("Returning compound ID as-is");
        return compoundId;
    }

    private static List<MdxExpression> getObjectIdentifierList(List<ObjectIdentifier> list, Set<String> propertyWords) {
        ObjectIdentifier last = list.getLast();
        final String name = last instanceof NameObjectIdentifier nameObjectIdentifier ? nameObjectIdentifier.name()
                : null;
        if (last.quoting().equals(ObjectIdentifier.Quoting.UNQUOTED) && name != null
                && propertyWords.contains(name.toUpperCase())) {
            List<ObjectIdentifier> ll = new ArrayList<>();
            for (int i = 0; i < list.size() - 1; i++) {
                ll.add(list.get(i));
            }
            List<MdxExpression> l = getObjectIdentifierList(ll, propertyWords);
            return List.of(new RightFunctionCall(new PlainPropertyOperationAtom(name), l));
        }
        return List.of(new CompoundId() {
            @Override
            public List<? extends ObjectIdentifier> objectIdentifiers() {
                return list;
            }
        });
    }

    public static Expression getExpression(Expression expression, Set<String> propertyWords) {
        logger.debug("Processing expression: {}", expression.getClass().getSimpleName());
        if (expression instanceof org.eclipse.daanse.mdx.model.api.expression.StringLiteral stringLiteral) {
            logger.debug("Processing string literal: '{}'", stringLiteral.value());
            try {
                String strippedValue = stripQuotes(stringLiteral.value(), "'", "'", "''");
                MdxParser parser = new MdxParser(strippedValue);
                parser.setPropertyWords(propertyWords);
                parser.Expression();
                Expression result = (Expression) parser.peekNode();
                logger.debug("Successfully parsed string literal expression");
                return result;
            } catch (Exception e) {
                logger.error("Failed to parse string literal expression: '{}'", stringLiteral.value(), e);
                e.printStackTrace();
            }
        }
        return expression;

    }

}
