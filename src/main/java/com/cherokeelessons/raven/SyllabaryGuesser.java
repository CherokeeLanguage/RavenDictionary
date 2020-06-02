package com.cherokeelessons.raven;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;

public class SyllabaryGuesser {

	private static String removeVowelLengthsAndToneMarks(String text) {
		text = StringUtils.replaceChars(text, "ạẹịọụṿ", "aeiouv");
		text = text.replaceAll("[¹²³⁴]", "");
		text = text.replace(SyllabaryConverter.UpperMark+"h", "");
		text = text.replace(SyllabaryConverter.UpperMark, "");
		return text;
	}

	private final Map<String, String> lat2chr = new HashMap<String, String>();

	public SyllabaryGuesser() {
		lat2chr.putAll(SyllabaryConverter.lat2chr());
	}

	private static boolean rawforms = false;
	private final static Set<String> unhandled = new HashSet<>();

	public String get(String pronounce) {

		pronounce = fixupForm(pronounce);

		if (rawforms) {
			return guessTheSyllables(pronounce);
		}

		pronounce = guessTheSyllables(pronounce);

		StringBuilder sb = new StringBuilder();
		pronounce = removeVowelLengthsAndToneMarks(pronounce);
		if (StringUtils.containsOnly(pronounce, "-")) {
			return "";
		}

		for (String syllable : StringUtils.split(pronounce, "-")) {
			if (syllable.matches("[ '()]")){
				sb.append(syllable);
				continue;
			}
			String x = syllable;
			x = x.replaceAll("h$", "");
			x = x.replaceAll("^kw", "gw");
			x = x.replaceAll("^hw", "w");
			x = x.replaceAll("^hy", "y");
			x = x.replaceAll("^hs", "s");
			x = x.replaceAll("^hn(?=[^a])", "n");
			x = x.replaceAll("^dl(?=[^a])", "tl");
			String s = lat2chr.get(x);
			if (StringUtils.isEmpty(s)) {
				if (!unhandled.contains(x)) {
					unhandled.add(x);
					System.err.println("Unhandled latin 2 syllabary: '"
							+ syllable + "'");
				}
				s = "<" + syllable + ">";
			}
			sb.append(s);
		}
		return sb.toString();
	}

	private String fixupForm(String pronounce) {
		/*
		 * g + ɂ + i => gi
		 */
//		pronounce = pronounce.replace("gɂi", "gi");
//		pronounce = pronounce.replace("gɂị", "gị");
		pronounce = pronounce.replace("ch", "j");
		pronounce = pronounce.replace("nhd", "nvhd");
		return pronounce;
	}

