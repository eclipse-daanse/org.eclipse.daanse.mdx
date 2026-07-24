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
package org.eclipse.daanse.mdx.unparser.simple;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.eclipse.daanse.mdx.model.api.DMVStatement;
import org.eclipse.daanse.mdx.model.api.DrillthroughStatement;
import org.eclipse.daanse.mdx.model.api.ExplainStatement;
import org.eclipse.daanse.mdx.model.api.MdxStatement;
import org.eclipse.daanse.mdx.model.api.RefreshStatement;
import org.eclipse.daanse.mdx.model.api.ReturnItem;
import org.eclipse.daanse.mdx.model.api.SelectStatement;
import org.eclipse.daanse.mdx.model.api.UpdateStatement;
import org.eclipse.daanse.mdx.model.api.expression.CallExpression;
import org.eclipse.daanse.mdx.model.api.expression.CompoundId;
import org.eclipse.daanse.mdx.model.api.expression.KeyObjectIdentifier;
import org.eclipse.daanse.mdx.model.api.expression.Literal;
import org.eclipse.daanse.mdx.model.api.expression.MdxExpression;
import org.eclipse.daanse.mdx.model.api.expression.NameObjectIdentifier;
import org.eclipse.daanse.mdx.model.api.expression.NullLiteral;
import org.eclipse.daanse.mdx.model.api.expression.NumericLiteral;
import org.eclipse.daanse.mdx.model.api.expression.ObjectIdentifier;
import org.eclipse.daanse.mdx.model.api.expression.StringLiteral;
import org.eclipse.daanse.mdx.model.api.expression.SymbolLiteral;
import org.eclipse.daanse.mdx.model.api.expression.operation.AmpersandQuotedPropertyOperationAtom;
import org.eclipse.daanse.mdx.model.api.expression.operation.BracesOperationAtom;
import org.eclipse.daanse.mdx.model.api.expression.operation.CaseOperationAtom;
import org.eclipse.daanse.mdx.model.api.expression.operation.CastOperationAtom;
import org.eclipse.daanse.mdx.model.api.expression.operation.EmptyOperationAtom;
import org.eclipse.daanse.mdx.model.api.expression.operation.FunctionOperationAtom;
import org.eclipse.daanse.mdx.model.api.expression.operation.InfixOperationAtom;
import org.eclipse.daanse.mdx.model.api.expression.operation.InternalOperationAtom;
import org.eclipse.daanse.mdx.model.api.expression.operation.MethodOperationAtom;
import org.eclipse.daanse.mdx.model.api.expression.operation.ParenthesesOperationAtom;
import org.eclipse.daanse.mdx.model.api.expression.operation.PlainPropertyOperationAtom;
import org.eclipse.daanse.mdx.model.api.expression.operation.PostfixOperationAtom;
import org.eclipse.daanse.mdx.model.api.expression.operation.PrefixOperationAtom;
import org.eclipse.daanse.mdx.model.api.expression.operation.QuotedPropertyOperationAtom;
import org.eclipse.daanse.mdx.model.api.select.Axis;
import org.eclipse.daanse.mdx.model.api.select.CreateCellCalculationBodyClause;
import org.eclipse.daanse.mdx.model.api.select.CreateMemberBodyClause;
import org.eclipse.daanse.mdx.model.api.select.CreateSetBodyClause;
import org.eclipse.daanse.mdx.model.api.select.MeasureBodyClause;
import org.eclipse.daanse.mdx.model.api.select.MemberPropertyDefinition;
import org.eclipse.daanse.mdx.model.api.select.SelectCellPropertyListClause;
import org.eclipse.daanse.mdx.model.api.select.SelectCubeClause;
import org.eclipse.daanse.mdx.model.api.select.SelectCubeClauseName;
import org.eclipse.daanse.mdx.model.api.select.SelectCubeClauseSubStatement;
import org.eclipse.daanse.mdx.model.api.select.SelectDimensionPropertyListClause;
import org.eclipse.daanse.mdx.model.api.select.SelectQueryAsteriskClause;
import org.eclipse.daanse.mdx.model.api.select.SelectQueryAxesClause;
import org.eclipse.daanse.mdx.model.api.select.SelectQueryAxisClause;
import org.eclipse.daanse.mdx.model.api.select.SelectQueryClause;
import org.eclipse.daanse.mdx.model.api.select.SelectQueryEmptyClause;
import org.eclipse.daanse.mdx.model.api.select.SelectSlicerAxisClause;
import org.eclipse.daanse.mdx.model.api.select.SelectWithClause;
import org.eclipse.daanse.mdx.unparser.api.UnParser;
import org.osgi.service.component.annotations.Component;

