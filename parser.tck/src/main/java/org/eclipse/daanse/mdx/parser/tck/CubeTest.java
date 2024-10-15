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
package org.eclipse.daanse.mdx.parser.tck;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.Set;

import org.eclipse.daanse.mdx.model.api.SelectStatement;
import org.eclipse.daanse.mdx.model.api.expression.ObjectIdentifier.Quoting;
import org.eclipse.daanse.mdx.model.api.select.SelectSubcubeClauseName;
import org.eclipse.daanse.mdx.parser.api.MdxParserException;
import org.eclipse.daanse.mdx.parser.api.MdxParserProvider;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.osgi.service.component.annotations.RequireServiceComponentRuntime;
import org.osgi.test.common.annotation.InjectService;

@RequireServiceComponentRuntime
class CubeTest {

    static Set<String> propertyWords = Set.of("ORDINAL", "VALUE", "DATAMEMBER", "MEMBER_CAPTION", "FIRSTSIBLING",
            "CURRENTMEMBER", "CURRENTORDINAL", "DIMENSION", "LASTSIBLING", "PARENT", "NEXTMEMBER", "UNIQUE_NAME",
            "UNIQUENAME", "MEMBERS", "SIBLINGS", "ORDERKEY", "DEFAULTMEMBER", "LEVEL", "FIRSTCHILD", "LASTCHILD",
            "CURRENT", "NAME", "CHILDREN", "PREVMEMBER", "LEVEL_NUMBER", "ALLMEMBERS", "COUNT", "CAPTION", "HIERARCHY");

    @Nested
    class SelectSubcubeClauseNameTest {
        @ParameterizedTest
        @ValueSource(strings = { "c", //
                "cube", // Reserved Word but quoted
                "with whitespace", "with [inner]", "1", "." })
        void testQuoted(String cubeName, @InjectService MdxParserProvider mdxParserProvider) throws MdxParserException {
            String mdx = mdxCubeNameQuoted(cubeName);

            SelectStatement selectStatement = mdxParserProvider.newParser(mdx, propertyWords).parseSelectStatement();
            assertThat(selectStatement).isNotNull();
            assertThat(selectStatement.selectSubcubeClause()).isNotNull()
                    .isInstanceOfSatisfying(SelectSubcubeClauseName.class, s -> {
                        assertThat(s.cubeName()).isNotNull().satisfies(n -> {
                            assertThat(n.name()).isEqualTo(cubeName);
                            assertThat(n.quoting()).isEqualByComparingTo(Quoting.QUOTED);
                        });
                    });
        }

        @Disabled
        @ParameterizedTest
        @ValueSource(strings = { "[]", "[a].[a]" })
        void testQuotedFail(String cubeName, @InjectService MdxParserProvider mdxParserProvider)
                throws MdxParserException {
            String mdx = mdx_selectFromCubeName(cubeName);

            assertThrows(MdxParserException.class, () -> {
                mdxParserProvider.newParser(mdx, propertyWords).parseSelectStatement();
            });

        }

        @ParameterizedTest
        @ValueSource(strings = { "a", "a1", "aAaAaA", "AaAaAaA" })
        void testUnquoted(String cubeName, @InjectService MdxParserProvider mdxParserProvider)
                throws MdxParserException {
            String mdx = mdx_selectFromCubeName(cubeName);

            SelectStatement selectStatement = mdxParserProvider.newParser(mdx, propertyWords).parseSelectStatement();
            assertThat(selectStatement).isNotNull();
            assertThat(selectStatement.selectSubcubeClause()).isNotNull()
                    .isInstanceOfSatisfying(SelectSubcubeClauseName.class, s -> {
                        assertThat(s.cubeName()).isNotNull().satisfies(n -> {
                            assertThat(n.name()).isEqualTo(cubeName);
                            assertThat(n.quoting()).isEqualByComparingTo(Quoting.UNQUOTED);
                        });
                    });
        }

        // TODO: check ""
        @Disabled
        @ParameterizedTest
        @ValueSource(strings = { "", "1", "1 1", "a a", "-", "cube", // Reserved Word
                "CURRENTCUBE"// Reserved Word
        })
        void testUnquotedFail(String cubeName, @InjectService MdxParserProvider mdxParserProvider)
                throws MdxParserException {
            String mdx = mdx_selectFromCubeName(cubeName);

            assertThrows(MdxParserException.class, () -> {
                mdxParserProvider.newParser(mdx, propertyWords).parseSelectStatement();
            });

            //
        }

        private static String mdxCubeNameQuoted(String cubeName) {
            cubeName = cubeName.replace("]", "]]");
            return mdx_selectFromCubeName("[" + cubeName + "]");
        }

        private static String mdx_selectFromCubeName(String cubeName) {
            return "SELECT FROM " + cubeName;
        }
    }

}
