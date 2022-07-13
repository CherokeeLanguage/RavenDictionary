package net.cherokeedictionary.lyx;

import com.cherokeelessons.raven.App;
import com.cherokeelessons.raven.JsonConverter;
import net.cherokeedictionary.lyx.LyxEntry.HasStemmedForms;
import net.cherokeedictionary.shared.StemEntry;
import net.cherokeedictionary.shared.StemType;
import org.apache.commons.lang3.StringUtils;

import java.text.Normalizer;
import java.text.Normalizer.Form;
import java.util.ArrayList;
import java.util.List;

public class VerbEntry extends LyxEntry implements HasStemmedForms {
    public DefinitionLine present3rd = null;
    public DefinitionLine present1st = null;
    public DefinitionLine remotepast = null;
    public DefinitionLine habitual = null;
    public DefinitionLine imperative = null;
    public DefinitionLine infinitive = null;
    public ExampleLine[] example = null;
    private String _sortKey = null;

    static String chopPrefix(String text) {
        return newPrefix("", text);
    }

    static String newPrefix(String prefix, String text) {
        if (StringUtils.isBlank(text)) {
            return "";
        }
        return prefix + StringUtils.substring(text, 1);
    }

    @Override
    public String getLyxCode() {
        boolean helperIsHas = definition.matches(".*?(He|She) has\\b.*?");
        boolean isFemaleOnly = definition.matches(".*?\\bShe\\b.*?");
        boolean let_it = present1st == null || StringUtils.isBlank(present1st.syllabary)
                || present1st.syllabary.startsWith("-");
        final String nfdPronounce = Normalizer.normalize(imperative.pronounce, Form.NFD);
		boolean progressiveImperative = nfdPronounce.replaceAll("(?i)[^a-z]", "").endsWith("esdi");
        String helperVerb1st = "am";
        String helperVerb3rdPast = "did";
        if (helperIsHas) {
            helperVerb1st = "have";
            helperVerb3rdPast = "had";
        }

        StringBuilder sb = new StringBuilder();
        sb.append(lyxSyllabaryPronounceDefinition(id, present3rd, pos, definition));
        sb.append("\\begin_deeper\n");
        if (let_it) {
            sb.append(lyxSyllabaryPronounce(present1st));
            sb.append(lyxSyllabaryPronounce(remotepast, "It " + helperVerb3rdPast + LDOTS));
            sb.append(lyxSyllabaryPronounce(habitual, "It often" + LDOTS));
            if (progressiveImperative) {
            	sb.append(lyxSyllabaryPronounce(imperative, "It will be" + LDOTS));
            } else {
            	sb.append(lyxSyllabaryPronounce(imperative, "Let it" + LDOTS));
            }
            sb.append(lyxSyllabaryPronounce(infinitive, "For it" + LDOTS));
        } else {
            String subject = isFemaleOnly ? "She" : "He";
            String object = isFemaleOnly ? "her" : "him";
            sb.append(lyxSyllabaryPronounce(present1st, "I " + helperVerb1st + LDOTS));
            sb.append(lyxSyllabaryPronounce(remotepast, subject + " " + helperVerb3rdPast + LDOTS));
            sb.append(lyxSyllabaryPronounce(habitual, subject + " often" + LDOTS));
            if (progressiveImperative) {
            	sb.append(lyxSyllabaryPronounce(imperative, "You will be" + LDOTS));
            } else {
            	sb.append(lyxSyllabaryPronounce(imperative, "Let you" + LDOTS));
            }
            sb.append(lyxSyllabaryPronounce(infinitive, "For " + object + LDOTS));
        }
        sb.append("\\end_deeper\n");
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

    @Override
    protected String sortKey() {
        if (StringUtils.isEmpty(_sortKey)) {
            StringBuilder sb = new StringBuilder();
            sb.append(present3rd.syllabary.replaceAll("[^Ꭰ-Ᏼ]", ""));
            sb.append(" ");
            sb.append(present1st.syllabary.replaceAll("[^Ꭰ-Ᏼ]", ""));
            sb.append(" ");
            sb.append(remotepast.syllabary.replaceAll("[^Ꭰ-Ᏼ]", ""));
            sb.append(" ");
            sb.append(habitual.syllabary.replaceAll("[^Ꭰ-Ᏼ]", ""));
            sb.append(" ");
            sb.append(imperative.syllabary.replaceAll("[^Ꭰ-Ᏼ]", ""));
            sb.append(" ");
            sb.append(infinitive.syllabary.replaceAll("[^Ꭰ-Ᏼ]", ""));
            sb.append(" ");
            sb.append(present3rd.pronounce.replace("-", ""));
            sb.append(" ");
            sb.append(present1st.pronounce.replace("-", ""));
            sb.append(" ");
            sb.append(remotepast.pronounce.replace("-", ""));
            sb.append(" ");
            sb.append(habitual.pronounce.replace("-", ""));
            sb.append(" ");
            sb.append(imperative.pronounce.replace("-", ""));
            sb.append(" ");
            sb.append(infinitive.pronounce.replace("-", ""));
            _sortKey = sb.toString();
            _sortKey = _sortKey.replaceAll(" +", " ");
            _sortKey = StringUtils.strip(_sortKey);
        }
        return _sortKey;
    }

    @Override
    public List<String> getSyllabary() {
        List<String> list = new ArrayList<>();
        list.add(present3rd.syllabary);
        list.add(present1st.syllabary);
        list.add(remotepast.syllabary);
        list.add(habitual.syllabary);
        list.add(imperative.syllabary);
        list.add(infinitive.syllabary);
        return list;
    }

    @Override
    public List<String> getPronunciations() {
        List<String> list = new ArrayList<>();
        list.add(present3rd.pronounce);
        list.add(present1st.pronounce);
        list.add(remotepast.pronounce);
        list.add(habitual.pronounce);
        list.add(imperative.pronounce);
        list.add(infinitive.pronounce);
        return list;
    }

    @Override
    public List<StemEntry> getStems() {
        NormalizedVerbEntry e = new NormalizedVerbEntry();

        e.pres3 = StringUtils.strip(present3rd.syllabary.replaceAll("[^Ꭰ-Ᏼ]", ""));
        if (e.pres3.contains(",")) {
            e.pres3 = StringUtils.substringBefore(e.pres3, ",");
            e.pres3 = StringUtils.strip(e.pres3);
        }
        e.pres1 = StringUtils.strip(present1st.syllabary.replaceAll("[^Ꭰ-Ᏼ]", ""));
        if (e.pres1.contains(",")) {
            e.pres1 = StringUtils.substringAfterLast(e.pres1, ",");
            e.pres1 = StringUtils.strip(e.pres1);
        }
        e.past = StringUtils.strip(remotepast.syllabary.replaceAll("[^Ꭰ-Ᏼ]", ""));
        if (e.past.contains(",")) {
            e.past = StringUtils.substringBefore(e.past, ",");
            e.past = StringUtils.strip(e.past);
        }
        e.habit = StringUtils.strip(habitual.syllabary.replaceAll("[^Ꭰ-Ᏼ]", ""));
        if (e.habit.contains(",")) {
            e.habit = StringUtils.substringBefore(e.habit, ",");
            e.habit = StringUtils.strip(e.habit);
        }
        e.imp = StringUtils.strip(imperative.syllabary.replaceAll("[^Ꭰ-Ᏼ]", ""));
        if (e.imp.contains(",")) {
            e.imp = StringUtils.substringAfterLast(e.imp, ",");
            e.imp = StringUtils.strip(e.imp);
        }
        e.imp = fixImperativeSuffix(e.imp.replaceAll("[^Ꭰ-Ᏼ]", ""));
        e.inf = StringUtils.strip(infinitive.syllabary.replaceAll("[^Ꭰ-Ᏼ]", ""));
        if (e.inf.contains(",")) {
            e.inf = StringUtils.substringBefore(e.inf, ",");
            e.inf = StringUtils.strip(e.inf);
        }
        /*
         * Strip direct object if easy to identify
         */
        NormalizedVerbEntry.removeDirectObject(e);
        /*
         * Strip Ꮻ- prefix if easy to identify
         */
        NormalizedVerbEntry.removeᏫprefix(e);
        /*
         * Strip Ꮒ- prefix if easy to identify
         */
        NormalizedVerbEntry.removeᏂprefix(e);
        /*
         * String Ꮥ- prefix if easy to identify
         */
        NormalizedVerbEntry.removeᏕprefix(e);
        /*
         * String Ꭲ- (again) prefix if easy to identify
         */
        NormalizedVerbEntry.removeᎢprefix(e);

        /*
         * Ꭰ
         */
        if (e.pres3.startsWith("Ꭰ") && e.pres1.startsWith("Ꮵ")) {
            return generateConsonentStems(e);
        }
        if (e.pres3.startsWith("Ꭰ") && e.pres1.startsWith("Ꭶ")) {
            return generateVowelStems("Ꭰ", e);
        }
        if (e.pres3.startsWith("Ꭰ") && !e.past.startsWith("ᎤᏩ")) {
            if (e.pres3.equals("ᎠᎦᏍᎦ")) {
                new JsonConverter().toJson(generateVowelStems("Ꭰ", e));
            }
            return generateVowelStems("Ꭰ", e);
        }

        /*
         * Ꭶ
         */
        if (e.pres3.startsWith("Ꭶ") && e.pres1.startsWith("Ꮵ")) {
            return generateConsonentStems(e);
        }
        if (e.pres3.startsWith("Ꭶ") && e.pres1.startsWith("Ꭶ")) {
            return generateVowelStems("Ꭰ", e);
        }

        /*
         * Ꭷ
         */
        if (e.pres3.startsWith("Ꭷ") && e.pres1.startsWith("Ꮵ")) {
            return generateConsonentStems(e);
        }
        if (e.pres3.startsWith("Ꭷ") && e.pres1.startsWith("Ꭶ")) {
            return generateVowelStems("Ꭰ", e);
        }
        if (e.pres3.startsWith("Ꭷ") && e.imp.startsWith("Ꭿ")) {
            return generateConsonentStems(e);
        }

        if (e.pres3.startsWith("Ꭷ") && e.past.startsWith("Ꭴ") && !e.past.matches("^[Ꮹ-Ꮾ].*")) {
            return generateConsonentStems(e);
        }

        if (e.pres3.startsWith("Ꭷ") && StringUtils.isEmpty(e.past) && StringUtils.isEmpty(e.imp)
                && StringUtils.isEmpty(e.inf)) {
            return generateConsonentStems(e);
        }

        if (e.pres3.startsWith("Ꭴ") && e.pres1.startsWith("ᎠᏆ")) {
            return generateVowelStems("Ꭰ", e);
        }
        if (e.pres3.startsWith("Ꭼ") && e.past.startsWith("ᎤᏩ")) {
            e.past = chopPrefix(e.past);
            e.inf = chopPrefix(e.inf);
            return generateVowelStems("Ꭵ", e);
        }
        if (e.pres3.startsWith("Ꭶ") && e.past.startsWith("ᎤᏩ")) {
            e.past = chopPrefix(e.past);
            e.inf = chopPrefix(e.inf);
            return generateVowelStems("Ꭰ", e);
        }
        if (e.pres3.startsWith("Ꭴ") && e.pres1.startsWith("ᎠᎩ")) {
            return generateConsonentStems(e);
        }
        if (e.pres3.startsWith("Ꭴ") && e.pres1.startsWith("ᎠᎧ")) {
            return generateVowelStems("Ꭵ", e);
        }
        if (e.pres3.startsWith("ᎤᏮ") && e.pres1.startsWith("ᎠᏋ")) {
            e.pres3 = chopPrefix(e.pres3);
            e.past = chopPrefix(e.past);
            e.habit = chopPrefix(e.habit);
            e.imp = chopPrefix(e.inf);
            return generateVowelStems("Ꭵ", e);
        }
        if (e.pres3.startsWith("ᎤᏪ") && e.pres1.startsWith("ᎠᏇ")) {
            e.pres3 = chopPrefix(e.pres3);
            e.past = chopPrefix(e.past);
            e.habit = chopPrefix(e.habit);
            e.imp = chopPrefix(e.inf);
            return generateVowelStems("Ꭱ", e);
        }
        if (e.pres3.startsWith("ᎤᏬ") && e.pres1.startsWith("ᎠᏉ")) {
            e.pres3 = chopPrefix(e.pres3);
            e.past = chopPrefix(e.past);
            e.habit = chopPrefix(e.habit);
            e.imp = chopPrefix(e.inf);
            return generateVowelStems("Ꭳ", e);
        }
        if (e.pres3.startsWith("Ꮽ") && e.pres1.startsWith("ᏩᏆ")) {
            e.pres3 = chopPrefix(e.pres3);
            e.past = chopPrefix(e.past);
            e.habit = chopPrefix(e.habit);
            e.imp = chopPrefix(e.inf);
            return generateVowelStems("Ꭰ", e);
        }

        if (e.pres3.startsWith("Ꭸ") && e.past.startsWith("ᎤᏪ")) {
            e.past = chopPrefix(e.past);
            e.inf = chopPrefix(e.inf);
            return generateVowelStems("Ꭱ", e);
        }

        if (e.pres3.startsWith("Ꭺ") && e.past.startsWith("ᎤᏬ")) {
            e.past = chopPrefix(e.past);
            e.inf = chopPrefix(e.inf);
            return generateVowelStems("Ꭳ", e);
        }

        if (e.pres3.startsWith("Ꭻ") && e.past.startsWith("ᎤᏭ")) {
            e.past = chopPrefix(e.past);
            e.inf = chopPrefix(e.inf);
            return generateVowelStems("Ꭴ", e);
        }

        if (e.pres3.startsWith("Ꭼ") && e.past.startsWith("ᎤᏮ")) {
            e.past = chopPrefix(e.past);
            e.inf = chopPrefix(e.inf);
            return generateVowelStems("Ꭵ", e);
        }
        if (e.pres3.startsWith("Ꭱ") && e.imp.startsWith("Ꭾ")) {
            e.past = chopPrefix(e.past);
            e.inf = chopPrefix(e.inf);
            return generateVowelStems("Ꭱ", e);
        }
        /*
         * eh! "Ꭲ-/Ꭿ-" stemmed verbs don't parse nicely!
         */
        if (e.pres3.startsWith("Ꭹ") && e.past.startsWith("ᎤᏫ")) {
            List<StemEntry> list = new ArrayList<StemEntry>();
            list.add(new StemEntry(newPrefix("Ꭲ", e.pres3), StemType.PresentContinous));
            list.add(new StemEntry(newPrefix("Ꭿ", chopPrefix(e.past)), StemType.RemotePast));
            list.add(new StemEntry(newPrefix("Ꭲ", e.habit), StemType.Habitual));
            if (e.imp.startsWith("Ꮂ")) {
                list.add(new StemEntry(newPrefix("Ꭵ", e.imp), StemType.Immediate));
            } else {
                App.err("Normalize Corner Case Needed: " + e.getEntries().toString());
            }
            list.add(new StemEntry(newPrefix("Ꭿ", chopPrefix(e.inf)), StemType.Deverbal));
            return list;
        }

        if (e.pres3.startsWith("Ꭴ") && !e.pres3.matches("^Ꭴ[Ꮹ-Ꮾ].*")) {
            return generateConsonentStems(e);
        }

        if (e.pres3.startsWith("Ꭶ") && !e.past.matches("^Ꭴ[Ꮹ-Ꮾ].*")) {
            return generateConsonentStems(e);
        }

        /*
         * corner case for ᎬᏙᎠ / ktoa and similar entries (Ꭶ + Ꮩ => ᎬᏙ and no
         * 1st person or past entry)
         */
        if (e.pres3.startsWith("Ꭼ") && e.imp.startsWith("Ꭽ")) {
            return generateVowelStems("Ꭰ", e);
        }
        /*
         * corner case for ᎬᎿ similar entries where they have no past entry)
         */
        if (e.pres3.startsWith("Ꭼ") && e.imp.startsWith("Ꮂ")) {
            return generateVowelStems("Ꭵ", e);
        }
        /*
         * "Ꭼ" + !"ᎤᏮ" is an odd corner case and should always be processed
         * close to last...
         */
        if (e.pres3.startsWith("Ꭼ") && e.past.startsWith("Ꭴ")) {
            return generateVowelStems("Ꭵ", e);
        }
        App.info("No normalization method for: " + e.getEntries().toString());
        return new ArrayList<StemEntry>();
    }

    public List<StemEntry> generateVowelStems(String vowel, NormalizedVerbEntry e) {
        if (e.imp.startsWith("Ꮻ")) {
            e.imp = chopPrefix(e.imp);
        }
        List<StemEntry> list = new ArrayList<StemEntry>();
        list.add(new StemEntry(newPrefix(vowel, e.pres3), StemType.PresentContinous));
        list.add(new StemEntry(newPrefix(vowel, e.past), StemType.RemotePast));
        list.add(new StemEntry(newPrefix(vowel, e.habit), StemType.Habitual));
        list.add(new StemEntry(newPrefix(vowel, e.imp), StemType.Immediate));
        list.add(new StemEntry(newPrefix(vowel, e.inf), StemType.Deverbal));
        return list;
    }

    public List<StemEntry> generateConsonentStems(NormalizedVerbEntry e) {
        if (e.imp.startsWith("Ꮻ")) {
            e.imp = chopPrefix(e.imp);
        }
        List<StemEntry> list = new ArrayList<StemEntry>();
        list.add(new StemEntry(chopPrefix(e.pres3), StemType.PresentContinous));
        list.add(new StemEntry(chopPrefix(e.past), StemType.RemotePast));
        list.add(new StemEntry(chopPrefix(e.habit), StemType.Habitual));
        list.add(new StemEntry(chopPrefix(e.imp), StemType.Immediate));
        list.add(new StemEntry(chopPrefix(e.inf), StemType.Deverbal));
        return list;
    }

    private String fixImperativeSuffix(String imp) {
        if (StringUtils.isBlank(imp)) {
            return "";
        }
        String suffix = StringUtils.right(imp, 1);
        /*
         * matches -Ꭷ !
         */
        if (StringUtils.containsAny("ᎠᎦᎧᎭᎳᎹᎾᎿᏆᏌᏓᏔᏜᏝᏣᏩᏯ", suffix)) {
            return imp;
        }
        /*
         * does not match -Ꭷ !
         */
        while (!StringUtils.containsAny("ᎠᎦᎭᎳᎹᎾᎿᏆᏌᏓᏔᏜᏝᏣᏩᏯ", suffix)) {
            char c = suffix.charAt(0);
            if (c < 'Ꭰ') {
                return imp;
            }
            c--;
            suffix = String.valueOf(c);
        }
        String recent_past_form = StringUtils.left(imp, imp.length() - 1) + suffix;
        return recent_past_form;
    }
}