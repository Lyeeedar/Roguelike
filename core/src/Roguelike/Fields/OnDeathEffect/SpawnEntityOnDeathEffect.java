package Roguelike.Fields.OnDeathEffect;

import java.util.HashMap;

import Roguelike.Entity.GameEntity;
import com.badlogic.gdx.Game;
import net.objecthunter.exp4j.Expression;
import net.objecthunter.exp4j.ExpressionBuilder;
import Roguelike.Fields.Field;
import Roguelike.Tiles.GameTile;

import com.badlogic.gdx.utils.XmlReader.Element;

import exp4j.Helpers.EquationHelper;

public class SpawnEntityOnDeathEffect extends AbstractOnDeathEffect
{
	private String entityName;

	@Override
	public void process(Field field, GameTile tile)
	{
		GameEntity entity = GameEntity.load( entityName );

		tile.addGameEntity( entity );
	}

	@Override
	public void parse(Element xml)
	{
		entityName = xml.getText();
	}

}