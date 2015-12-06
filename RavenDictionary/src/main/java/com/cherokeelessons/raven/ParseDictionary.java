package com.cherokeelessons.raven;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.Reader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.io.LineIterator;
import org.apache.commons.lang3.StringEscapeUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.text.translate.AggregateTranslator;
import org.apache.commons.lang3.text.translate.CharSequenceTranslator;
import org.apache.commons.lang3.text.translate.EntityArrays;
import org.apache.commons.lang3.text.translate.LookupTranslator;
import org.apache.commons.lang3.text.translate.NumericEntityUnescaper;

public class ParseDictionary implements Runnable {

	private final File in;
	public ParseDictionary(File in) {
		this.in=in;
		entries = new ArrayList<IEntry>();
	}
	
	private final List<IEntry> entries;
	

	public List<IEntry> getEntries() {
		return entries;
	}

	@Override
	public void run() {
		try {
			_run();
		} catch (FileNotFoundException e) {
			throw new RuntimeException(e);
		}
		Collections.sort(entries);
	}

	private void _run() throws FileNotFoundException {
		Reader reader=new FileReader(in);
		LineIterator li = new LineIterator(reader);
		while (li.hasNext()) {
			//look for CHAPTER marker
			String line = li.next();
			if (!line.startsWith("\\begin_layout Chapter")) {
				continue;
			}
			line = li.next();
			if (!line.startsWith("Dictionary")) {
				continue;
			}
			App.info("Found start of primary dictionary.");
			break;
		}
		
		final StateObject state=new StateObject();
		final StringBuilder entry = new StringBuilder();
		while (li.hasNext()) {
			//process entries until we see a new Chapter start
			String line = li.next();
			if (line.startsWith("\\begin_layout Chapter")) {
				break;
			}
			if (line.startsWith("\\begin_layout Description")){
				if (entry.length()>0) {
					String string = entry.toString();
					if (!string.contains("&rdquo;")||!string.contains("&ldquo")){
						System.err.println("BAD ENTRY: "+string);
						throw new RuntimeException("BAD ENTRY: "+string);
					}
					String fixup = fixup(string);
					if (fixup.contains("walking")){
						System.out.println(string);
						System.out.println(fixup);
					}
					IEntry parse = parse(fixup);
					entries.add(parse);
				}
				entry.setLength(0);
				entry.append(parse(line, li, state));
			}
			if (line.startsWith("\\begin_deeper")){
				entry.append(parseUntil("\\end_deeper", li, state));
			}
			continue;
		}
		if (entry.length()>0) {
			String string = entry.toString();
			entries.add(parse(fixup(string)));
		}
	}

	private String fixup(String string) {
		string = string.replace("<ul class=\"ul_dl\">", "");
		string = string.replace("<li class=\"li_dt chr\"><span class=\"dt\">", "");
		string = string.replace("</span></li><li class=\"li_dd chr\">", " ");
		string = string.replace("\\phantomsection{}", "");
		string = string.replace("&rdquo;", "|}");
		string = string.replace("&ldquo;", "{|");
		string = string.replace("</span>", "");
		if (string.endsWith("</li>")){
			string = StringUtils.substringBeforeLast(string, "</li>");
		}
		string = string.replace("</li>", "\n\t");
		string = string.replace("&nbsp;", " ");
		return string;
	}
	
	private IEntry parse(String definition) {
		Iterator<String> ilines = Arrays.asList(StringUtils.split(definition, "\n")).iterator();
		String line = StringUtils.strip(ilines.next());
		String pos = StringUtils.substringBetween(definition, "(", ")");
		if (StringUtils.isBlank(pos)) {
			pos="";
		}
		PresetEntry entry = new PresetEntry();
		String syllabary = StringUtils.substringBefore(line, " [");
		String pronounce = StringUtils.substringBetween(line, "[", "]");
		String type = StringUtils.substringBetween(line, "(", ")");
		String def = StringUtils.substringAfter(line, "{|");
		def=StringUtils.substringBeforeLast(def, "|}");
		def=StringEscapeUtils.unescapeHtml4(def);
		entry.addSyllabary(syllabary);
		entry.addPronunciation(pronounce);
		entry.setType(StringUtils.defaultString(type));
		entry.setDef(def);
		while (ilines.hasNext()) {
			line=StringUtils.strip(ilines.next());
			if (line.startsWith("-")){
				entry.addSyllabary("-");
				entry.addPronunciation("");
				continue;
			}
			if (line.startsWith("IRR")){
				entry.addSyllabary("IRR");
				entry.addPronunciation("");
				continue;
			}
			if (line.startsWith("<div class=")){
				String tmp = StringUtils.substringAfter(line, ">");
				tmp = StringUtils.substringBeforeLast(tmp, "<");
				entry.addNote(tmp);
				continue;
			}
			syllabary = StringUtils.substringBefore(line, " [");
			pronounce = StringUtils.substringBetween(line, "[", "]");
			entry.addSyllabary(syllabary);
			entry.addPronunciation(pronounce);
		}
		return entry;
	}

