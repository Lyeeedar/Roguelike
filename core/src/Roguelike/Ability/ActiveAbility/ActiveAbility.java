package Roguelike.Ability.ActiveAbility;

import Roguelike.Ability.AbilityTree;
import Roguelike.Ability.ActiveAbility.CostType.AbstractCostType;
import Roguelike.Ability.ActiveAbility.EffectType.AbstractEffectType;
import Roguelike.Ability.ActiveAbility.MovementType.AbstractMovementType;
import Roguelike.Ability.ActiveAbility.MovementType.MovementTypeBolt;
import Roguelike.Ability.ActiveAbility.MovementType.MovementTypeRay;
import Roguelike.Ability.ActiveAbility.MovementType.MovementTypeSmite;
import Roguelike.Ability.ActiveAbility.TargetingType.AbstractTargetingType;
import Roguelike.Ability.ActiveAbility.TargetingType.TargetingTypeSelf;
import Roguelike.Ability.ActiveAbility.TargetingType.TargetingTypeTile;
import Roguelike.Ability.IAbility;
import Roguelike.AssetManager;
import Roguelike.Entity.Entity;
import Roguelike.Entity.GameEntity;
import Roguelike.GameEvent.IGameObject;
import Roguelike.Global;
import Roguelike.Global.Direction;
import Roguelike.Global.Passability;
import Roguelike.Items.Item;
import Roguelike.Items.Item.EquipmentSlot;
import Roguelike.Lights.Light;
import Roguelike.Pathfinding.ShadowCastCache;
import Roguelike.Pathfinding.ShadowCaster;
import Roguelike.Screens.GameScreen;
import Roguelike.Sound.SoundInstance;
import Roguelike.Sprite.Sprite;
import Roguelike.Sprite.SpriteAnimation.MoveAnimation;
import Roguelike.Sprite.SpriteEffect;
import Roguelike.Tiles.GameTile;
import Roguelike.Tiles.Point;
import Roguelike.Util.EnumBitflag;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.XmlReader;
import com.badlogic.gdx.utils.XmlReader.Element;

import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;

public class ActiveAbility implements IAbility, IGameObject
{
	public int cooldownAccumulator;
	public int cooldown = 1;
	public Array<AbstractCostType> costTypes = new Array<AbstractCostType>();
	public AbstractTargetingType targetingType = new TargetingTypeTile();
	public AbstractMovementType movementType = new MovementTypeSmite();
	public Array<AbstractEffectType> effectTypes = new Array<AbstractEffectType>();
	public Array<GameTile> AffectedTiles = new Array<GameTile>();
	public GameTile source;
	private GameEntity caster;
	private HashMap<String, Integer> variableMap = new HashMap<String, Integer>();
	public EnumBitflag<Passability> abilityPassability = new EnumBitflag<Passability>( Passability.LEVITATE );
	public Light light;
	public Sprite Icon;
	public boolean hasValidTargets = true;
	private String name;
	private String description;
	private int cone = 0;
	private int aoe = 0;
	private boolean excludeSelf = false;
	private int range = 1;
	private float screenshake = 0;
	private ShadowCastCache cache = new ShadowCastCache();
	private Sprite movementSprite;
	private Sprite hitSprite;
	private boolean singleSprite = false;
	private Sprite useSprite;
	private boolean spentCost = false;

	public AbilityTree.AbilityStage tree;

	// ----------------------------------------------------------------------
	@Override
	public void setCaster(Entity e)
	{
		caster = (GameEntity)e;
		setVariableMap( caster.getVariableMap() );
	}

	// ----------------------------------------------------------------------
	@Override
	public void setTree(AbilityTree.AbilityStage tree )
	{
		this.tree = tree;
	}

	// ----------------------------------------------------------------------
	public GameEntity getCaster()
	{
		return caster;
	}

	// ----------------------------------------------------------------------
	public void setVariableMap(HashMap<String, Integer> map)
	{
		variableMap = map;
	}

