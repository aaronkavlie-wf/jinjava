package com.hubspot.jinjava.tree.parse;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertEquals;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.google.common.io.Resources;
import com.hubspot.jinjava.JinjavaConfig;
import com.hubspot.jinjava.LegacyOverrides;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.stream.Collectors;
import org.apache.commons.lang3.StringUtils;
import org.junit.Before;
import org.junit.Test;

public class TokenScannerTest {
  private JinjavaConfig config;
  private String script;

  private TokenScanner scanner;
  private TokenScannerSymbols symbols;

  @Before
  public void setup() {
    config = JinjavaConfig.newBuilder().build();
    symbols = config.getTokenScannerSymbols();
  }

  @Test
  public void test2() {
    script = "{{abc.b}}{% if x %}{{abc{%endif";
    scanner = new TokenScanner(script, config);
    assertEquals("{{abc.b}}", scanner.next().image);
    assertEquals("if x", scanner.next().content.trim());
    assertEquals("{{abc{%endif", scanner.next().content.trim());
  }

  @Test
  public void test3() {
    script = "{{abc.b}}{% if x %}{{{abc}}{%endif%}";
    scanner = new TokenScanner(script, config);
    assertEquals("{{abc.b}}", scanner.next().image);
    assertEquals("if x", scanner.next().content.trim());
    Token tk = scanner.next();
    assertEquals("{{{abc}}", tk.image);
    assertEquals(symbols.getExprStart(), tk.getType());
    assertEquals("{%endif%}", scanner.next().image);
  }

  @Test
  public void test4() {
    script = "{{abc.b}}{% if x %}{{!abc}}{%endif%}";
    scanner = new TokenScanner(script, config);
    assertEquals("{{abc.b}}", scanner.next().image);
    assertEquals("if x", scanner.next().content.trim());
    Token tk = scanner.next();
    assertEquals("{{!abc}}", tk.image);
    assertEquals(symbols.getExprStart(), tk.getType());
    assertEquals("{%endif%}", scanner.next().image);
  }

  @Test
  public void test5() {
    script = "{{abc.b}}{% if x %}a{{abc}\\}{%endif%}";
    scanner = new TokenScanner(script, config);
    assertEquals("{{abc.b}}", scanner.next().image);
    assertEquals("if x", scanner.next().content.trim());
    assertEquals("a", scanner.next().content.trim());
    assertEquals("{{abc}\\}{%endif%}", scanner.next().content.trim());
  }

  @Test
  public void test7() {
    script = "a{{abc.b}}{% if x   %}a{{abc!}#}%}}}{%endif";
    scanner = new TokenScanner(script, config);
    assertEquals("a", scanner.next().image);
    assertEquals("{{abc.b}}", scanner.next().image);
    assertEquals("if x", scanner.next().content.trim());
    assertEquals("a", scanner.next().content.trim());
    assertEquals("{{abc!}#}%}}", scanner.next().image);
    assertEquals("}", scanner.next().content.trim());
    assertEquals(symbols.getFixed(), scanner.next().getType());
  }

  @Test
  public void test8() {
    script = "a{{abc.b}}{% if x   %}a{{abc}}{%endif{{";
    scanner = new TokenScanner(script, config);
    assertEquals("a", scanner.next().image);
    assertEquals("{{abc.b}}", scanner.next().image);
    assertEquals("if x", scanner.next().content.trim());
    assertEquals("a", scanner.next().content.trim());
    assertEquals(symbols.getExprStart(), scanner.next().getType());
    assertEquals("{%endif{{", scanner.next().content.trim());
  }

  @Test
  public void test9() {
    script = "a{{abc.b}}{% if x   %}a{{abc}\\}{%endif{";
    scanner = new TokenScanner(script, config);
    assertEquals("a", scanner.next().image);
    assertEquals("{{abc.b}}", scanner.next().image);
    assertEquals("if x", scanner.next().content.trim());
    assertEquals("a", scanner.next().content.trim());
    assertEquals(symbols.getFixed(), scanner.next().getType());
  }

  @Test
  public void test10() {
    script = "a{{abc.b}}{% if x %}a{{abc}\\}{{#%endif{";
    scanner = new TokenScanner(script, config);
    assertEquals("a", scanner.next().image);
    assertEquals("{{abc.b}}", scanner.next().image);
    assertEquals("if x", scanner.next().content.trim());
    assertEquals("a", scanner.next().content.trim());
    assertEquals("{{abc}\\}{", scanner.next().image);
    assertEquals(symbols.getNote(), scanner.next().getType());
  }

