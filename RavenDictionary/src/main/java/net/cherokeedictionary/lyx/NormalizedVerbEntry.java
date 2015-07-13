package net.cherokeedictionary.lyx;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;

import com.cherokeelessons.raven.App;

public class NormalizedVerbEntry {
	public String pres3;
	public String pres1;
	public String past;
	public String habit;
	public String imp;
	public String inf;
	public List<String> getEntries(){
		List<String> list = new ArrayList<>();
		list.add(pres3);
		list.add(pres1);
		list.add(past);
		list.add(habit);
		list.add(imp);
		list.add(inf);
		return list;
	}
	public static void removeDirectObject(NormalizedVerbEntry e) {
		String object = StringUtils.getCommonPrefix(e.pres3, e.past, e.imp);
		if (StringUtils.isBlank(object)) {
			return;
		}
		App.info("Found direct object: '"+object+"'");
		e.habit=StringUtils.removeStart(e.habit, object);
		e.pres1=StringUtils.removeStart(e.pres1, object);
		e.imp=StringUtils.removeStart(e.imp, object);
		e.pres3=StringUtils.removeStart(e.pres3, object);
		e.past=StringUtils.removeStart(e.past, object);
	}
	public static void removeᎢprefix(NormalizedVerbEntry e) {
		if (!e.pres3.startsWith("Ꭲ")){
			return;
		}
		e.pres3=chop(e.pres3);
		if (e.pres1.startsWith("Ꭲ")){
			e.pres1=chop(e.pres1);
		}
		if (e.past.startsWith("Ꭵ")){
			e.past=chop(e.past);
		}
		if (e.habit.startsWith("Ꭲ")){
			e.habit=chop(e.habit);
		}
		if (e.imp.startsWith("Ꮵ")||e.imp.startsWith("Ꭲ")){
			e.imp=chop(e.imp);
		}
		if (e.inf.startsWith("Ꭵ")){
			e.inf=chop(e.inf);
		}
	}
	public static void removeᏫprefix(NormalizedVerbEntry e) {
		
		if (e.imp.startsWith("Ꮻ") && e.pres3.startsWith("Ꮻ")){
			e.imp=newPrefix("Ꭿ", e.imp);
		}
		if (e.imp.startsWith("Ꮻ") && e.pres3.startsWith("Ꮽ")){
			e.imp=chop(e.imp);
		}
		if (e.imp.startsWith("Ꮻ") && e.pres3.startsWith("Ꮹ")){
			e.imp=newPrefix("Ꭿ", e.imp);
		}
		if (e.imp.startsWith("ᏫᏕ") && e.pres3.startsWith("Ꮣ")){
			e.imp=chop(e.imp);
		}
		if (e.imp.startsWith("ᏫᏨ") && e.pres3.startsWith("Ꭴ")){
			e.imp=chop(e.imp);
		}
		if (e.imp.startsWith("ᏫᏣ")){
			e.imp=chop(e.imp);
		}
		if (e.pres3.startsWith("Ꮹ")){
			e.pres3=newPrefix("Ꭰ", e.pres3);
		}
		if (e.habit.startsWith("Ꮹ")){
			e.habit=newPrefix("Ꭰ", e.habit);
		}
		if (e.pres3.startsWith("Ꮽ")){
			e.pres3=newPrefix("Ꭴ", e.pres3);
		}
		if (e.habit.startsWith("Ꮽ")){
			e.habit=newPrefix("Ꭴ", e.habit);
		}
		if (e.pres3.startsWith("Ꮻ")){
			e.pres3=chop(e.pres3);
		}
		if (e.habit.startsWith("Ꮻ")){
			e.habit=chop(e.habit);
		}
		if (e.pres1.startsWith("Ꮹ")){
			e.pres1=newPrefix("Ꭰ", e.pres1);
		}
		if (e.pres1.startsWith("Ꮻ")){
			e.pres1=chop(e.pres1);
		}
		if (e.past.startsWith("Ꮽ")){
			e.past=newPrefix("Ꭴ", e.past);
		}
		if (e.inf.startsWith("Ꮽ")){
			e.inf=newPrefix("Ꭴ", e.inf);
		}
		warnIfStartsWithAnyRange("Ꮻ", "Ꮾ", e);
	}
	
