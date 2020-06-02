package com.cherokeelessons.raven;

import org.apache.commons.lang3.StringUtils;

public class Consts {

	public static final String LDOTS = "\\SpecialChar ldots\n";
	public static String numbersSuperscriptZero = "⁰";
	public static String[] numbersSuperscript = { "¹", "²", "³", "⁴", "⁵", "⁶", "⁷", "⁸", "⁹" };
	public static String[] numbersCircled = { "①", "②", "③", "④", "⑤", "⑥", "⑦", "⑧", "⑨", "⑩" };
	// public static String[] numbersParen =
	// {"⑴","⑵","⑶","⑷","⑸","⑹","⑺","⑻","⑼","⑽","⑾","⑿","⒀","⒁","⒂","⒃","⒄","⒅","⒆","⒇"};
	// public static String[] numbersDotted =
	// {"⒈","⒉","⒊","⒋","⒌","⒍","⒎","⒏","⒐","⒑","⒒","⒓","⒔","⒕","⒖","⒗","⒘","⒙","⒚","⒛"};
	// public static String[] lettersLcParen =
	// {"⒜","⒝","⒞","⒟","⒠","⒡","⒢","⒣","⒤","⒥","⒦","⒧","⒨","⒩","⒪","⒫","⒬","⒭","⒮","⒯","⒰","⒱","⒲","⒳","⒴","⒵"};
	public static String[] definitionMarkers = numbersCircled;
	public static String splitRegex = splitRegex();

	private static String splitRegex() {
		return "\\s*[;" + StringUtils.join(definitionMarkers) + "]\\s*";
	}
}
