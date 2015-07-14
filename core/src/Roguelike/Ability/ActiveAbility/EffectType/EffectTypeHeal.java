package Roguelike.Ability.ActiveAbility.EffectType;

import java.util.HashMap;

import net.objecthunter.exp4j.Expression;
import net.objecthunter.exp4j.ExpressionBuilder;
import Roguelike.Ability.ActiveAbility.ActiveAbility;
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
		equation = xml.get("Heal").toUpperCase();
	}

	@Override
	public void update(ActiveAbility aa, float time, GameTile tile)
	{
		HashMap<String, Integer> variableMap = aa.caster.getVariableMap();
		
		for (String name : reliesOn)
		{
			if (!variableMap.containsKey(name.toUpperCase()))
			{
				variableMap.put(name.toUpperCase(), 0);
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
		
		tile.Entity.applyHealing(raw);
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