  @Test
  public void test11() {
    script = "a{#abc.b#}{% if x %}a{{abc}\\}{{{{#endif{";
    scanner = new TokenScanner(script, config);
    assertEquals("a", scanner.next().image);
    assertEquals("{#abc.b#}", scanner.next().image);
    assertEquals("if x", scanner.next().content.trim());
    assertEquals("a", scanner.next().content.trim());
    assertEquals("{{abc}\\}{{{", scanner.next().content.trim());
    assertEquals("{#endif{", scanner.next().image);
  }

  @Test
  public void test12() {
    script = "{#abc.b#}{% if x %}a{{abc}\\}{{{{#endif{";
    scanner = new TokenScanner(script, config);
    assertEquals("{#abc.b#}", scanner.next().image);
    assertEquals("if x", scanner.next().content.trim());
    assertEquals("a", scanner.next().content.trim());
    assertEquals("{{abc}\\}{{{", scanner.next().content.trim());
    assertEquals(symbols.getNote(), scanner.next().getType());
  }

  @Test
  public void test13() {
    script =
      "{#abc{#.b#}{#xy{!ad!}{%dbc%}{{dff}}d{#bc#}d#}#}{% if x %}a{{abc}\\}{{{{#endif{";
    scanner = new TokenScanner(script, config);
    assertEquals("{#abc{#.b#}", scanner.next().image);
  }

  @Test
  public void test14() {
    script =
      "abc{#.b#}{#xy{!ad!}{%dbc%}{{dff}}d{#bc#}d#}#}{% if x %}a{{abc}\\}{{{{#endif{";
    scanner = new TokenScanner(script, config);
    assertEquals("abc", scanner.next().image);
    assertEquals("{#.b#}", scanner.next().image);
    assertEquals("{#xy{!ad!}{%dbc%}{{dff}}d{#bc#}", scanner.next().image);
  }

  @Test
  public void test15() {
    script =
      "abc{#.b#}{#xy{!ad!}{#DD#}{%dbc%}{{dff}}d{#bc#}d#}#}{% if x %}a{{abc}\\}{{{{#endif{";
    scanner = new TokenScanner(script, config);
    assertEquals("abc", scanner.next().image);
    assertEquals("{#.b#}", scanner.next().image);
    assertEquals("{#xy{!ad!}{#DD#}", scanner.next().image);
  }

  @Test
  public void test16() {
    script =
      "{#{#abc{#.b#}{#xy{!ad!}{%dbc%}{{dff}}d{#bc#}d#}#}{% if x %}a{{abc}\\}{{{{#endif{";
    scanner = new TokenScanner(script, config);
    assertEquals("{#{#abc{#.b#}", scanner.next().image);
  }

  @Test
  public void test17() {
    script =
      "{#abc{#.b#}{#xy{!ad!}{%dbc%}{{dff}}d{#bc#}d#}#}{% if x %}#}a#}{{abc}\\}#}{{{{#endif{";
    scanner = new TokenScanner(script, config);
    assertEquals("{#abc{#.b#}", scanner.next().image);
  }

  @Test
  public void itGetsPositionsOfTokens() {
    List<Token> tokens = tokens("positions");
    assertThat(tokens.stream().map(Token::getStartPosition).collect(Collectors.toList()))
      .isEqualTo(ImmutableList.of(1, 0, 4, 0, 6, 0, 6, 0, 4, 0, 4, 13, 25, 0, 1));
  }

  @Test
  public void itProperlyTokenizesCommentBlocksContainingTags() {
    List<Token> tokens = tokens("comment-with-tags");
    assertThat(tokens).hasSize(5);
    assertThat(tokens.get(4)).isInstanceOf(TagToken.class);
    assertThat(StringUtils.substringBetween(tokens.get(4).toString(), "{%", "%}").trim())
      .isEqualTo("endif");
  }

  @Test
  public void itProperlyTokenizesCommentWithTrailingTokens() {
    List<Token> tokens = tokens("comment-plus");
    assertThat(tokens).hasSize(2);
    assertThat(tokens.get(tokens.size() - 1)).isInstanceOf(TextToken.class);
    assertThat(
        StringUtils
          .substringBetween(tokens.get(tokens.size() - 1).toString(), "{~", "~}")
          .trim()
      )
      .isEqualTo("and here's some extra.");
  }

