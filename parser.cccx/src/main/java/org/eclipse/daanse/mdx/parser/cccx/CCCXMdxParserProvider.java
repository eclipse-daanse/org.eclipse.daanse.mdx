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
package org.eclipse.daanse.mdx.parser.cccx;

import java.util.Set;

import org.eclipse.daanse.mdx.parser.api.MdxParser;
import org.eclipse.daanse.mdx.parser.api.MdxParserException;
import org.eclipse.daanse.mdx.parser.api.MdxParserProvider;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.ServiceScope;
import org.osgi.service.metatype.annotations.AttributeDefinition;
import org.osgi.service.metatype.annotations.Designate;
import org.osgi.service.metatype.annotations.ObjectClassDefinition;

@Designate(ocd = CCCXMdxParserProvider.Config.class)
@Component(scope = ServiceScope.SINGLETON, property = { "parser.type=cccx" }, configurationPid = CCCXMdxParserProvider.PID, service = MdxParserProvider.class)
public class CCCXMdxParserProvider implements MdxParserProvider {

    public static final String PID = "daanse.mdx.parser.cccx.CCCXMdxParserProvider";

    /** How deep a statement may be nested, see {@link Config#maxNesting()}. */
    public static final int DEFAULT_MAX_NESTING = 1000;

    @ObjectClassDefinition(name = "Daanse MDX Parser (cccx)")
    public @interface Config {

        @AttributeDefinition(description = "How deep a statement may be nested, counted in nested productions of "
                + "the grammar. The parser is recursive, deeper input is refused with a MdxParserException. "
                + "The default takes about 80 nested parentheses or function calls and needs a thread stack "
                + "of 512k; raise it together with the stack size.")
        int maxNesting() default DEFAULT_MAX_NESTING;
    }

    private final int maxNesting;

    public CCCXMdxParserProvider() {
        this(DEFAULT_MAX_NESTING);
    }

    public CCCXMdxParserProvider(int maxNesting) {
        this.maxNesting = maxNesting;
    }

    @Activate
    public CCCXMdxParserProvider(Config config) {
        this(config.maxNesting());
    }

    @Override
    public MdxParser newParser(CharSequence mdx, Set<String> propertyWords) throws MdxParserException {
        return new MdxParserWrapper(mdx, propertyWords, maxNesting);
    }
}
