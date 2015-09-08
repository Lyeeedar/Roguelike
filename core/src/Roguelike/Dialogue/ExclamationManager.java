package Roguelike.Dialogue;

import net.objecthunter.exp4j.Expression;
import net.objecthunter.exp4j.ExpressionBuilder;
import Roguelike.Global;
import Roguelike.Entity.GameEntity;
import Roguelike.Sound.SoundInstance;
import Roguelike.Tiles.GameTile;

import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.XmlReader.Element;

import exp4j.Helpers.EquationHelper;

public class ExclamationManager
{
	public ExclamationEventWrapper seePlayer;
	public ExclamationEventWrapper seeAlly;
	public ExclamationEventWrapper seeEnemy;
	public ExclamationEventWrapper lowHealth;
	public ExclamationEventWrapper victory;

	public void update( float delta )
	{
		if ( seePlayer != null && seePlayer.cooldownAccumulator > 0 )
		{
			seePlayer.cooldownAccumulator -= delta;
		}

		if ( seeAlly != null && seeAlly.cooldownAccumulator > 0 )
		{
			seeAlly.cooldownAccumulator -= delta;
		}

		if ( seeEnemy != null && seeEnemy.cooldownAccumulator > 0 )
		{
			seeEnemy.cooldownAccumulator -= delta;
		}
	}

	public void process( Array<GameTile> tiles, GameEntity entity )
	{
		if ( seePlayer != null )
		{
			processSeePlayer( tiles, entity );
		}

		if ( seeAlly != null )
		{
			processSeeAlly( tiles, entity );
		}

		if ( seeEnemy != null )
		{
			processSeeEnemy( tiles, entity );
		}
	}

	private void processSeePlayer( Array<GameTile> tiles, GameEntity entity )
	{
		boolean canSeePlayer = false;
		for ( GameTile tile : tiles )
		{
			if ( tile.entity != null && tile.entity == Global.CurrentLevel.player )
			{
				canSeePlayer = true;
				break;
			}
		}

		if ( canSeePlayer )
		{
			if ( seePlayer.cooldownAccumulator <= 0 )
			{
				seePlayer.process( entity );
			}

			seePlayer.cooldownAccumulator = seePlayer.cooldown;
		}
	}

	private void processSeeAlly( Array<GameTile> tiles, GameEntity entity )
	{
		boolean canSeeAlly = false;
		for ( GameTile tile : tiles )
		{
			if ( tile.entity != null && tile.entity.isAllies( entity ) )
			{
				canSeeAlly = true;
				break;
			}
		}

		if ( canSeeAlly )
		{
			if ( seeAlly.cooldownAccumulator <= 0 )
			{
				seeAlly.process( entity );
			}

			seeAlly.cooldownAccumulator = seeAlly.cooldown;
		}
	}

	private void processSeeEnemy( Array<GameTile> tiles, GameEntity entity )
	{
		boolean canSeeEnemy = false;
		for ( GameTile tile : tiles )
		{
			if ( tile.entity != null && !tile.entity.isAllies( entity ) )
			{
				canSeeEnemy = true;
				break;
			}
		}

		if ( canSeeEnemy )
		{
			if ( seeEnemy.cooldownAccumulator <= 0 )
			{
				seeEnemy.process( entity );
			}

			seeEnemy.cooldownAccumulator = seeEnemy.cooldown;
		}
	}

	public void parse( Element xml )
	{
		Element seePlayerElement = xml.getChildByName( "SeePlayer" );
		if ( seePlayerElement != null )
		{
			seePlayer = ExclamationEventWrapper.load( seePlayerElement );
		}

		Element seeAllyElement = xml.getChildByName( "SeeAlly" );
		if ( seeAllyElement != null )
		{
			seeAlly = ExclamationEventWrapper.load( seeAllyElement );
		}

		Element seeEnemyElement = xml.getChildByName( "SeeEnemy" );
		if ( seeEnemyElement != null )
		{
			seeEnemy = ExclamationEventWrapper.load( seeEnemyElement );
		}
	}

	public static ExclamationManager load( Element xml )
	{
		ExclamationManager manager = new ExclamationManager();
		manager.parse( xml );
		return manager;
	}

	private static class ExclamationEventWrapper
	{
		public Array<ExclamationWrapper> groups = new Array<ExclamationWrapper>();
		public float cooldown;
		public float cooldownAccumulator;

		public void process( GameEntity entity )
		{
			for ( ExclamationWrapper wrapper : groups )
			{
				if ( processCondition( wrapper.condition, null ) )
				{
					entity.setPopupText( Global.expandNames( wrapper.text ), 2 );

					if ( wrapper.sound != null )
					{
						wrapper.sound.play( entity.tile );
					}

					break;
				}
			}
		}

		// ----------------------------------------------------------------------
		public boolean processCondition( String condition, String[] reliesOn )
		{
			ExpressionBuilder expB = EquationHelper.createEquationBuilder( condition );
			EquationHelper.setVariableNames( expB, Global.GlobalVariables, "" );

			Expression exp = EquationHelper.tryBuild( expB );
			if ( exp == null ) { return false; }

			EquationHelper.setVariableValues( exp, Global.GlobalVariables, "" );

			int raw = (int) exp.evaluate();

			return raw > 0;
		}

		public void parse( Element xml )
		{
			cooldown = xml.getFloat( "Cooldown", 5 );

			for ( int i = 0; i < xml.getChildCount(); i++ )
			{
				groups.add( ExclamationWrapper.load( xml.getChild( i ) ) );
			}
		}

		public static ExclamationEventWrapper load( Element xml )
		{
			ExclamationEventWrapper event = new ExclamationEventWrapper();
			event.parse( xml );
			return event;
		}
	}

	private static class ExclamationWrapper
	{
		public String condition;
		public SoundInstance sound;
		public String text;

		public void parse( Element xml )
		{
			condition = xml.getAttribute( "Condition", "1" ).toLowerCase();
			Element soundEl = xml.getChildByName( "Sound" );
			if ( soundEl != null )
			{
				sound = SoundInstance.load( soundEl );
			}

			text = xml.get( "Text" );
		}

		public static ExclamationWrapper load( Element xml )
		{
			ExclamationWrapper wrapper = new ExclamationWrapper();
			wrapper.parse( xml );
			return wrapper;
		}
	}
}
