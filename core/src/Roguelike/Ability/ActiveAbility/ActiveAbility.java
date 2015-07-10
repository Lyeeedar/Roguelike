package Roguelike.Ability.ActiveAbility;

import java.io.IOException;

import Roguelike.AssetManager;
import Roguelike.Ability.IAbility;
import Roguelike.Ability.ActiveAbility.AbilityType.AbstractAbilityType;
import Roguelike.Ability.ActiveAbility.EffectType.AbstractEffectType;
import Roguelike.Ability.ActiveAbility.MovementType.AbstractMovementType;
import Roguelike.Ability.ActiveAbility.MovementType.MovementTypeBolt;
import Roguelike.Ability.ActiveAbility.TargetingType.AbstractTargetingType;
import Roguelike.Entity.Entity;
import Roguelike.Lights.Light;
import Roguelike.Shadows.ShadowCaster;
import Roguelike.Sprite.Sprite;
import Roguelike.Tiles.GameTile;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.XmlReader;
import com.badlogic.gdx.utils.XmlReader.Element;

public class ActiveAbility implements IAbility
{
	private String name;
	private String description;
	
	private int aoe;
	public int range;
			
	public float cooldownAccumulator;
	public float cooldown;
	
	private AbstractAbilityType abilityType;
	private AbstractTargetingType targetingType;
	private AbstractMovementType movementType;
	private Array<AbstractEffectType> effectTypes = new Array<AbstractEffectType>();
	public Array<GameTile> AffectedTiles = new Array<GameTile>();
	
	public Entity caster;
	
	//----------------------------------------------------------------------
	// rendering
	public Light light;
	public Sprite Icon;
	public Sprite movementSprite;
	public Sprite hitSprite;
	
	public ActiveAbility copy()
	{
		ActiveAbility aa = new ActiveAbility();
		
		aa.name = name;
		aa.description = description;
		aa.aoe = aoe;
		aa.range = range;
		
		aa.abilityType = abilityType.copy();
		aa.targetingType = targetingType.copy();
		aa.movementType = movementType.copy();
		
		for (AbstractEffectType effect : effectTypes)
		{
			aa.effectTypes.add(effect.copy());
		}
		
		for (GameTile tile : AffectedTiles)
		{
			aa.AffectedTiles.add(tile);
		}
		
		aa.caster = caster;
		
		aa.light = light != null ? light.copy() : null;
		aa.Icon = Icon != null ? Icon.copy() : null;
		aa.movementSprite = movementSprite != null ? movementSprite.copy() : null;
		aa.hitSprite = hitSprite != null ? hitSprite.copy() : null;
		
		return aa;
	}
	
	public Array<GameTile> getAffectedTiles()
	{
		return AffectedTiles;
	}
	
	public Sprite getSprite()
	{
		return movementSprite;
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

		return table;
	}

	public boolean isTargetValid(GameTile tile, int[][] valid)
	{
		for (int[] validTile : valid)
		{
			if (validTile[0] == tile.x && validTile[1] == tile.y)
			{
				return true;
			}
		}
		
		return false;
	}
	
	public int[][] getValidTargets()
	{		
		Array<int[]> validTargets = new Array<int[]>();
		
		Array<int[]> output = new Array<int[]>();
		ShadowCaster shadow = new ShadowCaster(caster.Tile.Level.getGrid(), range);
		shadow.ComputeFOV(caster.Tile.x, caster.Tile.y, output);
		
		for (int[] tilePos : output)
		{
			GameTile tile = caster.Tile.Level.getGameTile(tilePos);
			
			if (targetingType.isTargetValid(this, tile))
			{
				validTargets.add(tilePos);
			}
		}
		
		return validTargets.toArray(int[].class);
	}
	
	public void lockTarget(GameTile tile)
	{
		movementType.init(this, tile.x, tile.y);
	}
	
	public void updateAccumulators(float cost)
	{
		movementType.updateAccumulators(cost);
	}
	
	public boolean needsUpdate()
	{
		if (movementType.needsUpdate()) { return true; }
		
		return false;
	}
	
	public boolean update()
	{
		boolean finished = movementType.update(this);
		if (movementSprite != null) { movementSprite.rotation = movementType.direction.GetAngle(); }
		
		if (finished)
		{
			if (aoe > 0)
			{
				GameTile epicenter = AffectedTiles.peek();
				
				Array<int[]> output = new Array<int[]>();
				
				ShadowCaster shadow = new ShadowCaster(epicenter.Level.getGrid(), aoe);
				shadow.ComputeFOV(epicenter.x, epicenter.y, output);
				
				AffectedTiles.clear();
				for (int[] tilePos : output)
				{
					GameTile tile = epicenter.Level.getGameTile(tilePos);
					
					if (tile.TileData.Passable)
					{
						AffectedTiles.add(tile);
					}
				}
			}
			
			for (GameTile tile : AffectedTiles)
			{
				for (AbstractEffectType effect : effectTypes)
				{
					effect.update(this, 1, tile);
				}
			}
		}
		
		return finished;
	}
	
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
		
		String extendsElement = xmlElement.getAttribute("Extends", null);
		if (extendsElement != null)
		{
			internalLoad(extendsElement);
		}
		
		this.name = xmlElement.get("Name", this.name);
		description = xmlElement.get("Description", description);
		aoe = xmlElement.getInt("AOE", aoe);
		range = xmlElement.getInt("Range", range);
		cooldown = xmlElement.getFloat("Cooldown", cooldown);
		
		Icon = xmlElement.getChildByName("Icon") != null ? AssetManager.loadSprite(xmlElement.getChildByName("Icon")) : Icon;
		movementSprite = xmlElement.getChildByName("MovementSprite") != null ? AssetManager.loadSprite(xmlElement.getChildByName("MovementSprite")) : movementSprite;
		hitSprite = xmlElement.getChildByName("HitSprite") != null ? AssetManager.loadSprite(xmlElement.getChildByName("HitSprite")) : hitSprite;
		
		Element lightElement = xmlElement.getChildByName("Light");
		if (lightElement != null)
		{
			light = Roguelike.Lights.Light.load(lightElement);
		}
		
		Element typeElement = xmlElement.getChildByName("Type");
		if (typeElement != null)
		{
			abilityType = AbstractAbilityType.load(typeElement.getChild(0));
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
	}
	
	public static ActiveAbility load(String name)
	{
		ActiveAbility ab = new ActiveAbility();
		
		ab.internalLoad(name);
		
		return ab;
	}
}