	private String fixSpecials(String line) {
		if (!line.contains("\\")) {
			return line;
		}
		line = line.replace("\\SpecialChar \\ldots{}", "…");
		return line;
	}
	
	private void discardUntil(String marker, LineIterator iline,
			StateObject state) {
		parseUntil(marker, iline, new StateObject());
	}
	
	private String parseUntil(String marker, LineIterator iline,
			StateObject state) {
		StringBuilder tmp = new StringBuilder();
		while (iline.hasNext()) {
			String nextline = iline.next();
			if (nextline.equals(marker)) {
				break;
			}
			tmp.append(parse(nextline, iline, state));
		}
		return tmp.toString();
	}
	
	private String begin_description(LineIterator iline,
			StateObject state) {
		StringBuilder tmp = new StringBuilder();
		if (!iline.hasNext()) {
			return "";
		}
		int g = state.size();
		String definition = parseUntil("\\end_layout", iline, state);
		while (state.size() > g) {
			definition += state.popGrouping();
		}
		String tmpdefinition = definition;

		final String defined = StringUtils.substringBefore(tmpdefinition, " ");

		if (!StringUtils.isEmpty(StringUtils.strip(defined))
				&& !defined.equals("&nbsp;")) {
			tmp.append("<li class=\"li_dt chr\"><span class=\"dt\">");
			tmp.append(defined);
			tmp.append("</span></li>");
		}
		if (definition.contains(" ")) {
			tmp.append("<li class=\"li_dd chr\">");
			tmp.append(StringUtils.substringAfter(definition, " "));
			tmp.append("</li>");
		}
		return tmp.toString();
	}
	
	private String begin_layout(String cls, LineIterator iline,
			StateObject state) {
		StringBuilder tmp = new StringBuilder();
		/*
		 * generic layouts should be top level elements
		 */
		while (state.hasGroupsToClose()) {
			tmp.append(state.popGrouping());
		}
		/*
		 * treat all other layouts as a standard div ...
		 */
		cls = cls.replaceAll("([^a-zA-Z])", "_");
		String opentag = "\n<div class=\"" + cls + "\">";
		String closetag = "</div><!-- " + cls + " -->";

		final boolean same_layout = state.isActiveGroup(closetag);

		if (same_layout) {
			opentag = "";
			closetag = "";
		}
		int g = state.size();
		if (!StringUtils.isEmpty(closetag)) {
			state.pushGrouping(closetag);
		}
		final String parsed = parseUntil("\\end_layout", iline, state);
		// if (!StringUtils.isEmpty(parsed)) {
		tmp.append(opentag);
		tmp.append(parsed);
		// }
		while (state.size() > g) {
			tmp.append(state.popGrouping());
		}
		return tmp.toString();
	}

	private static class StateObject {

		public StateObject() {
		}

		public boolean hasGroupsToClose() {
			return grouping_stack.size() > 0;
		}

		public int size() {
			return grouping_stack.size();
		}

		public boolean containsGroup(String group) {
			if (grouping_stack.size() == 0) {
				return false;
			}
			return grouping_stack.contains(group);
		}

		public boolean isActiveGroup(String group) {
			if (grouping_stack.size() == 0) {
				return false;
			}
			return grouping_stack.get(grouping_stack.size() - 1).equals(group);
		}

		public String lastGrouping() {
			if (grouping_stack.size() != 0) {
				return grouping_stack.get(grouping_stack.size() - 1);
			}
			return "";
		}

		public String popGrouping() {
			if (grouping_stack.size() == 0) {
				return "";
			}
			return grouping_stack.remove(grouping_stack.size() - 1);
		}

		public int pushGrouping(String grouping) {
			grouping_stack.add(grouping);
			return grouping_stack.size();
		}

		final private List<String> grouping_stack = new ArrayList<>();
	}
	
