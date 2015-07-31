package Roguelike.Ability.ActiveAbility.EffectType;

import java.util.HashMap;

import net.objecthunter.exp4j.Expression;
import net.objecthunter.exp4j.ExpressionBuilder;
import Roguelike.Ability.ActiveAbility.ActiveAbility;
import Roguelike.Entity.Entity;
import Roguelike.Global.Direction;
import Roguelike.Lights.Light;
import Roguelike.Sprite.SpriteEffect;
import Roguelike.Tiles.GameTile;

import com.badlogic.gdx.utils.XmlReader.Element;

import exp4j.Helpers.EquationHelper;

public class EffectTypeHeal extends AbstractEffectType
{
	private String equation;
	private String[] reliesOn;
	
	@Override
	public void parse(Element xml)
	{	
		reliesOn = xml.getAttribute("ReliesOn", "").split(",");		
		equation = xml.getText().toLowerCase();
	}

	@Override
	public void update(ActiveAbility aa, float time, GameTile tile)
	{		
		if (tile.entity != null)
		{
			applyToEntity(tile.entity, aa);
		}
		
		if (tile.environmentEntity != null)
		{
			applyToEntity(tile.environmentEntity, aa);
		}
	}

	private void applyToEntity(Entity e, ActiveAbility aa)
	{
		HashMap<String, Integer> variableMap = aa.variableMap;
		
		for (String name : reliesOn)
		{
			if (!variableMap.containsKey(name.toLowerCase()))
			{
				variableMap.put(name.toLowerCase(), 0);
			}
		}
		
		ExpressionBuilder expB = EquationHelper.createEquationBuilder(equation);
		EquationHelper.setVariableNames(expB, variableMap, "");
							
		Expression exp = EquationHelper.tryBuild(expB);
		if (exp == null)
		{
			return;
		}
		
		EquationHelper.setVariableValues(exp, variableMap, "");
							
		int raw = (int)exp.evaluate();
		
		e.applyHealing(raw);
	}
	
	@Override
	public AbstractEffectType copy()
	{
		EffectTypeHeal heal = new EffectTypeHeal();
		heal.equation = equation;
		heal.reliesOn = reliesOn;
		
		return heal;
	}
}
