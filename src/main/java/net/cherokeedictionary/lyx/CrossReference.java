package net.cherokeedictionary.lyx;

import org.apache.commons.lang3.StringUtils;

public class CrossReference {
    private String syllabary;
    private String ref;
    private int forEntry;

    public CrossReference() {
    }

    public CrossReference(int toEntry, String raw_ref, String syllabary) {
        this.forEntry = toEntry;
        this.ref = raw_ref;
        this.syllabary = syllabary;
    }

    public String getLyxCode(boolean nolayout) {
        StringBuilder sb = new StringBuilder();
        if (!nolayout) {
            sb.append("\\begin_layout Standard\n");
            sb.append("\\noindent\n");
            sb.append("\\align left\n");
        }
        sb.append("\\series bold\n");
        sb.append(LyxEntry.hyphenateSyllabary(syllabary));
        sb.append("\n");
        sb.append("\\series default\n");
        if (ref.contains("(")) {
            String sub = StringUtils.substringBetween(ref, "(", ")");
            sub = StringUtils.strip(sub);
            sb.append(" \\begin_inset Quotes eld\n\\end_inset\n");
            sb.append(sub);
            sb.append("\n\\begin_inset Quotes erd\n\\end_inset\n");
        }
        sb.append("\\begin_inset space ~\n\\end_inset\n");
        sb.append("(pg\n");
        sb.append("\\begin_inset space ~\n\\end_inset\n");
        sb.append("\\begin_inset CommandInset ref\n");
        sb.append("LatexCommand pageref\n");
        sb.append("reference \"");
        sb.append("_" + Integer.toString(forEntry, Character.MAX_RADIX));
        sb.append("\"\n\n");
        sb.append("\\end_inset\n");
        sb.append(")\n");
        if (!nolayout) {
            sb.append("\\end_layout\n");
        }
        return sb.toString();
    }

    public String getLyxCode() {
        return getLyxCode(false);
    }
}
