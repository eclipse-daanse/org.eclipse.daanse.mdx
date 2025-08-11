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
package org.eclipse.daanse.mdx.combinedtest;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Set;

import org.eclipse.daanse.mdx.model.api.MdxStatement;
import org.eclipse.daanse.mdx.parser.api.MdxParserException;
import org.eclipse.daanse.mdx.parser.api.MdxParserProvider;
import org.eclipse.daanse.mdx.parser.ccc.CCCMdxParserProvider;
import org.eclipse.daanse.mdx.parser.cccx.CCCXMdxParserProvider;
import org.eclipse.daanse.mdx.unparser.api.UnParser;
import org.osgi.service.component.annotations.RequireServiceComponentRuntime;
import org.osgi.test.common.annotation.InjectService;
import org.osgi.test.common.service.ServiceAware;

@RequireServiceComponentRuntime
class UnparseParsedTest {

    public static Set<String> reservedWords = Set.of("ORDINAL", "VALUE", "DATAMEMBER", "MEMBER_CAPTION", "FIRSTSIBLING",
            "CURRENTMEMBER", "CURRENTORDINAL", "DIMENSION", "LASTSIBLING", "PARENT", "NEXTMEMBER", "UNIQUE_NAME",
            "UNIQUENAME", "MEMBERS", "SIBLINGS", "ORDERKEY", "DEFAULTMEMBER", "LEVEL", "FIRSTCHILD", "LASTCHILD",
            "CURRENT", "NAME", "CHILDREN", "PREVMEMBER", "LEVEL_NUMBER", "ALLMEMBERS", "COUNT", "CAPTION", "HIERARCHY");
    public static String MDX = """
            //<SELECT WITH clause>
            WITH
                // CELL CALCULATION <CREATE CELL CALCULATION body clause>
                // [ CALCULATED ] MEMBER <CREATE MEMBER body clause>
                MEMBER
                    [Measures].[Calculate Internet Sales Amount] AS
                    // Arithmetic Function
                    round(9.12345678, 3)
                    + [Measures].[Internet Sales Amount],
                    FORE_COLOR = iif([Customer].[Gender].CurrentMember is [Customer].[Gender].&[F], RGB(144,238,144), RGB(0,0,0)),
                    BACK_COLOR = iif([Customer].[Gender].CurrentMember is [Customer].[Gender].&[F], RGB(255,255,224), RGB(135,206,250)),
                    FONT_SIZE = iif([Customer].[Gender].CurrentMember is [Customer].[Gender].&[F], 10, 16),
                    FORMAT_STRING = iif([Customer].[Gender].CurrentMember is [Customer].[Gender].&[F], 'Standard', 'Currency')
                // SET <CREATE SET body clause>
                SET MySet AS
                    // Function Union(Set_Expression1, Set_Expression2 [,...n][, ALL])
                    Union([Customer].[Gender].Members, {[Customer].[Gender].&[F]})
            SELECT
                // <SELECT query axis clause>
                [Gender].[Gender].Members ON COLUMNS,
                // <SELECT query axis clause>
                {[Customer].[Customer].[Aaron A. Allen], [Customer].[Customer].[Abigail Clark]}
                // <SELECT dimension property list clause>
                DIMENSION PROPERTIES PARENT_UNIQUE_NAME, HIERARCHY_UNIQUE_NAME, CUSTOM_ROLLUP, UNARY_OPERATOR, KEY0, MEMBER_TYPE
                ON ROWS,
                // <SELECT query axis clause>
                [Date].[Calendar].[Calendar Year].Members -  {[Date].[Calendar].[Calendar Year].&[2003]} ON PAGES
            FROM
                // <SELECT subcube clause>
                (
                    SELECT
                        // <SELECT slicer axis clause>
                        {[Customer].[Gender].&[F], [Customer].[Gender].&[M]} ON COLUMNS,
                        // <SELECT slicer axis clause>
                        [Measures].[Internet Sales Amount]  ON ROWS
                    FROM
                        // <SELECT subcube clause>
                        (
                            SELECT
                            FROM
                                // Cube_Name
                                [Adventure Works]
                        )
                    // <SELECT slicer axis clause>
                    WHERE [Measures].[Internet Sales Amount]
                )
            // <SELECT slicer axis clause>
            WHERE ([Measures].[Calculate Internet Sales Amount], {[Product].[Category].&[1],[Product].[Category].&[3]})
            // <SELECT cell property list clause>
            CELL PROPERTIES BACK_COLOR, FORE_COLOR, FONT_SIZE, FORMAT_STRING, VALUE, FORMATTED_VALUE
                        """;

    @org.junit.jupiter.api.Test
    void testFullStatement(
            @InjectService(filter = "(component.name=org.eclipse.daanse.mdx.parser.cccx.CCCXMdxParserProvider)") MdxParserProvider mdxParserProvider,
            @InjectService UnParser unParser) throws MdxParserException {

        MdxStatement mdxStatement = mdxParserProvider.newParser(MDX, reservedWords).parseMdxStatement();
        StringBuilder resultmdx = unParser.unparseMdxStatement(mdxStatement);
        assertThat(resultmdx).isNotNull().isNotBlank();

    }

    @org.junit.jupiter.api.Test
    void test(
            @InjectService(filter = "(component.name=org.eclipse.daanse.mdx.parser.cccx.CCCXMdxParserProvider)") MdxParserProvider cccMdxParserProvider,
            @InjectService(filter = "(component.name=org.eclipse.daanse.mdx.parser.ccc.CCCMdxParserProvider)") MdxParserProvider cccxMdxParserProvider)
            throws MdxParserException {

        MdxStatement mdxStatement = cccMdxParserProvider.newParser(MDX, reservedWords).parseMdxStatement();
        assertThat(mdxStatement).isNotNull();

        mdxStatement = cccxMdxParserProvider.newParser(MDX, reservedWords).parseMdxStatement();
        assertThat(mdxStatement).isNotNull();

    }

    @org.junit.jupiter.api.Test
    void testFullStatement(@InjectService() ServiceAware<MdxParserProvider> mdxParserProvider) {

        mdxParserProvider.getServiceReferences().forEach(sr ->

        System.out.println(sr.getProperties()));

    }

}
