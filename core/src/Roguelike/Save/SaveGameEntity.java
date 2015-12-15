package Roguelike.Save;

import java.util.HashMap;

import Roguelike.Ability.AbilityTree;
import Roguelike.Ability.IAbility;
import Roguelike.Entity.GameEntity;
import Roguelike.Items.Inventory;
import Roguelike.StatusEffect.StatusEffect;
import Roguelike.Tiles.Point;

import com.badlogic.gdx.utils.Array;

public final class SaveGameEntity extends SaveableObject<GameEntity>
{
	public String fileName;
	public int hp;
	public int essence;
	public Point pos = new Point();
	public boolean isPlayer = false;
	public Array<StatusEffect> statuses = new Array<StatusEffect>();
	public Array<CooldownWrapper> abilityCooldown = new Array<CooldownWrapper>();
	public Inventory inventory;
	public String UID;
	public HashMap<String, Integer> dialogueData = new HashMap<String, Integer>();

	// need to save ai

	@Override
	public void store( GameEntity obj )
	{
		fileName = obj.fileName;
		hp = obj.HP;
		essence = obj.essence;
		pos.set( obj.tile[0][0].x, obj.tile[0][0].y );
		for ( StatusEffect status : obj.statusEffects )
		{
			statuses.add( status );
		}
		inventory = obj.inventory;

		for ( AbilityTree a : obj.slottedAbilities )
		{
			abilityCooldown.add( new CooldownWrapper( a.current.current.getCooldown() ) );
		}

		UID = obj.UID;

		if ( obj.dialogue != null )
		{
			dialogueData = (HashMap<String, Integer>) obj.dialogue.data.clone();
		}
	}

	@Override
	public GameEntity create()
	{
		GameEntity entity = GameEntity.load( fileName );

		entity.essence = essence;
		entity.HP = hp;
		for ( StatusEffect saveStatus : statuses )
		{
			entity.addStatusEffect( saveStatus );
		}
		entity.inventory = inventory;

		if ( !isPlayer )
		{
			for ( int i = 0; i < abilityCooldown.size; i++ )
			{
				entity.slottedAbilities.get( i ).current.current.setCooldown( abilityCooldown.get( i ).val );
			}
		}

		entity.UID = UID;

		if ( entity.dialogue != null )
		{
			entity.dialogue.data = (HashMap<String, Integer>) dialogueData.clone();
		}

		return entity;
	}

	// Neccessary due to Kryo issue with boxed primitives.
	public static final class CooldownWrapper
	{
		public int val;

		public CooldownWrapper()
		{

		}

		public CooldownWrapper( int val )
		{
			this.val = val;
		}
	}
}
