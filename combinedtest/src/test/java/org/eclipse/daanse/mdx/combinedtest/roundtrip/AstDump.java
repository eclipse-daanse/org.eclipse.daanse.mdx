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
package org.eclipse.daanse.mdx.combinedtest.roundtrip;

import java.util.List;
import java.util.Locale;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.eclipse.daanse.mdx.model.api.DrillthroughStatement;
import org.eclipse.daanse.mdx.model.api.ExplainStatement;
import org.eclipse.daanse.mdx.model.api.MdxStatement;
import org.eclipse.daanse.mdx.model.api.RefreshStatement;
import org.eclipse.daanse.mdx.model.api.SelectStatement;
import org.eclipse.daanse.mdx.model.api.TransactionStatement;
import org.eclipse.daanse.mdx.model.api.UpdateStatement;
import org.eclipse.daanse.mdx.model.api.expression.CallExpression;
import org.eclipse.daanse.mdx.model.api.expression.CompoundId;
import org.eclipse.daanse.mdx.model.api.expression.KeyObjectIdentifier;
import org.eclipse.daanse.mdx.model.api.expression.MdxExpression;
import org.eclipse.daanse.mdx.model.api.expression.NameObjectIdentifier;
import org.eclipse.daanse.mdx.model.api.expression.NullLiteral;
import org.eclipse.daanse.mdx.model.api.expression.NumericLiteral;
import org.eclipse.daanse.mdx.model.api.expression.ObjectIdentifier;
import org.eclipse.daanse.mdx.model.api.expression.StringLiteral;
import org.eclipse.daanse.mdx.model.api.expression.SymbolLiteral;
import org.eclipse.daanse.mdx.model.api.expression.operation.CastOperationAtom;
import org.eclipse.daanse.mdx.model.api.select.Axis;
import org.eclipse.daanse.mdx.model.api.select.CreateMemberBodyClause;
import org.eclipse.daanse.mdx.model.api.select.CreateSetBodyClause;
import org.eclipse.daanse.mdx.model.api.select.MemberPropertyDefinition;
import org.eclipse.daanse.mdx.model.api.select.SelectCubeClause;
import org.eclipse.daanse.mdx.model.api.select.SelectCubeClauseName;
import org.eclipse.daanse.mdx.model.api.select.SelectCubeClauseSubStatement;
import org.eclipse.daanse.mdx.model.api.select.SelectDimensionPropertyListClause;
import org.eclipse.daanse.mdx.model.api.select.SelectQueryAsteriskClause;
import org.eclipse.daanse.mdx.model.api.select.SelectQueryAxesClause;
import org.eclipse.daanse.mdx.model.api.select.SelectQueryAxisClause;
import org.eclipse.daanse.mdx.model.api.select.SelectQueryClause;
import org.eclipse.daanse.mdx.model.api.select.SelectQueryEmptyClause;
import org.eclipse.daanse.mdx.model.api.select.SelectWithClause;
import org.eclipse.daanse.mdx.model.api.select.UpdateClause;

/**
 * Renders a model.api tree as a parser-neutral s-expression. The parser.cccx
 * nodes have no {@code equals}, so two trees are compared through their dump.
 * String values are length-prefixed, a value can therefore never imitate
 * structure.
 */
final class AstDump {

    private AstDump() {
    }

    static String dump(MdxStatement statement) {
        return switch (statement) {
        case SelectStatement s -> "(select with=" + list(s.selectWithClauses(), AstDump::with) + " query="
                + query(s.selectQueryClause()) + " from=" + cube(s.selectCubeClause()) + " where="
                + s.selectSlicerAxisClause().map(w -> dump(w.expression())).orElse("-") + " cellprops="
                + s.selectCellPropertyListClause()
                        .map(c -> c.cell() + list(c.properties(), AstDump::str))
                        .orElse("-")
                + ")";
        case DrillthroughStatement s -> "(drillthrough maxrows=" + s.maxRows().map(String::valueOf).orElse("-")
                + " firstrowset=" + s.firstRowSet().map(String::valueOf).orElse("-") + " "
                + dump(s.selectStatement()) + " return=" + list(s.returnItems(), r -> dump(r.compoundId())) + ")";
        case ExplainStatement s -> "(explain " + dump(s.mdxStatement()) + ")";
        case RefreshStatement s -> "(refresh " + dump(s.cubeName()) + ")";
        case UpdateStatement s -> "(update " + dump(s.cubeName()) + " " + list(s.updateClauses(), AstDump::update)
                + ")";
        case TransactionStatement s -> "(transaction " + s.kind() + ")";
        };
    }

