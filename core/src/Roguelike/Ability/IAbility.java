package Roguelike.Ability;

import Roguelike.Entity.Entity;
import Roguelike.Sprite.Sprite;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;

public interface IAbility
{
	String getDescription();

	String getName();

	void setTree(AbilityTree.AbilityStage tree );

	void onTurn();

	int getCooldown();

	void setCooldown( int val );

	Table createTable( Skin skin, Entity entity );

	Sprite getIcon();
}
