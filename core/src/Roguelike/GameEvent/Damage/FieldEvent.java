package Roguelike.GameEvent.Damage;

import net.objecthunter.exp4j.Expression;
import net.objecthunter.exp4j.ExpressionBuilder;
import Roguelike.Fields.Field;
import Roguelike.GameEvent.IGameObject;

import com.badlogic.gdx.utils.XmlReader.Element;

import exp4j.Helpers.EquationHelper;

public class FieldEvent extends AbstractOnDamageEvent
{
	public String condition;
	public String attackerField;
	public String defenderField;
	public String stacksEqn;
	
	private String[] reliesOn;
	
	@Override
	public boolean handle(DamageObject obj, IGameObject parent)
	{		
		if (condition != null)
		{
			ExpressionBuilder expB = EquationHelper.createEquationBuilder(condition);
			obj.writeVariableNames(expB, reliesOn);

			Expression exp = EquationHelper.tryBuild(expB);
			if (exp == null)
			{
				return false;
			}
			
			obj.writeVariableValues(exp, reliesOn);
						
			double conditionVal = exp.evaluate();
			
			if (conditionVal == 0)
			{
				return false;
			}
		}
		
		int stacks = 1;
		
		if (stacksEqn != null)
		{
			ExpressionBuilder expB = EquationHelper.createEquationBuilder(stacksEqn);
			obj.writeVariableNames(expB, reliesOn);
			
			Expression exp = EquationHelper.tryBuild(expB);
			if (exp != null)
			{
				obj.writeVariableValues(exp, reliesOn);
				
				stacks = (int)Math.ceil(exp.evaluate());
			}
		}
		
		if (attackerField != null)
		{
			Field field = Field.load(attackerField);
			field.trySpawnInTile(obj.attacker.tile, stacks);
		}
		
		if (defenderField != null)
		{
			Field field = Field.load(defenderField);
			field.trySpawnInTile(obj.defender.tile, stacks);
		}
		
		return true;
	}

	@Override
	public void parse(Element xml)
	{
		reliesOn = xml.getAttribute("ReliesOn", "").split(",");
		condition = xml.getAttribute("Condition", null); if (condition != null) { condition = condition.toLowerCase(); }
		attackerField = xml.get("Attacker", null);
		defenderField = xml.get("Defender", null);
		stacksEqn = xml.get("Stacks", null); if (stacksEqn != null) { stacksEqn = stacksEqn.toLowerCase(); }
	}

}