@Component
public class SimpleUnparser implements UnParser {

    private static final String DELIMITER = "\r\n";


    public CharSequence unparseSelectStatement(SelectStatement selectStatement) {
        StringBuilder sb = new StringBuilder();
        if (!selectStatement.selectWithClauses().isEmpty()) {
            sb = sb.append("WITH ");
            sb = sb.append(unparseSelectWithClauses(selectStatement.selectWithClauses()));
            sb = sb.append(" ");
        }

        sb = sb.append("SELECT ");
        sb = sb.append(unparseSelectQueryClause(selectStatement.selectQueryClause()));
        sb = sb.append(" FROM ");
        sb = sb.append(unparseSelectCubeClause(selectStatement.selectCubeClause()));

        Optional<SelectSlicerAxisClause> ssac = selectStatement.selectSlicerAxisClause();
        if (ssac.isPresent()) {
            sb = sb.append(" ");// whitespace before WHERE
            sb.append(unparseSelectSlicerAxisClause(ssac.get()));
        }

        Optional<SelectCellPropertyListClause> ssplc = selectStatement.selectCellPropertyListClause();
        if (ssplc.isPresent()) {
            sb.append(" ");
            sb = sb.append(unparseSelectCellPropertyListClause(ssplc.get()));
        }

        return sb;

    }

    public CharSequence unparseSelectCellPropertyListClause(SelectCellPropertyListClause clause) {
        StringBuilder sb = new StringBuilder();

        if (clause.cell()) {

            sb.append("CELL ");
        }
        sb.append(unparseProperties(clause.properties()));
        return sb;
    }

    public CharSequence unparseProperties(List<String> propertyList) {
        StringBuilder sb = new StringBuilder();
        if (propertyList != null && !propertyList.isEmpty()) {
            sb.append("PROPERTIES ");

            String properties = propertyList.stream().collect(Collectors.joining("\r\n, "));
            sb.append(properties);
        }
        return sb;
    }

    public CharSequence unparseSelectSlicerAxisClause(SelectSlicerAxisClause clause) {
        StringBuilder sb = new StringBuilder("WHERE ");

        return sb.append(unparseExpression(clause.expression()));
    }

    public CharSequence unparseSelectCubeClause(SelectCubeClause clause) {

        if (clause instanceof SelectCubeClauseName sscn) {
            return unparseSelectCubeClauseName(sscn);
        }
        if (clause instanceof SelectCubeClauseSubStatement sscs) {
            return unparseSelectCubeClauseSubStatement(sscs);
        }

        return new StringBuilder();
    }

    public CharSequence unparseSelectCubeClauseSubStatement(SelectCubeClauseSubStatement clause) {
        StringBuilder sb = new StringBuilder();

        Optional<SelectSlicerAxisClause> sOptional = clause.selectSlicerAxisClause();

        sb.append(" ( ").append(DELIMITER);
        sb.append("  SELECT ").append(DELIMITER);
        sb.append(unparseSelectQueryClause(clause.selectQueryClause()));
        sb.append(" FROM ").append(DELIMITER);
        sb.append(unparseSelectCubeClause(clause.selectCubeClause()));

        if (sOptional.isPresent()) {

            sb.append(unparseSelectSlicerAxisClause(sOptional.get()));
        }
        sb.append(DELIMITER);
        sb.append(" ) ").append(DELIMITER);

        return sb;
    }

