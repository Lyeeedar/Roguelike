package Roguelike.DungeonGeneration;

import java.io.IOException;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.XmlReader;
import com.badlogic.gdx.utils.XmlReader.Element;

public class EncounterParser
{
	public final Encounter[] encounters;
	
	public EncounterParser(String enemy)
	{
		XmlReader xml = new XmlReader();
		Element xmlElement = null;

		try
		{
			xmlElement = xml.parse(Gdx.files.internal("Entities/Enemies/"+enemy+"/"+enemy+".xml"));
		} 
		catch (IOException e)
		{
			e.printStackTrace();
		}
		
		Array<Encounter> encArray = new Array<Encounter>();
		
		Element encounterElement = xmlElement.getChildByName("Encounters");
		
		for (Element encounter : encounterElement.getChildrenByName("Encounter"))
		{
			Encounter enc = new Encounter();
			
			if (encounter.getAttribute("Boss", null) != null)
			{
				enc.isBoss = true;
			}
			
			Array<String> mobs = new Array<String>();
			
			for (int i = 0; i < encounter.getChildCount(); i++)
			{
				Element encEl = encounter.getChild(i);
				
				int count = 0;
				
				try
				{
					count = Integer.parseInt(encEl.getText());
				} catch (Exception e) {}
				
				for (int ii = 0; ii < count; ii++)
				{
					mobs.add("Enemies/"+enemy+"/"+encEl.getName());
				}
			}
			
			enc.mobs = mobs.toArray(String.class);
			
			encArray.add(enc);
		}
		
		encounters = encArray.toArray(Encounter.class);
	}
	
	public static class Encounter
	{
		public boolean isBoss = false;
		public String[] mobs;
	}
}
