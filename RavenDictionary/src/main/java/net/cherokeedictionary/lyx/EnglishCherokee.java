package net.cherokeedictionary.lyx;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.lang3.StringUtils;

public class EnglishCherokee implements Comparable<EnglishCherokee> {
	private String english;

	public EnglishCherokee() {
	}

	public List<Reference> refs = new ArrayList<>();

	public EnglishCherokee(EnglishCherokee ec) {
		this.english = ec.english.intern();
		this.refs.addAll(ec.refs);
	}

	public void setEnglish(String english) {
		english = StringUtils.left(english, 1).toUpperCase()
				+ StringUtils.substring(english, 1);
		this.english = english;
	}

	public String getDefinition() {
		String eng = StringUtils.strip(english);
		while (eng.endsWith(".") || eng.endsWith(",")) {
			eng = StringUtils.strip(StringUtils.left(eng, eng.length() - 1));
		}
		eng = transform(eng);
		if (eng.startsWith("(")) {
			String sub = StringUtils.substringBetween(eng, "(", ")");
			eng = StringUtils.substringAfter(eng, "(" + sub + ")");
			eng += " " + "(" + sub + ")";
		}
		eng = transform(eng);
		while (eng.endsWith(".") || eng.endsWith(",")) {
			eng = StringUtils.strip(StringUtils.left(eng, eng.length() - 1));
		}
		eng = eng.replace(", (", " (");
		eng = StringUtils.left(eng, 1).toUpperCase()
				+ StringUtils.substring(eng, 1);
		return eng;
	}

	public String getLyxCode(boolean bold) {
		StringBuilder sb = new StringBuilder();
		String eng = StringUtils.strip(getDefinition().replace("\\n", " "));
		if (bold) {
			sb.append("\\begin_layout Standard\n");
			sb.append("\\series bold\n");
			sb.append(eng);
			sb.append("\n");
			sb.append("\\series default\n");
		} else {
			sb.append("\\begin_layout Standard\n");
			sb.append(eng);
		}

		sb.append(": ");
		Iterator<Reference> irefs = refs.iterator();
		if (irefs.hasNext()) {
			Reference ref = irefs.next();
			sb.append(LyxEntry.hyphenateSyllabary(ref.syllabary));
			sb.append(" [");
			sb.append(stickySpaces(ref.pronounce));
			sb.append("] ");
			sb.append(stickySpaces("pg "));
			sb.append("\\begin_inset CommandInset ref\n"
					+ "LatexCommand pageref\n" + "reference \"");
			sb.append("_"+Integer.toString(ref.toLabel, Character.MAX_RADIX));
			sb.append("\"\n" + "\\end_inset\n");
			sb.append(")\n");
		}
		while (irefs.hasNext()) {
			Reference ref = irefs.next();
			sb.append(", ");
			sb.append(LyxEntry.hyphenateSyllabary(ref.syllabary));
			sb.append(" [");
			sb.append(stickySpaces(ref.pronounce));
			sb.append("] ");
			sb.append(stickySpaces("(pg "));
			sb.append("\\begin_inset CommandInset ref\n"
					+ "LatexCommand pageref\n" + "reference \"");
			sb.append("_"+Integer.toString(ref.toLabel, Character.MAX_RADIX));
			sb.append("\"\n" + "\\end_inset\n");
			sb.append(")\n");
		}
		sb.append("\\end_layout\n\n");
		return sb.toString();
	}