    public CharSequence unparseSelectCubeClauseName(SelectCubeClauseName clause) {

        return unparseNameObjectIdentifier(clause.cubeName());
    }

    private StringBuilder unparseNameObjectIdentifier(NameObjectIdentifier nameObjectIdentifier) {

        StringBuilder sb = new StringBuilder();
        switch (nameObjectIdentifier.quoting()) {
        case KEY -> sb.append("&").append(nameObjectIdentifier.name());

        case QUOTED -> sb.append("[").append(nameObjectIdentifier.name().replace("]", "]]")).append("]");

        case UNQUOTED -> sb.append(nameObjectIdentifier.name());

        }

        return sb;
    }

    public CharSequence unparseSelectQueryClause(SelectQueryClause clause) {

        if (clause instanceof SelectQueryAsteriskClause) {
            return unparseSelectQueryAsteriskClause();
        }
        if (clause instanceof SelectQueryAxesClause sqaxc) {
            return unparseSelectQueryAxesClause(sqaxc);

        }
        if (clause instanceof SelectQueryEmptyClause) {
            return unparseSelectQueryEmptyClause();

        }

        return "";
    }

    private CharSequence unparseSelectQueryEmptyClause() {
        return "";/* empty */
    }

    private CharSequence unparseSelectQueryAxesClause(SelectQueryAxesClause clause) {
        return clause.selectQueryAxisClauses().stream()
            .map(this::unparseSelectQueryAxisClause)
            .collect(StringBuilder::new, (sb, obj) -> {
                if (sb.length() > 0) sb.append(DELIMITER).append(",");
                sb.append(obj);
            }, StringBuilder::append);
    }

    public CharSequence unparseSelectQueryAxisClause(SelectQueryAxisClause clause) {
        StringBuilder sb = new StringBuilder();

        if (clause.nonEmpty()) {

            sb.append("NON EMPTY ");
        }
        sb.append(unparseExpression(clause.expression()));
        sb.append(" ON ");
        sb.append(unparseAxis(clause.axis()));

        return sb;
    }

    public CharSequence unparseExpression(MdxExpression expression) {

        if (expression instanceof CallExpression ce) {
            return unparseCallExpression(ce);
        }
        if (expression instanceof Literal l) {
            return unparseLiteral(l);
        }
        if (expression instanceof CompoundId cId) {
            return unparseCompoundId(cId);
        }
        if (expression instanceof ObjectIdentifier oi) {
            return unparseObjectIdentifier(oi);
        }

        return new StringBuilder();
    }

    private CharSequence unparseObjectIdentifier(ObjectIdentifier objectIdentifier) {

        if (objectIdentifier instanceof KeyObjectIdentifier koi) {

            return unparseKeyObjectIdentifier(koi);
        }
        if (objectIdentifier instanceof NameObjectIdentifier noi) {

            return unparseNameObjectIdentifier(noi);

        }
        return "";
    }

    private CharSequence unparseKeyObjectIdentifier(KeyObjectIdentifier koi) {
        StringBuilder sb = new StringBuilder();

        String s = koi.nameObjectIdentifiers().stream().map(this::unparseNameObjectIdentifier).map(Object::toString)
                .collect(Collectors.joining("&"));

        sb.append("&").append(s);
        return sb;
    }

    private CharSequence unparseCompoundId(CompoundId compoundId) {
        return compoundId.objectIdentifiers().stream()
            .map(this::unparseObjectIdentifier)
            .collect(
                StringBuilder::new,
                (sb, part) -> {
                    if (sb.length() > 0) sb.append('.');
                    sb.append(part);
                },
                StringBuilder::append
            );
    }

