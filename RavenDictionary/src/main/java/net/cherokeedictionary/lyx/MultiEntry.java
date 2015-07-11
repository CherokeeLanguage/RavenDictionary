package net.cherokeedictionary.lyx;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.lang3.StringUtils;

public class MultiEntry extends LyxEntry {
	public List<DefinitionLine> deflist = new ArrayList<DefinitionLine>();
	public ExampleLine[] example = null;

	public void addDefinition(DefinitionLine def) {
		deflist.add(def);
	}

	public void addEntry(String syllabary, String pronunciation) {
		DefinitionLine def = new DefinitionLine();
		def.pronounce = pronunciation;
		def.syllabary = syllabary;
		addDefinition(def);
	}

	@Override
	public List<String> getSyllabary() {
		List<String> list = new ArrayList<>();
		for (DefinitionLine def : deflist) {
			list.add(def.syllabary);
		}
		return list;
	}

	@Override
	public List<String> getPronunciations() {
		List<String> list = new ArrayList<>();
		for (DefinitionLine def : deflist) {
			list.add(def.pronounce);
		}
		return list;
	}

	@Override
	public String getLyxCode() {
		StringBuilder sb = new StringBuilder();
		Iterator<DefinitionLine> ilist = deflist.iterator();
		sb.append(lyxSyllabaryPronounceDefinition(id, ilist.next(), pos,
				definition));
		if (ilist.hasNext()) {
			sb.append("\\begin_deeper\n");
			while (ilist.hasNext()) {
				sb.append(lyxSyllabaryPronounce(ilist.next()));
			}
			sb.append("\\end_deeper\n");
		}
		return sb.toString();
	}

	private String _sortKey = null;

	@Override
	protected String sortKey() {
		if (StringUtils.isEmpty(_sortKey)) {
			StringBuilder sb = new StringBuilder();
			for (DefinitionLine def : deflist) {
				sb.append(def.syllabary.replaceAll("[^Ꭰ-Ᏼ]", ""));
				sb.append(" ");
			}
			for (DefinitionLine def : deflist) {
				sb.append(def.pronounce.replace("-", ""));
				sb.append(" ");
			}
			_sortKey = sb.toString();
			_sortKey = _sortKey.replaceAll(" +", " ");
			_sortKey = StringUtils.strip(_sortKey);
		}
		return _sortKey;
	}
}