	// ----------------------------------------------------------------------
	public HashMap<String, Integer> getVariableMap()
	{
		if (tree != null)
		{
			variableMap.put("level", tree.level);
		}
		else
		{
			variableMap.put("level", 1);
		}

		return variableMap;
	}

	// ----------------------------------------------------------------------
	public static ActiveAbility load( String name )
	{
		ActiveAbility ab = new ActiveAbility();

		ab.internalLoad( name );

		return ab;
	}

	// ----------------------------------------------------------------------
	public static ActiveAbility load( Element xml )
	{
		ActiveAbility ab = new ActiveAbility();

		ab.internalLoad( xml );

		return ab;
	}

	// ----------------------------------------------------------------------
	public boolean isAvailable()
	{
		if ( !hasValidTargets ) { return false; }

		if ( cooldownAccumulator > 0 ) { return false; }

		for ( AbstractCostType cost : costTypes )
		{
			if ( !cost.isCostAvailable( this ) ) { return false; }
		}

		return true;
	}

	// ----------------------------------------------------------------------
	public ActiveAbility copy()
	{
		ActiveAbility aa = new ActiveAbility();

		aa.name = name;
		aa.description = description;
		aa.aoe = aoe;
		aa.range = range;
		aa.cone = cone;
		aa.screenshake = screenshake;
		aa.cache = cache.copy();

		aa.targetingType = targetingType.copy();
		aa.movementType = movementType.copy();

		for ( AbstractCostType cost : costTypes )
		{
			aa.costTypes.add( cost.copy() );
		}

		for ( AbstractEffectType effect : effectTypes )
		{
			aa.effectTypes.add( effect.copy() );
		}

		for ( GameTile tile : AffectedTiles )
		{
			aa.AffectedTiles.add( tile );
		}

		aa.light = light != null ? light.copy() : null;
		aa.Icon = Icon != null ? Icon.copy() : null;
		aa.movementSprite = movementSprite != null ? movementSprite.copy() : null;
		aa.hitSprite = hitSprite != null ? hitSprite.copy() : null;
		aa.useSprite = useSprite != null ? useSprite.copy() : null;
		aa.singleSprite = singleSprite;

		aa.tree = tree;

		return aa;
	}

	// ----------------------------------------------------------------------
	public Sprite getSprite()
	{
		return movementSprite;
	}

	// ----------------------------------------------------------------------
	public boolean isTargetValid( GameTile tile, Array<Point> valid )
	{
		for ( Point validTile : valid )
		{
			if ( validTile.x == tile.x && validTile.y == tile.y ) { return true; }
		}

		return false;
	}

	// ----------------------------------------------------------------------
	public Array<Point> getValidTargets()
	{
		Array<Point> validTargets = new Array<Point>();

		Array<Point> output = cache.getShadowCast( source.level.getGrid(), source.x, source.y, getRange(), caster );

		for ( Point tilePos : output )
		{
			GameTile tile = source.level.getGameTile( tilePos );

			if ( targetingType.isTargetValid( this, tile ) )
			{
				validTargets.add( tilePos.copy() );
			}
		}

		return validTargets;
	}

	// ----------------------------------------------------------------------
	public int getRange()
	{
		if ( range > 0 ) { return range; }
		Item item = caster.getInventory().getEquip( EquipmentSlot.WEAPON );
		if ( item != null ) { return item.getRange( caster ); }
		return 1;
	}

	// ----------------------------------------------------------------------
	public void lockTarget( GameTile tile )
	{
		int cx = 0;
		int cy = 0;
		int dst = Integer.MAX_VALUE;

		for ( int x = 0; x < caster.size; x++ )
		{
			for ( int y = 0; y < caster.size; y++ )
			{
				int tmpdst = Math.abs( tile.x - ( caster.tile[ 0 ][ 0 ].x + x ) ) + Math.abs( tile.y - ( caster.tile[ 0 ][ 0 ].y + y ) );

				if ( tmpdst < dst )
				{
					dst = tmpdst;
					cx = x;
					cy = y;
				}
			}
		}

		source = caster.tile[ cx ][ cy ];

		movementType.init( this, tile.x, tile.y );
	}