    static String dump(MdxExpression expression) {
        // the parser.cccx id token is identifier, compound id and symbol at once:
        // identifier first
        if (expression instanceof CallExpression c && c.operationAtom() instanceof CastOperationAtom
                && c.expressions().size() == 2 && c.expressions().get(1) instanceof SymbolLiteral type) {
            // the type of a cast is a symbol, whatever else the node may be
            return "(cast " + dump(c.expressions().get(0)) + " (symbol" + str(type.value()) + "))";
        }
        if (expression instanceof CallExpression c) {
            return "(call " + c.operationAtom().getClass().getSimpleName() + str(c.operationAtom().name()) + " "
                    + list(c.expressions(), AstDump::dump) + ")";
        }
        if (expression instanceof NameObjectIdentifier n) {
            return "(id " + n.quoting() + str(n.name()) + ")";
        }
        if (expression instanceof KeyObjectIdentifier k) {
            return "(key " + list(k.nameObjectIdentifiers(), AstDump::dump) + ")";
        }
        if (expression instanceof CompoundId c && c.objectIdentifiers().size() == 1) {
            // parser.cccx has no compound id of one part
            return dump((MdxExpression) c.objectIdentifiers().get(0));
        }
        if (expression instanceof CompoundId c) {
            return "(compound " + list(c.objectIdentifiers(), AstDump::dump) + ")";
        }
        if (expression instanceof StringLiteral s) {
            return "(string" + str(s.value()) + ")";
        }
        if (expression instanceof SymbolLiteral s) {
            return "(symbol" + str(s.value()) + ")";
        }
        if (expression instanceof NumericLiteral n) {
            return "(number " + n.value().stripTrailingZeros().toPlainString() + ")";
        }
        if (expression instanceof NullLiteral) {
            return "(null)";
        }
        throw new IllegalArgumentException("unknown expression " + expression.getClass());
    }

    private static String with(SelectWithClause clause) {
        return switch (clause) {
        case CreateMemberBodyClause m -> "(member " + dump(m.compoundId()) + " " + dump(m.expression()) + " props="
                + list(m.memberPropertyDefinitions(), AstDump::property) + ")";
        case CreateSetBodyClause s -> "(set " + dump(s.compoundId()) + " " + dump(s.expression()) + ")";
        default -> "(" + clause.getClass().getSimpleName() + ")";
        };
    }

    private static String property(MemberPropertyDefinition definition) {
        return "(prop " + dump((MdxExpression) definition.objectIdentifier()) + " " + dump(definition.expression())
                + ")";
    }

    private static String query(SelectQueryClause clause) {
        return switch (clause) {
        case SelectQueryAsteriskClause _ -> "*";
        case SelectQueryEmptyClause _ -> "()";
        case SelectQueryAxesClause a -> list(a.selectQueryAxisClauses(), AstDump::axis);
        };
    }

    private static String axis(SelectQueryAxisClause clause) {
        SelectDimensionPropertyListClause properties = clause.selectDimensionPropertyListClause();
        Axis axis = clause.axis();
        return "(axis " + axis.ordinal() + " nonempty=" + clause.nonEmpty() + " " + dump(clause.expression())
                + " dimprops=" + (properties == null || properties.properties() == null ? "-"
                        : list(properties.properties(), AstDump::dump))
                + ")";
    }

    private static String cube(SelectCubeClause clause) {
        return switch (clause) {
        case SelectCubeClauseName n -> dump(n.cubeName());
        case SelectCubeClauseSubStatement s -> "(subselect query=" + query(s.selectQueryClause()) + " from="
                + cube(s.selectCubeClause()) + " where="
                + s.selectSlicerAxisClause().map(w -> dump(w.expression())).orElse("-") + ")";
        };
    }

    private static String update(UpdateClause clause) {
        return "(set " + dump(clause.tupleExp()) + " = " + dump(clause.valueExp()) + " " + clause.allocation() + " by="
                + clause.weight().map(AstDump::dump).orElse("-") + ")";
    }

    private static String dump(ObjectIdentifier identifier) {
        return dump((MdxExpression) identifier);
    }

    private static String str(String value) {
        return value == null ? "<null>" : String.format(Locale.ROOT, "<%d:%s>", value.length(), value);
    }

    private static <T> String list(List<? extends T> list, Function<T, String> f) {
        return list.stream().map(f).collect(Collectors.joining(" ", "[", "]"));
    }
}
