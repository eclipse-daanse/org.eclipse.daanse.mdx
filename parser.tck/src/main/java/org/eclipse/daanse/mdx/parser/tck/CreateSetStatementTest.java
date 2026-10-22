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

import org.eclipse.daanse.mdx.model.api.CreateSetStatement;
import org.eclipse.daanse.mdx.model.api.CubeReference;
import org.eclipse.daanse.mdx.model.api.MdxStatement;
import org.eclipse.daanse.mdx.model.api.SetEvaluation;
import org.eclipse.daanse.mdx.model.api.expression.CallExpression;
import org.eclipse.daanse.mdx.model.api.expression.NameObjectIdentifier;
import org.eclipse.daanse.mdx.model.api.expression.ObjectIdentifier.Quoting;
import org.eclipse.daanse.mdx.model.api.expression.StringLiteral;
import org.eclipse.daanse.mdx.model.api.expression.operation.BracesOperationAtom;
import org.eclipse.daanse.mdx.model.api.expression.operation.FunctionOperationAtom;
import org.eclipse.daanse.mdx.model.api.select.MemberPropertyDefinition;
import org.eclipse.daanse.mdx.parser.api.MdxParserException;
import org.eclipse.daanse.mdx.parser.api.MdxParserProvider;
import org.junit.jupiter.api.Test;
import org.osgi.service.component.annotations.RequireServiceComponentRuntime;
import org.osgi.test.common.annotation.InjectService;

/**
 * CREATE SET as SSAS takes it in an XMLA Execute (Adventure Works). The first segment after SET is always the cube,
 * the body holds the set name without it.
 */
@RequireServiceComponentRuntime
class CreateSetStatementTest {

    private static CreateSetStatement parse(MdxParserProvider provider, String mdx) throws MdxParserException {
        MdxStatement statement = provider.newParser(mdx, propertyWords).parseMdxStatement();
        assertThat(statement).isInstanceOf(CreateSetStatement.class);
        return (CreateSetStatement) statement;
    }

    @Test
    void session(@InjectService MdxParserProvider mdxParserProvider) throws MdxParserException {
        CreateSetStatement statement = parse(mdxParserProvider, """
                CREATE SESSION SET [Adventure Works].[Probe Set] AS
                {[Product].[Category].&[1], [Product].[Category].&[3]}""");
        assertThat(statement.session()).isTrue();
        assertThat(statement.evaluation()).isEmpty();
        assertThat(statement.hidden()).isFalse();
        checkCube(statement.cube(), "Adventure Works");
        var name = statement.body().compoundId().objectIdentifiers();
        assertThat(name).hasSize(1);
        checkNameObjectIdentifiers(name, 0, "Probe Set", Quoting.QUOTED);
        assertThat(((CallExpression) statement.body().expression()).operationAtom())
                .isEqualTo(new BracesOperationAtom());
        assertThat(((CallExpression) statement.body().expression()).expressions()).hasSize(2);
        assertThat(statement.body().memberPropertyDefinitions()).isEmpty();
    }

    @Test
    void dynamicWithProperties(@InjectService MdxParserProvider mdxParserProvider) throws MdxParserException {
        CreateSetStatement statement = parse(mdxParserProvider, """
                CREATE DYNAMIC SET [Adventure Works].[Two Best] AS
                TopCount([Product].[Category].[Category].Members, 2, [Measures].[Internet Sales Amount]),
                CAPTION = 'Two Best', DISPLAY_FOLDER = 'Probe'""");
        assertThat(statement.session()).isFalse();
        assertThat(statement.evaluation()).contains(SetEvaluation.DYNAMIC);
        assertThat(statement.hidden()).isFalse();
        assertThat(((CallExpression) statement.body().expression()).operationAtom())
                .isEqualTo(new FunctionOperationAtom("TopCount"));

        List<? extends MemberPropertyDefinition> properties = statement.body().memberPropertyDefinitions();
        assertThat(properties).hasSize(2);
        assertThat(((NameObjectIdentifier) properties.get(0).objectIdentifier()).name()).isEqualTo("CAPTION");
        assertThat(((StringLiteral) properties.get(0).expression()).value()).isEqualTo("Two Best");
        assertThat(((NameObjectIdentifier) properties.get(1).objectIdentifier()).name()).isEqualTo("DISPLAY_FOLDER");
        assertThat(((StringLiteral) properties.get(1).expression()).value()).isEqualTo("Probe");
    }

    @Test
    void staticSet(@InjectService MdxParserProvider mdxParserProvider) throws MdxParserException {
        CreateSetStatement statement = parse(mdxParserProvider, """
                CREATE STATIC SET [Adventure Works].[Static] AS
                Filter([Product].[Category].[Category].Members, [Measures].[Internet Sales Amount] > 0)""");
        assertThat(statement.evaluation()).contains(SetEvaluation.STATIC);
        assertThat(statement.hidden()).isFalse();
        // the name may be one of the words
        checkNameObjectIdentifiers(statement.body().compoundId().objectIdentifiers(), 0, "Static", Quoting.QUOTED);
    }

    @Test
    void hidden(@InjectService MdxParserProvider mdxParserProvider) throws MdxParserException {
        CreateSetStatement statement = parse(mdxParserProvider,
                "CREATE HIDDEN SET [Adventure Works].[Hidden] AS {[Product].[Category].&[1]}");
        assertThat(statement.session()).isFalse();
        assertThat(statement.evaluation()).isEmpty();
        assertThat(statement.hidden()).isTrue();
    }

    @Test
    void currentCube(@InjectService MdxParserProvider mdxParserProvider) throws MdxParserException {
        CreateSetStatement statement = parse(mdxParserProvider,
                "CREATE SET CURRENTCUBE.[Current] AS {[Product].[Category].&[4]}");
        assertThat(statement.cube()).isInstanceOf(CubeReference.Current.class);
        checkNameObjectIdentifiers(statement.body().compoundId().objectIdentifiers(), 0, "Current", Quoting.QUOTED);
    }

    @Test
    void bracketedCurrentCubeIsACubeName(@InjectService MdxParserProvider mdxParserProvider)
            throws MdxParserException {
        CreateSetStatement statement = parse(mdxParserProvider, "CREATE SET [CURRENTCUBE].[x] AS {}");
        checkCube(statement.cube(), "CURRENTCUBE");
    }

    @Test
    void allWords(@InjectService MdxParserProvider mdxParserProvider) throws MdxParserException {
        CreateSetStatement statement = parse(mdxParserProvider,
                "create session static hidden set currentcube.[x] as {}, caption = 'c'");
        assertThat(statement.session()).isTrue();
        assertThat(statement.evaluation()).contains(SetEvaluation.STATIC);
        assertThat(statement.hidden()).isTrue();
        assertThat(statement.cube()).isInstanceOf(CubeReference.Current.class);
        assertThat(statement.body().memberPropertyDefinitions()).hasSize(1);
    }

    @Test
    void parseCreateSetStatement(@InjectService MdxParserProvider mdxParserProvider) throws MdxParserException {
        CreateSetStatement statement = mdxParserProvider
                .newParser("CREATE SET [Adventure Works].[S] AS {}", propertyWords).parseCreateSetStatement();
        checkCube(statement.cube(), "Adventure Works");
    }
}
