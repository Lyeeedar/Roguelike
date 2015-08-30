package Roguelike.Entity.AI.BehaviourTree.Actions;

import Roguelike.Global;
import Roguelike.Entity.GameEntity;
import Roguelike.Entity.AI.BehaviourTree.BehaviourTree.BehaviourTreeState;
import Roguelike.Sound.SoundInstance;

import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.utils.XmlReader.Element;

public class ActionShout extends AbstractAction
{
	private String key;

	@Override
	public BehaviourTreeState evaluate( GameEntity entity )
	{
		SoundInstance sound = entity.soundBank.get( key );

		if ( sound == null )
		{
			State = BehaviourTreeState.FAILED;
			return State;
		}

		Object value = getData( key, null );

		if ( value == null )
		{
			State = BehaviourTreeState.FAILED;
			return State;
		}

		entity.popup = new Label( sound.text, Global.skin );

		sound.shoutFaction = entity.factions;
		sound.key = key;
		sound.value = value;

		sound.play( entity.tile );

		State = BehaviourTreeState.SUCCEEDED;
		return State;
	}

	@Override
	public void cancel()
	{

	}

	@Override
	public void parse( Element xmlElement )
	{
		key = xmlElement.getAttribute( "Key" );
	}
}