	public static String guessTheSyllables(String pronounce) {
		
		pronounce = pronounce.replaceAll(SyllabaryConverter.UpperMark, "");
		
		pronounce = pronounce.replaceAll("ạgt", "ạgạt");
		pronounce = pronounce.replaceAll("ạkt", "ạgạt");
		pronounce = pronounce.replaceAll("ukt", "ugạt");
		pronounce = pronounce.replaceAll("ịkt", "ịgạt");
		
		pronounce = pronounce.replaceAll("ịw(?=s)", "ịwạ");
		pronounce = pronounce.replaceAll("ịwɂ(?=s)", "ịwạ");
		pronounce = pronounce.replaceAll("ịwh(?=s)", "ịwạ");
		
		pronounce = pronounce.replaceAll("ạw(?=s)", "ạwạ");
		pronounce = pronounce.replaceAll("ạwɂ(?=s)", "ạwạ");
		pronounce = pronounce.replaceAll("ạwh(?=s)", "ạwạ");
		
		pronounce = pronounce.replaceAll("([oọ][¹²³⁴]?)wht", "$1wat");
		
		pronounce = pronounce.replaceAll("(?<=[aạ][¹²³⁴]?l)(?=(d|t)(d|t)?[aeiouvạẹịọṿ])", "a");
		
		pronounce = pronounce.replaceAll("^(ah?n)(?!=[aeiouvạẹịọṿ])", "$1i");
		
		pronounce = pronounce.replaceAll("(?<=s(kh?|g)wɂ?)(?=s)", "i");
		
		pronounce = pronounce.replaceAll("(?<=[aeiouvạẹịọụṿ](d|t)s)(?=(k|g))", "i");
		
		// kh+vowel
		pronounce = pronounce.replaceAll("kh(?=[aeiouvạẹịọụṿ])", "-k");
		
		// ɂhn[v]
		pronounce = pronounce.replaceAll("ɂhn", "-n");
		
		pronounce = pronounce.replaceAll("ɂh", "-");
		
		pronounce = pronounce.replaceAll("shd", "s-d");
		
		// dv³hns => dv³hni "He is shaking his head no."
//		pronounce = pronounce.replaceAll("dv³hn(?=[^aeiouvạẹịọụṿ¹²³⁴\\-])", "dv³hnị");
		
		// ahnsi => anvsi
//		pronounce = pronounce.replace("ahnsi", "anvsi");
		
		
		//tɂs => ti-s (for "dreaming" and similar)
//		pronounce = pronounce.replaceAll("(?<=[iị][¹²³⁴]?t)ɂ(?=s)", "i-");
		
		//fall back for any other tɂs => ts
//		pronounce = pronounce.replaceAll("tɂs", "ts");
		
		// m [space] => ma [space]
		pronounce = pronounce.replace("m ", "ma ");
		if (pronounce.endsWith("m")){
			pronounce+="a";
		}
		
		// uhl+vowel => uh - l+vowel
//		pronounce = pronounce.replaceAll("(?<=uh)(?=l[aeiouv])", "-");
		
		// consonent + glottal stop + vowel should be consonent + vowel
		pronounce = pronounce.replaceAll(
				"(?<=[^aeiouvạẹịọụṿ¹²³⁴\\-])ɂ(?=[aeiouvạẹịọụṿ¹²³⁴\\-])", "");
		
		// dsh+vowel
		pronounce = pronounce.replaceAll("dsh(?=[aeiouvạẹịọụṿ])", "-ts");

		// kw+vowel
		pronounce = pronounce.replaceAll("k(?=w[aeiouvạẹịọụṿ][¹²³⁴]?[¹²³⁴]?)",
				"-g");

		// gw+vowel
		pronounce = pronounce.replaceAll("g(?=w[aeiouvạẹịọụṿ][¹²³⁴]?[¹²³⁴]?)",
				"-g");

		// hl+vowel
//		pronounce = pronounce.replaceAll("h(?=l[aeiouvạẹịọụṿ][¹²³⁴]?[¹²³⁴]?)",
//				"-h");

		// tl+vowel
		pronounce = pronounce.replaceAll("t(?=l[aeiouvạẹịọụṿ][¹²³⁴]?[¹²³⁴]?)",
				"-t");

		// dl+vowel
		pronounce = pronounce.replaceAll("t(?=l[aeiouvạẹịọụṿ][¹²³⁴]?[¹²³⁴]?)",
				"-d");

		// h + semi-vowels + vowel
		pronounce = pronounce.replaceAll("h(?=(w|y)[aeiouvạẹịọụṿ¹²³⁴]+)", "-h");

		// consonent + h + consonent => consonent + h - consonent
		pronounce = pronounce.replaceAll(
				"(?<=[^aeiouvạẹịọụṿ¹²³⁴\\-])h(?=[^aeiouvạẹịọụṿ¹²³⁴\\-])", "h-");

		// hkw => h-kw
		pronounce = pronounce.replaceAll("h(?=kw)", "h-");
		// hgw => h-gw
		pronounce = pronounce.replaceAll("h(?=gw)", "h-");
		// hts => h-ts
		pronounce = pronounce.replaceAll("h(?=ts)", "h-");

		// vowel + h and vowel
		pronounce = pronounce.replaceAll(
				"(?<=[aeiouvạẹịọụṿ][¹²³⁴]?[¹²³⁴]?)(h[aeiouvạẹịọụṿ][¹²³⁴]*)",
				"-$1");

		// consonent + h and vowel
		pronounce = pronounce.replaceAll(
				"(?<=[^aeiouvạẹịọụṿ¹²³⁴\\-])(h[aeiouvạẹịọụṿ][¹²³⁴]*)", "-$1");

		// h + consonent-not-[sldth] + consonent => - h + consonent + consonent
		// pronounce=pronounce.replaceAll("h(?=[^aeiouvạẹịọụṿsldth][^aeiouvạẹịọụṿ¹²³⁴\\\\-])",
		// "-h");

		// consonent + vowel + h? + tone?
		pronounce = pronounce.replaceAll(
				"(?<=[^aeiouvạẹịọụṿ¹²³⁴\\-])([aeiouvạẹịọụṿ])([¹²³⁴]*)(h?[¹²³⁴]*)-?",
				"$1$3$2-");

		// ^ vowel + h?
		pronounce = pronounce.replaceAll("^([aeiouvạẹịọụṿ]+)([¹²³⁴]*)(h?)-?",
				"$1$3$2-");

		// split out bare "s"
		pronounce = pronounce.replaceAll("(s)(?=[^aeiouvạẹịọụṿ¹²³⁴\\-])", "s-");

		// vowel cluster splitting
		pronounce = pronounce.replaceAll(
				"-([aeiouvạẹịọụṿ¹²³⁴])(?=[aeiouvạẹịọụṿ¹²³⁴])", "-$1-");

		// look for any syllables starting with vowels left over
		pronounce = pronounce.replaceAll(
				"-([aeiouvạẹịọụṿ¹²³⁴]h?)(?=[^aeiouvạẹịọụṿ¹²³⁴\\-])", "-$1-");

		// non-combining consonent clusters should be split up...
		pronounce = pronounce.replaceAll(
				"(j|l|m|n|s|w|y)(?=[^aeiouvạẹịọụṿ¹²³⁴\\-])", "$1-");
		pronounce = pronounce.replaceAll("(k)(?=[^waeiouvạẹịọụṿ¹²³⁴\\-])",
				"$1-");
		pronounce = pronounce.replaceAll("(g)(?=[^waeiouvạẹịọụṿ¹²³⁴\\-])",
				"$1-");
		pronounce = pronounce.replaceAll("(t)(?=[^slaeiouvạẹịọụṿ¹²³⁴\\-])",
				"$1-");
		pronounce = pronounce.replaceAll("(d)(?=[^slaeiouvạẹịọụṿ¹²³⁴\\-])",
				"$1-");
		pronounce = pronounce.replaceAll("(ɂh)(?=[^wynlaeiouvạẹịọụṿ¹²³⁴\\-])",
				"ɂ");
		pronounce = pronounce.replaceAll("(h)(?=[^wynlaeiouvạẹịọụṿ¹²³⁴\\-])",
				"$1-");
		
		// strip out glottal stops last
		pronounce = pronounce.replaceAll("-?ɂ-?", "-");

		// make sure spaces don't end up as syllable parts
		pronounce = pronounce.replaceAll(" +", "- -");
		
		// eh... what the hell are the "'" stuff in the King data????
		pronounce = pronounce.replaceAll("'", "-'-");
		
		// other stuff
		pronounce = pronounce.replaceAll("([()])", "-$1-");
		
		// general cleanup
		pronounce = pronounce.replaceAll("-+", "-");
		pronounce = StringUtils.strip(pronounce, "-");
		pronounce = StringUtils.strip(pronounce);
		
		pronounce = pronounce.replaceAll("-d(?=s[aeiouvạẹịọụṿ])", "-t");
		
		// final vowel cluster splitting
		pronounce = pronounce.replaceAll("(?<=[aeiouvạẹịọụṿ][¹²³⁴]?)(?=[aeiouvạẹịọụṿ])", "-");

		return pronounce;
	}

	public void reprocess(List<String> list) {
		boolean invalid=false;
		if (list.size()==6){			
			String x = list.get(1);
			x=x.replaceAll("^Ꭰ\\[g\\]", "ᎠᎩ");
			x=x.replaceAll("^\\[g\\]ɂ?", "Ꭶ");
			list.set(1, x);
		}
		for (String entry: list) {
			invalid |= entry.contains("[");
			if (invalid) {
//				System.out.println("reprocess: "+list.get(0));
				break;
			}
		}
	}

}
