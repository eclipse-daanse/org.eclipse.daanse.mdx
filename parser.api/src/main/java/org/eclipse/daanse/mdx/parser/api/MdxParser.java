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
package org.eclipse.daanse.mdx.parser.api;

import java.util.List;
import java.util.Optional;

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

public interface MdxParser {

    MdxStatement parseMdxStatement() throws MdxParserException;

    SelectStatement parseSelectStatement() throws MdxParserException;

    SelectQueryAsteriskClause parseSelectQueryAsteriskClause() throws MdxParserException;

    SelectQueryAxesClause parseSelectQueryAxesClause() throws MdxParserException;

    MdxExpression parseExpression() throws MdxParserException;

    SelectSubcubeClause parseSelectSubcubeClause() throws MdxParserException;

    DMVStatement parseDMVStatement() throws MdxParserException;

    DrillthroughStatement parseDrillthroughStatement() throws MdxParserException;

    ExplainStatement parseExplainStatement() throws MdxParserException;

    RefreshStatement parseRefreshStatement() throws MdxParserException;

    SelectWithClause parseSelectWithClause() throws MdxParserException;

    MemberPropertyDefinition parseMemberPropertyDefinition() throws MdxParserException;

    List<? extends ReturnItem> parseReturnItems() throws MdxParserException;

    SelectCellPropertyListClause parseSelectCellPropertyListClause() throws MdxParserException;

    SelectDimensionPropertyListClause parseSelectDimensionPropertyListClause() throws MdxParserException;

    SelectQueryAxisClause parseSelectQueryAxisClause() throws MdxParserException;

    Optional<SelectSlicerAxisClause> parseSelectSlicerAxisClause() throws MdxParserException;

    UpdateStatement parseUpdateStatement() throws MdxParserException;
}
