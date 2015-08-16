package Roguelike.Ability.ActiveAbility;

import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;

import Roguelike.AssetManager;
import Roguelike.Global.Direction;
import Roguelike.Global.Passability;
import Roguelike.Global.Statistic;
import Roguelike.Ability.IAbility;
import Roguelike.Ability.ActiveAbility.AbilityType.AbstractAbilityType;
import Roguelike.Ability.ActiveAbility.CostType.AbstractCostType;
import Roguelike.Ability.ActiveAbility.EffectType.AbstractEffectType;
import Roguelike.Ability.ActiveAbility.MovementType.AbstractMovementType;
import Roguelike.Ability.ActiveAbility.MovementType.MovementTypeBolt;
import Roguelike.Ability.ActiveAbility.MovementType.MovementTypeRay;
import Roguelike.Ability.ActiveAbility.MovementType.MovementTypeSmite;
import Roguelike.Ability.ActiveAbility.TargetingType.AbstractTargetingType;
import Roguelike.Ability.ActiveAbility.TargetingType.TargetingTypeSelf;
import Roguelike.Ability.ActiveAbility.TargetingType.TargetingTypeTile;
import Roguelike.Entity.GameEntity;
import Roguelike.GameEvent.GameEventHandler;
import Roguelike.GameEvent.IGameObject;
import Roguelike.Items.Item;
import Roguelike.Items.Item.EquipmentSlot;
import Roguelike.Lights.Light;
import Roguelike.Pathfinding.ShadowCastCache;
import Roguelike.Pathfinding.ShadowCaster;
import Roguelike.Screens.GameScreen;
import Roguelike.Sound.SoundInstance;
import Roguelike.Sprite.Sprite;
import Roguelike.Sprite.SpriteEffect;
import Roguelike.Tiles.GameTile;
import Roguelike.Tiles.Point;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Pools;
import com.badlogic.gdx.utils.XmlReader;
import com.badlogic.gdx.utils.XmlReader.Element;

public class ActiveAbility implements IAbility, IGameObject
{
	private String name;
	private String description;
	
	private int cone = 0;
	private int aoe = 0;
	private boolean excludeSelf = false;
	private int range = 1;
	private float screenshake = 0;
			
	public float cooldownAccumulator;
	public float cooldown = 1;
	
	public Array<AbstractCostType> costTypes = new Array<AbstractCostType>();
	public AbstractTargetingType targetingType = new TargetingTypeTile();
	public AbstractMovementType movementType = new MovementTypeSmite();
	public Array<AbstractEffectType> effectTypes = new Array<AbstractEffectType>();
	public Array<GameTile> AffectedTiles = new Array<GameTile>();
	
	public GameTile source;
	public GameEntity caster;
	public HashMap<String, Integer> variableMap = new HashMap<String, Integer>();
	
	public Array<Passability> abilityPassability = new Array<Passability>(new Passability[]{Passability.LEVITATE});
	
	private ShadowCastCache cache = new ShadowCastCache();
	
	//----------------------------------------------------------------------
	// rendering
	public Light light;
	public Sprite Icon;
	private Sprite movementSprite;
	private Sprite hitSprite;
	private Sprite useSprite;
	
	private boolean spentCost = false;
	
	//----------------------------------------------------------------------
	public int getRange()
	{
		if (range > 0) { return range; }
		Item item = caster.getInventory().getEquip(EquipmentSlot.MAINWEAPON);
		if (item != null) { return item.getRange(caster); }
		return 1;
	}
	
	//----------------------------------------------------------------------
	public boolean isAvailable()
	{
		if (cooldownAccumulator > 0) { return false; }
		
		for (AbstractCostType cost : costTypes)
		{
			if (!cost.isCostAvailable(this)) { return false; }
		}
		
		return true;
	}
	
	//----------------------------------------------------------------------
	public ActiveAbility copy()
	{
		ActiveAbility aa = new ActiveAbility();
		
		aa.name = name;
		aa.description = description;
		aa.aoe = aoe;
		aa.range = range;
		aa.cone = cone;
		aa.screenshake = screenshake;
		aa.cache = cache.copy();
		
		aa.targetingType = targetingType.copy();
		aa.movementType = movementType.copy();
		
		for (AbstractCostType cost : costTypes)
		{
			aa.costTypes.add(cost.copy());
		}
		
		for (AbstractEffectType effect : effectTypes)
		{
			aa.effectTypes.add(effect.copy());
		}
		
		for (GameTile tile : AffectedTiles)
		{
			aa.AffectedTiles.add(tile);
		}
		
		aa.light = light != null ? light.copy() : null;
		aa.Icon = Icon != null ? Icon.copy() : null;
		aa.movementSprite = movementSprite != null ? movementSprite.copy() : null;
		aa.hitSprite = hitSprite != null ? hitSprite.copy() : null;
		aa.useSprite = useSprite != null ? useSprite.copy() : null;
		
		return aa;
	}
	
	//----------------------------------------------------------------------
	public Array<GameTile> getAffectedTiles()
	{
		return AffectedTiles;
	}
	