	private String transform(String eng) {
		eng = eng.replace("\\n", " ");
		eng = StringUtils.strip(eng);
		String lc = eng.toLowerCase();
		if (lc.startsWith("n.")) {
			eng = StringUtils.substring(eng, 2);
			eng = StringUtils.strip(eng);
			lc = eng.toLowerCase();
		}
		if (lc.startsWith("v. t.")) {
			eng = StringUtils.substring(eng, 5);
			eng = StringUtils.strip(eng);
			lc = eng.toLowerCase();
		}
		if (lc.startsWith("v.t.")) {
			eng = StringUtils.substring(eng, 4);
			eng = StringUtils.strip(eng);
			lc = eng.toLowerCase();
		}
		if (lc.startsWith("v. i.")) {
			eng = StringUtils.substring(eng, 5);
			eng = StringUtils.strip(eng);
			lc = eng.toLowerCase();
		}
		if (lc.startsWith("v.i.")) {
			eng = StringUtils.substring(eng, 4);
			eng = StringUtils.strip(eng);
			lc = eng.toLowerCase();
		}
		if (lc.startsWith("adv.")) {
			eng = StringUtils.substring(eng, 4);
			eng = StringUtils.strip(eng);
			lc = eng.toLowerCase();
		}
		if (lc.startsWith("adj.")) {
			eng = StringUtils.substring(eng, 4);
			eng = StringUtils.strip(eng);
			lc = eng.toLowerCase();
		}
		if (lc.contains(".") && lc.indexOf(".") < 4 && !lc.startsWith("1")) {
//			App.err("WARNING: BAD DEFINITION! => " + eng);
		}
		if (lc.startsWith("becoming ")) {
			eng = StringUtils.substring(eng, 9) + " (becoming)";
			lc = eng.toLowerCase();
		}
		chopper: {
			if (lc.startsWith("he, it is ")) {
				eng = StringUtils.substring(eng, 10);
				break chopper;
			}
			if (lc.startsWith("they're ")) {
				eng = StringUtils.substring(eng, 8) + " (they are)";
				break chopper;
			}
			if (lc.startsWith("they are ")) {
				eng = StringUtils.substring(eng, 9) + " (they are)";
				break chopper;
			}
			if (lc.startsWith("at the ")) {
				eng = StringUtils.substring(eng, 7) + " (at the)";
				break chopper;
			}
			if (lc.startsWith("at a ")) {
				eng = StringUtils.substring(eng, 5) + " (at a)";
				break chopper;
			}
			if (lc.startsWith("at ")) {
				eng = StringUtils.substring(eng, 3) + " (at)";
				break chopper;
			}
			if (lc.startsWith("in the ")) {
				eng = StringUtils.substring(eng, 7) + " (in the)";
				break chopper;
			}
			if (lc.startsWith("in a ")) {
				eng = StringUtils.substring(eng, 5) + " (in a)";
				break chopper;
			}
			if (lc.startsWith("in (")) {
				break chopper;
			}
			if (lc.startsWith("in ")) {
				eng = StringUtils.substring(eng, 3) + " (in)";
				break chopper;
			}
			if (lc.startsWith("on the ")) {
				eng = StringUtils.substring(eng, 7) + " (on the)";
				break chopper;
			}
			if (lc.startsWith("on a ")) {
				eng = StringUtils.substring(eng, 5) + " (on a)";
				break chopper;
			}
			if (lc.startsWith("on ")) {
				eng = StringUtils.substring(eng, 3) + " (on)";
				break chopper;
			}
			if (lc.startsWith("she's ")) {
				eng = StringUtils.substring(eng, 6);
				break chopper;
			}
			if (lc.startsWith("he/it is ")) {
				eng = StringUtils.substring(eng, 9);
				break chopper;
			}
			if (lc.startsWith("he, it's ")) {
				eng = StringUtils.substring(eng, 9);
				break chopper;
			}
			if (lc.startsWith("is ")) {
				eng = StringUtils.substring(eng, 3);
				break chopper;
			}
			if (lc.startsWith("the ")) {
				eng = StringUtils.substring(eng, 4);
				break chopper;
			}
			if (lc.startsWith("a ") && !lc.startsWith("a lot")) {
				eng = StringUtils.substring(eng, 2);
				break chopper;
			}
			if (lc.startsWith("an ")) {
				eng = StringUtils.substring(eng, 3);
				break chopper;
			}
			if (lc.startsWith("he's ")) {
				eng = StringUtils.substring(eng, 5);
				break chopper;
			}
			if (lc.startsWith("he is ")) {
				eng = StringUtils.substring(eng, 6);
				break chopper;
			}
			if (lc.startsWith("it's ")) {
				eng = StringUtils.substring(eng, 5);
				break chopper;
			}
			if (lc.startsWith("it is ")) {
				eng = StringUtils.substring(eng, 6);
				break chopper;
			}
			if (lc.startsWith("his, her")) {
				if (eng.length() > 8) {
					eng = StringUtils.substring(eng, 8) + " (his/her)";
					break chopper;
				}
			}
			if (lc.startsWith("its ")) {
				eng = StringUtils.substring(eng, 4);
				break chopper;
			}
			if (lc.startsWith("his ")) {
				eng = StringUtils.substring(eng, 4) + " (his)";
				break chopper;
			}
			if (lc.startsWith("her ")) {
				eng = StringUtils.substring(eng, 4) + " (her)";
				break chopper;
			}
			if (lc.startsWith("he ")) {
				eng = StringUtils.substring(eng, 3);
				break chopper;
			}
			if (lc.startsWith("she ")) {
				eng = StringUtils.substring(eng, 4);
				break chopper;
			}
			if (lc.startsWith("it ")) {
				eng = StringUtils.substring(eng, 3);
				break chopper;
			}
		}
		return eng;
	}

	@Override
	public int compareTo(EnglishCherokee arg0) {
		String e1 = getDefinition();
		String e2 = arg0.getDefinition();
		int cmp = e1.compareToIgnoreCase(e2);
		if (cmp != 0) {
			return cmp;
		}
		cmp = e1.compareTo(e2);
		if (cmp != 0) {
			return cmp;
		}
		List<Reference> ref1 = new ArrayList<>();
		List<Reference> ref2 = new ArrayList<>();
		ref1.addAll(refs);
		ref2.addAll(arg0.refs);
		Collections.sort(ref1);
		Collections.sort(ref2);

		int rsz1 = ref1.size();
		int rsz2 = ref2.size();
		for (int ix = 0; ix < rsz1 && ix < rsz2; ix++) {
			cmp = ref1.get(ix).syllabary.compareTo(ref2.get(ix).syllabary);
			if (cmp != 0) {
				return cmp;
			}
		}
		cmp = rsz1 - rsz2;
		if (cmp != 0) {
			return cmp;
		}

		for (int ix = 0; ix < rsz1 && ix < rsz2; ix++) {
			cmp = ref1.get(ix).pronounce.compareTo(ref2.get(ix).pronounce);
			if (cmp != 0) {
				return cmp;
			}
		}

		for (int ix = 0; ix < rsz1 && ix < rsz2; ix++) {
			cmp = ref1.get(ix).toLabel - ref2.get(ix).toLabel;
			if (cmp != 0) {
				return cmp;
			}
		}
		return 0;
	}

	@Override
	public boolean equals(Object obj) {
		if (!(obj instanceof EnglishCherokee)) {
			return false;
		}
		return compareTo((EnglishCherokee) obj) == 0;
	}

	public String stickySpaces(String text) {
		return text.replace(" ", "\n\\begin_inset space ~\n\\end_inset\n");
	}
}