	// ----------------------------------------------------------------------
	public void updateAccumulators( float cost )
	{
		movementType.updateAccumulators( cost );
	}

	// ----------------------------------------------------------------------
	public boolean needsUpdate()
	{
		return movementType.needsUpdate();

	}

	// ----------------------------------------------------------------------
	public boolean update()
	{
		if ( !spentCost )
		{
			for ( AbstractCostType cost : costTypes )
			{
				cost.spendCost( this );
			}
			spentCost = true;

			if ( useSprite != null )
			{
				Sprite sprite = useSprite.copy();
				sprite.size = caster.size;

				caster.tile[ 0 ][ 0 ].spriteEffects.add( new SpriteEffect( sprite, Direction.CENTER, light != null ? light.copyNoFlag() : null ) );
			}
		}

		boolean finished = movementType.update( this );
		if ( movementSprite != null )
		{
			movementSprite.rotation = movementType.direction.getAngle();
		}

		if ( finished )
		{
			// play move sprites, if applicable
			if ( !( movementType instanceof MovementTypeBolt ) )
			{
				if ( movementSprite != null )
				{
					for ( GameTile tile : AffectedTiles )
					{
						int[] diff = tile.getPosDiff( source );

						Sprite sprite = movementSprite.copy();
						if ( sprite.spriteAnimation == null && movementType instanceof MovementTypeSmite )
						{
							sprite.spriteAnimation = new MoveAnimation();
						}

						if ( sprite.spriteAnimation != null )
						{
							int distMoved = ( Math.abs( diff[ 0 ] ) + Math.abs( diff[ 1 ] ) ) / Global.TileSize;
							sprite.spriteAnimation.set( 0.05f * distMoved, diff );
						}

						Vector2 vec = new Vector2( diff[ 0 ] * -1, diff[ 1 ] * -1 );
						vec.nor();
						float x = vec.x;
						float y = vec.y;
						double dot = 0 * x + 1 * y; // dot product
						double det = 0 * y - 1 * x; // determinant
						float angle = (float) Math.atan2( det, dot ) * MathUtils.radiansToDegrees;
						sprite.rotation = angle;

						if ( AffectedTiles.size > 1 )
						{
							sprite.renderDelay = sprite.animationDelay * tile.getDist( source ) + sprite.animationDelay;
						}

						SpriteEffect effect = new SpriteEffect( sprite, Direction.CENTER, light != null ? light.copyNoFlag() : null );

						tile.spriteEffects.add( effect );
					}
				}
			}

			GameTile epicenter = AffectedTiles.peek();

			if ( movementType instanceof MovementTypeRay )
			{
				epicenter = AffectedTiles.first();
			}
			else if ( aoe > 0 )
			{
				Array<Point> output = new Array<Point>();

				ShadowCaster shadow = new ShadowCaster( epicenter.level.getGrid(), aoe, abilityPassability, caster );
				shadow.ComputeFOV( epicenter.x, epicenter.y, output );

				for ( Point tilePos : output )
				{
					GameTile tile = epicenter.level.getGameTile( tilePos );
					AffectedTiles.add( tile );
				}

				Global.PointPool.freeAll( output );
			}
			else if ( cone > 0 )
			{
				Direction dir = Direction.getDirection( source, epicenter );

				Point epicenterPoint = Global.PointPool.obtain().set( epicenter.x, epicenter.y );
				Array<Point> cone = Direction.buildCone( dir, epicenterPoint, this.cone );
				Global.PointPool.free( epicenterPoint );

				Array<Point> output = new Array<Point>();

				ShadowCaster shadow = new ShadowCaster( epicenter.level.getGrid(), this.cone, abilityPassability, caster );
				shadow.ComputeFOV( epicenter.x, epicenter.y, output );

				for ( Point tilePos : cone )
				{
					GameTile tile = epicenter.level.getGameTile( tilePos );

					boolean canAdd = false;
					for ( Point visPos : output )
					{
						GameTile visTile = epicenter.level.getGameTile( visPos );

						if ( tile == visTile )
						{
							canAdd = true;
							break;
						}
					}

					if ( canAdd )
					{
						AffectedTiles.add( tile );
					}
				}

				Global.PointPool.freeAll( output );
			}

			// minimise list
			HashSet<GameTile> added = new HashSet<GameTile>();
			Iterator<GameTile> itr = AffectedTiles.iterator();
			while ( itr.hasNext() )
			{
				GameTile tile = itr.next();

				if ( !added.contains( tile ) )
				{
					added.add( tile );
				}
				else
				{
					itr.remove();
				}
			}

			if ( excludeSelf )
			{
				AffectedTiles.removeValue( epicenter, true );
			}

			for ( GameTile tile : AffectedTiles )
			{
				for ( AbstractEffectType effect : effectTypes )
				{
					effect.update( this, 1, tile );
				}

				if ( getHitSprite() != null && ( aoe == 0 || !singleSprite ) )
				{
					int[] diff = tile.getPosDiff( epicenter );

					Sprite sprite = getHitSprite().copy();
					if ( sprite.spriteAnimation != null )
					{
						int distMoved = ( Math.abs( diff[ 0 ] ) + Math.abs( diff[ 1 ] ) ) / Global.TileSize;
						sprite.spriteAnimation.set( 0.05f * distMoved, diff );
					}

					Vector2 vec = new Vector2( diff[ 0 ] * -1, diff[ 1 ] * -1 );
					vec.nor();
					float x = vec.x;
					float y = vec.y;
					double dot = 0 * x + 1 * y; // dot product
					double det = 0 * y - 1 * x; // determinant
					float angle = (float) Math.atan2( det, dot ) * MathUtils.radiansToDegrees;
					sprite.rotation = angle;

					SpriteEffect effect = new SpriteEffect( sprite, Direction.CENTER, light != null ? light.copyNoFlag() : null );

					sprite.renderDelay = sprite.animationDelay * tile.getDist( epicenter ) + sprite.animationDelay;

					SoundInstance sound = sprite.sound;
					if ( sound != null )
					{
						sound.play( tile );
					}

					tile.spriteEffects.add( effect );
				}
			}

			if ( getHitSprite() != null && aoe > 0 && singleSprite )
			{
				int ex = epicenter.x - aoe;
				int ey = epicenter.y - aoe;

				GameTile tile = epicenter.level.getGameTile( ex, ey );
				int[] diff = tile.getPosDiff( epicenter );

				Sprite sprite = getHitSprite().copy();
				if ( sprite.spriteAnimation != null )
				{
					int distMoved = ( Math.abs( diff[ 0 ] ) + Math.abs( diff[ 1 ] ) ) / Global.TileSize;
					sprite.spriteAnimation.set( 0.05f * distMoved, diff );
				}
				sprite.size = aoe * 2 + 1;

				SpriteEffect effect = new SpriteEffect( sprite, Direction.CENTER, light != null ? light.copyNoFlag() : null );

				SoundInstance sound = sprite.sound;
				if ( sound != null )
				{
					sound.play( tile );
				}

				tile.spriteEffects.add( effect );
			}

			if ( screenshake > 0 )
			{
				// check distance for screenshake
				float dist = Vector2.dst( epicenter.x, epicenter.y, epicenter.level.player.tile[ 0 ][ 0 ].x, epicenter.level.player.tile[ 0 ][ 0 ].y );
				float shakeRadius = screenshake;
				if ( aoe != 0 && dist > aoe )
				{
					shakeRadius *= ( dist - aoe ) / ( aoe * 2 );
				}

				if ( shakeRadius > 2 )
				{
					GameScreen.Instance.screenShakeRadius = shakeRadius;
					GameScreen.Instance.screenShakeAngle = MathUtils.random() * 360;
				}
			}
		}

		return finished;
	}

