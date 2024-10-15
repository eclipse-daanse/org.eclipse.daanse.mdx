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
import org.osgi.service.component.annotations.Component;

@Component(service = MdxParserProvider.class)
public class MdxParserProviderImpl implements MdxParserProvider {

    @Override
    public MdxParser newParser(CharSequence mdx, Set<String> propertyWords) throws MdxParserException {
        return new MdxParserWrapper(mdx, propertyWords);
    }
}
