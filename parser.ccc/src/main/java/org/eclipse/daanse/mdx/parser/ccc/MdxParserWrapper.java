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
package org.eclipse.daanse.mdx.parser.ccc;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.eclipse.daanse.mdx.model.api.DMVStatement;
import org.eclipse.daanse.mdx.model.api.DrillthroughStatement;
import org.eclipse.daanse.mdx.model.api.ExplainStatement;
import org.eclipse.daanse.mdx.model.api.MdxStatement;
import org.eclipse.daanse.mdx.model.api.RefreshStatement;
import org.eclipse.daanse.mdx.model.api.ReturnItem;
import org.eclipse.daanse.mdx.model.api.SelectStatement;
import org.eclipse.daanse.mdx.model.api.UpdateStatement;
import org.eclipse.daanse.mdx.model.api.expression.MdxExpression;
import org.eclipse.daanse.mdx.model.api.select.MemberPropertyDefinition;
import org.eclipse.daanse.mdx.model.api.select.SelectCellPropertyListClause;
import org.eclipse.daanse.mdx.model.api.select.SelectDimensionPropertyListClause;
import org.eclipse.daanse.mdx.model.api.select.SelectQueryAsteriskClause;
import org.eclipse.daanse.mdx.model.api.select.SelectQueryAxesClause;
import org.eclipse.daanse.mdx.model.api.select.SelectQueryAxisClause;
import org.eclipse.daanse.mdx.model.api.select.SelectSlicerAxisClause;
import org.eclipse.daanse.mdx.model.api.select.SelectSubcubeClause;
import org.eclipse.daanse.mdx.model.api.select.SelectWithClause;
import org.eclipse.daanse.mdx.parser.api.MdxParserException;

public class MdxParserWrapper implements org.eclipse.daanse.mdx.parser.api.MdxParser {

    private static final Logger logger = LoggerFactory.getLogger(MdxParserWrapper.class);
    private MdxParser delegate;

    public MdxParserWrapper(CharSequence mdx, Set<String> propertyWords) throws MdxParserException {
        logger.debug("Creating MdxParserWrapper with mdx length: {}, propertyWords size: {}",
                mdx != null ? mdx.length() : 0, propertyWords != null ? propertyWords.size() : 0);

        if (mdx == null) {
            logger.error("MDX statement is null");
            throw new MdxParserException("statement must not be null");
        } else if (mdx.length() == 0) {
            logger.error("MDX statement is empty");
            throw new MdxParserException("statement must not be empty");
        }
        try {
            delegate = new MdxParser(mdx);
            delegate.setPropertyWords(propertyWords);
            logger.debug("MdxParserWrapper created successfully");
        } catch (Exception e) {
            logger.error("Failed to create MdxParser delegate", e);
            throw new MdxParserException("statement must not be empty");
        }
    }

    @Override
    public MdxStatement parseMdxStatement() throws MdxParserException {
        logger.debug("Parsing MDX statement");
        try {
            MdxStatement result = delegate.parseMdxStatement();
            logger.debug("Successfully parsed MDX statement: {}", result.getClass().getSimpleName());
            return result;

        } catch (Exception e) {
            logger.error("Failed to parse MDX statement", e);
            throw new MdxParserException(e);
        } finally {
            dump();
        }

    }

    private void dump() {
        Node root = delegate.rootNode();
        if (root != null) {
            logger.trace("Dumping parser AST");
            root.dump();
        }
    }

    @Override
    public SelectQueryAsteriskClause parseSelectQueryAsteriskClause() throws MdxParserException {
        try {
            return delegate.parseSelectQueryAsteriskClause();

        } catch (Exception e) {
            throw new MdxParserException(e);
        } finally {
            dump();
        }

    }

    @Override
    public SelectStatement parseSelectStatement() throws MdxParserException {
        logger.debug("Parsing SELECT statement");
        try {
            SelectStatement result = delegate.parseSelectStatement();
            logger.debug("Successfully parsed SELECT statement");
            return result;

        } catch (Exception e) {
            logger.error("Failed to parse SELECT statement", e);
            throw new MdxParserException(e);
        } finally {
            dump();
        }
    }