	// ----------------------------------------------------------------------
	public Sprite getHitSprite()
	{
		if ( hitSprite != null ) { return hitSprite; }
		Item wep = caster.getInventory().getEquip( EquipmentSlot.WEAPON );
		if ( wep != null ) { return wep.getWeaponHitEffect(); }
		return caster.defaultHitEffect;
	}

	// ----------------------------------------------------------------------
	private void internalLoad( String name )
	{
		XmlReader xml = new XmlReader();
		Element xmlElement = null;

		try
		{
			xmlElement = xml.parse( Gdx.files.internal( "Abilities/" + name + ".xml" ) );
		}
		catch ( IOException e )
		{
			e.printStackTrace();
		}

		internalLoad( xmlElement );
	}

	// ----------------------------------------------------------------------
	private void internalLoad( Element xmlElement )
	{

		String extendsElement = xmlElement.getAttribute( "Extends", null );
		if ( extendsElement != null )
		{
			internalLoad( extendsElement );
		}

		this.name = xmlElement.get( "Name", this.name );
		description = xmlElement.get( "Description", description );

		Element aoeElement = xmlElement.getChildByName( "AOE" );
		if ( aoeElement != null )
		{
			aoe = Integer.parseInt( aoeElement.getText() );
			excludeSelf = aoeElement.getBoolean( "ExcludeSelf", false );
		}

		cone = xmlElement.getInt( "Cone", cone );
		range = xmlElement.getInt( "Range", range );
		cooldown = xmlElement.getInt( "Cooldown", cooldown );
		screenshake = xmlElement.getFloat( "ScreenShake", screenshake );

		Icon = xmlElement.getChildByName( "Icon" ) != null ? AssetManager.loadSprite( xmlElement.getChildByName( "Icon" ) ) : Icon;
		movementSprite = xmlElement.getChildByName( "MovementSprite" ) != null ? AssetManager.loadSprite( xmlElement.getChildByName( "MovementSprite" ) ) : movementSprite;
		useSprite = xmlElement.getChildByName( "UseSprite" ) != null ? AssetManager.loadSprite( xmlElement.getChildByName( "UseSprite" ) ) : useSprite;

		Element hitSpriteEl = xmlElement.getChildByName( "HitSprite" );
		if ( hitSpriteEl != null )
		{
			hitSprite = AssetManager.loadSprite( xmlElement.getChildByName( "HitSprite" ) );
			singleSprite = hitSpriteEl.getBoolean( "SingleSprite", false );
		}

		Element lightElement = xmlElement.getChildByName( "Light" );
		if ( lightElement != null )
		{
			light = Roguelike.Lights.Light.load( lightElement );
		}

		Element costsElement = xmlElement.getChildByName( "Cost" );
		if ( costsElement != null )
		{
			for ( int i = 0; i < costsElement.getChildCount(); i++ )
			{
				Element costElement = costsElement.getChild( i );
				costTypes.add( AbstractCostType.load( costElement ) );
			}
		}

		Element targetingElement = xmlElement.getChildByName( "Targeting" );
		if ( targetingElement != null )
		{
			targetingType = AbstractTargetingType.load( targetingElement.getChild( 0 ) );
		}

		Element movementElement = xmlElement.getChildByName( "Movement" );
		if ( movementElement != null )
		{
			movementType = AbstractMovementType.load( movementElement.getChild( 0 ) );
		}
		else if (targetingType instanceof TargetingTypeSelf)
		{
			movementType = new MovementTypeSmite();
		}

		Element effectsElement = xmlElement.getChildByName( "Effect" );
		if ( effectsElement != null )
		{
			for ( int i = 0; i < effectsElement.getChildCount(); i++ )
			{
				Element effectElement = effectsElement.getChild( i );
				effectTypes.add( AbstractEffectType.load( effectElement ) );
			}
		}

		Element passabilityElement = xmlElement.getChildByName( "Passability" );
		if ( passabilityElement != null )
		{
			abilityPassability = Passability.parseArray( passabilityElement.getText() );
		}
	}