	private String parse(String line, LineIterator iline,
			StateObject state) {
		StringBuilder tmp = new StringBuilder();
		line = fixSpecials(line);
		whichparsing: {
			if (line.startsWith("\\begin_inset Note Note")) {
				discardUntil("\\end_inset", iline, new StateObject());
				break whichparsing;
			}
			if (line.startsWith("\\begin_inset CommandInset label")) {
				discardUntil("\\end_inset", iline, new StateObject());
				break whichparsing;
			}
			if (line.startsWith("\\begin_inset CommandInset ref")) {
				discardUntil("\\end_inset", iline, new StateObject());
				break whichparsing;
			}
			if (line.equals("\\begin_inset ERT")) {
				// discard until end of ERT
				tmp.append(parseErt(iline));
				break whichparsing;
			}
			if (line.equals("\\begin_deeper")) {
				StateObject substate = new StateObject();
				// shove in tag based on what inside of ...
				String close = "<!-- LOST -->";
				String astate = state.lastGrouping();
				whichstate: {
					if (astate.contains("</ul>")) {
						tmp.append("\n<!-- NESTED --><li class=\"nested\"><div>\n");
						close = "\n</div></li><!-- ul nested -->\n";
						break whichstate;
					}
					if (astate.contains("</ol>")) {
						tmp.append("\n<!-- NESTED --><li class=\"nested\"><div>\n");
						close = "\n</div></li><!-- ol nested -->\n";
						break whichstate;
					}
					tmp.append("\n<!-- NESTED --><div class=\"nested\">\n");
					close = "\n</div><!-- div nested -->\n";
				}
				tmp.append(parseUntil("\\end_deeper", iline, substate));
				while (substate.hasGroupsToClose()) {
					tmp.append(substate.popGrouping());
				}
				tmp.append(close);
				break whichparsing;
			}
			String group_itemize = "</ul><!-- itemize -->";
			String group_description = "</ul><!-- description -->";
			String group_enumerate = "</ol><!-- enumerate -->";
			if (line.startsWith("\\begin_layout Enumerate")) {
				if (!state.isActiveGroup(group_enumerate)) {
					while (state.containsGroup(group_itemize)) {
						tmp.append(state.popGrouping());
					}
					while (state.containsGroup(group_description)) {
						tmp.append(state.popGrouping());
					}
					state.pushGrouping(group_enumerate);
					tmp.append("\n<!-- enumerate --><ol>");
				}
				int g = state.size();
				tmp.append(begin_itemize(iline, state));
				while (state.size() > g) {
					tmp.append(state.popGrouping());
				}
				break whichparsing;
			}
			if (line.startsWith("\\begin_layout Description")) {
				if (!state.isActiveGroup(group_description)) {
					state.pushGrouping(group_description);
					tmp.append("<ul class=\"ul_dl\">");
				}
				int g = state.size();
				tmp.append(begin_description(iline, state));
				while (state.size() > g) {
					tmp.append(state.popGrouping());
				}
				break whichparsing;
			}
			if (line.startsWith("\\begin_layout ")) {
				String cls = StringUtils
						.substringAfter(line, "\\begin_layout ");
				tmp.append(begin_layout(cls, iline, state));
				break whichparsing;
			}
			if (line.equals("\\begin_inset space ~")) {
				tmp.append("&nbsp;");
				discardUntil("\\end_inset", iline, state);
				break whichparsing;
			}
			if (line.equals("\\begin_inset Quotes eld")) {
				tmp.append("&ldquo;");
				discardUntil("\\end_inset", iline, state);
				break whichparsing;
			}
			if (line.equals("\\begin_inset Quotes erd")) {
				tmp.append("&rdquo;");
				discardUntil("\\end_inset", iline, state);
				break whichparsing;
			}
			if (line.equals("\\begin_inset Newline newline")) {
				tmp.append("<br/>");
				discardUntil("\\end_inset", iline, state);
				break whichparsing;
			}
			if (line.equals("\\begin_inset CommandInset line")) {
				tmp.append("<hr/>");
				discardUntil("\\end_inset", iline, state);
				break whichparsing;
			}
			if (line.equals("\\lang english")) {
				break whichparsing;
			}
			if (line.equals("\\lang american")) {
				break whichparsing;
			}
			if (line.startsWith("\\begin_inset Newpage")) {
				discardUntil("\\end_inset", iline, state);
				while (state.hasGroupsToClose()) {
					tmp.append(state.popGrouping());
				}
				tmp.append("<!-- epub:clear page -->");
				break whichparsing;
			}
			if (line.startsWith("\\begin_inset VSpace")) {
				// while (state.hasGroupsToClose()) {
				// tmp.append(state.popGrouping());
				// }
				discardUntil("\\end_inset", iline, state);
				break whichparsing;
			}
			if (line.startsWith("\\begin_inset CommandInset toc")) {
				// while (state.hasGroupsToClose()) {
				// tmp.append(state.popGrouping());
				// }
				tmp.append("<!-- TOC -->");
				discardUntil("\\end_inset", iline, state);
				break whichparsing;
			}
			if (line.startsWith("\\begin_inset CommandInset index_print")) {
				// while (state.hasGroupsToClose()) {
				// tmp.append(state.popGrouping());
				// }
				tmp.append("<!-- INDEX -->");
				discardUntil("\\end_inset", iline, state);
				break whichparsing;
			}
			if (line.startsWith("\\begin_inset Graphics")) {}
			if (line.equals("\\backslash")) {
				tmp.append("\\");
				break whichparsing;
			}
			while (line.startsWith("\\change_deleted")) {
				do {
					line=iline.next();
				} while (!line.startsWith("\\change_"));
				line=iline.next();
			}
			while (line.startsWith("\\change_unchanged")) {
				line=iline.next();
			}
			while (line.startsWith("\\change_inserted")) {
				line=iline.next();
			}
			/*
			 * FAILSAFE DIE
			 */
			if (line.startsWith("\\") || line.startsWith("<")) {
				System.err
						.println("FATAL. Unhandled statement: '" + line + "'");
				System.exit(-1);
			}
			tmp.append(minimalEscape(line));
		}
		return tmp.toString();
	}
	