	public static void removeᏂprefix(NormalizedVerbEntry e) {
		//they and I
		if (e.pres1.startsWith("ᏃᏥ")){
			e.pres1="Ꭳ"+chop(e.pres1);
		}
		//you all
		if (e.imp.startsWith("ᏂᏥ")){
			e.imp="Ꭲ"+chop(e.imp);
		}
		
		if (e.imp.startsWith("Ꮒ") && e.pres1.startsWith("Ꮵ")) {
			e.imp=newPrefix("Ꭿ", e.imp);
		}
		
		if (e.imp.startsWith("Ꮕ") && e.pres3.startsWith("Ꮒ")){
			e.imp=newPrefix("Ꮂ", e.imp);
		}
		if (e.imp.startsWith("Ꮒ") && e.pres3.startsWith("Ꮒ")){
			e.imp=newPrefix("Ꭿ", e.imp);
		}
		if (e.imp.startsWith("Ꮒ") && e.pres3.startsWith("Ꮔ")){
			e.imp=chop(e.imp);
		}
		if (e.imp.startsWith("Ꮒ") && e.pres3.startsWith("Ꮎ")){
			e.imp=newPrefix("Ꭿ", e.imp);
		}
		if (e.imp.startsWith("ᏂᏗ") && e.pres3.startsWith("Ꮣ")){
			e.imp=chop(e.imp);
		}
		if (e.imp.startsWith("ᏂᏨ") && e.pres3.startsWith("Ꭴ")){
			e.imp=chop(e.imp);
		}				
		if (e.pres3.startsWith("Ꮎ")){
			e.pres3=newPrefix("Ꭰ", e.pres3);
		}
		if (e.habit.startsWith("Ꮎ")){
			e.habit=newPrefix("Ꭰ", e.habit);
		}
		if (e.pres3.startsWith("Ꮔ")){
			e.pres3=newPrefix("Ꭴ", e.pres3);
		}
		if (e.habit.startsWith("Ꮔ")){
			e.habit=newPrefix("Ꭴ", e.habit);
		}
		if (e.pres3.startsWith("Ꮒ")){
			e.pres3=chop(e.pres3);
		}
		if (e.habit.startsWith("Ꮒ")){
			e.habit=chop(e.habit);
		}
		if (e.pres1.startsWith("Ꮎ")){
			e.pres1=newPrefix("Ꭰ", e.pres1);
		}
		if (e.pres1.startsWith("Ꮒ")){
			e.pres1=chop(e.pres1);
		}
		if (e.past.startsWith("Ꮔ")){
			e.past=newPrefix("Ꭴ", e.past);
		}
		if (e.inf.startsWith("ᎢᏳ")){
			e.inf=newPrefix("Ꭴ", e.inf);
		}
		if (e.inf.startsWith("ᎢᏯ")){
			e.inf=newPrefix("Ꭰ", e.inf);
		}
		if (e.inf.startsWith("ᎢᏰ")){
			e.inf=newPrefix("Ꭱ", e.inf);
		}
		if (e.inf.startsWith("ᎢᏲ")){
			e.inf=newPrefix("Ꭳ", e.inf);
		}
		warnIfStartsWithAnyRange("Ꮒ", "Ꮕ", e);
		if (e.inf.startsWith("Ꭲ")){
			App.err("Need to add Ꮒ/inf rule for: "+e.getEntries().toString());
		}
	}
	
