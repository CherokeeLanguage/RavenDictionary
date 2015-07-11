package net.cherokeedictionary.lyx;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;

public class NounEntry extends LyxEntry {
	public DefinitionLine single;
	public DefinitionLine plural;
	public ExampleLine[] example = null;

	@Override
	public List<String> getSyllabary() {
		List<String> list = new ArrayList<>();
		list.add(single.syllabary);
		list.add(plural.syllabary);
		return list;
	}

	@Override
	public List<String> getPronunciations() {
		List<String> list = new ArrayList<>();
		list.add(single.pronounce);
		list.add(plural.pronounce);
		return list;
	}

	@Override
	public String getLyxCode() {
		StringBuilder sb = new StringBuilder();
		sb.append(lyxSyllabaryPronounceDefinition(id, single, pos, definition));
		if (isOnlySyllabary(plural.syllabary)) {
			sb.append("\\begin_deeper\n");
			sb.append(lyxSyllabaryPronounce(plural));
			sb.append("\\end_deeper\n");
		}
		return sb.toString();
	}

	private String _sortKey = null;

	@Override
	protected String sortKey() {
		if (StringUtils.isEmpty(_sortKey)) {
			StringBuilder sb = new StringBuilder();
			sb.append(single.syllabary.replaceAll("[^Ꭰ-Ᏼ]", ""));
			sb.append(" ");
			sb.append(plural.syllabary.replaceAll("[^Ꭰ-Ᏼ]", ""));
			sb.append(" ");
			sb.append(single.pronounce.replace("-", ""));
			sb.append(" ");
			sb.append(plural.pronounce.replace("-", ""));
			_sortKey = sb.toString();
			_sortKey = _sortKey.replaceAll(" +", " ");
			_sortKey = StringUtils.strip(_sortKey);
		}
		return _sortKey;
	}
}