package Roguelike.Entity.AI.BehaviourTree.Actions;

import Roguelike.Entity.Entity;
import Roguelike.Entity.AI.BehaviourTree.BehaviourTree.BehaviourTreeState;
import Roguelike.Entity.Tasks.TaskWait;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.utils.XmlReader.Element;

public class ActionProcessInput extends AbstractAction
{

	@Override
	public BehaviourTreeState evaluate(Entity entity)
	{
		int[] targetPos = (int[])getData("ClickPos", null);
		
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
				targetPos = new int[]{ entity.Tile.x + x, entity.Tile.y + y };
			}
		}
		
		if (targetPos != null)
		{
			if (targetPos[0] == entity.Tile.x && targetPos[1] == entity.Tile.y)
			{
				entity.Tasks.add(new TaskWait());
				
				setData("Pos", null);
			}
			else
			{
				setData("Pos", targetPos);
			}
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
