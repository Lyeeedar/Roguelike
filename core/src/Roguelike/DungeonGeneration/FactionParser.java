package Roguelike.DungeonGeneration;

import java.io.IOException;
import java.util.EnumMap;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.XmlReader;
import com.badlogic.gdx.utils.XmlReader.Element;

public class FactionParser
{
	public enum FeaturePlacementType
	{
		FURTHEST,
		WALL,
		CENTRE,
		ANY
	}
	
	public Array<Encounter> encounters = new Array<Encounter>();
	
	public EnumMap<FeaturePlacementType, Array<Feature>> features = new EnumMap<FeaturePlacementType, Array<Feature>>(FeaturePlacementType.class);
	
	private FactionParser()
	{
		for (FeaturePlacementType type : FeaturePlacementType.values())
		{
			features.put(type, new Array<Feature>());
		}
	}
		
	private void internalLoad(String name)
	{
		XmlReader xml = new XmlReader();
		Element xmlElement = null;

		try
		{
			xmlElement = xml.parse(Gdx.files.internal("Entities/Enemies/"+name+"/"+name+".xml"));
		} 
		catch (IOException e)
		{
			e.printStackTrace();
		}
		
		Element featuresElement = xmlElement.getChildByName("Features");
		for (Element featureElement : featuresElement.getChildrenByName("Feature"))
		{
			Feature feature = new Feature();
			feature.minRange = featureElement.getInt("RangeMin", 0);
			feature.maxRange = featureElement.getInt("RangeMax", 100);
			feature.coverage = featureElement.getInt("Coverage", 50);
			feature.tileData = featureElement.getChildByName("TileData");
			feature.environmentData = featureElement.getChildByName("EnvironmentData");
			
			FeaturePlacementType type = FeaturePlacementType.valueOf(featureElement.get("Placement").toUpperCase());
			
			features.get(type).add(feature);
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
					mobs.add("Enemies/"+name+"/"+encEl.getName());
				}
			}
			
			enc.mobs = mobs.toArray(String.class);
			
			encArray.add(enc);
		}		
	}
	
	public static FactionParser load(String faction)
	{
		FactionParser fp = new FactionParser();
		
		fp.internalLoad(faction);
		
		return fp;
	}
	
	public static class Encounter
	{
		public boolean isBoss = false;
		public String[] mobs;
	}
	
	public static class Feature
	{
		public Element tileData;
		
		public Element environmentData;
		
		public int minRange;
		public int maxRange;
		
		public int coverage;
		
		public Symbol getAsSymbol(Symbol current)
		{
			Symbol symbol = new Symbol();
			symbol.character = 'F';
			symbol.tileData = tileData != null ? tileData : current.tileData;
			symbol.environmentData = environmentData != null ? environmentData : current.environmentData;
			symbol.metaValue = current.metaValue;
			
			return symbol;
		}
		
		public int getNumTilesToPlace(int influence, int numValidTiles)
		{
			float currentInfluence = (float)(influence - minRange) / (float)(maxRange - minRange);
			float currentCoverage = ((float)coverage * currentInfluence) / 100;
			int numTilesToPlace = (int)Math.ceil(numValidTiles * currentCoverage);
			
			return numTilesToPlace;
		}
	}
}
