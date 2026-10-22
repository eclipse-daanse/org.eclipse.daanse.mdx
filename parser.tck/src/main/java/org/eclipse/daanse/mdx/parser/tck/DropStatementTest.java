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

import org.eclipse.daanse.mdx.model.api.CubeReference;
import org.eclipse.daanse.mdx.model.api.DropMemberStatement;
import org.eclipse.daanse.mdx.model.api.DropSetStatement;
import org.eclipse.daanse.mdx.model.api.MdxStatement;
import org.eclipse.daanse.mdx.model.api.QualifiedName;
import org.eclipse.daanse.mdx.model.api.expression.ObjectIdentifier.Quoting;
import org.eclipse.daanse.mdx.parser.api.MdxParserException;
import org.eclipse.daanse.mdx.parser.api.MdxParserProvider;
import org.junit.jupiter.api.Test;
import org.osgi.service.component.annotations.RequireServiceComponentRuntime;
import org.osgi.test.common.annotation.InjectService;

/** DROP MEMBER and DROP SET as SSAS takes them; each name is the cube and the name without it. */
@RequireServiceComponentRuntime
class DropStatementTest {

    @Test
    void dropMember(@InjectService MdxParserProvider mdxParserProvider) throws MdxParserException {
        MdxStatement statement = mdxParserProvider
                .newParser("DROP MEMBER [Adventure Works].[Measures].[Twice]", propertyWords).parseMdxStatement();
        assertThat(statement).isInstanceOf(DropMemberStatement.class);
        List<? extends QualifiedName> names = ((DropMemberStatement) statement).names();
        assertThat(names).hasSize(1);
        checkCube(names.get(0).cube(), "Adventure Works");
        var name = names.get(0).compoundId().objectIdentifiers();
        assertThat(name).hasSize(2);
        checkNameObjectIdentifiers(name, 0, "Measures", Quoting.QUOTED);
        checkNameObjectIdentifiers(name, 1, "Twice", Quoting.QUOTED);
    }

    @Test
    void dropMembers(@InjectService MdxParserProvider mdxParserProvider) throws MdxParserException {
        MdxStatement statement = mdxParserProvider.newParser(
                "DROP MEMBER [Adventure Works].[Measures].[Count], [Adventure Works].[Measures].[Invisible]",
                propertyWords).parseMdxStatement();
        assertThat(statement).isInstanceOf(DropMemberStatement.class);
        List<? extends QualifiedName> names = ((DropMemberStatement) statement).names();
        assertThat(names).hasSize(2);
        checkNameObjectIdentifiers(names.get(0).compoundId().objectIdentifiers(), 1, "Count", Quoting.QUOTED);
        checkCube(names.get(1).cube(), "Adventure Works");
        checkNameObjectIdentifiers(names.get(1).compoundId().objectIdentifiers(), 1, "Invisible", Quoting.QUOTED);
    }

    @Test
    void dropSets(@InjectService MdxParserProvider mdxParserProvider) throws MdxParserException {
        MdxStatement statement = mdxParserProvider
                .newParser("DROP SET [Adventure Works].[Ordered], [Adventure Works].[Hidden]", propertyWords)
                .parseMdxStatement();
        assertThat(statement).isInstanceOf(DropSetStatement.class);
        List<? extends QualifiedName> names = ((DropSetStatement) statement).names();
        assertThat(names).hasSize(2);
        checkCube(names.get(0).cube(), "Adventure Works");
        assertThat(names.get(0).compoundId().objectIdentifiers()).hasSize(1);
        checkNameObjectIdentifiers(names.get(0).compoundId().objectIdentifiers(), 0, "Ordered", Quoting.QUOTED);
        checkNameObjectIdentifiers(names.get(1).compoundId().objectIdentifiers(), 0, "Hidden", Quoting.QUOTED);
    }

    @Test
    void currentCube(@InjectService MdxParserProvider mdxParserProvider) throws MdxParserException {
        DropSetStatement statement = mdxParserProvider.newParser("drop set CurrentCube.[S]", propertyWords)
                .parseDropSetStatement();
        assertThat(statement.names().get(0).cube()).isInstanceOf(CubeReference.Current.class);
    }

    @Test
    void parseDropMemberStatement(@InjectService MdxParserProvider mdxParserProvider) throws MdxParserException {
        DropMemberStatement statement = mdxParserProvider
                .newParser("DROP MEMBER [AW].[Measures].[M]", propertyWords).parseDropMemberStatement();
        assertThat(statement.names()).hasSize(1);
    }
}
