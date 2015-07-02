package Roguelike.Entity;

import java.io.IOException;
import java.util.EnumMap;
import java.util.HashMap;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.XmlReader;
import com.badlogic.gdx.utils.XmlReader.Element;
import com.badlogic.gdx.utils.reflect.ClassReflection;
import com.badlogic.gdx.utils.reflect.ReflectionException;

import Roguelike.AssetManager;
import Roguelike.Entity.AI.AbilityAI.AbilityAIBeam;
import Roguelike.Entity.AI.AbilityAI.AbilityAIBolt;
import Roguelike.Entity.AI.AbilityAI.AbilityAIStrike;
import Roguelike.Entity.AI.AbilityAI.AbstractAbilityAI;
import Roguelike.Global.Statistics;
import Roguelike.Levels.Level;
import Roguelike.Lights.Light;
import Roguelike.Sprite.Sprite;
import Roguelike.Tiles.GameTile;

public class ActiveAbility implements IAbility
{
	//####################################################################//
	//region Constructor
	
	//endregion Constructor
	//####################################################################//
	//region Public Methods
	
	//----------------------------------------------------------------------
	public EnumMap<Statistics, Integer> getFullStatistics()
	{
		EnumMap<Statistics, Integer> stats = Statistics.getStatisticsBlock();
		
		for (Statistics s : Statistics.values())
		{
			int val = m_statistics.get(s);
			val += Caster.getStatistic(s);
			stats.put(s, val);
		}
		
		return stats;
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
		
		table.add(new Label(Name, skin)).expandX().left();
		table.row();
		
		Label descLabel = new Label(Description, skin);
		descLabel.setWrap(true);
		table.add(descLabel).expand().left().width(com.badlogic.gdx.scenes.scene2d.ui.Value.percentWidth(1, table));
		table.row();
		
		return table;
	}
	
	//----------------------------------------------------------------------
	public void update()
	{
		if (Type == AbilityType.OneShot)
		{
			AI[aiIndex].update(this);
		}
		else if (Type == AbilityType.Channeled)
		{
			for (int i = 0; i < AI.length; i++)
			{
				AI[i].update(this);
			}
		}
	}
	
	//----------------------------------------------------------------------
	public void advanceAI()
	{
		if (Type == AbilityType.OneShot)
		{
			aiIndex++;
			
			if (aiIndex < AI.length)
			{
				update();
			}
		}
	}
	
	//----------------------------------------------------------------------
	public boolean isAlive()
	{
		if (Type == AbilityType.OneShot)
		{
			return aiIndex < AI.length;
		}
		else if (Type == AbilityType.Channeled)
		{
			return Caster.Channeling == this;
		}
		
		return false;
	}
	
	//----------------------------------------------------------------------
	public float getActionDelay()
	{
		return 1.0f / m_statistics.get(Statistics.SPEED);
	}
	
	//----------------------------------------------------------------------
	public float getCooldown()
	{
		return m_statistics.get(Statistics.COOLDOWN);
	}
	
	//----------------------------------------------------------------------
	public int getStatistic(Statistics stat)
	{
		return m_statistics.get(stat);
	}
	
	//----------------------------------------------------------------------
	public void addAffectedTile(GameTile tile, float rotation)
	{
		AbilityTile at = new AbilityTile();
		at.Tile = tile;
		at.Sprite = Sprite.copy();
		at.Sprite.rotation = rotation;
		
		AffectedTiles.add(at);
	}
	
	//----------------------------------------------------------------------
	public void Init(int targetx, int targety)
	{
		for (AbstractAbilityAI ai : AI)
		{
			ai.Init(this, targetx, targety);
		}
	}
	
	//----------------------------------------------------------------------
	public ActiveAbility copy()
	{
		ActiveAbility ab = new ActiveAbility();
		
		ab.Name = Name;
		ab.Description = Description;
		ab.Icon = Icon.copy();
		
		ab.Sprite = Sprite.copy();
		ab.Type = Type;
		ab.Light = Light;
		ab.m_statistics = Statistics.copy(m_statistics);
		
		Array<AbstractAbilityAI> aiList = new Array<AbstractAbilityAI>();
		
		for (AbstractAbilityAI ai : AI)
		{			
			aiList.add(ai.copy());
		}
		
		ab.AI = aiList.toArray(AbstractAbilityAI.class);
		
		return ab;
	}
	
