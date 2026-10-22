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

import org.eclipse.daanse.mdx.model.api.CreateMemberStatement;
import org.eclipse.daanse.mdx.model.api.CreateSetStatement;
import org.eclipse.daanse.mdx.model.api.DrillthroughStatement;
import org.eclipse.daanse.mdx.model.api.DropMemberStatement;
import org.eclipse.daanse.mdx.model.api.DropSetStatement;
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
        this(mdx, propertyWords, MdxParser.DEFAULT_MAX_NESTING);
    }

    /**
     * @param maxNesting how deep a statement may be nested, counted in nested
     *                   productions of the grammar; a value below 1 is the default
     */
    public MdxParserWrapper(CharSequence mdx, Set<String> propertyWords, int maxNesting)
            throws MdxParserException {
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
            delegate.setMaxNesting(maxNesting);
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
            logger.debug("Failed to parse MDX statement", pe);
            throw toMdxParserException(pe);
        } catch (Exception e) {
            logger.debug("Failed to parse MDX statement", e);
            throw new MdxParserException(e);
        } catch (StackOverflowError e) {
            logger.debug("Failed to parse, nested too deep");
            throw tooDeep(e);
        }

    }

    /** ParseException may carry no token (message-only constructor). */
    private static MdxParserException toMdxParserException(ParseException pe) {
        if (pe.getToken() != null) {
            return atToken(pe);
        }
        return new MdxParserException(pe.getMessage(), pe);
    }

    /**
     * At the end of the input the EOF token lies past the text: column 1, and with a final EOL added a line the
     * text does not have. The statement ends right after its last token, so that is where it is reported.
     */
    private static MdxParserException atToken(ParseException pe) {
        Token token = pe.getToken();
        String message = pe.getMessage();
        int line = token.getBeginLine();
        int column = token.getBeginColumn();
        Token last = token.getType() == Token.TokenType.EOF ? token.getPrevious() : null;
        if (last != null) {
            line = last.getEndLine();
            column = last.getEndColumn() + 1;
            message = message.replace("around) " + token.getLocation(),
                    "around) " + token.getInputSource() + ":" + line + ":" + column);
        }
        return new MdxParserException(message, pe, line, column);
    }

    /**
     * Every failure of a parse is a MdxParserException, also the stack that runs
     * out on input the nesting limit of the grammar did not stop.
     */
    private static MdxParserException tooDeep(StackOverflowError e) {
        // no cause: its stack trace is as long as the stack and says nothing
        return new MdxParserException("statement is nested too deep");
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

    @Override
    public CreateMemberStatement parseCreateMemberStatement() throws MdxParserException {
        return parse("Create Member Statement", delegate::parseCreateMemberStatement);
    }

    @Override
    public CreateSetStatement parseCreateSetStatement() throws MdxParserException {
        return parse("Create Set Statement", delegate::parseCreateSetStatement);
    }

    @Override
    public DropMemberStatement parseDropMemberStatement() throws MdxParserException {
        return parse("Drop Member Statement", delegate::parseDropMemberStatement);
    }

    @Override
    public DropSetStatement parseDropSetStatement() throws MdxParserException {
        return parse("Drop Set Statement", delegate::parseDropSetStatement);
    }

    private <T> T parse(String what, ParseAction<T> action) throws MdxParserException {
        try {
            logger.debug("Parsing {}", what);
            T result = action.run();
            logger.debug("Successfully parsed " + what);
            return result;
        } catch (ParseException pe) {
            logger.debug("Failed to parse  " + what, pe);
            throw toMdxParserException(pe);
        } catch (Exception e) {
            logger.debug("Failed to parse  " + what, e);
            throw new MdxParserException("Failed to parse " + what, e);
        } catch (StackOverflowError e) {
            logger.debug("Failed to parse, nested too deep");
            throw tooDeep(e);
        }
    }
}
