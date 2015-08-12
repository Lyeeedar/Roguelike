package Roguelike.Fields.OnDeathEffect;

import java.util.HashMap;

import net.objecthunter.exp4j.Expression;
import net.objecthunter.exp4j.ExpressionBuilder;
import Roguelike.Fields.Field;

import com.badlogic.gdx.utils.XmlReader.Element;

import exp4j.Helpers.EquationHelper;

public class SpawnOnDeathEffect extends AbstractOnDeathEffect
{
	String fieldName;
	String stacksEqn;

	@Override
	public void process(Field field)
	{
		HashMap<String, Integer> variableMap = new HashMap<String, Integer>();

		variableMap.put("stacks", field.stacks);
		
		ExpressionBuilder expB = EquationHelper.createEquationBuilder(stacksEqn);
		EquationHelper.setVariableNames(expB, variableMap, "");
				
		Expression exp = EquationHelper.tryBuild(expB);
		if (exp == null)
		{
			return;
		}
		
		EquationHelper.setVariableValues(exp, variableMap, "");
		
		int stacks = (int)exp.evaluate();
		
		if (stacks == 0)
		{
			return;
		}
		
		Field newField = Field.load(fieldName);
		newField.stacks = stacks;
		
		field.tile.addField(newField);
	}

	@Override
	public void parse(Element xml)
	{
		fieldName = xml.get("Field");
		stacksEqn = xml.get("Stacks", "1").toLowerCase();
	}

}