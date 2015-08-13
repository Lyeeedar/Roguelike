package Roguelike.Entity.AI.BehaviourTree.Actions;

import Roguelike.Entity.GameEntity;
import Roguelike.Entity.AI.BehaviourTree.BehaviourTree.BehaviourTreeState;
import Roguelike.Entity.Tasks.TaskWait;
import Roguelike.Tiles.Point;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.utils.Pools;
import com.badlogic.gdx.utils.XmlReader.Element;

public class ActionProcessInput extends AbstractAction
{

	@Override
	public BehaviourTreeState evaluate(GameEntity entity)
	{
		Point targetPos = (Point)getData("ClickPos", null);
		
		if (targetPos != null)
		{
			setData("ClickPos", null);
		}
		else
		{
			boolean up = Gdx.input.isKeyPressed(Keys.UP);
			boolean down = Gdx.input.isKeyPressed(Keys.DOWN);
			boolean left = Gdx.input.isKeyPressed(Keys.LEFT);
			boolean right = Gdx.input.isKeyPressed(Keys.RIGHT);
			boolean space = Gdx.input.isKeyPressed(Keys.SPACE);
			
			int x = 0;
			int y = 0;
			
			if ( up )
			{
				y = 1;
			}
			else if ( down )
			{
				y = -1;
			}
			
			if ( left )
			{
				x = -1;
			}
			else if ( right )
			{
				x = 1;
			}
			
			if (x != 0 || y != 0 || space)
			{
				targetPos = Pools.obtain(Point.class).set(entity.tile.x + x, entity.tile.y + y);
			}
		}
		
		if (targetPos != null)
		{
			Point oldPos = (Point)getData("Pos", null);
			if (oldPos != null) { Pools.free(oldPos); }
			
			if (targetPos.x == entity.tile.x && targetPos.y == entity.tile.y)
			{
				entity.tasks.add(new TaskWait());
				
				setData("Pos", null);
			}
			else
			{
				setData("Pos", targetPos);
			}
			
			setData("Rest", null);
		}

		return targetPos != null ? BehaviourTreeState.SUCCEEDED : BehaviourTreeState.FAILED;
	}

	@Override
	public void cancel()
	{
	}

	@Override
	public void parse(Element xmlElement)
	{
	}

}
