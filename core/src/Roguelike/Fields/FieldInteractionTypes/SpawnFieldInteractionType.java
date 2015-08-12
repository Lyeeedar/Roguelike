package Roguelike.Fields.FieldInteractionTypes;

import java.util.HashMap;

import net.objecthunter.exp4j.Expression;
import net.objecthunter.exp4j.ExpressionBuilder;
import Roguelike.Fields.Field;

import com.badlogic.gdx.utils.XmlReader.Element;

import exp4j.Helpers.EquationHelper;

public class SpawnFieldInteractionType extends AbstractFieldInteractionType
{
	String fieldName;
	String stacksEqn;

	@Override
	public Field process(Field src, Field dst)
	{
		HashMap<String, Integer> variableMap = new HashMap<String, Integer>();

		variableMap.put("srcstacks", src.stacks);
		variableMap.put("dststacks", dst.stacks);
		
		ExpressionBuilder expB = EquationHelper.createEquationBuilder(stacksEqn);
		EquationHelper.setVariableNames(expB, variableMap, "");
				
		Expression exp = EquationHelper.tryBuild(expB);
		if (exp == null)
		{
			return null;
		}
		
		EquationHelper.setVariableValues(exp, variableMap, "");
		
		int stacks = (int)exp.evaluate();
		
		if (stacks == 0)
		{
			return null;
		}
		
		Field field = Field.load(fieldName);
		field.stacks = stacks;
		
		return field;
	}

	@Override
	public void parse(Element xml)
	{
		fieldName = xml.get("Field");
		stacksEqn = xml.get("Stacks", "1").toLowerCase();
	}

}