	private void internalLoad(String name)
	{
		XmlReader xml = new XmlReader();
		Element xmlElement = null;
		
		try
		{
			xmlElement = xml.parse(Gdx.files.internal("Abilities/Active/"+name+".xml"));
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
		
		Name = xmlElement.get("Name", Name);
		Description = xmlElement.get("Description", Description);
		
		Element statElement = xmlElement.getChildByName("Statistics");
		if (statElement != null)
		{
			Statistics.load(statElement, m_statistics);
		}
		
		Element iconElement = xmlElement.getChildByName("Icon");
		if (iconElement != null)
		{
			Icon = AssetManager.loadSprite(iconElement);
		}
		
		Element spriteElement = xmlElement.getChildByName("Sprite");
		if (spriteElement != null)
		{
			Sprite = AssetManager.loadSprite(spriteElement);
		}
		
		Type = xmlElement.get("Type", null) != null ? AbilityType.valueOf(xmlElement.get("Type")) : Type;
		
		Element lightElement = xmlElement.getChildByName("Light");
		if (lightElement != null)
		{
			Light = Roguelike.Lights.Light.load(lightElement);
		}
		
		
		Element aiElement = xmlElement.getChildByName("AI");
		if (aiElement != null)
		{
			Array<AbstractAbilityAI> aiList = new Array<AbstractAbilityAI>();
			for (int i = 0; i < aiElement.getChildCount(); i++)
			{
				Element e = aiElement.getChild(i);
				
				try
				{			
					Class<AbstractAbilityAI> c = ClassMap.get(e.getName());
					AbstractAbilityAI ai = (AbstractAbilityAI)ClassReflection.newInstance(c);

					ai.parse(e);
					
					aiList.add(ai);
				} 
				catch (ReflectionException re) { re.printStackTrace(); }
			}
			
			AI = aiList.toArray(AbstractAbilityAI.class);
		}
	}
	
	//----------------------------------------------------------------------
	public static ActiveAbility load(String name)
	{
		ActiveAbility ab = new ActiveAbility();
		
		ab.internalLoad(name);
		
		return ab;
	}
		
	//endregion Public Methods
	//####################################################################//
	//region Private Methods
		
	//endregion Private Methods
	//####################################################################//
	//region Data
	
	//----------------------------------------------------------------------
	private static HashMap<String, Class> ClassMap = new HashMap<String, Class>();
	static
	{
		ClassMap.put("Bolt", AbilityAIBolt.class);
		ClassMap.put("Beam", AbilityAIBeam.class);
		ClassMap.put("Strike", AbilityAIStrike.class);
	}
	
	//----------------------------------------------------------------------
	public enum AbilityType
	{
		OneShot,
		Channeled
	}
	
	//----------------------------------------------------------------------
	public static class AbilityTile
	{
		public GameTile Tile;
		public Sprite Sprite;
	}
	
	//----------------------------------------------------------------------
	public String Name;
	public String Description;
	public Sprite Icon;
	
	//----------------------------------------------------------------------
	public AbilityType Type;
	
	//----------------------------------------------------------------------
	public Sprite Sprite;
	public Light Light;
	
	//----------------------------------------------------------------------
	public float actionDelayAccumulator;
	
	//----------------------------------------------------------------------
	public float cooldownAccumulator;
	
	//----------------------------------------------------------------------
	public Entity Caster;
	
	//----------------------------------------------------------------------
	public AbstractAbilityAI[] AI;
	int aiIndex = 0;
	
	//----------------------------------------------------------------------
	public Array<AbilityTile> AffectedTiles = new Array<AbilityTile>(false, 16);
	
	//----------------------------------------------------------------------
	public Level level;
	
	//----------------------------------------------------------------------
	private EnumMap<Statistics, Integer> m_statistics = Statistics.getStatisticsBlock();
		
	//endregion Data
	//####################################################################//
}
