package Roguelike.Ability.ActiveAbility.MovementType;

import Roguelike.Global;
import Roguelike.Ability.ActiveAbility.ActiveAbility;
import Roguelike.Global.Direction;
import Roguelike.Sprite.MoveAnimation;
import Roguelike.Sprite.Sprite;
import Roguelike.Sprite.SpriteEffect;
import Roguelike.Sprite.MoveAnimation.MoveEquation;
import Roguelike.Sprite.StretchAnimation;
import Roguelike.Sprite.StretchAnimation.StretchEquation;
import Roguelike.Tiles.GameTile;

import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.XmlReader.Element;

public class MovementTypeSmite extends AbstractMovementType
{
	private String animationType;
	private String animationEquation;
	private float animationTime;
	
	@Override
	public void parse(Element xml)
	{
		animationType = xml.get("AnimationType", "Move").toUpperCase();
		animationEquation = xml.get("AnimationEquation", "Linear").toUpperCase();
		animationTime = xml.getFloat("AnimationTime", 1);
	}

	@Override
	public void init(ActiveAbility ab, int endx, int endy)
	{
		ab.AffectedTiles.clear();
		ab.AffectedTiles.add(ab.source.level.getGameTile(endx, endy));
	}

	@Override
	public boolean update(ActiveAbility ab)
	{
		if (ab.getSprite() != null)
		{
			Sprite sprite = ab.getSprite().copy();
			SpriteEffect effect = new SpriteEffect(sprite, Direction.CENTER, null);
			
			GameTile tile = ab.AffectedTiles.get(0);
			
			int[] diff = tile.getPosDiff(ab.source);
			int distMoved = ( Math.abs(diff[0]) + Math.abs(diff[1]) ) / Global.TileSize;
			
			if (animationType.equals("MOVE"))
			{
				effect.Sprite.spriteAnimation = new MoveAnimation(0.05f * distMoved * animationTime, diff, MoveEquation.valueOf(animationEquation));
			}
			else if (animationType.equals("STRETCH"))
			{
				effect.Sprite.spriteAnimation = new StretchAnimation(0.01f * distMoved * animationTime, diff, 0.5f, StretchEquation.valueOf(animationEquation));
			}
			
			// calc rotation
			Vector2 vec = new Vector2(diff[0]*-1, diff[1]*-1);
			vec.nor();
			float x = vec.x;
			float y = vec.y;
			double dot = 0*x + 1*y; // dot product
			double det = 0*y - 1*x; // determinant
			float angle = (float) Math.atan2(det, dot) * MathUtils.radiansToDegrees;
			effect.Sprite.rotation = angle;
			
			tile.spriteEffects.add(effect);
		}
		
		return true;
	}

	
	@Override
	public AbstractMovementType copy()
	{
		MovementTypeSmite t = new MovementTypeSmite();
		t.animationType = animationType;
		t.animationEquation = animationEquation;
		t.animationTime = animationTime;
		return t;
	}

	@Override
	public void updateAccumulators(float cost)
	{
	}

	
	@Override
	public boolean needsUpdate()
	{
		return false;
	}

}
