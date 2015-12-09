package Roguelike.Ability;

import Roguelike.Entity.Entity;
import Roguelike.Sprite.Sprite;

import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;

public interface IAbility
{
	public String getName();

	public void onTurn();

	public void setCooldown( int val );

	public int getCooldown();

	public Table createTable( Skin skin, Entity entity );

	public Sprite getIcon();
}
