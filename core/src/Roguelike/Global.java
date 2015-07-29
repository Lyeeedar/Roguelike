package Roguelike;

import java.util.EnumMap;
import java.util.HashMap;

import net.objecthunter.exp4j.Expression;
import net.objecthunter.exp4j.ExpressionBuilder;
import Roguelike.DungeonGeneration.RecursiveDockGenerator;
import Roguelike.Entity.Entity;
import Roguelike.Entity.EnvironmentEntity;
import Roguelike.Entity.GameEntity;
import Roguelike.GameEvent.GameEventHandler;
import Roguelike.GameEvent.Damage.DamageObject;
import Roguelike.Items.Item;
import Roguelike.Levels.Level;
import Roguelike.Screens.GameScreen;
import Roguelike.Tiles.GameTile;
import Roguelike.UI.MessageStack.Line;
import Roguelike.UI.MessageStack.Message;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.XmlReader.Element;
import com.sun.xml.internal.ws.util.StringUtils;

import exp4j.Functions.RandomFunction;
import exp4j.Helpers.EquationHelper;
import exp4j.Operators.BooleanOperators;

public class Global
{
	//----------------------------------------------------------------------
	public static final int NUM_ABILITY_SLOTS = 8;
	
	//----------------------------------------------------------------------
	public static int TileSize = 32;
	
	//----------------------------------------------------------------------
	public static Level CurrentLevel;
	
	//----------------------------------------------------------------------
	public static float AUT = 0;

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
	