	// ----------------------------------------------------------------------
	@Override
	public String getDescription()
	{
		return description;
	}

	// ----------------------------------------------------------------------
	@Override
	public String getName()
	{
		return name;
	}

	// ----------------------------------------------------------------------
	@Override
	public void onTurn()
	{
		cooldownAccumulator--;
		if ( cooldownAccumulator < 0 )
		{
			cooldownAccumulator = 0;
		}
	}

	// ----------------------------------------------------------------------
	@Override
	public int getCooldown()
	{
		return cooldownAccumulator;
	}

	// ----------------------------------------------------------------------
	@Override
	public void setCooldown( int val )
	{
		cooldownAccumulator = val;
	}

	// ----------------------------------------------------------------------
	@Override
	public Table createTable( Skin skin, Entity entity )
	{
		Table table = new Table();

		Table header = new Table();

		header.add( new Label( name, skin, "title" ) ).expandX().left();

		{
			Label label = new Label( "Active", skin );
			label.setFontScale( 0.7f );
			header.add( label ).expandX().right();
		}
		table.add(header).expandX().fillX().left();

		table.row();

		String level = "Level: " + tree.level;

		if (tree.level == 10)
		{
			if (tree.branch1 != null)
			{
				level += " ( Mutate )";
			}
			else
			{
				level += " ( Max )";
			}
		}
		else
		{
			float per = (float) tree.exp / (float) tree.expToNextLevel;
			per *= 100;
			level += " ( " + (int)per + "% )";
		}

		table.add(new Label(level, skin)).left();
		table.row();

		Label descLabel = new Label( description, skin );
		descLabel.setWrap( true );
		table.add( descLabel ).expand().left().width( com.badlogic.gdx.scenes.scene2d.ui.Value.percentWidth( 1, table ) );
		table.row();

		table.add( new Label( "Cooldown: " + cooldown, skin ) ).expandX().left();
		table.row();

		table.add( new Label( "Range: " + getRange(), skin ) ).expandX().left();
		table.row();

		if (aoe > 0)
		{
			table.add( new Label( "AOE: " + aoe, skin ) ).expandX().left();
			table.row();
		}

		if (cone > 0)
		{
			table.add( new Label( "Cone: " + cone, skin ) ).expandX().left();
			table.row();
		}

		for ( AbstractCostType cost : costTypes )
		{
			String string = cost.toString( this );
			Label label = new Label( string, skin );
			label.setWrap( true );

			table.add( label ).expand().left().width( com.badlogic.gdx.scenes.scene2d.ui.Value.percentWidth( 1, table ) );
			table.row();
		}

		table.add( new Label( "", skin ) );
		table.row();

		for ( AbstractEffectType effect : effectTypes )
		{
			String string = effect.toString( this );
			Label label = new Label( string, skin );
			label.setWrap( true );

			table.add( label ).expand().left().width( com.badlogic.gdx.scenes.scene2d.ui.Value.percentWidth( 1, table ) ).padBottom( 5 );
			table.row();
		}

		if ( !hasValidTargets )
		{
			table.add( new Label( "[RED]No valid targets", skin ) ).expandX().left();
			table.row();
		}

		if ( cooldownAccumulator > 0 )
		{
			table.add( new Label( "[RED]On cooldown", skin ) ).expandX().left();
			table.row();
		}

		return table;
	}

	// ----------------------------------------------------------------------
	@Override
	public Sprite getIcon()
	{
		return Icon;
	}
}
