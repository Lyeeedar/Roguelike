package Roguelike.Pathfinding;

import Roguelike.Global.Passability;
import Roguelike.Util.EnumBitflag;

public interface PathfindingTile
{
	boolean getPassable( EnumBitflag<Passability> travelType, Object self );

	int getInfluence( EnumBitflag<Passability> travelType, Object self );
}