    private CharSequence unparseCompoundIds(List<? extends CompoundId> compoundIdList) {
        return compoundIdList.stream().map(this::unparseCompoundId).map(Object::toString)
                .collect(Collectors.joining(","));
    }

    private CharSequence unparseLiteral(Literal literal) {

        if (literal instanceof NullLiteral) {
            return unparseNullLiteral();
        }
        if (literal instanceof NumericLiteral numericLiteral) {
            return unparseNumericLiteral(numericLiteral);

        }
        if (literal instanceof StringLiteral stringLiteral) {
            return unparseStringLiteral(stringLiteral);

        }
        if (literal instanceof SymbolLiteral symbolLiteral) {
            return unparseSymbolLiteral(symbolLiteral);

        }

        return "";
    }

    private CharSequence unparseSymbolLiteral(SymbolLiteral symbolLiteral) {
        return new StringBuilder(symbolLiteral.value());
    }

    private CharSequence unparseStringLiteral(StringLiteral stringLiteral) {
        return new StringBuilder(stringLiteral.value());

    }

    private CharSequence unparseNumericLiteral(NumericLiteral numericLiteral) {
        return new StringBuilder(numericLiteral.value().toString());

    }

    private StringBuilder unparseNullLiteral() {
        return new StringBuilder("NULL");

    }

    private CharSequence unparseCallExpression(CallExpression callExpression) {
        StringBuilder sb = new StringBuilder();
        String name = callExpression.operationAtom().name();
        List<? extends MdxExpression> expressions = callExpression.expressions();
        CharSequence expressionText;
        String object = "";
        if (callExpression.operationAtom() instanceof MethodOperationAtom && !expressions.isEmpty()) {
            expressionText = unparseExpressions(expressions.subList(1, expressions.size()));
            object = unparseExpression(expressions.get(0)).toString();
        } else {
            expressionText = unparseExpressions(expressions);
        }
        switch (callExpression.operationAtom()) {
        case AmpersandQuotedPropertyOperationAtom _UNNAMED ->
            sb.append(expressionText).append(".[&").append(name).append("]");
        case BracesOperationAtom _UNNAMED -> sb.append("{").append(expressionText).append("}");
        case CastOperationAtom _UNNAMED -> sb.append("CAST(").append(expressionText.toString().replace(",", " AS ")).append(")");
        case CaseOperationAtom _UNNAMED -> {
            sb.append("CASE ");
            sb.append(unparseExpression(expressions.get(0)));
            int i = 0;
            boolean hasElse = false;
            if ((expressions.size() - 1) > 2 && (expressions.size() - 1) % 2 != 0) {
                hasElse = true;
            }
            for (int j = 1; j < expressions.size() - 1; j += 2) {
                sb.append(" WHEN ");
                sb.append(unparseExpression(expressions.get(j)));
                if (j + 1 < expressions.size()) {
                    sb.append(" THEN ");
                    sb.append(unparseExpression(expressions.get(j + 1)));
                }
            }
            if (hasElse) {
                sb.append(" ELSE ");
                sb.append(unparseExpression(expressions.get(expressions.size() - 1)));
            }
            sb.append(" END");
        }
        case EmptyOperationAtom _UNNAMED -> sb.append("");
        case FunctionOperationAtom _UNNAMED -> sb.append(name).append("(").append(expressionText).append(")");
        case InfixOperationAtom _UNNAMED -> {
            sb.append(unparseExpression(expressions.get(0)));
            sb.append(" ");
            sb.append(name);
            sb.append(" ");
            sb.append(unparseExpression(expressions.get(1)));
        }
        case InternalOperationAtom _UNNAMED -> sb.append("$").append(expressionText);
        case MethodOperationAtom _UNNAMED ->
            sb.append(object).append(".").append(name).append("(").append(expressionText).append(")");
        case ParenthesesOperationAtom _UNNAMED -> sb.append("(").append(expressionText).append(")");
        case PlainPropertyOperationAtom _UNNAMED -> sb.append(expressionText).append(".").append(name);
        case PostfixOperationAtom _UNNAMED -> sb.append(expressionText).append(" ").append(name);
        case PrefixOperationAtom _UNNAMED -> sb.append(name).append(" ").append(expressionText);
        case QuotedPropertyOperationAtom _UNNAMED -> sb.append(expressionText).append(".&").append(name).append("");

        }

        return sb;
    }

