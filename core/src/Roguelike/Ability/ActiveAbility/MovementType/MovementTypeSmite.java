package Roguelike.Ability.ActiveAbility.MovementType;

import Roguelike.Ability.ActiveAbility.ActiveAbility;

import com.badlogic.gdx.utils.XmlReader.Element;

public class MovementTypeSmite extends AbstractMovementType
{

	@Override
	public void parse( Element xml )
	{

	}

	@Override
	public void init( ActiveAbility ab, int endx, int endy )
	{
		ab.AffectedTiles.clear();
		ab.AffectedTiles.add( ab.source.level.getGameTile( endx, endy ) );
	}

	@Override
	public boolean update( ActiveAbility ab )
	{

		return true;
	}

	@Override
	public AbstractMovementType copy()
	{
		MovementTypeSmite t = new MovementTypeSmite();

		return t;
	}

	@Override
	public void updateAccumulators( float cost )
	{
	}

	@Override
	public boolean needsUpdate()
	{
		return false;
	}

}
