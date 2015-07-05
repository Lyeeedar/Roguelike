package Roguelike;

import java.util.EnumMap;
import java.util.HashMap;

import Roguelike.Tiles.GameTile;

import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.utils.XmlReader.Element;

public class Global
{
	public static final int NUM_ABILITY_SLOTS = 8;
	
	//----------------------------------------------------------------------
	public enum Direction
	{
		CENTER(0, 0),
		NORTH(0, 1),
		SOUTH(0, -1),
		EAST(1, 0),
		WEST(-1, 0),
		NORTHEAST(1, 1),
		NORTHWEST(-1, 1),
		SOUTHEAST(1, -1),
		SOUTHWEST(-1, -1);

		private final int x;
		private final int y;
		private final float angle;

		Direction(int x, int y) 
		{
			this.x = x;
			this.y = y;

			// basis vector = 0, 1
					double dot = 0*x + 1*y; // dot product
					double det = 0*y - 1*x; // determinant
					angle = (float) Math.atan2(det, dot) * MathUtils.radiansToDegrees;
		}

		public int GetX()
		{
			return x;
		}

		public int GetY()
		{
			return y;
		}

		public float GetAngle()
		{
			return angle;
		}

		public static Direction getDirection(int x, int y)
		{
			Direction d = Direction.CENTER;

			for (Direction dir : Direction.values())
			{
				if (dir.GetX() == x && dir.GetY() == y)
				{
					d = dir;
					break;
				}
			}

			return d;
		}

		public static Direction getDirection(int[] dir)
		{
			return getDirection(dir[0], dir[1]);
		}
		
		public static Direction getDirection(GameTile t1, GameTile t2)
		{
			return getDirection(t2.x - t1.x, t2.y - t1.y);
		}
	}
	
	//----------------------------------------------------------------------
	public enum Statistics
	{
		MAXHP,
		RANGE,
		SPEED,
		WEIGHT,
		COOLDOWN,

		ATTACKPOWER,
		DEFENSEPOWER,

		METALATTACK,
		WOODATTACK,
		AIRATTACK,
		WATERATTACK,
		FIREATTACK,

		METALDEFENSE,
		WOODDEFENSE,
		AIRDEFENSE,
		WATERDEFENSE,
		FIREDEFENSE;
		
		public static EnumMap<Statistics, Integer> getStatisticsBlock()
		{
			EnumMap<Statistics, Integer> stats = new EnumMap<Statistics, Integer>(Statistics.class);
			
			for (Statistics stat : Statistics.values())
			{
				stats.put(stat, 0);
			}

			return stats;
		}

		public static EnumMap<Statistics, Integer> load(Element xml, EnumMap<Statistics, Integer> values)
		{
			for (int i = 0; i < xml.getChildCount(); i++)
			{
				Element el = xml.getChild(i);

				values.put(Statistics.valueOf(el.getName().toUpperCase()), Integer.parseInt(el.getText()));
			}

			return values;
		}
		
		public static EnumMap<Statistics, Integer> copy(EnumMap<Statistics, Integer> stats)
		{
			EnumMap<Statistics, Integer> newStats = new EnumMap<Statistics, Integer>(Statistics.class);
			for (Statistics s : Statistics.values())
			{
				newStats.put(s, stats.get(s));
			}			
			return newStats;
		}

	}

	//----------------------------------------------------------------------
	public enum Tier1Element
	{
		METAL(Statistics.METALATTACK, Statistics.METALDEFENSE),
		WOOD(Statistics.WOODATTACK, Statistics.WOODDEFENSE),
		AIR(Statistics.AIRATTACK, Statistics.AIRDEFENSE),
		WATER(Statistics.WATERATTACK, Statistics.WATERDEFENSE),
		FIRE(Statistics.FIREATTACK, Statistics.FIREDEFENSE);
		
		public Tier1Element Weakness;
		public Tier1Element Strength;
		