    private CharSequence unparseExpressions(List<? extends MdxExpression> expressions) {
        return expressions.stream()
            .map(this::unparseExpression)
            .collect(
                StringBuilder::new,
                (sb, part) -> {
                    if (sb.length() > 0) sb.append(',');
                    sb.append(part);
                },
                StringBuilder::append
            );
    }

    private CharSequence unparseSelectQueryAsteriskClause() {
        return "*";
    }

    public CharSequence unparseSelectWithClauses(List<? extends SelectWithClause> clauses) {
        return clauses.stream()
            .map(this::unparseSelectWithClause)
            .collect(
                StringBuilder::new,
                (sb, part) -> {
                    if (sb.length() > 0) sb.append(DELIMITER).append(" ");
                    sb.append(part);
                },
                StringBuilder::append
            );
    }

    public CharSequence unparseSelectWithClause(SelectWithClause clause) {
        if (clause instanceof CreateCellCalculationBodyClause cccbc) {
            return unparseCreateCellCalculationBodyClause(cccbc);
        } else if (clause instanceof CreateMemberBodyClause c) {
            return unparseCreateMemberBodyClause(c);
        } else if (clause instanceof CreateSetBodyClause c) {
            return unparseCreateSetBodyClause(c);
        } else if (clause instanceof MeasureBodyClause mbc) {
            return unparseMeasureBodyClause(mbc);
        }
        return "";

    }

    public CharSequence unparseCreateCellCalculationBodyClause(CreateCellCalculationBodyClause cccbc) {
        //TODO
        return "";

    }

    public CharSequence unparseCreateMemberBodyClause(CreateMemberBodyClause clause) {

        StringBuilder sb = new StringBuilder();
        sb.append("MEMBER ");

        sb.append(unparseCompoundId(clause.compoundId())).append(" AS ").append(unparseExpression(clause.expression()));

        if (!clause.memberPropertyDefinitions().isEmpty()) {
            sb.append(" ");

            String ret = clause.memberPropertyDefinitions().stream().map(this::unparseMemberPropertyDefinition)
                    .collect(Collectors.joining("\r\n,", ",\r\n ", ""));
            sb.append(ret);
        }

        return sb;

    }

    public CharSequence unparseMemberPropertyDefinition(MemberPropertyDefinition mpd) {

        StringBuilder sb = new StringBuilder();
        sb.append(unparseObjectIdentifier(mpd.objectIdentifier()));
        sb.append(" = ");
        sb.append(unparseExpression(mpd.expression()));
        return sb;
    }

    public CharSequence unparseCreateSetBodyClause(CreateSetBodyClause clause) {

        StringBuilder sb = new StringBuilder();
        sb.append("SET ");
        sb.append(unparseCompoundId(clause.compoundId())).append(" AS ").append(unparseExpression(clause.expression()));

        return sb;

    }

    public CharSequence unparseMeasureBodyClause(MeasureBodyClause mbc) {
        return new StringBuilder();

    }

