package Roguelike.Ability.ActiveAbility.EffectType;

import Roguelike.Ability.ActiveAbility.ActiveAbility;
import Roguelike.Entity.Entity;
import Roguelike.Entity.EnvironmentEntity;
import Roguelike.Entity.GameEntity;
import Roguelike.Global;
import Roguelike.StatusEffect.StatusEffect;
import Roguelike.Tiles.GameTile;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.XmlReader;
import exp4j.Helpers.EquationHelper;

import java.util.HashMap;

/**
 * Created by Philip on 26/12/2015.
 */
public class EffectTypeSummon extends AbstractEffectType
{
	String entityName;
	int duration;

	String[] reliesOn;
	String countEqn;

	@Override
	public void update( ActiveAbility aa, float time, GameTile tile, GameEntity gentity, EnvironmentEntity envEntity )
	{
		HashMap<String, Integer> variableMap = aa.getVariableMap();

		for ( String name : reliesOn )
		{
			if ( !variableMap.containsKey( name ) )
			{
				variableMap.put( name, 0 );
			}
		}

		int count = EquationHelper.evaluate( countEqn, variableMap );
		for (int i = 0; i < count; i++)
		{
			GameEntity entity = GameEntity.load( entityName );

			entity.factions = aa.getCaster().factions;
			entity.essence = 0;

			StatusEffect status = StatusEffect.load( "Summon" );
			status.duration = duration;

			entity.addStatusEffect( status );

			if ( tile.entity == null && tile.getPassable( entity.getTravelType(), entity ) )
			{
				tile.addGameEntity( entity );
			}
			else
			{
				for ( Global.Direction dir : Global.Direction.values() )
				{
					GameTile testTile = tile.level.getGameTile( tile.x + dir.getX(), tile.y + dir.getY() );

					if ( testTile != null && testTile.entity == null && testTile.getPassable( entity.getTravelType(), entity ) )
					{
						testTile.addGameEntity( entity );
						break;
					}
				}
			}
		}
	}

	@Override
	public void parse( XmlReader.Element xml )
	{
		reliesOn = xml.getAttribute( "ReliesOn", "" ).toLowerCase().split( "," );
		countEqn = xml.getAttribute( "Count", "1" ).toLowerCase();

		entityName = xml.getText();
		duration = xml.getIntAttribute( "Duration", 10 );
	}

	@Override
	public AbstractEffectType copy()
	{
		EffectTypeSummon cpy = new EffectTypeSummon();
		cpy.entityName = entityName;
		cpy.duration = duration;
		cpy.reliesOn = reliesOn;
		cpy.countEqn = countEqn;

		return cpy;
	}

	@Override
	public Array<String> toString( ActiveAbility aa )
	{
		Array<String> lines = new Array<String>(  );
		lines.add( "Summons " + entityName + " for " + duration + " turns." );
		return lines;
	}
}