		static
		{
			Tier1Element.METAL.Weakness = Tier1Element.FIRE;
			Tier1Element.METAL.Strength = Tier1Element.WOOD;
			
			Tier1Element.WOOD.Weakness = Tier1Element.METAL;
			Tier1Element.WOOD.Strength = Tier1Element.AIR;
			
			Tier1Element.AIR.Weakness = Tier1Element.WOOD;
			Tier1Element.AIR.Strength = Tier1Element.WATER;
			
			Tier1Element.WATER.Weakness = Tier1Element.AIR;
			Tier1Element.WATER.Strength = Tier1Element.FIRE;
			
			Tier1Element.FIRE.Weakness = Tier1Element.WATER;
			Tier1Element.FIRE.Strength = Tier1Element.METAL;
		}
		
		public Statistics AttackStatistic;
		public Statistics DefenseStatistic;
		
		Tier1Element(Statistics att, Statistics def)
		{
			this.AttackStatistic = att;
			this.DefenseStatistic = def;
		}
		
		public static EnumMap<Tier1Element, Integer> getElementMap()
		{
			EnumMap<Tier1Element, Integer> map = new EnumMap<Tier1Element, Integer>(Tier1Element.class);
			
			for (Tier1Element el : Tier1Element.values())
			{
				map.put(el, 0);
			}
			
			return map;
		}
	}
	
	//----------------------------------------------------------------------
	public enum Tier2ElementHarmful
	{
		// Status Effects
		POISON(Tier1Element.METAL, Tier1Element.WOOD),
		PARALYZE(Tier1Element.METAL, Tier1Element.AIR),
		TORPOR(Tier1Element.METAL, Tier1Element.WATER),
		IMMOLATE(Tier1Element.METAL, Tier1Element.FIRE),
		
		// ABility Modifiers
		EXTEND(Tier1Element.WOOD, Tier1Element.AIR),
		ENHANCE(Tier1Element.WOOD, Tier1Element.WATER),
		EXPLODE(Tier1Element.WOOD, Tier1Element.FIRE),
		
		// Damage
		ICE(Tier1Element.WATER, Tier1Element.AIR),	
		PLASMA(Tier1Element.WATER, Tier1Element.FIRE),
		LIGHTNING(Tier1Element.AIR, Tier1Element.FIRE);
		
		public final Tier1Element[] Tier1Elements;
		Tier2ElementHarmful(Tier1Element e1, Tier1Element e2)
		{
			Tier1Elements = new Tier1Element[]{e1, e2};
		}
	}
	
	//----------------------------------------------------------------------
	public enum Tier2ElementHelpful
	{
		// Status Effects
		REGENERATION(Tier1Element.METAL, Tier1Element.WOOD),
		DODGE(Tier1Element.METAL, Tier1Element.AIR),
		HASTE(Tier1Element.METAL, Tier1Element.WATER),
		POWER(Tier1Element.METAL, Tier1Element.FIRE),

		// ABility Modifiers
		EXTEND(Tier1Element.WOOD, Tier1Element.AIR),
		ENHANCE(Tier1Element.WOOD, Tier1Element.WATER),
		EXPLODE(Tier1Element.WOOD, Tier1Element.FIRE),

		// Damage
		ICECHARGE(Tier1Element.WATER, Tier1Element.AIR),	
		PLASMACHARGE(Tier1Element.WATER, Tier1Element.FIRE),
		LIGHTNINGCHARGE(Tier1Element.AIR, Tier1Element.FIRE);

		public final Tier1Element[] Tier1Elements;
		Tier2ElementHelpful(Tier1Element e1, Tier1Element e2)
		{
			Tier1Elements = new Tier1Element[]{e1, e2};
		}
	}
	
	//----------------------------------------------------------------------
	public static int calculateDamage(EnumMap<Statistics, Integer> _attack, EnumMap<Statistics, Integer> _defense)
	{
		float attackScale = 0;
		
		for (Tier1Element el : Tier1Element.values())
		{
			Tier1Element strength = el.Strength;
			Tier1Element weakness = el.Weakness;
			
			float defense = _defense.get(strength.DefenseStatistic) - ( _defense.get(weakness.DefenseStatistic) / 2.0f );
			
			if (defense <= 0)
			{
				defense = Math.abs(defense) + 1;
			}
			else
			{
				defense = 1 / ( defense + 1 );
			}
			
			attackScale += _attack.get(el.AttackStatistic) * defense;
		}
		
		float attack = ( _attack.get(Statistics.ATTACKPOWER) * attackScale ) / _defense.get(Statistics.DEFENSEPOWER);
		
		return (int)Math.ceil(attack);
	}
}
