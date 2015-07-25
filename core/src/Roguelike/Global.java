package Roguelike;

import java.util.EnumMap;
import java.util.HashMap;

import net.objecthunter.exp4j.Expression;
import net.objecthunter.exp4j.ExpressionBuilder;
import Roguelike.Entity.Entity;
import Roguelike.Entity.EnvironmentEntity;
import Roguelike.Entity.GameEntity;
import Roguelike.GameEvent.GameEventHandler;
import Roguelike.GameEvent.Damage.DamageObject;
import Roguelike.Tiles.GameTile;
import Roguelike.UI.MessageStack.Line;
import Roguelike.UI.MessageStack.Message;

import com.badlogic.gdx.graphics.Color;
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

		private Direction clockwise;
		private Direction anticlockwise;

		static
		{
			Direction.CENTER.clockwise = Direction.CENTER;
			Direction.CENTER.anticlockwise = Direction.CENTER;

			Direction.NORTH.anticlockwise = Direction.NORTHWEST;
			Direction.NORTH.clockwise = Direction.NORTHEAST;

			Direction.NORTHEAST.anticlockwise = Direction.NORTH;
			Direction.NORTHEAST.clockwise = Direction.EAST;

			Direction.EAST.anticlockwise = Direction.NORTHEAST;
			Direction.EAST.clockwise = Direction.SOUTHEAST;

			Direction.SOUTHEAST.anticlockwise = Direction.EAST;
			Direction.SOUTHEAST.clockwise = Direction.SOUTH;

			Direction.SOUTH.anticlockwise = Direction.SOUTHEAST;
			Direction.SOUTH.clockwise = Direction.SOUTHWEST;

			Direction.SOUTHWEST.anticlockwise = Direction.SOUTH;
			Direction.SOUTHWEST.clockwise = Direction.WEST;

			Direction.WEST.anticlockwise = Direction.SOUTHWEST;
			Direction.WEST.clockwise = Direction.NORTHWEST;

			Direction.NORTHWEST.anticlockwise = Direction.WEST;
			Direction.NORTHWEST.clockwise = Direction.NORTH;
		}

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

		public Direction GetClockwise()
		{
			return clockwise;
		}

		public Direction GetAnticlockwise()
		{
			return anticlockwise;
		}

		public static Direction getDirection(int x, int y)
		{
			x = MathUtils.clamp(x, -1, 1);
			y = MathUtils.clamp(y, -1, 1);

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

		public static HashMap<String, Integer> statsBlockToVariableBlock(EnumMap<Statistics, Integer> stats)
		{
			HashMap<String, Integer> variableMap = new HashMap<String, Integer>();

			for (Statistics key : stats.keySet())
			{
				variableMap.put(key.toString(), stats.get(key));
			}

			return variableMap;
		}

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
		METAL(Color.LIGHT_GRAY),
		WOOD(Color.GREEN),
		AIR(Color.CYAN),
		WATER(Color.BLUE),
		FIRE(Color.ORANGE);

		public Tier1Element Weakness;
		public Tier1Element Strength;

		public Statistics Attack;
		public Statistics Pierce;
		public Statistics Defense;
		public Statistics Hardiness;

		public Color Colour;

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

		Tier1Element(Color colour)
		{
			this.Colour = colour;

			Attack = Statistics.valueOf(toString()+"_ATK");
			Pierce = Statistics.valueOf(toString()+"_PIERCE");
			Defense = Statistics.valueOf(toString()+"_DEF");
			Hardiness = Statistics.valueOf(toString()+"_HARDINESS");
		}

		public static EnumMap<Tier1Element, Integer> getElementBlock()
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
	public static void calculateDamage(Entity attacker, Entity defender, HashMap<String, Integer> additionalAttack, boolean doEvents)
	{
		DamageObject damObj = new DamageObject(attacker, defender, additionalAttack);
		runEquations(damObj);

		if (doEvents)
		{
			for (GameEventHandler handler : attacker.getAllHandlers())
			{
				handler.onDealDamage(damObj);
			}

			for (GameEventHandler handler : defender.getAllHandlers())
			{
				handler.onReceiveDamage(damObj);
			}
		}

		printMessages(damObj);

		defender.applyDamage(damObj.getTotalDamage(), attacker);
	}

	//----------------------------------------------------------------------
	private static void runEquations(DamageObject damObj)
	{
		for (Tier1Element el : Tier1Element.values())
		{
			float attack = damObj.attackerVariableMap.get(el.Attack.toString().toLowerCase());
			float pierce = damObj.attackerVariableMap.get(el.Pierce.toString().toLowerCase());

			float defense = damObj.defenderVariableMap.get(el.Defense.toString().toLowerCase());
			float hardiness = 1.0f - damObj.defenderVariableMap.get(el.Hardiness.toString().toLowerCase()) / 100.0f;

			float maxReduction = attack * hardiness;

			float reduction = defense - pierce;
			reduction = Math.min(reduction, maxReduction);

			attack -= reduction;
			attack = Math.max(attack, 0);

			damObj.damageMap.put(el, (int)MathUtils.ceil(attack));
		}

		damObj.setDamageVariables();
	}

	//----------------------------------------------------------------------
	private static void printMessages(DamageObject damObj)
	{		
		Line line = new Line(new Message(" ("));

		boolean first = true;
		for (Tier1Element el : Tier1Element.values())
		{
			int dam = damObj.damageMap.get(el);

			if (dam != 0)
			{
				if (first)
				{
					first = false;					
					line.messages.add(new Message("" + dam, el.Colour));
				}
				else
				{
					line.messages.add(new Message(", " + dam, el.Colour));
				}
			}			
		}

		line.messages.add(new Message(")"));

		line.messages.insert(0, new Message(damObj.attacker.name + " hits " + damObj.defender.name + " for " + damObj.getTotalDamage() + " damage!"));

		if (damObj.getTotalDamage() > 0)
		{
			RoguelikeGame.Instance.addConsoleMessage(line);
		}
	}
}
