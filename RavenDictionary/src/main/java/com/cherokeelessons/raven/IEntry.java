package com.cherokeelessons.raven;

import java.util.List;

public interface IEntry {

	public int compareTo(IEntry o);

	public boolean equals(Object obj);

	public String formattedDefinition();

	public String getDef();

	public List<String> getPronunciations();

	public List<String> getSyllabary();

	public String getType();

	void setDef(String def);

	public void setType(String type);

	public int size();

	public String sortKey();

	void setGenus(String genus);

	String getGenus();
}