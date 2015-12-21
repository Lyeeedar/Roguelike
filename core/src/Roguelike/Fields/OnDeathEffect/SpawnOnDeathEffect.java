package Roguelike.Fields.OnDeathEffect;

import java.util.HashMap;

import net.objecthunter.exp4j.Expression;
import net.objecthunter.exp4j.ExpressionBuilder;
import Roguelike.Fields.Field;
import Roguelike.Tiles.GameTile;

import com.badlogic.gdx.utils.XmlReader.Element;

import exp4j.Helpers.EquationHelper;

public class SpawnOnDeathEffect extends AbstractOnDeathEffect
{
	private String fieldName;
	private String stacksEqn;

	@Override
	public void process(Field field, GameTile tile)
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

		tile.addField(newField);
	}

	@Override
	public void parse(Element xml)
	{
		fieldName = xml.getText();
		stacksEqn = xml.getAttribute("Stacks", "1").toLowerCase();
	}

}