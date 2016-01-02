package Roguelike.Entity.AI.BehaviourTree.Conditionals;

import java.util.HashMap;

import net.objecthunter.exp4j.Expression;
import net.objecthunter.exp4j.ExpressionBuilder;

import com.badlogic.gdx.utils.XmlReader.Element;

import exp4j.Helpers.EquationHelper;
import Roguelike.Entity.GameEntity;
import Roguelike.Entity.AI.BehaviourTree.BehaviourTree.BehaviourTreeState;

public class ConditionalCheckValue extends AbstractConditional
{
	//####################################################################//
	//region Public Methods

	//----------------------------------------------------------------------
	@Override
	public BehaviourTreeState evaluate(GameEntity entity)
	{
		int keyVal = 0;
		if (key != null)
		{
			Object storedValue = getData(key, null);

			if (condition == null)
			{
				State = storedValue != null ? succeed : fail;
				return State;
			}
			else
			{
				if (storedValue instanceof Boolean)
				{
					keyVal = (Boolean)storedValue ? 1 : 0;
				}
				else if (storedValue instanceof Integer)
				{
					keyVal = (Integer)storedValue;
				}
				else if (storedValue instanceof Float)
				{
					keyVal = Math.round((Float)storedValue);
				}
				else
				{
					keyVal = storedValue != null ? 1 : 0;
				}
			}
		}

		HashMap<String, Integer> variableMap = entity.getVariableMap();
		if (key != null)
		{
			variableMap.put( key.toLowerCase(), keyVal );
		}

		int conditionVal = EquationHelper.evaluate( condition, variableMap );

		State = conditionVal == 1 ? succeed : fail;
		return State;
	}

	//----------------------------------------------------------------------
	@Override
	public void cancel()
	{

	}

	//----------------------------------------------------------------------
	@Override
	public void parse(Element xml)
	{
		this.key = xml.getAttribute("Key", null);
		this.succeed = BehaviourTreeState.valueOf(xml.getAttribute("Success", "SUCCEEDED").toUpperCase());
		this.fail = BehaviourTreeState.valueOf(xml.getAttribute("Failure", "FAILED").toUpperCase());

		this.condition = xml.getAttribute("Condition", null);
		if (this.condition != null) { this.condition = this.condition.toLowerCase(); }
	}

	//endregion Public Methods
	//####################################################################//
	//region Data

	//----------------------------------------------------------------------
	public String key;

	//----------------------------------------------------------------------
	public String condition;

	//----------------------------------------------------------------------
	public BehaviourTreeState succeed;

	//----------------------------------------------------------------------
	public BehaviourTreeState fail;

	//endregion Data
	//####################################################################//
}
