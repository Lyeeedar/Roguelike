package Roguelike.Ability;

import Roguelike.Sprite.Sprite;

import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;

public interface IAbility
{
	public String getName();
	public Table createTable(Skin skin);
	public Sprite getIcon();
}
