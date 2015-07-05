package Roguelike.Entity.AI.BehaviourTree.Actions;

import Roguelike.Entity.Entity;
import Roguelike.Entity.AI.BehaviourTree.BehaviourTree.BehaviourTreeState;
import Roguelike.Global.Statistics;
import Roguelike.Shadows.ShadowCaster;
import Roguelike.Tiles.GameTile;

import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.XmlReader.Element;

public class ActionGetAllVisible extends AbstractAction
{
	private enum FindType
	{
		TILES,
		ENTITIES,
		ALLIES,
		ENEMIES
	}
	private FindType Type;
	private String Key;

	@Override
	public BehaviourTreeState evaluate(Entity entity)
	{
		Array<int[]> output = new Array<int[]>();
		
		ShadowCaster shadower = new ShadowCaster(entity.Tile.Level.getGrid(), entity.getStatistic(Statistics.RANGE));
		shadower.ComputeFOV(entity.Tile.x, entity.Tile.y, output);
		
		Array<GameTile> visibleTiles = new Array<GameTile>();
		for (int[] tile : output)
		{
			visibleTiles.add(entity.Tile.Level.getGameTile(tile));
		}
		
		if (Type == FindType.TILES)
		{
			setData(Key, visibleTiles);
			State = visibleTiles.size > 0 ? BehaviourTreeState.SUCCEEDED : BehaviourTreeState.FAILED;
		}
		else
		{
			Array<Entity> entities = new Array<Entity>();
			
			for (GameTile tile : visibleTiles)
			{
				Entity e = tile.Entity;
				
				if (e == null)
				{
					
				}
				else if (Type == FindType.ENTITIES)
				{
					entities.add(e);
				}
				else if (Type == FindType.ALLIES)
				{
					if (entity.isAllies(e)) { entities.add(e); }
				}
				else if (Type == FindType.ENEMIES)
				{
					if (!entity.isAllies(e)) { entities.add(e); }
				}
			}
			
			setData(Key, entities);
			State = entities.size > 0 ? BehaviourTreeState.SUCCEEDED : BehaviourTreeState.FAILED;
		}
		
		return State;
	}

	@Override
	public void cancel()
	{
	}

	@Override
	public void parse(Element xmlElement)
	{
		Type = FindType.valueOf(xmlElement.getAttribute("Type").toUpperCase());
		Key = xmlElement.getAttribute("Key");
	}

}
