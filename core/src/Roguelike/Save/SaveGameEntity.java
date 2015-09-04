package Roguelike.Save;

import java.util.HashMap;

import Roguelike.Ability.ActiveAbility.ActiveAbility;
import Roguelike.Entity.GameEntity;
import Roguelike.Items.Inventory;
import Roguelike.StatusEffect.StatusEffect;
import Roguelike.Tiles.Point;

import com.badlogic.gdx.utils.Array;

public class SaveGameEntity extends SaveableObject<GameEntity>
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
		pos.set( obj.tile.x, obj.tile.y );
		for ( StatusEffect status : obj.statusEffects )
		{
			statuses.add( status );
		}
		inventory = obj.inventory;

		for ( ActiveAbility aa : obj.slottedActiveAbilities )
		{
			abilityCooldown.add( new CooldownWrapper( aa.cooldownAccumulator ) );
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
				entity.slottedActiveAbilities.get( i ).cooldownAccumulator = abilityCooldown.get( i ).val;
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
	public static class CooldownWrapper
	{
		public float val;

		public CooldownWrapper()
		{

		}

		public CooldownWrapper( float val )
		{
			this.val = val;
		}
	}
}
