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
            throw new MdxParserException("Failed to create MdxParser delegate", e);
        }
    }

    @Override
    public MdxStatement parseMdxStatement() throws MdxParserException {
        logger.debug("Parsing MDX statement");
        try {
            MdxStatement result = delegate.parseMdxStatement();
            logger.debug("Successfully parsed MDX statement: {}", result.getClass().getSimpleName());
            return result;

        } catch (ParseException pe) {
            logger.error("Failed to parse MDX statement", pe);
            throw toMdxParserException(pe);
        } catch (Exception e) {
            logger.error("Failed to parse MDX statement", e);
            throw new MdxParserException(e);
        } finally {
            dump();
        }

    }

    /** ParseException may carry no token (message-only constructor). */
    private static MdxParserException toMdxParserException(ParseException pe) {
        if (pe.getToken() != null) {
            return new MdxParserException(pe.getMessage(), pe, pe.getToken().getBeginLine(),
                    pe.getToken().getBeginColumn());
        }
        return new MdxParserException(pe.getMessage(), pe);
    }

    private void dump() {
        // Node.dump() writes the whole tree to System.out, so it has to be gated on the
        // level like the message above it. Ungated, every parse printed its AST: ~177k
        // lines per TCK test group, unreachable by any logging configuration.
        if (!logger.isTraceEnabled()) {
            return;
        }
        Node root = delegate.rootNode();
        if (root != null) {
            logger.trace("Dumping parser AST");
            root.dump();
        }
    }

    @Override
    public SelectQueryAsteriskClause parseSelectQueryAsteriskClause() throws MdxParserException {
        return parse("Select Query Asterisk Clause", delegate::parseSelectQueryAsteriskClause);
    }

    @Override
    public SelectStatement parseSelectStatement() throws MdxParserException {
        return parse("SELECT statement", delegate::parseSelectStatement);
    }

    @Override
    public SelectQueryAxesClause parseSelectQueryAxesClause() throws MdxParserException {
        return parse("Select Query Axes Clause", delegate::parseSelectQueryAxesClause);
    }

    @Override
    public MdxExpression parseExpression() throws MdxParserException {
        return parse("MDX expression", delegate::parseExpression);
    }

    @Override
    public SelectCubeClause parseSelectCubeClause() throws MdxParserException {
        return parse("Select Cube Clause", delegate::parseSelectCubeClause);
    }

    @Override
    public SelectWithClause parseSelectWithClause() throws MdxParserException {
        return parse("Select With Clause", delegate::parseSelectWithClause);
    }

    @Override
    public SelectQueryAxisClause parseSelectQueryAxisClause() throws MdxParserException {
        return parse("Select Query Axis Clause", delegate::parseSelectQueryAxisClause);
    }

    @Override
    public Optional<SelectSlicerAxisClause> parseSelectSlicerAxisClause() throws MdxParserException {
        return parse("Select Slicer Axis Clause", delegate::parseSelectSlicerAxisClause);
    }

    @Override
    public SelectCellPropertyListClause parseSelectCellPropertyListClause() throws MdxParserException {
        return parse("Select Cell Property List Clause", delegate::parseSelectCellPropertyListClause);
    }

    @Override
    public DrillthroughStatement parseDrillthroughStatement() throws MdxParserException {
        return parse("Drillthrough Statement", delegate::parseDrillthroughStatement);
    }

    @Override
    public ExplainStatement parseExplainStatement() throws MdxParserException {
        return parse("Explain Statement", delegate::parseExplainStatement);
    }

    @Override
    public List<? extends ReturnItem> parseReturnItems() throws MdxParserException {
        return parse("Return Items", delegate::parseReturnItems);
    }

    @Override
    public MemberPropertyDefinition parseMemberPropertyDefinition() throws MdxParserException {
        return parse("Member Property Definition", delegate::parseMemberPropertyDefinition);
    }

    @Override
    public SelectDimensionPropertyListClause parseSelectDimensionPropertyListClause() throws MdxParserException {
        return parse("Select Dimension PropertyList Clause", delegate::parseSelectDimensionPropertyListClause);
    }

    @Override
    public RefreshStatement parseRefreshStatement() throws MdxParserException {
        return parse("Refresh Statement", delegate::parseRefreshStatement);
    }

    @Override
    public UpdateStatement parseUpdateStatement() throws MdxParserException {
        return parse("Update Statement", delegate::parseUpdateStatement);
    }

    private <T> T parse(String what, ParseAction<T> action) throws MdxParserException {
        try {
            logger.debug("Parsing {}", what);
            T result = action.run();
            logger.debug("Successfully parsed " + what);
            return result;
        } catch (ParseException pe) {
            logger.error("Failed to parse  " + what, pe);
            throw toMdxParserException(pe);
        } catch (Exception e) {
            logger.error("Failed to parse  " + what, e);
            throw new MdxParserException("Failed to parse " + what, e);
        } finally {
            dump();
        }
    }
}
