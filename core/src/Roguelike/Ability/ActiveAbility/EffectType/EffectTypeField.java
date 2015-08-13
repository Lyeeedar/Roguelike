package Roguelike.Ability.ActiveAbility.EffectType;

import net.objecthunter.exp4j.Expression;
import net.objecthunter.exp4j.ExpressionBuilder;
import Roguelike.Ability.ActiveAbility.ActiveAbility;
import Roguelike.Fields.Field;
import Roguelike.GameEvent.Damage.DamageObject;
import Roguelike.Tiles.GameTile;

import com.badlogic.gdx.utils.XmlReader.Element;

import exp4j.Helpers.EquationHelper;

public class EffectTypeField extends AbstractEffectType
{
	public String condition;
	public String fieldName;
	public String stacksEqn;
	
	private String[] reliesOn;
	
	@Override
	public void parse(Element xml)
	{
		reliesOn = xml.getAttribute("ReliesOn", "").split(",");
		condition = xml.getAttribute("Condition", null); if (condition != null) { condition = condition.toLowerCase(); }
		fieldName = xml.get("Field");
		stacksEqn = xml.get("Stacks", null); if (stacksEqn != null) { stacksEqn = stacksEqn.toLowerCase(); }
	}

	@Override
	public void update(ActiveAbility aa, float time, GameTile tile)
	{		
		if (condition != null)
		{
			ExpressionBuilder expB = EquationHelper.createEquationBuilder(condition);
			EquationHelper.setVariableNames(expB, aa.variableMap, "");

			Expression exp = EquationHelper.tryBuild(expB);
			if (exp == null)
			{
				return;
			}
			
			EquationHelper.setVariableValues(exp, aa.variableMap, "");
						
			double conditionVal = exp.evaluate();
			
			if (conditionVal == 0)
			{
				return;
			}
		}
		
		int stacks = 1;
		
		if (stacksEqn != null)
		{
			ExpressionBuilder expB = EquationHelper.createEquationBuilder(stacksEqn);
			EquationHelper.setVariableNames(expB, aa.variableMap, "");
			
			Expression exp = EquationHelper.tryBuild(expB);
			if (exp != null)
			{
				EquationHelper.setVariableValues(exp, aa.variableMap, "");
				
				stacks = (int)Math.ceil(exp.evaluate());
			}
		}
		
		if (stacks > 0)
		{
			Field field = Field.load(fieldName);
			field.trySpawnInTile(tile, stacks);
		}
	}

	@Override
	public AbstractEffectType copy()
	{
		EffectTypeField e = new EffectTypeField();
		e.condition = condition;
		e.fieldName = fieldName;
		e.stacksEqn = stacksEqn;
		e.reliesOn = reliesOn;
		return e;
	}
}

