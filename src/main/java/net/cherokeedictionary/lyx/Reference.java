package net.cherokeedictionary.lyx;


public class Reference implements Comparable<Reference> {
	public Reference() {
	}

	public Reference(String syllabary, String pronounce, int toLabel) {
		this.syllabary = syllabary.intern();
		this.pronounce = pronounce.intern();
		this.toLabel = toLabel;
	}

	public String syllabary;
	public String pronounce;
	public int toLabel;
	public String pos;

	@Override
	public int compareTo(Reference o) {
		int cmp = syllabary.compareTo(o.syllabary);
		if (cmp != 0) {
			return cmp;
		}
		cmp = pronounce.compareTo(pronounce);
		if (cmp != 0) {
			return cmp;
		}
		return toLabel - o.toLabel;
	}
}