	private String minimalEscape(String text) {
		text = StringEscapeUtils.escapeHtml4(text);
		return UNESCAPE_EXTHTML4.translate(text);
	}
	
	private final CharSequenceTranslator UNESCAPE_EXTHTML4 = new AggregateTranslator(
			new LookupTranslator(EntityArrays.ISO8859_1_UNESCAPE()),
			new LookupTranslator(EntityArrays.HTML40_EXTENDED_UNESCAPE()),
			new NumericEntityUnescaper());
	
	private String parseErt(LineIterator iline) {
		StateObject state = new StateObject();
		StringBuilder tmp = new StringBuilder();
		if (!iline.hasNext()) {
			return "";
		}
		discardUntil("\\begin_layout Plain Layout", iline, state);
		String inset = parseUntil("\\end_layout", iline, state);
		parseert: {
			if (inset.startsWith("\\rule{")) {
				StringBuilder style = new StringBuilder();
				String[] parms = StringUtils.substringsBetween(inset, "{", "}");
				if (parms != null) {
					int w = Integer.parseInt(parms[0].replaceAll("[^0-9]", ""));
					String percent = ((w * 100) / 6) + "%";
					style.append("width: " + percent + ";");
					if (parms.length > 1) {
						style.append("border-height: " + parms[1] + ";");
					}
				}
				tmp.append("<hr style=\"border-style: solid;");
				tmp.append(style.toString());
				tmp.append("\"/>");
				break parseert;
			}
			if (inset.startsWith("%")) {
				break parseert;
			}
			if (inset.startsWith("\\frontmatter")) {
				break parseert;
			}
			if (inset.startsWith("\\pagestyle")) {
				break parseert;
			}
			if (inset.startsWith("\\mainmatter")) {
				break parseert;
			}
			if (inset.startsWith("\\pagenumbering")) {
				break parseert;
			}
			if (inset.startsWith("\\thispagestyle")) {
				break parseert;
			}
			if (inset.startsWith("\\begin{")) {
				break parseert;
			}
			if (inset.startsWith("\\end{")) {
				break parseert;
			}
			if (inset.startsWith("\\ThisCenterWallPaper")) {
				break parseert;
			}
			tmp.append(inset);

		}
		discardUntil("\\end_inset", iline, state);
		return tmp.toString();

	}

	private String begin_itemize(LineIterator iline, StateObject state) {
		StringBuilder tmp = new StringBuilder();
		if (!iline.hasNext()) {
			return "";
		}
		int g = state.size();
		state.pushGrouping("</li>");
		String item = parseUntil("\\end_layout", iline, state);
		tmp.append("<li>");
		tmp.append(item);
		while (state.size() > g) {
			tmp.append(state.popGrouping());
		}
		return tmp.toString();
	}
}
