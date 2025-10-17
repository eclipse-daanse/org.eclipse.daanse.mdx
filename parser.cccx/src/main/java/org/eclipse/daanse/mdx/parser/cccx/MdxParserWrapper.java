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
import org.eclipse.daanse.mdx.model.api.select.SelectCubeClause;
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
            MdxStatement result = (MdxStatement) delegate.MdxStatement();
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
            delegate.SelectQueryAsteriskClause();
            return (SelectQueryAsteriskClause) delegate.peekNode();
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
            delegate.SelectStatement();
            SelectStatement result = (SelectStatement) delegate.peekNode();
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
            delegate.SelectQueryAxesClause();
            return (SelectQueryAxesClause) delegate.peekNode();
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
            delegate.Expression();
            MdxExpression result = (MdxExpression) delegate.peekNode();
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
    public SelectCubeClause parseSelectCubeClause() throws MdxParserException {
        try {
            delegate.SelectCubeClause();
            return (SelectCubeClause) delegate.peekNode();
        } catch (Exception e) {
            throw new MdxParserException(e);
        } finally {
            dump();
        }
    }

    public SelectWithClause parseSelectWithClause() throws MdxParserException {
        try {
            delegate.SelectWithClause();
            return (SelectWithClause) delegate.peekNode();
        } catch (Exception e) {
            throw new MdxParserException(e);
        } finally {
            dump();
        }
    }

    public SelectQueryAxisClause parseSelectQueryAxisClause() throws MdxParserException {
        try {
            delegate.SelectQueryAxisClause();
            return (SelectQueryAxisClause) delegate.peekNode();
        } catch (Exception e) {
            throw new MdxParserException(e);
        } finally {
            dump();
        }
    }

    public Optional<SelectSlicerAxisClause> parseSelectSlicerAxisClause() throws MdxParserException {
        try {
            delegate.SelectSlicerAxisClause();
            return Optional.of((SelectSlicerAxisClause) delegate.peekNode());
        } catch (Exception e) {
            throw new MdxParserException(e);
        } finally {
            dump();
        }
    }

    public SelectCellPropertyListClause parseSelectCellPropertyListClause() throws MdxParserException {
        try {
            delegate.SelectCellPropertyListClause();
            return (SelectCellPropertyListClause) delegate.peekNode();
        } catch (Exception e) {
            throw new MdxParserException(e);
        } finally {
            dump();
        }
    }

    public DrillthroughStatement parseDrillthroughStatement() throws MdxParserException {
        try {
            delegate.DrillthroughStatement();
            return (DrillthroughStatement) delegate.peekNode();
        } catch (Exception e) {
            throw new MdxParserException(e);
        } finally {
            dump();
        }
    }

    public ExplainStatement parseExplainStatement() throws MdxParserException {
        try {
            delegate.ExplainStatement();
            return (ExplainStatement) delegate.peekNode();
        } catch (Exception e) {
            throw new MdxParserException(e);
        } finally {
            dump();
        }
    }

    public List<? extends ReturnItem> parseReturnItems() throws MdxParserException {
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
            delegate.MemberPropertyDefinition();
            return (MemberPropertyDefinition) delegate.peekNode();
        } catch (Exception e) {
            throw new MdxParserException(e);
        } finally {
            dump();
        }
    }

    public SelectDimensionPropertyListClause parseSelectDimensionPropertyListClause() throws MdxParserException {
        try {
            delegate.SelectDimensionPropertyListClause();
            return (SelectDimensionPropertyListClause) delegate.peekNode();
        } catch (Exception e) {
            throw new MdxParserException(e);
        } finally {
            dump();
        }
    }

    public RefreshStatement parseRefreshStatement() throws MdxParserException {
        try {
            delegate.RefreshStatement();
            return (RefreshStatement) delegate.peekNode();

        } catch (Exception e) {
            throw new MdxParserException(e);
        } finally {
            dump();
        }
    }

    public UpdateStatement parseUpdateStatement() throws MdxParserException {
        try {
            delegate.UpdateStatement();
            return (UpdateStatement) delegate.peekNode();

        } catch (Exception e) {
            throw new MdxParserException(e);
        } finally {
            dump();
        }
    }

    public DMVStatement parseDMVStatement() throws MdxParserException {
        try {
            delegate.DMVStatement();
            return (DMVStatement) delegate.peekNode();
        } catch (Exception e) {
            throw new MdxParserException(e);
        } finally {
            dump();
        }
    }
}