	//----------------------------------------------------------------------
	public Sprite getSprite()
	{
		return movementSprite;
	}
	
	//----------------------------------------------------------------------
	public Sprite getHitSprite()
	{
		if (hitSprite != null) { return hitSprite; }
		Item wep = caster.getInventory().getEquip(EquipmentSlot.MAINWEAPON);
		if (wep != null)
		{
			return wep.getWeaponHitEffect();
		}
		return caster.defaultHitEffect;
	}
	
	//----------------------------------------------------------------------
	public Sprite getUseSprite()
	{
		return useSprite;
	}
	
	//----------------------------------------------------------------------
	public Sprite getIcon()
	{
		return Icon;
	}

	//----------------------------------------------------------------------
	public Table createTable(Skin skin)
	{
		Table table = new Table();

		table.add(new Label(name, skin)).expandX().left();
		table.row();

		Label descLabel = new Label(description, skin);
		descLabel.setWrap(true);
		table.add(descLabel).expand().left().width(com.badlogic.gdx.scenes.scene2d.ui.Value.percentWidth(1, table));
		table.row();
		
		for (AbstractCostType cost : costTypes)
		{
			String string = cost.getCostString(this);
			table.add(new Label(string, skin)).expandX().left();
			table.row();
		}

		return table;
	}

	//----------------------------------------------------------------------
	public boolean isTargetValid(GameTile tile, Array<Point> valid)
	{
		for (Point validTile : valid)
		{
			if (validTile.x == tile.x && validTile.y == tile.y)
			{
				return true;
			}
		}
		
		return false;
	}
	
	//----------------------------------------------------------------------
	public Array<Point> getValidTargets()
	{		
		Array<Point> validTargets = new Array<Point>();
		
		Array<Point> output = cache.getShadowCast(source.level.getGrid(), source.x, source.y, getRange());
		
		for (Point tilePos : output)
		{
			GameTile tile = source.level.getGameTile(tilePos);
			
			if (targetingType.isTargetValid(this, tile))
			{
				validTargets.add(tilePos.copy());
			}
		}
				
		return validTargets;
	}
	
	//----------------------------------------------------------------------
	public void lockTarget(GameTile tile)
	{
		movementType.init(this, tile.x, tile.y);
	}
	
	//----------------------------------------------------------------------
	public void updateAccumulators(float cost)
	{
		movementType.updateAccumulators(cost);
	}
	
	//----------------------------------------------------------------------
	public boolean needsUpdate()
	{
		if (movementType.needsUpdate()) { return true; }
		
		return false;
	}
	
	//----------------------------------------------------------------------
	public boolean update()
	{
		if (!spentCost)
		{
			for (AbstractCostType cost : costTypes)
			{
				cost.spendCost(this);
			}
			spentCost = true;
		}
		
		boolean finished = movementType.update(this);
		if (movementSprite != null) { movementSprite.rotation = movementType.direction.getAngle(); }
		
		if (finished)
		{
			GameTile epicenter = AffectedTiles.peek();
			
			if (movementType instanceof MovementTypeRay)
			{
				epicenter = AffectedTiles.first();
			}
			else if (aoe > 0)
			{
				Array<Point> output = new Array<Point>();
				
				ShadowCaster shadow = new ShadowCaster(epicenter.level.getGrid(), aoe, abilityPassability);
				shadow.ComputeFOV(epicenter.x, epicenter.y, output);
				
				for (Point tilePos : output)
				{
					GameTile tile = epicenter.level.getGameTile(tilePos);					
					AffectedTiles.add(tile);
				}
				
				Pools.freeAll(output);
			}
			else if (cone > 0)
			{
				Direction dir = Direction.getDirection(source, epicenter);
				
				Point epicenterPoint = Pools.obtain(Point.class).set(epicenter.x, epicenter.y);				
				Array<Point> cone = Direction.buildCone(dir, epicenterPoint, this.cone);
				Pools.free(epicenterPoint);
				
				Array<Point> output = new Array<Point>();
				
				ShadowCaster shadow = new ShadowCaster(epicenter.level.getGrid(), this.cone, abilityPassability);
				shadow.ComputeFOV(epicenter.x, epicenter.y, output);
				
				for (Point tilePos : cone)
				{
					GameTile tile = epicenter.level.getGameTile(tilePos);
					
					boolean canAdd = false;
					for (Point visPos : output)
					{
						GameTile visTile = epicenter.level.getGameTile(visPos);
						
						if (tile == visTile)
						{
							canAdd = true;
							break;
						}
					}
					
					if (canAdd)
					{
						AffectedTiles.add(tile);
					}
				}
				
				Pools.freeAll(output);
			}
			
			// minimise list
			HashSet<GameTile> added = new HashSet<GameTile>();
			Iterator<GameTile> itr = AffectedTiles.iterator();
			while (itr.hasNext())
			{
				GameTile tile = itr.next();
				
				if (!added.contains(tile))
				{
					added.add(tile);
				}
				else
				{
					itr.remove();
				}
			}
			
			if (excludeSelf)
			{
				AffectedTiles.removeValue(epicenter, true);
			}
			
			for (GameTile tile : AffectedTiles)
			{
				for (AbstractEffectType effect : effectTypes)
				{
					effect.update(this, 1, tile);
				}
				
				if (getHitSprite() != null)
				{
					Light l = light != null ? light.copy() : null;
					Sprite s = getHitSprite().copy();
					s.renderDelay = s.animationDelay * tile.getDist(epicenter) + s.animationDelay;
					tile.spriteEffects.add(new SpriteEffect(s, Direction.CENTER, l));
					
					SoundInstance sound = s.sound;
					if (sound != null) { sound.play(tile); }
				}
			}
			
			if (screenshake > 0)
			{
				// check distance for screenshake
				float dist = Vector2.dst(epicenter.x, epicenter.y,  epicenter.level.player.tile.x,  epicenter.level.player.tile.y);
				float shakeRadius = screenshake;
				if (aoe != 0 && dist > aoe)
				{
					shakeRadius *= (dist-aoe) / (aoe*2);
				}
				
				if (shakeRadius > 2)
				{
					GameScreen.Instance.screenShakeRadius = shakeRadius;
					GameScreen.Instance.screenShakeAngle = MathUtils.random() * 360;
				}
			}			
		}
		
		return finished;
	}
	
