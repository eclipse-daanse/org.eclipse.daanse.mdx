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

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import java.util.Optional;
import java.util.Random;
import java.util.Set;

import org.eclipse.daanse.mdx.model.api.MdxStatement;
import org.eclipse.daanse.mdx.model.api.expression.MdxExpression;
import org.eclipse.daanse.mdx.model.api.expression.ObjectIdentifier.Quoting;
import org.eclipse.daanse.mdx.model.api.expression.operation.AmpersandQuotedPropertyOperationAtom;
import org.eclipse.daanse.mdx.model.api.expression.operation.CastOperationAtom;
import org.eclipse.daanse.mdx.model.api.expression.operation.FunctionOperationAtom;
import org.eclipse.daanse.mdx.model.api.expression.operation.QuotedPropertyOperationAtom;
import org.eclipse.daanse.mdx.model.record.SelectStatementR;
import org.eclipse.daanse.mdx.model.record.expression.CallExpressionR;
import org.eclipse.daanse.mdx.model.record.expression.CompoundIdR;
import org.eclipse.daanse.mdx.model.record.expression.KeyObjectIdentifierR;
import org.eclipse.daanse.mdx.model.record.expression.NameObjectIdentifierR;
import org.eclipse.daanse.mdx.model.record.expression.StringLiteralR;
import org.eclipse.daanse.mdx.model.record.expression.SymbolLiteralR;
import org.eclipse.daanse.mdx.model.record.select.AxisR;
import org.eclipse.daanse.mdx.model.record.select.CreateMemberBodyClauseR;
import org.eclipse.daanse.mdx.model.record.select.MemberPropertyDefinitionR;
import org.eclipse.daanse.mdx.model.record.select.SelectCubeClauseNameR;
import org.eclipse.daanse.mdx.model.record.select.SelectQueryAxesClauseR;
import org.eclipse.daanse.mdx.model.record.select.SelectQueryAxisClauseR;
import org.eclipse.daanse.mdx.model.record.select.SelectSlicerAxisClauseR;
import org.eclipse.daanse.mdx.parser.api.MdxParserException;
import org.eclipse.daanse.mdx.parser.api.MdxParserProvider;
import org.eclipse.daanse.mdx.parser.ccc.CCCMdxParserProvider;
import org.eclipse.daanse.mdx.parser.cccx.CCCXMdxParserProvider;
import org.eclipse.daanse.mdx.unparser.simple.SimpleUnparser;
import org.junit.jupiter.api.Test;

/**
 * Puts generated values into every slot of a statement that holds free text
 * and reads the unparsed text back with both parsers. The alphabet is made of
 * the characters that end a token or open another one. No carriage return: the
 * lexers read every line ending as a line feed, inside a literal too. The value
 * changes, it stays a value.
 */
class GeneratedValuesRoundTripTest {

    private static final String[] ALPHABET = { "\"", "\"\"", "'", "''", "[", "]", "]]", ",", ".", "&", "(", ")", "{",
            "}", "--", "/*", "*/", "//", "\n", "\t", " ", "=", "a", "B", "0", "_", "SELECT", " FROM ", "é" };

    private static final int CASES = 500;

    private final SimpleUnparser unparser = new SimpleUnparser();
    private final MdxParserProvider ccc = new CCCMdxParserProvider();
    private final MdxParserProvider cccx = new CCCXMdxParserProvider();

    @Test
    void generatedValuesStayValues() {
        Random random = new Random(20260918L);
        for (int i = 0; i < CASES; i++) {
            MdxStatement statement = statement(random);
            String text = unparser.unparseMdxStatement(statement).toString();
            String expected = AstDump.dump(statement);

            assertThat(AstDump.dump(parse(ccc, text))).as("ccc:\n%s", text).isEqualTo(expected);
            assertThat(AstDump.dump(parse(cccx, text))).as("cccx:\n%s", text).isEqualTo(expected);
        }
    }

    private static MdxStatement parse(MdxParserProvider provider, String text) {
        try {
            return provider.newParser(text, Set.of()).parseMdxStatement();
        } catch (MdxParserException e) {
            throw new AssertionError(provider.getClass().getSimpleName() + " does not parse:\n" + text, e);
        }
    }

    private static MdxStatement statement(Random random) {
        CompoundIdR member = new CompoundIdR(List.of(quoted(random), quoted(random)));
        MdxExpression call = new CallExpressionR(new FunctionOperationAtom("Foo"),
                List.of(new StringLiteralR(value(random)), new StringLiteralR(value(random))));

        CreateMemberBodyClauseR with = new CreateMemberBodyClauseR(member, new StringLiteralR(value(random)),
                List.of(new MemberPropertyDefinitionR(new StringLiteralR(value(random)),
                        new NameObjectIdentifierR("FORMAT_STRING", Quoting.UNQUOTED))));

        MdxExpression key = new CompoundIdR(List.of(quoted(random), new KeyObjectIdentifierR(
                List.of(quoted(random), new NameObjectIdentifierR("k" + random.nextInt(9), Quoting.UNQUOTED)))));
        MdxExpression quotedProperty = new CallExpressionR(new QuotedPropertyOperationAtom(name(random)),
                List.of(call));
        MdxExpression keyProperty = new CallExpressionR(new AmpersandQuotedPropertyOperationAtom(name(random)),
                List.of(call));
        MdxExpression cast = new CallExpressionR(new CastOperationAtom(),
                List.of(call, new SymbolLiteralR(symbol(random))));

        return new SelectStatementR(List.of(with),
                new SelectQueryAxesClauseR(List.of(new SelectQueryAxisClauseR(false, key, AxisR.COLUMNS_NAMED, null),
                        new SelectQueryAxisClauseR(false, quotedProperty, AxisR.ROWS_NAMED, null),
                        new SelectQueryAxisClauseR(false, keyProperty, AxisR.PAGES_NAMED, null))),
                new SelectCubeClauseNameR(quoted(random)), Optional.of(new SelectSlicerAxisClauseR(cast)),
                Optional.empty());
    }

    private static NameObjectIdentifierR quoted(Random random) {
        return new NameObjectIdentifierR(name(random), Quoting.QUOTED);
    }

    /** The identifier record takes no empty name. */
    private static String name(Random random) {
        String name = value(random);
        return name.isEmpty() ? "]" : name;
    }

    private static String symbol(Random random) {
        return "t" + value(random);
    }

    private static String value(Random random) {
        StringBuilder sb = new StringBuilder();
        int parts = random.nextInt(6);
        for (int i = 0; i < parts; i++) {
            sb.append(ALPHABET[random.nextInt(ALPHABET.length)]);
        }
        return sb.toString();
    }
}