		public static Array<int[]> buildCone(Direction dir, int[] start, int range)
		{
			Array<int[]> hitTiles = new Array<int[]>();
			
			Direction anticlockwise = dir.GetAnticlockwise();
			Direction clockwise = dir.GetClockwise();
			
			int[] acwOffset = { dir.GetX() - anticlockwise.GetX(), dir.GetY() - anticlockwise.GetY() };
			int[] cwOffset = { dir.GetX() - clockwise.GetX(), dir.GetY() - clockwise.GetY() };
			
			hitTiles.add(new int[]{
					start[0] + anticlockwise.GetX(), 
					start[1] + anticlockwise.GetY()}
			);
			
			hitTiles.add(new int[]{
					start[0] + dir.GetX(), 
					start[1] + dir.GetY()}
			);
			
			hitTiles.add(new int[]{
					start[0] + clockwise.GetX(), 
					start[1] + clockwise.GetY()}
			);
			
			for (int i = 2; i <= range; i++)
			{
				int acx = start[0] + anticlockwise.GetX()*i;
				int acy = start[1] + anticlockwise.GetY()*i;
				
				int nx = start[0] + dir.GetX()*i;
				int ny = start[1] + dir.GetY()*i;
				
				int cx = start[0] + clockwise.GetX()*i;
				int cy = start[1]+ clockwise.GetY()*i;
				
				// add base tiles
				hitTiles.add(new int[]{acx, acy});
				hitTiles.add(new int[]{nx, ny});
				hitTiles.add(new int[]{cx, cy});
				
				// add anticlockwise - mid
				int acwdiff = Math.max(Math.abs(acx - nx), Math.abs(acy - ny));
				for (int ii = 1; ii < acwdiff; ii++)
				{
					int px = nx + acwOffset[0] * ii;
					int py = ny + acwOffset[1] * ii;
					
					hitTiles.add(new int[]{px, py});
				}
				
				// add mid - clockwise
				int cwdiff = Math.max(Math.abs(cx - nx), Math.abs(cy - ny));
				for (int ii = 1; ii < cwdiff; ii++)
				{
					int px = nx + cwOffset[0] * ii;
					int py = ny + cwOffset[1] * ii;
					
					hitTiles.add(new int[]{px, py});
				}
			}
			
			return hitTiles;
		}
	}

	//----------------------------------------------------------------------
	public enum Statistic
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

		public static HashMap<String, Integer> statsBlockToVariableBlock(EnumMap<Statistic, Integer> stats)
		{
			HashMap<String, Integer> variableMap = new HashMap<String, Integer>();

			for (Statistic key : stats.keySet())
			{
				variableMap.put(key.toString(), stats.get(key));
			}

			return variableMap;
		}

		public static EnumMap<Statistic, Integer> getStatisticsBlock()
		{
			EnumMap<Statistic, Integer> stats = new EnumMap<Statistic, Integer>(Statistic.class);

			for (Statistic stat : Statistic.values())
			{
				stats.put(stat, 0);
			}

			return stats;
		}

		public static EnumMap<Statistic, Integer> load(Element xml, EnumMap<Statistic, Integer> values)
		{
			for (int i = 0; i < xml.getChildCount(); i++)
			{

				Element el = xml.getChild(i);

				Statistic stat = Statistic.valueOf(el.getName().toUpperCase());
				String eqn = el.getText();				
				int newVal = values.get(stat);

				ExpressionBuilder expB = EquationHelper.createEquationBuilder(eqn);
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

		public static EnumMap<Statistic, Integer> copy(EnumMap<Statistic, Integer> map)
		{
			EnumMap<Statistic, Integer> newMap = new EnumMap<Statistic, Integer>(Statistic.class);
			for (Statistic e : Statistic.values())
			{
				newMap.put(e, map.get(e));
			}			
			return newMap;
		}

		public static String formatString(String input)
		{
			String[] words = input.split("_");
			
			String output = "";
			
			for (String word : words)
			{
				word = StringUtils.capitalize(word.toLowerCase());
				output += word + " ";
			}
				
			return output.trim();
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

		public Statistic Attack;
		public Statistic Pierce;
		public Statistic Defense;
		public Statistic Hardiness;

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

			Attack = Statistic.valueOf(toString()+"_ATK");
			Pierce = Statistic.valueOf(toString()+"_PIERCE");
			Defense = Statistic.valueOf(toString()+"_DEF");
			Hardiness = Statistic.valueOf(toString()+"_HARDINESS");
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
		
		public static EnumMap<Tier1Element, Integer> load(Element xml, EnumMap<Tier1Element, Integer> values)
		{
			for (int i = 0; i < xml.getChildCount(); i++)
			{
				Element el = xml.getChild(i);

				Tier1Element elem = Tier1Element.valueOf(el.getName().toUpperCase());
				String eqn = el.getText();				
				int newVal = values.get(elem);

				ExpressionBuilder expB = EquationHelper.createEquationBuilder(eqn);
				expB.variable("Value");

				Expression exp = EquationHelper.tryBuild(expB);
				if (exp != null)
				{
					exp.setVariable("Value", newVal);
					newVal = (int)exp.evaluate();
				}

				values.put(elem, newVal);
			}

			return values;
		}
		
		public static EnumMap<Tier1Element, Integer> copy(EnumMap<Tier1Element, Integer> map)
		{
			EnumMap<Tier1Element, Integer> newMap = new EnumMap<Tier1Element, Integer>(Tier1Element.class);
			for (Tier1Element e : Tier1Element.values())
			{
				newMap.put(e, map.get(e));
			}			
			return newMap;
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
	public static void newGame()
	{
		RecursiveDockGenerator generator = new RecursiveDockGenerator("Forest", 0);
		//VillageGenerator generator = new VillageGenerator(100, 100);
		generator.generate();
		CurrentLevel = generator.getLevel();

		boolean exit = false;
		for (int x = 0; x < CurrentLevel.width; x++)
		{
			for (int y = 0; y < CurrentLevel.height; y++)
			{
				GameTile tile = CurrentLevel.getGameTile(x, y);
				if (tile.metaValue != null && tile.metaValue.equals("PlayerSpawn"))
				{
					CurrentLevel.player = GameEntity.load("player");

					for (int i = 0; i < 40; i++)
					{
						//Global.CurrentLevel.player.Inventory.Items.add(AssetManager.loadItem("Jewelry/Necklace/GoldNecklace"));
					}
					CurrentLevel.player.getInventory().m_items.add(Item.load("Weapon/MainWeapon/sword"));
					CurrentLevel.player.getInventory().m_items.add(Item.load("Weapon/OffWeapon/shield"));
					CurrentLevel.player.getInventory().m_items.add(Item.load("Armour/Body/WoodArmour"));
					CurrentLevel.player.getInventory().m_items.add(Item.load("Jewelry/Necklace/GoldNecklace"));

					tile.addObject(CurrentLevel.player);

					exit = true;
					break;
				}
			}
			if (exit) { break;} 
		}

		CurrentLevel.updateVisibleTiles();
		//Global.CurrentLevel.revealWholeGlobal.CurrentLevel();
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
			GameScreen.Instance.addConsoleMessage(line);
		}
	}
}
