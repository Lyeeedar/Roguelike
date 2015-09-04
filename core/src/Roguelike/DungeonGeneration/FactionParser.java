package Roguelike.DungeonGeneration;

import java.io.IOException;
import java.util.Random;

import Roguelike.Util.FastEnumMap;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.XmlReader;
import com.badlogic.gdx.utils.XmlReader.Element;

public class FactionParser
{
	public enum FeaturePlacementType
	{
		FURTHEST, WALL, CENTRE, ANY
	}

	public Array<Encounter> encounters = new Array<Encounter>();

	public FastEnumMap<FeaturePlacementType, Array<Feature>> features = new FastEnumMap<FeaturePlacementType, Array<Feature>>( FeaturePlacementType.class );

	private FactionParser()
	{
		for ( FeaturePlacementType type : FeaturePlacementType.values() )
		{
			features.put( type, new Array<Feature>() );
		}
	}

	public Encounter getEncounter( Random ran, int influence )
	{
		if ( influence == 100 ) { return encounters.get( encounters.size - 1 ); }

		Array<Encounter> validEncounters = new Array<Encounter>();
		int totalWeight = 0;

		for ( Encounter enc : encounters )
		{
			if ( enc.minRange <= influence && enc.maxRange >= influence )
			{
				validEncounters.add( enc );
				totalWeight += enc.weight;
			}
		}

		int ranVal = ran.nextInt( totalWeight );

		int currentVal = 0;
		for ( Encounter enc : validEncounters )
		{
			currentVal += enc.weight;
			if ( currentVal >= ranVal ) { return enc; }
		}

		return null;
	}

	private void internalLoad( String name )
	{
		XmlReader xml = new XmlReader();
		Element xmlElement = null;

		try
		{
			xmlElement = xml.parse( Gdx.files.internal( "Entities/Enemies/" + name + "/" + name + ".xml" ) );
		}
		catch ( IOException e )
		{
			e.printStackTrace();
		}

		Element featuresElement = xmlElement.getChildByName( "Features" );
		for ( Element featureElement : featuresElement.getChildrenByName( "Feature" ) )
		{
			Feature feature = Feature.load( featureElement );
			features.get( feature.type ).add( feature );
		}

		Element encounterElement = xmlElement.getChildByName( "Encounters" );

		for ( Element encounter : encounterElement.getChildrenByName( "Encounter" ) )
		{
			Encounter enc = Encounter.load( encounter, name );
			encounters.add( enc );
		}
	}

	public static FactionParser load( String faction )
	{
		if ( !Gdx.files.internal( "Entities/Enemies/" + faction + "/" + faction + ".xml" ).exists() ) { return null; }

		FactionParser fp = new FactionParser();

		fp.internalLoad( faction );

		return fp;
	}

	public static class Encounter
	{
		public Mob[] mobs;
		public int totalMobWeight;

		public int minRange;
		public int maxRange;

		public int coverage;

		public int minCoverage;
		public int maxCoverage;

		public int weight;

		public Array<String> getMobsToPlace( int influence, int numValidTiles )
		{
			float currentInfluence = (float) ( influence - minRange ) / (float) ( maxRange - minRange );
			float currentCoverage = ( coverage * currentInfluence ) / 100;
			int numMobsToPlace = (int) Math.ceil( numValidTiles * currentCoverage );

			numMobsToPlace = MathUtils.clamp( numMobsToPlace, minCoverage, maxCoverage );

			float factor = (float) numMobsToPlace / (float) totalMobWeight;

			Array<String> mobArr = new Array<String>();

			for ( Mob mob : mobs )
			{
				float scaledNum = mob.weight * factor;
				int num = (int) Math.ceil( scaledNum );

				for ( int i = 0; i < num; i++ )
				{
					mobArr.add( mob.enemy );
				}
			}

			return mobArr;
		}

		public static Encounter load( Element xml, String name )
		{
			Encounter enc = new Encounter();
			enc.minRange = xml.getInt( "RangeMin", 0 );
			enc.maxRange = xml.getInt( "RangeMax", 100 );
			enc.coverage = xml.getInt( "Coverage", 5 );
			enc.weight = xml.getInt( "Weight", 1 );
			enc.minCoverage = xml.getInt( "MinCoverage", 0 );
			enc.maxCoverage = xml.getInt( "MaxCoverage", Integer.MAX_VALUE );

			Array<Mob> mobs = new Array<Mob>();

			Element mobsElement = xml.getChildByName( "Mobs" );

			for ( int i = 0; i < mobsElement.getChildCount(); i++ )
			{
				Element encEl = mobsElement.getChild( i );

				int weight = 0;

				try
				{
					weight = Integer.parseInt( encEl.getText() );
				}
				catch ( Exception e )
				{
				}

				mobs.add( new Mob( "Enemies/" + name + "/" + encEl.getName(), weight ) );
				enc.totalMobWeight += weight;
			}

			enc.mobs = mobs.toArray( Mob.class );

			return enc;
		}
	}

	public static class Mob
	{
		public final String enemy;
		public final int weight;

		public Mob( String enemy, int weight )
		{
			this.enemy = enemy;
			this.weight = weight;
		}
	}

	public static class Feature
	{
		public Element tileData;
		public Element environmentData;

		public int minRange;
		public int maxRange;

		public int coverage;

		public int minCoverage;
		public int maxCoverage;

		public FeaturePlacementType type;

		public static Feature load( Element xml )
		{
			Feature feature = new Feature();
			feature.minRange = xml.getInt( "RangeMin", 0 );
			feature.maxRange = xml.getInt( "RangeMax", 100 );
			feature.coverage = xml.getInt( "Coverage", 50 );
			feature.minCoverage = xml.getInt( "MinCoverage", 0 );
			feature.maxCoverage = xml.getInt( "MaxCoverage", Integer.MAX_VALUE );
			feature.tileData = xml.getChildByName( "TileData" );
			feature.environmentData = xml.getChildByName( "EnvironmentData" );
			feature.type = FeaturePlacementType.valueOf( xml.get( "Placement" ).toUpperCase() );

			return feature;
		}

		public Symbol getAsSymbol( Symbol current )
		{
			Symbol symbol = current.copy();
			symbol.character = 'F';
			symbol.tileData = tileData != null ? tileData : current.tileData;
			symbol.environmentData = environmentData != null ? environmentData : current.environmentData;

			return symbol;
		}

		public int getNumTilesToPlace( int influence, int numValidTiles )
		{
			float currentInfluence = (float) ( influence - minRange ) / (float) ( maxRange - minRange );
			float currentCoverage = ( coverage * currentInfluence ) / 100;
			int numTilesToPlace = (int) Math.ceil( numValidTiles * currentCoverage );

			numTilesToPlace = MathUtils.clamp( numTilesToPlace, minCoverage, maxCoverage );

			return numTilesToPlace;
		}
	}
}
