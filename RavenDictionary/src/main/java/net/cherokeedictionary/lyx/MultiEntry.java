package net.cherokeedictionary.lyx;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.lang3.StringUtils;

import com.cherokeelessons.log.Log;

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
		boolean femaleOnly = definition.matches(".*?\\b(She|Hers|Her)\\b.*?");
		boolean posessive = definition.matches(".*?\\b(His|Hers|Her)\\b.*?");
		String meMine = posessive ? "My " : "Me ";
		String himHis = posessive ? (femaleOnly ? "Her " : "His ") : (femaleOnly ? "Her " : "Him ");
		StringBuilder sb = new StringBuilder();
		Iterator<DefinitionLine> ilist = deflist.iterator();
		DefinitionLine first = ilist.next();
		sb.append(lyxSyllabaryPronounceDefinition(id, first, pos, definition));
		int count = 1;
		int mode = deflist.size();
		if (ilist.hasNext()) {
			sb.append("\\begin_deeper\n");
			while (ilist.hasNext()) {
				count++;
				DefinitionLine next = ilist.next();
				String gloss;
				glossSelect: {
					boolean plural1st = next.syllabary.matches("Ꮣ[ᎩᏆᏇᏉᏊᏋ].*?");
					if (plural1st){
						Log.getLogger(this).warning("Plural 1st: "+definition);
					}
					boolean plural = next.syllabary.matches("[ᏓᏕᏗᏙᏚᏛᏔᏖᏘᏤᏦᏧᏨ].*?");
					if (mode == 2) {
						if (count==2 && (plural && !plural1st)) {
							gloss=himHis + LDOTS;
						}
						if (count==2 && (!plural || plural1st)) {
							gloss=meMine + LDOTS;
						} else {
							gloss=himHis + LDOTS;
						}
						
						if (plural) {
							gloss += " (more than one)";
						}
						break glossSelect;
					}
					if (mode == 4) {
						if (count==3 || count==4) {
							gloss=meMine + LDOTS;
						} else {
							gloss=himHis + LDOTS;
						}
						if (plural) {
							gloss += " (more than one)";
						}
						break glossSelect;
					}
					gloss="";
				}
				sb.append(lyxSyllabaryPronounce(next, gloss));
			}
			sb.append("\\end_deeper\n");
		}
		if (getNotes().size() != 0) {
			sb.append("\n\\begin_deeper\n");
			getNotes().stream().forEach(note -> {
				sb.append("\n\\begin_layout Standard\n");
				sb.append(note);
				sb.append("\n\\end_layout\n");
			});
			sb.append("\n\\end_deeper\n");
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