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
package org.eclipse.daanse.mdx.parser.tck;

import static org.assertj.core.api.Assertions.assertThat;
import static org.eclipse.daanse.mdx.parser.tck.CubeTest.propertyWords;
import static org.eclipse.daanse.mdx.parser.tck.MdxTestUtils.checkCube;
import static org.eclipse.daanse.mdx.parser.tck.MdxTestUtils.checkNameObjectIdentifiers;

import java.util.List;

import org.eclipse.daanse.mdx.model.api.CreateMemberStatement;
import org.eclipse.daanse.mdx.model.api.MdxStatement;
import org.eclipse.daanse.mdx.model.api.expression.CallExpression;
import org.eclipse.daanse.mdx.model.api.expression.NameObjectIdentifier;
import org.eclipse.daanse.mdx.model.api.expression.NumericLiteral;
import org.eclipse.daanse.mdx.model.api.expression.ObjectIdentifier.Quoting;
import org.eclipse.daanse.mdx.model.api.expression.StringLiteral;
import org.eclipse.daanse.mdx.model.api.expression.operation.InfixOperationAtom;
import org.eclipse.daanse.mdx.model.api.select.MemberPropertyDefinition;
import org.eclipse.daanse.mdx.parser.api.MdxParserException;
import org.eclipse.daanse.mdx.parser.api.MdxParserProvider;
import org.junit.jupiter.api.Test;
import org.osgi.service.component.annotations.RequireServiceComponentRuntime;
import org.osgi.test.common.annotation.InjectService;

/**
 * CREATE MEMBER as SSAS takes it in an XMLA Execute (Adventure Works). The first segment after MEMBER is always the
 * cube, the body holds the member name without it.
 */
@RequireServiceComponentRuntime
class CreateMemberStatementTest {

    private static CreateMemberStatement parse(MdxParserProvider provider, String mdx) throws MdxParserException {
        MdxStatement statement = provider.newParser(mdx, propertyWords).parseMdxStatement();
        assertThat(statement).isInstanceOf(CreateMemberStatement.class);
        return (CreateMemberStatement) statement;
    }

    @Test
    void withProperties(@InjectService MdxParserProvider mdxParserProvider) throws MdxParserException {
        CreateMemberStatement statement = parse(mdxParserProvider, """
                CREATE MEMBER [Adventure Works].[Measures].[Twice] AS [Measures].[Internet Sales Amount] * 2,
                FORMAT_STRING = 'Currency', VISIBLE = 1""");
        assertThat(statement.session()).isFalse();
        assertThat(statement.calculated()).isFalse();
        checkCube(statement.cube(), "Adventure Works");

        List<? extends org.eclipse.daanse.mdx.model.api.expression.ObjectIdentifier> name = statement.body()
                .compoundId().objectIdentifiers();
        assertThat(name).hasSize(2);
        checkNameObjectIdentifiers(name, 0, "Measures", Quoting.QUOTED);
        checkNameObjectIdentifiers(name, 1, "Twice", Quoting.QUOTED);

        assertThat(statement.body().expression()).isInstanceOf(CallExpression.class);
        assertThat(((CallExpression) statement.body().expression()).operationAtom())
                .isEqualTo(new InfixOperationAtom("*"));

        List<? extends MemberPropertyDefinition> properties = statement.body().memberPropertyDefinitions();
        assertThat(properties).hasSize(2);
        assertThat(((NameObjectIdentifier) properties.get(0).objectIdentifier()).name()).isEqualTo("FORMAT_STRING");
        assertThat(properties.get(0).expression()).isInstanceOf(StringLiteral.class);
        assertThat(((StringLiteral) properties.get(0).expression()).value()).isEqualTo("Currency");
        assertThat(((NameObjectIdentifier) properties.get(1).objectIdentifier()).name()).isEqualTo("VISIBLE");
        assertThat(properties.get(1).expression()).isInstanceOf(NumericLiteral.class);
    }

    @Test
    void sessionCalculated(@InjectService MdxParserProvider mdxParserProvider) throws MdxParserException {
        CreateMemberStatement statement = parse(mdxParserProvider, """
                CREATE SESSION CALCULATED MEMBER [Adventure Works].[Product].[Category].[All Products].[Bikes And Clothing]
                AS [Product].[Category].&[1] + [Product].[Category].&[3]""");
        assertThat(statement.session()).isTrue();
        assertThat(statement.calculated()).isTrue();
        checkCube(statement.cube(), "Adventure Works");

        var name = statement.body().compoundId().objectIdentifiers();
        assertThat(name).hasSize(4);
        checkNameObjectIdentifiers(name, 0, "Product", Quoting.QUOTED);
        checkNameObjectIdentifiers(name, 3, "Bikes And Clothing", Quoting.QUOTED);
        assertThat(((CallExpression) statement.body().expression()).operationAtom())
                .isEqualTo(new InfixOperationAtom("+"));
        assertThat(statement.body().memberPropertyDefinitions()).isEmpty();
    }

    @Test
    void quotedFormula(@InjectService MdxParserProvider mdxParserProvider) throws MdxParserException {
        CreateMemberStatement statement = parse(mdxParserProvider,
                "CREATE MEMBER [Adventure Works].[Measures].[Quoted] AS '[Measures].[Internet Sales Amount] / 2'");
        // the quoted text is the formula, not a string
        assertThat(statement.body().expression()).isInstanceOf(CallExpression.class);
        assertThat(((CallExpression) statement.body().expression()).operationAtom())
                .isEqualTo(new InfixOperationAtom("/"));
    }

    @Test
    void firstSegmentIsTheCube(@InjectService MdxParserProvider mdxParserProvider) throws MdxParserException {
        // SSAS reads [Measures] as the cube here and then refuses it; the parser only reads it
        CreateMemberStatement statement = parse(mdxParserProvider, "CREATE MEMBER [Measures].[No Cube] AS 2");
        checkCube(statement.cube(), "Measures");
        var name = statement.body().compoundId().objectIdentifiers();
        assertThat(name).hasSize(1);
        checkNameObjectIdentifiers(name, 0, "No Cube", Quoting.QUOTED);
        assertThat(statement.body().expression()).isInstanceOf(NumericLiteral.class);
    }

    @Test
    void caseOfTheWords(@InjectService MdxParserProvider mdxParserProvider) throws MdxParserException {
        CreateMemberStatement statement = parse(mdxParserProvider,
                "create session calculated member [AW].[Measures].[x] as 1");
        assertThat(statement.session()).isTrue();
        assertThat(statement.calculated()).isTrue();
    }

    @Test
    void parseCreateMemberStatement(@InjectService MdxParserProvider mdxParserProvider) throws MdxParserException {
        CreateMemberStatement statement = mdxParserProvider
                .newParser("CREATE MEMBER CURRENTCUBE.[Measures].[x] AS 1", propertyWords)
                .parseCreateMemberStatement();
        assertThat(statement.cube()).isInstanceOf(org.eclipse.daanse.mdx.model.api.CubeReference.Current.class);
    }
}
