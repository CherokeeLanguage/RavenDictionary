package net.cherokeedictionary.lyx;

import net.cherokeedictionary.shared.StemEntry;

import java.util.*;

public class WordForm implements Comparable<WordForm> {
    public List<Reference> references = new ArrayList<Reference>();
    public StemEntry stemEntry = new StemEntry();

    public WordForm() {
    }

    public WordForm(WordForm wf) {
        references = wf.references;
        references.addAll(wf.references);
        stemEntry = new StemEntry(wf.stemEntry.syllabary, wf.stemEntry.stemtype);
    }

    public static void dedupeBySyllabary(List<Reference> references2) {
        Set<String> already = new HashSet<>();
        Iterator<Reference> iref = references2.iterator();
        while (iref.hasNext()) {
            String syllabary = iref.next().syllabary;
            if (already.contains(syllabary)) {
                iref.remove();
                continue;
            }
            already.add(syllabary);
        }

    }

    @Override
    public int compareTo(WordForm arg0) {
        int cmp = stemEntry.syllabary.compareTo(arg0.stemEntry.syllabary);
        if (cmp != 0) {
            return cmp;
        }
        if (Arrays.equals(references.toArray(), arg0.references.toArray())) {
            return 0;
        }
        return references.toString().compareTo(arg0.references.toString());
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof WordForm)) {
            return false;
        }
        return compareTo((WordForm) obj) == 0;
    }

    public String getLyxCode() {
        boolean briefmode = false;
        if (references.size() == 1) {
            briefmode = references.get(0).syllabary.equals(stemEntry.syllabary);
        }
        StringBuilder sb = new StringBuilder();
        sb.append("\\begin_layout Standard\n");
        sb.append("\\series bold\n");
        sb.append(LyxEntry.hyphenateSyllabary(stemEntry.syllabary));
        sb.append("\n");
        sb.append("\\series default\n");
        if (!briefmode) {
            sb.append(": ");
        }
        for (int ix = 0; ix < references.size(); ix++) {
            if (ix > 0) {
                sb.append(", ");
            }
            String ref = LyxEntry.hyphenateSyllabary(references.get(ix).syllabary);
            int id = references.get(ix).toLabel;
            if (!briefmode) {
                sb.append(ref);
            }
            sb.append(" (pg ".replace(" ",
                    "\n\\begin_inset space ~\n\\end_inset\n"));
            sb.append("\\begin_inset CommandInset ref\n"
                    + "LatexCommand pageref\n" + "reference \"");
            sb.append("_" + Integer.toString(id, Character.MAX_RADIX));
            sb.append("\"\n" + "\n\\end_inset\n");
            sb.append(")\n");
        }
        sb.append("\\end_layout\n\n");
        return sb.toString();
    }
}