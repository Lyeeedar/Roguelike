package Roguelike.Fields.OnDeathEffect;

import java.util.HashMap;

import net.objecthunter.exp4j.Expression;
import net.objecthunter.exp4j.ExpressionBuilder;
import Roguelike.Fields.Field;
import Roguelike.StatusEffect.StatusEffect;
import Roguelike.Tiles.GameTile;

import com.badlogic.gdx.utils.XmlReader.Element;

import exp4j.Helpers.EquationHelper;

public class StatusOnDeathEffect extends AbstractOnDeathEffect
{
	private String condition;
	private String[] reliesOn;
	public String stacksEqn;
	public Element status;

	@Override
	public void process(Field field, GameTile tile)
	{
		HashMap<String, Integer> variableMap = new HashMap<String, Integer>();
		for (String name : reliesOn)
		{
			if (!variableMap.containsKey(name.toLowerCase()))
			{
				variableMap.put(name.toLowerCase(), 0);
			}
		}
		variableMap.put("stacks", field.stacks);

		if (condition != null)
		{

			int conditionVal = EquationHelper.evaluate( condition, variableMap );
			if (conditionVal == 0)
			{
				return;
			}
		}

		int stacks = 1;

		if (stacksEqn != null)
		{
			stacks = EquationHelper.evaluate( stacksEqn, variableMap );
		}

		for (int i = 0; i < stacks; i++)
		{
			if (tile.entity !=  null)
			{
				tile.entity.addStatusEffect(StatusEffect.load(status, field));
			}

			if (tile.environmentEntity != null && tile.environmentEntity.canTakeDamage)
			{
				tile.environmentEntity.addStatusEffect(StatusEffect.load(status, field));
			}
		}
	}

	@Override
	public void parse(Element xml)
	{
		condition = xml.getAttribute("Condition", null); if (condition != null) { condition = condition.toLowerCase(); }

		reliesOn = xml.getAttribute("ReliesOn", "").split(",");

		status = xml;
		stacksEqn = xml.getAttribute("Stacks", null); if (stacksEqn != null) { stacksEqn = stacksEqn.toLowerCase(); }
	}

}