	//----------------------------------------------------------------------
	private void internalLoad(String name)
	{
		XmlReader xml = new XmlReader();
		Element xmlElement = null;
		
		try
		{
			xmlElement = xml.parse(Gdx.files.internal("Abilities/Lines/"+name+".xml"));
		} 
		catch (IOException e)
		{
			e.printStackTrace();
		}
		
		internalLoad(xmlElement);
	}
	
	//----------------------------------------------------------------------
	private void internalLoad(Element xmlElement)
	{
		
		String extendsElement = xmlElement.getAttribute("Extends", null);
		if (extendsElement != null)
		{
			internalLoad(extendsElement);
		}
		
		this.name = xmlElement.get("Name", this.name);
		description = xmlElement.get("Description", description);
		
		Element aoeElement = xmlElement.getChildByName("AOE");
		if (aoeElement != null)
		{
			aoe = Integer.parseInt(aoeElement.getText());
			excludeSelf = aoeElement.getBoolean("ExcludeSelf", false);
		}
		
		
		cone = xmlElement.getInt("Cone", cone);
		range = xmlElement.getInt("Range", range);
		cooldown = xmlElement.getFloat("Cooldown", cooldown);
		screenshake = xmlElement.getFloat("ScreenShake", screenshake);
		
		Icon = xmlElement.getChildByName("Icon") != null ? AssetManager.loadSprite(xmlElement.getChildByName("Icon")) : Icon;
		movementSprite = xmlElement.getChildByName("MovementSprite") != null ? AssetManager.loadSprite(xmlElement.getChildByName("MovementSprite")) : movementSprite;
		hitSprite = xmlElement.getChildByName("HitSprite") != null ? AssetManager.loadSprite(xmlElement.getChildByName("HitSprite")) : hitSprite;
		useSprite = xmlElement.getChildByName("UseSprite") != null ? AssetManager.loadSprite(xmlElement.getChildByName("UseSprite")) : useSprite;
		
		Element lightElement = xmlElement.getChildByName("Light");
		if (lightElement != null)
		{
			light = Roguelike.Lights.Light.load(lightElement);
		}
		
		Element costsElement = xmlElement.getChildByName("Cost");
		if (costsElement != null)
		{
			for (int i = 0; i < costsElement.getChildCount(); i++)
			{
				Element costElement = costsElement.getChild(i);
				costTypes.add(AbstractCostType.load(costElement));
			}
		}
		
		Element targetingElement = xmlElement.getChildByName("Targeting");
		if (targetingElement != null)
		{
			targetingType = AbstractTargetingType.load(targetingElement.getChild(0));
		}
		
		Element movementElement = xmlElement.getChildByName("Movement");
		if (movementElement != null)
		{
			movementType = AbstractMovementType.load(movementElement.getChild(0));
		}
		
		Element effectsElement = xmlElement.getChildByName("Effect");
		if (effectsElement != null)
		{
			for (int i = 0; i < effectsElement.getChildCount(); i++)
			{
				Element effectElement = effectsElement.getChild(i);
				effectTypes.add(AbstractEffectType.load(effectElement));
			}
		}
		
		Element passabilityElement = xmlElement.getChildByName("Passability");
		if (passabilityElement != null)
		{
			abilityPassability = Passability.parseArray(passabilityElement.getText());
		}
	}
	
	//----------------------------------------------------------------------
	public static ActiveAbility load(String name)
	{
		ActiveAbility ab = new ActiveAbility();
		
		ab.internalLoad(name);
		
		return ab;
	}
	
	//----------------------------------------------------------------------
	public static ActiveAbility load(Element xml)
	{
		ActiveAbility ab = new ActiveAbility();
		
		ab.internalLoad(xml);
		
		return ab;
	}
	
	//----------------------------------------------------------------------
	@Override
	public String getName()
	{
		return name;
	}

	//----------------------------------------------------------------------
	@Override
	public String getDescription()
	{
		return description;
	}
}