    public CharSequence unparseDrillthroughStatement(DrillthroughStatement statement) {
        StringBuilder sb = new StringBuilder();
        sb.append("DRILLTHROUGH");

        statement.maxRows().ifPresent(maxRows ->
        sb.append(DELIMITER).append(" ").append("MAXROWS").append(" ").append(maxRows));

        statement.firstRowSet().ifPresent(firstRowSet -> sb.append(DELIMITER).append(" ").append("FIRSTROWSET").append(" ").append(firstRowSet));

        sb.append(DELIMITER).append(" ").append(unparseSelectStatement(statement.selectStatement()));

        if (statement.returnItems() != null && !statement.returnItems().isEmpty()) {
            sb.append(DELIMITER).append(" ").append(unparseReturnItems(statement.returnItems()));
        }

        return sb;
    }

    public CharSequence unparseReturnItems(List<? extends ReturnItem> returnItems) {
        StringBuilder sb = new StringBuilder();
        if (!returnItems.isEmpty()) {
            sb.append("RETURN ");
            sb.append(returnItems.stream().map(r -> unparseCompoundId(r.compoundId())).map(Object::toString)
                    .collect(Collectors.joining(",")));
        }
        return sb;
    }

    public CharSequence unparseExplainStatement(ExplainStatement statement) {
        StringBuilder sb = new StringBuilder();
        sb.append("EXPLAIN PLAN FOR");
        if (statement.mdxStatement() != null) {
            sb.append(DELIMITER).append(" ").append(unparseMdxStatement(statement.mdxStatement()));
        }
        return sb;
    }

    public CharSequence unparseDMVStatement(DMVStatement statement) {
        StringBuilder sb = new StringBuilder();
        sb.append("SELECT ").append(DELIMITER).append(" ");
        sb.append(unparseCompoundIds(statement.columns())).append(DELIMITER).append(" ");
        sb.append("FROM $SYSTEM.").append(unparseNameObjectIdentifier(statement.table()));
        if (statement.where() != null) {
            sb.append(DELIMITER).append(" ").append("WHERE ").append(unparseExpression(statement.where()));
        }
        return sb;
    }

    public CharSequence unparseRefreshStatement(RefreshStatement statement) {
        StringBuilder sb = new StringBuilder();
        if (statement.cubeName() != null) {
            sb.append("REFRESH CUBE ").append(unparseNameObjectIdentifier(statement.cubeName()));
        }
        return sb;
    }

    public CharSequence unparseUpdateStatement(UpdateStatement updateStatement) {
        StringBuilder sb = new StringBuilder();
        if (updateStatement.cubeName() != null) {
            sb.append("UPDATE CUBE ").append(unparseNameObjectIdentifier(updateStatement.cubeName()));
        }
        return sb;
    }

    public CharSequence unparseSelectDimensionPropertyListClause(SelectDimensionPropertyListClause clause) {
        StringBuilder sb = new StringBuilder();
        if (clause.properties() != null) {
            sb.append("DIMENSION");
            sb.append(DELIMITER).append(" ");
            sb.append("PROPERTIES ");
            sb.append(unparseCompoundIds(clause.properties()));
        }
        return sb;
    }

    @Override
    public CharSequence unparseMdxStatement(MdxStatement mdxStatement) {

        if (mdxStatement instanceof SelectStatement selectStatement) {
            return unparseSelectStatement(selectStatement);
        } else if (mdxStatement instanceof DrillthroughStatement drillthroughStatement) {
            return unparseDrillthroughStatement(drillthroughStatement);
        } else if (mdxStatement instanceof ExplainStatement explainStatement) {
            return unparseExplainStatement(explainStatement);
        } else if (mdxStatement instanceof DMVStatement dMVStatement) {
            return unparseDMVStatement(dMVStatement);
        } else if (mdxStatement instanceof RefreshStatement dMVStatement) {
            return unparseRefreshStatement(dMVStatement);
        } else if (mdxStatement instanceof UpdateStatement dMVStatement) {
            return unparseUpdateStatement(dMVStatement);
        }
        return "";
    }

    public CharSequence unparseAxis(Axis axis) {
        return new StringBuilder().append(axis.named() ? axis.name().toUpperCase() : axis.ordinal());

    }

}
