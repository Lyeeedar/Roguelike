package Roguelike.Save;

import java.util.HashMap;

import Roguelike.Ability.AbilityTree;
import Roguelike.Ability.IAbility;
import Roguelike.Entity.GameEntity;
import Roguelike.Items.Inventory;
import Roguelike.StatusEffect.StatusEffect;
import Roguelike.Tiles.Point;

import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.XmlReader;

public final class SaveGameEntity extends SaveableObject<GameEntity>
{
	public String fileName;
	public Array<XmlReader.Element> xmlData;

	public int hp;
	public int essence;
	public Point pos = new Point();
	public boolean isPlayer = false;
	public Array<StatusEffect> statuses = new Array<StatusEffect>();
	public Array<SaveAbilityTree> slottedAbilities = new Array<SaveAbilityTree>();
	public Inventory inventory;
	public String UID;
	public HashMap<String, Integer> dialogueData = new HashMap<String, Integer>();

	// need to save ai

	@Override
	public void store( GameEntity obj )
	{
		fileName = obj.fileName;
		xmlData = obj.xmlData;

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
			if (a != null)
			{
				SaveAbilityTree saveTree = new SaveAbilityTree();
				saveTree.store( a );

				slottedAbilities.add( saveTree );
			}
			else
			{
				slottedAbilities.add( null );
			}
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
		GameEntity entity = fileName != null ? GameEntity.load( fileName ) : GameEntity.load( xmlData );

		entity.essence = essence;
		entity.HP = hp;
		for ( StatusEffect saveStatus : statuses )
		{
			entity.addStatusEffect( saveStatus );
		}
		entity.inventory = inventory;

		for ( int i = 0; i < slottedAbilities.size; i++ )
		{
			SaveAbilityTree saveTree = slottedAbilities.get( i );

			if (entity.slottedAbilities.size <= i)
			{
				if (saveTree == null)
				{
					entity.slottedAbilities.add( null );
				}
				else
				{
					entity.slottedAbilities.add( saveTree.create() );
					entity.slottedAbilities.get( i ).current.current.setCaster( entity );
				}
			}
			else
			{
				AbilityTree tree = entity.slottedAbilities.get( i );

				if (tree == null)
				{
					if (saveTree != null)
					{
						entity.slottedAbilities.removeIndex( i );
						entity.slottedAbilities.insert( i, saveTree.create() );
					}
				}
				else
				{
					saveTree.writeData( tree );
				}

				entity.slottedAbilities.get( i ).current.current.setCaster( entity );
			}
		}

		entity.UID = UID;

		if ( entity.dialogue != null )
		{
			entity.dialogue.data = (HashMap<String, Integer>) dialogueData.clone();
		}

		return entity;
	}
}
