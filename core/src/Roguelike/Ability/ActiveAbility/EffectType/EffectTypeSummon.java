package Roguelike.Ability.ActiveAbility.EffectType;

import Roguelike.Ability.ActiveAbility.ActiveAbility;
import Roguelike.Entity.Entity;
import Roguelike.Entity.GameEntity;
import Roguelike.StatusEffect.StatusEffect;
import Roguelike.Tiles.GameTile;
import com.badlogic.gdx.utils.XmlReader;

/**
 * Created by Philip on 26/12/2015.
 */
public class EffectTypeSummon extends AbstractEffectType
{
	String entityName;
	int duration;

	@Override
	public void update( ActiveAbility aa, float time, GameTile tile )
	{
		GameEntity entity = GameEntity.load( entityName );

		entity.factions = aa.getCaster().factions;

		StatusEffect status = StatusEffect.load( "Summon" );
		status.duration = duration;

		entity.addStatusEffect( status );

		tile.addGameEntity( entity );
	}

	@Override
	public void parse( XmlReader.Element xml )
	{
		entityName = xml.getText();
		duration = xml.getIntAttribute( "Duration", 10 );
	}

	@Override
	public AbstractEffectType copy()
	{
		EffectTypeSummon cpy = new EffectTypeSummon();
		cpy.entityName = entityName;
		cpy.duration = duration;

		return cpy;
	}

	@Override
	public String toString( ActiveAbility aa )
	{
		return "Summons " + entityName + " for " + duration + " turns.";
	}
}