  @Test
  public void itProperlyTokenizesMultilineCommentTokens() {
    List<Token> tokens = tokens("multiline-comment");
    assertThat(tokens).hasSize(3);
    assertThat(tokens.get(2)).isInstanceOf(TextToken.class);
    assertThat(StringUtils.substringBetween(tokens.get(2).toString(), "{~", "~}").trim())
      .isEqualTo("goodbye.");
  }

  @Test
  public void itProperlyTokenizesCommentWithStartCommentTokenWithin() {
    List<Token> tokens = tokens("comment-with-start-comment-within");
    assertThat(tokens).hasSize(2);
    assertThat(tokens.get(1)).isInstanceOf(TagToken.class);
    assertThat(tokens.get(1).getImage()).contains("color");
  }

  @Test
  public void itProperlyTokenizesTagTokenWithTagTokenCharsWithinString() {
    List<Token> tokens = tokens("tag-with-tag-tokens-within-string");
    assertThat(tokens).hasSize(1);
    assertThat(tokens.get(0).getType()).isEqualTo(symbols.getTag());
    assertThat(tokens.get(0).content).contains("label='Blog Comments'");
  }

  @Test
  public void testQuotedTag() {
    List<Token> tokens = tokens("html-with-tag-in-attr");
    assertThat(tokens).hasSize(3);
    assertThat(tokens.get(0).getType()).isEqualTo(symbols.getFixed());
    assertThat(tokens.get(1).getType()).isEqualTo(symbols.getTag());
    assertThat(tokens.get(2).getType()).isEqualTo(symbols.getFixed());
  }

  @Test
  public void testEscapedQuoteWithinAttrValue() {
    List<Token> tokens = tokens("tag-with-quot-in-attr");
    assertThat(tokens).hasSize(1);
    assertThat(tokens.get(0).getType()).isEqualTo(symbols.getTag());
    assertThat(tokens.get(0).content.trim())
      .isEqualTo(
        "widget_block rich_text \"module\" overrideable=True, label='<p>We\\'ve included a great symbol</p>'"
      );
  }

  @Test
  public void testCommentWithWhitespaceChar() {
    List<Token> tokens = tokens("comment-without-whitespace");
    assertThat(tokens.get(0).content.trim()).isEqualTo("$");

    LegacyOverrides legacyOverrides = LegacyOverrides
      .newBuilder()
      .withWhitespaceRequiredWithinTokens(true)
      .build();
    JinjavaConfig config = JinjavaConfig
      .newBuilder()
      .withLegacyOverrides(legacyOverrides)
      .build();
    TokenScanner scanner = fixture("comment-without-whitespace", config);
    tokens = Lists.newArrayList(scanner);
    assertThat(tokens.get(0).content.trim()).isEqualTo("${#array[@]}");
  }

  @Test
  public void testEscapedBackslashWithinAttrValue() {
    List<Token> tokens = tokens("escape-char-tokens");

    List<String> tagHelpers = tokens
      .stream()
      .filter(t -> t.getType() == symbols.getTag())
      .map(t -> ((TagToken) t).getHelpers().trim().substring(1, 26))
      .collect(Collectors.toList());

    assertThat(tagHelpers)
      .containsExactly(
        "module_143819779285827357",
        "module_143819780991527688",
        "module_143819781983527999"
      );
  }

  @Test
  public void testLstripBlocks() {
    config =
      JinjavaConfig.newBuilder().withLstripBlocks(true).withTrimBlocks(true).build();

    List<Token> tokens = tokens("tag-with-trim-chars");
    assertThat(tokens).isNotEmpty();
  }

  @Test
  public void itTreatsEscapedQuotesSameWhenNotInQuotes() {
    List<Token> tokens = tokens("tag-with-all-escaped-quotes");
    assertThat(tokens).hasSize(8);
    assertThat(tokens.stream().map(Token::getType).collect(Collectors.toList()))
      .containsExactly(
        symbols.getNote(),
        symbols.getFixed(),
        symbols.getTag(),
        symbols.getFixed(),
        symbols.getTag(),
        symbols.getFixed(),
        symbols.getTag(),
        symbols.getFixed()
      );
  }

  private List<Token> tokens(String fixture) {
    TokenScanner t = fixture(fixture);
    return Lists.newArrayList(t);
  }

  private TokenScanner fixture(String fixture) {
    return fixture(fixture, config);
  }

  private TokenScanner fixture(String fixture, JinjavaConfig config) {
    try {
      return new TokenScanner(
        Resources.toString(
          Resources.getResource(String.format("parse/tokenizer/%s.jinja", fixture)),
          StandardCharsets.UTF_8
        ),
        config
      );
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }
}
