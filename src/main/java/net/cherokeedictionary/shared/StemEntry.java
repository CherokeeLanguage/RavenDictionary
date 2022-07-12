package net.cherokeedictionary.shared;


public class StemEntry {
    public String syllabary = "";
    public StemType stemtype = StemType.Other;

    public StemEntry(String syllabary, StemType stemType) {
        this.syllabary = syllabary;
        this.stemtype = stemType;
    }

    public StemEntry(StemEntry entry) {
        this.syllabary = entry.syllabary;
        this.stemtype = entry.stemtype;
    }
    public StemEntry() {
    }
}