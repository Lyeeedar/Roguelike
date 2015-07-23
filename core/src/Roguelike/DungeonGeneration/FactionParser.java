package Roguelike.DungeonGeneration;

import java.io.IOException;
import java.util.EnumMap;
import java.util.Random;

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
	
	public Encounter getEncounter(Random ran, int influence)
	{
		if (influence == 100)
		{
			return encounters.get(encounters.size-1);
		}
		
		Array<Encounter> validEncounters = new Array<Encounter>();
		int totalWeight = 0;
		
		for (Encounter enc : encounters)
		{
			if (enc.minRange <= influence && enc.maxRange >= influence)
			{
				validEncounters.add(enc);
				totalWeight += enc.weight;
			}
		}
		
		int ranVal = ran.nextInt(totalWeight);
		
		int currentVal = 0;
		for (Encounter enc : validEncounters)
		{
			currentVal += enc.weight;
			if (currentVal >= ranVal)
			{
				return enc;
			}
		}
		
		return null;
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
			
		Element encounterElement = xmlElement.getChildByName("Encounters");
		
		for (Element encounter : encounterElement.getChildrenByName("Encounter"))
		{
			Encounter enc = new Encounter();
			
			enc.minRange = encounter.getInt("RangeMin", 0);
			enc.maxRange = encounter.getInt("RangeMax", 100);
			enc.weight = encounter.getInt("Weight", 1);
			
			Array<String> mobs = new Array<String>();
			
			Element mobsElement = encounter.getChildByName("Mobs");
			
			for (int i = 0; i < mobsElement.getChildCount(); i++)
			{
				Element encEl = mobsElement.getChild(i);
				
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
			
			encounters.add(enc);
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
		public String[] mobs;
		
		public int minRange;
		public int maxRange;
		
		public int weight;
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
			Symbol symbol = current.copy();
			symbol.character = 'F';
			symbol.tileData = tileData != null ? tileData : current.tileData;
			symbol.environmentData = environmentData != null ? environmentData : current.environmentData;
			
			return symbol;
		}
		
		public int getNumTilesToPlace(int influence, int numValidTiles)
		{
			float currentInfluence = (float)(influence - minRange) / (float)(maxRange - minRange);
			float currentCoverage = ((float)coverage * currentInfluence) / 100;
			int numTilesToPlace = (int)Math.ceil(numValidTiles * currentCoverage);
			
			System.out.println("Influence: " + influence + " Num Valid Tiles: " + numValidTiles + " Tiles to place: " + numTilesToPlace);
			
			return numTilesToPlace;
		}
	}
}
