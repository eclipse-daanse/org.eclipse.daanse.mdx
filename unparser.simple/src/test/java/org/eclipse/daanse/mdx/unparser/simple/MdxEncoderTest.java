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
package org.eclipse.daanse.mdx.unparser.simple;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatIllegalArgumentException;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

class MdxEncoderTest {

    @Test
    void stringIsDelimitedAndQuotesAreDoubled() {
        assertThat(MdxEncoder.string("")).isEqualTo("\"\"");
        assertThat(MdxEncoder.string("plain")).isEqualTo("\"plain\"");
        assertThat(MdxEncoder.string("\"")).isEqualTo("\"\"\"\"");
        assertThat(MdxEncoder.string("a\"\"b")).isEqualTo("\"a\"\"\"\"b\"");
        assertThat(MdxEncoder.string("it's, [x] -- /* \n")).isEqualTo("\"it's, [x] -- /* \n\"");
    }

    @Test
    void bracketDoublesTheClosingBracket() {
        assertThat(MdxEncoder.bracket("")).isEqualTo("[]");
        assertThat(MdxEncoder.bracket("a b")).isEqualTo("[a b]");
        assertThat(MdxEncoder.bracket("]")).isEqualTo("[]]]");
        assertThat(MdxEncoder.bracket("a].[b")).isEqualTo("[a]].[b]");
    }

    @ParameterizedTest
    @ValueSource(strings = { "a", "_a1", "$x", "@param", "Ünicode" })
    void bareIdentifiers(String name) {
        assertThat(MdxEncoder.isBareIdentifier(name)).isTrue();
        assertThat(MdxEncoder.identifier(name)).isEqualTo(name);
        assertThat(MdxEncoder.bare(name, "name")).isEqualTo(name);
    }

    @ParameterizedTest
    @ValueSource(strings = { "", "@", "1a", "a b", "a.b", "a,b", "a]", "[a]", "a\nb", "a--", "a(", "&a" })
    void everythingElseIsBracketedOrRejected(String name) {
        assertThat(MdxEncoder.isBareIdentifier(name)).isFalse();
        assertThat(MdxEncoder.identifier(name)).isEqualTo(MdxEncoder.bracket(name));
        assertThatIllegalArgumentException().isThrownBy(() -> MdxEncoder.bare(name, "name"));
    }

    @Test
    void operatorsComeFromAFixedSet() {
        assertThat(MdxEncoder.infixOperator("and")).isEqualTo("and");
        assertThat(MdxEncoder.infixOperator("<>")).isEqualTo("<>");
        assertThat(MdxEncoder.prefixOperator("NOT")).isEqualTo("NOT");
        assertThat(MdxEncoder.postfixOperator("IS NULL")).isEqualTo("IS NULL");
        assertThatIllegalArgumentException().isThrownBy(() -> MdxEncoder.infixOperator(", [x] ="));
        assertThatIllegalArgumentException().isThrownBy(() -> MdxEncoder.prefixOperator("IS NULL"));
        assertThatIllegalArgumentException().isThrownBy(() -> MdxEncoder.postfixOperator("ON 0 FROM [x] --"));
    }

    @Test
    void nullIsRejected() {
        assertThatIllegalArgumentException().isThrownBy(() -> MdxEncoder.string(null));
        assertThatIllegalArgumentException().isThrownBy(() -> MdxEncoder.bracket(null));
        assertThatIllegalArgumentException().isThrownBy(() -> MdxEncoder.identifier(null));
    }
}
