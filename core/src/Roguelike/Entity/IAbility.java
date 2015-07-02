package Roguelike.Entity;

import Roguelike.Sprite.Sprite;

import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;

public interface IAbility
{
	public Table createTable(Skin skin);
	public Sprite getIcon();
}
