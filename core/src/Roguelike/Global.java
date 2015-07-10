package Roguelike;

import java.util.EnumMap;
import java.util.HashMap;

import net.objecthunter.exp4j.Expression;
import net.objecthunter.exp4j.ExpressionBuilder;
import Roguelike.Tiles.GameTile;

import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.utils.XmlReader.Element;

import exp4j.Functions.RandomFunction;
import exp4j.Helpers.EquationHelper;
import exp4j.Operators.BooleanOperators;

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
		// Basic stats
		MAXHP,
		RANGE,
		SPEED,
		WEIGHT,
		CARRYLIMIT,
		COOLDOWN,
		
		METAL_ATK,
		METAL_PIERCE,
		METAL_DEF,
		METAL_HARDINESS,
		
		WOOD_ATK,
		WOOD_PIERCE,
		WOOD_DEF,
		WOOD_HARDINESS,
		
		AIR_ATK,
		AIR_PIERCE,
		AIR_DEF,
		AIR_HARDINESS,
		
		WATER_ATK,
		WATER_PIERCE,
		WATER_DEF,
		WATER_HARDINESS,
		
		FIRE_ATK,
		FIRE_PIERCE,
		FIRE_DEF,
		FIRE_HARDINESS		
		;
		
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

				Statistics stat = Statistics.valueOf(el.getName().toUpperCase());
				String eqn = el.getText();				
				int newVal = values.get(stat);
				
				ExpressionBuilder expB = new ExpressionBuilder(eqn);
				BooleanOperators.applyOperators(expB);
				expB.function(new RandomFunction());
				expB.variable("Value");
				
				Expression exp = EquationHelper.tryBuild(expB);
				if (exp != null)
				{
					exp.setVariable("Value", newVal);
					newVal = (int)exp.evaluate();
				}
				
				values.put(stat, newVal);
			}

			return values;
		}
		
		public static EnumMap<Statistics, Integer> copy(EnumMap<Statistics, Integer> stats)
		{
			EnumMap<Statistics, Integer> map = new EnumMap<Statistics, Integer>(Statistics.class);
			for (Statistics e : Statistics.values())
			{
				map.put(e, stats.get(e));
			}			
			return map;
		}

	}

	//----------------------------------------------------------------------
	public enum Tier1Element
	{
		METAL,
		WOOD,
		AIR,
		WATER,
		FIRE;
		
		public Tier1Element Weakness;
		public Tier1Element Strength;
		
		public Statistics Attack;
		public Statistics Pierce;
		public Statistics Defense;
		public Statistics Hardiness;
		
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
		
		Tier1Element()
		{
			Attack = Statistics.valueOf(toString()+"_ATK");
			Pierce = Statistics.valueOf(toString()+"_PIERCE");
			Defense = Statistics.valueOf(toString()+"_DEF");
			Hardiness = Statistics.valueOf(toString()+"_HARDINESS");
		}
	}
	
	//----------------------------------------------------------------------
	public enum Tier1ComboHarmful
	{
		POISON(Tier1Element.METAL, Tier1Element.WOOD),
		PARALYZE(Tier1Element.METAL, Tier1Element.AIR),
		TORPOR(Tier1Element.METAL, Tier1Element.WATER),
		IMMOLATE(Tier1Element.METAL, Tier1Element.FIRE),
		MINDSHOCK(Tier1Element.WOOD, Tier1Element.AIR),
		CORROSION(Tier1Element.WOOD, Tier1Element.WATER),
		ACID(Tier1Element.WOOD, Tier1Element.FIRE),		
		ICE(Tier1Element.WATER, Tier1Element.AIR),	
		PLASMA(Tier1Element.WATER, Tier1Element.FIRE),
		LIGHTNING(Tier1Element.AIR, Tier1Element.FIRE);
		
		public final Tier1Element[] Tier1Elements;
		Tier1ComboHarmful(Tier1Element e1, Tier1Element e2)
		{
			Tier1Elements = new Tier1Element[]{e1, e2};
		}
	}
	
	//----------------------------------------------------------------------
	public enum Tier2ElementHelpful
	{
		REGENERATION(Tier1Element.METAL, Tier1Element.WOOD),
		DODGE(Tier1Element.METAL, Tier1Element.AIR),
		HASTE(Tier1Element.METAL, Tier1Element.WATER),
		POWER(Tier1Element.METAL, Tier1Element.FIRE),
		STABILITY(Tier1Element.WOOD, Tier1Element.AIR),
		PROTECTION(Tier1Element.WOOD, Tier1Element.WATER),
		RETALIATION(Tier1Element.WOOD, Tier1Element.FIRE),
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
	public static int calculateDamage(EnumMap<Statistics, Integer> attackerStats, EnumMap<Statistics, Integer> defenderStats)
	{
		float damage = 0;
		
		for (Tier1Element el : Tier1Element.values())
		{
			float attack = attackerStats.get(el.Attack);
			float pierce = attackerStats.get(el.Pierce);
			
			float defense = defenderStats.get(el.Defense);
			float hardiness = 1.0f - defenderStats.get(el.Hardiness) / 100.0f;
			
			float maxReduction = attack * hardiness;
			
			float reduction = defense - pierce;
			reduction = MathUtils.clamp(reduction, 0, maxReduction);
			
			attack -= reduction;
			attack = Math.max(attack, 0);
			
			damage += attack;
		}
				
		return (int)Math.ceil(damage);
	}
}
