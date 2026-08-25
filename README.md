# org.eclipse.daanse.mdx
MDX (Multi-Dimensional Expressions) parser, model and unparser — Eclipse Daanse.

## Requirements
- JDK 25+, Maven 3.9+

## Build
    mvn clean verify
Import in IDE: initially `mvn generate-sources` (CongoCC generates in `src/gen/java`).

## Modules
| Module | Purpose |
|---|---|
| model.api / model.record | AST-interfaces / immutable records |
| parser.api / parser.ccc / parser.cccx | Parser API, monolithic / CongoCC modular grammar |
| parser.tck | Common Parser Test Suite (OSGi) |
| unparser.api / unparser.simple | API unparser / reference implementation |

## Example
    MdxStatement stmt = mdxParserProvider.newParser("SELECT [a] ON COLUMNS FROM [c]", Set.of())
                                         .parseMdxStatement();
    CharSequence mdx  = unParser.unparseMdxStatement(stmt);