	private static void warnIfStartsWithAnyRange(String start,
			String end, NormalizedVerbEntry e) {
		String regex="^["+Pattern.quote(start)+"-"+Pattern.quote(end)+"].*";
		Pattern pattern = Pattern.compile(regex);
		for (String element: e.getEntries()) {
			if (pattern.matcher(element).matches()){
				App.err("Need to add rule for: "+" ["+start+"-"+end+"]"+e.getEntries().toString());
			}
		}
	}
	public static void removeᏕprefix(NormalizedVerbEntry e) {
		
		//you all
		if (e.imp.startsWith("ᏗᏥ")){
			e.imp="Ꭲ"+chop(e.imp);
		}
		//they and I
		if (e.pres1.startsWith("ᏙᏥ")){
			e.pres1="Ꭳ"+chop(e.pres1);
		}
		if (e.pres1.startsWith("ᏙᏣ")){
			e.pres1="Ꭳ"+chop(e.pres1);
		}
		//weird one
		//ᎣᏍᏗ- special
		if (e.imp.startsWith("ᏗᏍ") && e.pres1.startsWith("ᏙᏍ")){
			e.imp=newPrefix("Ꭳ", e.imp);
			e.pres1=newPrefix("Ꭳ", e.pres1);
		}
		
		if (e.imp.startsWith("Ꮦ")){
			e.imp=newPrefix("Ꭾ", e.imp);
		}
		if (e.imp.startsWith("Ꮩ")){
			e.imp=newPrefix("Ꮀ", e.imp);
		}
		if (e.imp.startsWith("Ꮪ")){
			e.imp=newPrefix("Ꮁ", e.imp);
		}
		if (e.imp.startsWith("Ꮫ")){
			e.imp=newPrefix("Ꮂ", e.imp);
		}
		if (e.imp.startsWith("Ꮨ")){
			e.imp=newPrefix("Ꭿ", e.imp);
		}
		if (e.imp.startsWith("Ꮤ")){
			e.imp=newPrefix("Ꭽ", e.imp);
		}		
		if (e.imp.startsWith("Ꮧ")){
			e.imp=chop(e.imp);
		}
		if (e.imp.startsWith("Ꮥ")){
			e.imp=chop(e.imp);
		}
		if (e.pres3.startsWith("Ꮣ")){
			e.pres3=newPrefix("Ꭰ", e.pres3);
		}
		if (e.habit.startsWith("Ꮣ")){
			e.habit=newPrefix("Ꭰ", e.habit);
		}
		if (e.pres3.startsWith("Ꮪ")){
			e.pres3=newPrefix("Ꭴ", e.pres3);
		}
		if (e.habit.startsWith("Ꮪ")){
			e.habit=newPrefix("Ꭴ", e.habit);
		}
		if (e.pres3.startsWith("Ꮥ")){
			e.pres3=chop(e.pres3);
		}
		if (e.habit.startsWith("Ꮥ")){
			e.habit=chop(e.habit);
		}
		if (e.pres1.startsWith("Ꮣ")){
			e.pres1=newPrefix("Ꭰ", e.pres1);
		}
		if (e.pres1.startsWith("ᏛᏆᏨ")){
			e.pres1=newPrefix("Ꭰ", e.pres1);
		}
		if (e.pres1.startsWith("Ꮥ")){
			e.pres1=chop(e.pres1);
		}		
		if (e.past.startsWith("Ꮪ")){
			e.past=newPrefix("Ꭴ", e.past);
		}
		if (e.past.startsWith("Ꮣ")){
			e.past=newPrefix("Ꭰ", e.past);
		}
		if (e.inf.startsWith("Ꮷ")){
			e.inf=newPrefix("Ꭴ", e.inf);
		}
		warnIfStartsWithAnyRange("Ꮣ", "Ꮫ", e);
	}
	private static String chop(String e) {
		return StringUtils.substring(e, 1);
	}
	private static String newPrefix(String prefix, String e) {
		return prefix+chop(e);
	}
}