    @Override
    public SelectQueryAxesClause parseSelectQueryAxesClause() throws MdxParserException {
        try {
            return delegate.parseSelectQueryAxesClause();

        } catch (Exception e) {
            throw new MdxParserException(e);
        } finally {
            dump();
        }
    }

    @Override
    public MdxExpression parseExpression() throws MdxParserException {
        logger.debug("Parsing MDX expression");
        try {
            MdxExpression result = delegate.parseExpression();
            logger.debug("Successfully parsed MDX expression: {}", result.getClass().getSimpleName());
            return result;

        } catch (Exception e) {
            logger.error("Failed to parse MDX expression", e);
            throw new MdxParserException(e);
        } finally {
            dump();
        }
    }

    @Override
    public SelectSubcubeClause parseSelectSubcubeClause() throws MdxParserException {
        try {
            return delegate.parseSelectSubcubeClause();

        } catch (Exception e) {
            throw new MdxParserException(e);
        } finally {
            dump();
        }
    }

    public SelectWithClause parseSelectWithClause() throws MdxParserException {
        try {
            return delegate.parseSelectWithClause();

        } catch (Exception e) {
            throw new MdxParserException(e);
        } finally {
            dump();
        }
    }

    public SelectQueryAxisClause parseSelectQueryAxisClause() throws MdxParserException {
        try {
            return delegate.parseSelectQueryAxisClause();

        } catch (Exception e) {
            throw new MdxParserException(e);
        } finally {
            dump();
        }
    }

    public Optional<SelectSlicerAxisClause> parseSelectSlicerAxisClause() throws MdxParserException {
        try {
            return delegate.parseSelectSlicerAxisClause();

        } catch (Exception e) {
            throw new MdxParserException(e);
        } finally {
            dump();
        }
    }

    public SelectCellPropertyListClause parseSelectCellPropertyListClause() throws MdxParserException {
        try {
            return delegate.parseSelectCellPropertyListClause();

        } catch (Exception e) {
            throw new MdxParserException(e);
        } finally {
            dump();
        }
    }

    public DrillthroughStatement parseDrillthroughStatement() throws MdxParserException {
        try {
            return delegate.parseDrillthroughStatement();

        } catch (Exception e) {
            throw new MdxParserException(e);
        } finally {
            dump();
        }
    }

    public ExplainStatement parseExplainStatement() throws MdxParserException {
        try {
            return delegate.parseExplainStatement();

        } catch (Exception e) {
            throw new MdxParserException(e);
        } finally {
            dump();
        }
    }

    public List<ReturnItem> parseReturnItems() throws MdxParserException {
        try {
            return delegate.parseReturnItems();

        } catch (Exception e) {
            throw new MdxParserException(e);
        } finally {
            dump();
        }
    }

    public MemberPropertyDefinition parseMemberPropertyDefinition() throws MdxParserException {
        try {
            return delegate.parseMemberPropertyDefinition();

        } catch (Exception e) {
            throw new MdxParserException(e);
        } finally {
            dump();
        }
    }

    public SelectDimensionPropertyListClause parseSelectDimensionPropertyListClause() throws MdxParserException {
        try {
            return delegate.parseSelectDimensionPropertyListClause();

        } catch (Exception e) {
            throw new MdxParserException(e);
        } finally {
            dump();
        }
    }

    public RefreshStatement parseRefreshStatement() throws MdxParserException {
        try {
            return delegate.parseRefreshStatement();

        } catch (Exception e) {
            throw new MdxParserException(e);
        } finally {
            dump();
        }
    }

    public UpdateStatement parseUpdateStatement() throws MdxParserException {
        try {
            return delegate.parseUpdateStatement();

        } catch (Exception e) {
            throw new MdxParserException(e);
        } finally {
            dump();
        }
    }

    @Override
    public DMVStatement parseDMVStatement() throws MdxParserException {
        try {
            return delegate.parseDMVStatement();

        } catch (Exception e) {
            throw new MdxParserException(e);
        } finally {
            dump();
        }
    }
}
