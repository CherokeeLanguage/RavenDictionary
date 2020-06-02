package net.cherokeedictionary.lyx;


public class ExampleEntry implements Comparable<ExampleEntry>{
	
	private static final String layout_start="\n\\begin_layout Standard\n";
	private static final String layout_end="\n\\end_layout\n";
	private static final String newline="\n\\begin_inset Newline newline\n\\end_inset\n";
	
	public String syllabary="";
	public String pronounce="";
	public String english="";
	
	public String getLyxCode() {
		StringBuilder sb = new StringBuilder();
		sb.append(layout_start);
		sb.append(LyxEntry.hyphenateSyllabary(markUnderlines(syllabary)));
		sb.append(newline);
		sb.append(markUnderlines(pronounce));
		sb.append(newline);
		sb.append(markUnderlines(english));
		sb.append(layout_end);		
		return sb.toString();
	}
	
	private String markUnderlines(String text) {
		text=text.replace("<u>", "\n\\bar under\n");
		text=text.replace("</u>", "\n\\bar default\n");
		return text;
	}

	@Override
	public boolean equals(Object obj) {
		if (!(obj instanceof ExampleEntry)) {
			return false;
		}
		return compareTo((ExampleEntry)obj)==0;
	}
	
	@Override
	public int compareTo(ExampleEntry o) {
		int cmp = syllabary.compareTo(o.syllabary);
		if (cmp!=0) {
			return cmp;
		}
		cmp = pronounce.compareTo(o.pronounce);
		if (cmp!=0) {
			return cmp;
		}
		return english.compareTo(o.english);
	}
}
