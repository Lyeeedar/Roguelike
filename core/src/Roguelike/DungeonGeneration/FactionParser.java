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

	public String name;

	public Array<DungeonFileParser.DFPRoom> rooms = new Array<DungeonFileParser.DFPRoom>(  );
	public Array<Creature> creatures = new Array<Creature>();
	public Array<String> bosses = new Array<String>();
	public Array<String> minibosses = new Array<String>(  );

	public FastEnumMap<FeaturePlacementType, Array<Feature>> features = new FastEnumMap<FeaturePlacementType, Array<Feature>>( FeaturePlacementType.class );

	private FactionParser()
	{
		for ( FeaturePlacementType type : FeaturePlacementType.values() )
		{
			features.put( type, new Array<Feature>() );
		}
	}

	public Array<Creature> getCreatures( Random ran, float difficulty, int influence )
	{
		Array<Creature> validCreatures = new Array<Creature>();

		for ( Creature creature : creatures )
		{
			if ( creature.minInfluence <= influence && creature.maxInfluence >= influence )
			{
				validCreatures.add( creature );
			}
		}

		Array<Creature> chosen = new Array<Creature>();
		float maxCost = difficulty;
		if ( maxCost < 1 )
		{
			maxCost = 1;
		}

		while ( maxCost >= 0 && validCreatures.size > 0 )
		{
			int index = ran.nextInt( validCreatures.size );
			Creature creature = validCreatures.get( index );

			if ( maxCost < creature.cost )
			{
				validCreatures.removeIndex( index );
			}
			else
			{
				maxCost -= creature.cost;
				chosen.add( creature );
			}
		}

		System.out.println( "Difficulty: " + difficulty + " Num Spawned: " + chosen.size );

		return chosen;
	}

	private void internalLoad( String faction, String path )
	{
		name = faction;

		XmlReader xml = new XmlReader();
		Element xmlElement = null;

		try
		{
			xmlElement = xml.parse( Gdx.files.internal( "Entities/" + path + "/" + faction + ".xml" ) );
		}
		catch ( IOException e )
		{
			e.printStackTrace();
		}

		Element featuresElement = xmlElement.getChildByName( "Features" );
		if ( featuresElement != null )
		{
			for ( Element featureElement : featuresElement.getChildrenByName( "Feature" ) )
			{
				Feature feature = Feature.load( featureElement );
				features.get( feature.type ).add( feature );
			}
		}

		Element creaturesElement = xmlElement.getChildByName( "Creatures" );
		if ( creaturesElement != null )
		{
			for ( int i = 0; i < creaturesElement.getChildCount(); i++ )
			{
				Element creatureElement = creaturesElement.getChild( i );

				creatures.add( Creature.load( creatureElement, path ) );
			}
		}

		Element bossesElement = xmlElement.getChildByName( "Bosses" );
		if ( bossesElement != null )
		{
			for ( int i = 0; i < bossesElement.getChildCount(); i++ )
			{
				Element bossElement = bossesElement.getChild( i );

				bosses.add( path + "/" + bossElement.getName() );
			}
		}

		Element miniBossesElement = xmlElement.getChildByName( "MiniBosses" );
		if ( miniBossesElement != null )
		{
			for ( int i = 0; i < miniBossesElement.getChildCount(); i++ )
			{
				Element miniBossElement = miniBossesElement.getChild( i );

				minibosses.add( path + "/" + miniBossElement.getName() );
			}
		}

		Element roomsElement = xmlElement.getChildByName( "Rooms" );
		if (roomsElement != null)
		{
			for (int i = 0; i < roomsElement.getChildCount(); i++)
			{
				Element roomElement = roomsElement.getChild( i );

				DungeonFileParser.DFPRoom room = DungeonFileParser.DFPRoom.parse( roomElement );

				// Fill in full creature paths
				for ( Symbol s : room.symbolMap.values() )
				{
					if (s.entityData != null)
					{
						// Attempt to find the creature
						String creaturePath = path + "/" + s.entityData;

						boolean found = false;

						if (!found)
						{
							for (Creature c : creatures)
							{
								if (c.entityName.equals( creaturePath ))
								{
									found = true;
									break;
								}
							}
						}

						if (!found)
						{
							for (String c : minibosses)
							{
								if (c.equals( creaturePath ))
								{
									found = true;
									break;
								}
							}
						}

						if (!found)
						{
							for (String c : bosses)
							{
								if (c.equals( creaturePath ))
								{
									found = true;
									break;
								}
							}
						}

						if (found)
						{
							s.entityData = creaturePath;
						}
					}
				}

				rooms.add(room);
			}
		}
	}

	public static FactionParser load( String faction )
	{
		String path = "Enemies/" + faction;
		if ( !Gdx.files.internal( "Entities/" + path + "/" + faction + ".xml" ).exists() )
		{
			path = "NPC/" + faction;

			if ( !Gdx.files.internal( "Entities/" + path + "/" + faction + ".xml" ).exists() ) { return null; }
		}

		FactionParser fp = new FactionParser();

		fp.internalLoad( faction, path );

		return fp;
	}

	public static class Creature
	{
		public String entityName;
		public float cost;

		public int minInfluence;
		public int maxInfluence;

		public static Creature load( Element xml, String path )
		{
			Creature creature = new Creature();

			creature.entityName = path + "/" + xml.getName();
			creature.cost = xml.getFloat( "Cost", 1 );

			creature.minInfluence = xml.getInt( "MinInfluence", 0 );
			creature.maxInfluence = xml.getInt( "MaxInfluence", 100 );

			return creature;
		}
	}

	public static class Feature
	{
		public Element tileData;
		public Element environmentData;
		public Element fieldData;

		public int minRange;
		public int maxRange;

		public int coverage;

		public float minCoverage = 0;
		public float maxCoverage = 100;

		public FeaturePlacementType type;

		public static Feature load( Element xml )
		{
			Feature feature = new Feature();
			feature.minRange = xml.getInt( "RangeMin", 0 );
			feature.maxRange = xml.getInt( "RangeMax", 100 );

			Element coverageElement = xml.getChildByName( "Coverage" );
			if ( coverageElement != null )
			{
				feature.coverage = Integer.parseInt( coverageElement.getText() );
				feature.minCoverage = coverageElement.getFloatAttribute( "Min", feature.minCoverage );
				feature.maxCoverage = coverageElement.getFloatAttribute( "Max", feature.maxCoverage );
			}
			else
			{
				feature.coverage = 50;
			}

			feature.tileData = xml.getChildByName( "TileData" );
			feature.environmentData = xml.getChildByName( "EnvironmentData" );
			feature.fieldData = xml.getChildByName( "FieldData" );
			feature.type = FeaturePlacementType.valueOf( xml.get( "Placement" ).toUpperCase() );

			return feature;
		}

		public Symbol getAsSymbol( Symbol current )
		{
			Symbol symbol = current.copy();
			symbol.character = 'F';
			symbol.tileData = tileData != null ? tileData : current.tileData;
			symbol.environmentData = environmentData != null ? environmentData : current.environmentData;
			symbol.fieldData = fieldData != null ? fieldData : current.fieldData;

			return symbol;
		}

		public int getNumTilesToPlace( int influence, int numValidTiles )
		{
			float currentInfluence = (float) ( influence - minRange ) / (float) ( maxRange - minRange );
			float currentCoverage = ( coverage * currentInfluence ) / 100;
			currentCoverage = MathUtils.clamp( currentCoverage, minCoverage, maxCoverage );

			int numTilesToPlace = (int) Math.floor( numValidTiles * currentCoverage );

			return numTilesToPlace;
		}
	}
}
