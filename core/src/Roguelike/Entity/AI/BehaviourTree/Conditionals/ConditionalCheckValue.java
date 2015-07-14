package Roguelike.Entity.AI.BehaviourTree.Conditionals;

import com.badlogic.gdx.utils.XmlReader.Element;

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
		Object storedValue = getData(key, null);
		
		if (storedValue == null) { State = fail; }
		else if (comparator == Comparator.EXISTS) { State = succeed; }
		else 
		{			
			boolean check = false;
			int comparison = 0;
			
			if (storedValue instanceof Integer)
			{
				Integer sVal = (Integer)storedValue;
				Integer cVal = Integer.parseInt(value);
				
				comparison = cVal.compareTo(sVal);
			}
			else if (storedValue instanceof Float)
			{
				Float sVal = (Float)storedValue;
				Float cVal = Float.parseFloat(value);
				
				comparison = cVal.compareTo(sVal);
			}
			else if (storedValue instanceof Boolean)
			{
				Boolean sVal = (Boolean)storedValue;
				Boolean cVal = Boolean.parseBoolean(value);
				
				comparison = cVal.compareTo(sVal);
			}
			else if (storedValue instanceof String)
			{
				String sVal = (String)storedValue;
				String cVal = value;
				
				comparison = cVal.compareTo(sVal);
			}
			
			if (comparator == Comparator.EQ) { check = comparison == 0; }
			else if (comparator == Comparator.NE) { check = comparison != 0; }
			else if (comparator == Comparator.LT) { check = comparison < 0; }
			else if (comparator == Comparator.LE) { check = comparison <= 0; }
			else if (comparator == Comparator.GT) { check = comparison > 0; }
			else if (comparator == Comparator.GE) { check = comparison >= 0; }
						
			State = check ? succeed : fail ; 
		}
		
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
		this.key = xml.getAttribute("Key");
		this.value = xml.getAttribute("Value", "");
		this.succeed = BehaviourTreeState.valueOf(xml.getAttribute("Success", "SUCCEEDED").toUpperCase());
		this.fail = BehaviourTreeState.valueOf(xml.getAttribute("Failure", "FAILED").toUpperCase());
		
		String comparator = xml.getAttribute("Comparator", "exists");
		if (comparator.equals("==") || comparator.equalsIgnoreCase("eq"))
		{
			this.comparator = Comparator.EQ;
		}
		else if (comparator.equals("!=") || comparator.equalsIgnoreCase("ne"))
		{
			this.comparator = Comparator.NE;
		}
		else if (comparator.equals("<") || comparator.equalsIgnoreCase("lt"))
		{
			this.comparator = Comparator.LT;
		}
		else if (comparator.equals("<=") || comparator.equalsIgnoreCase("le"))
		{
			this.comparator = Comparator.LE;
		}
		else if (comparator.equals(">") || comparator.equalsIgnoreCase("gt"))
		{
			this.comparator = Comparator.GT;
		}
		else if (comparator.equals(">=") || comparator.equalsIgnoreCase("ge"))
		{
			this.comparator = Comparator.GE;
		}
		else if (comparator.equalsIgnoreCase("exists"))
		{
			this.comparator = Comparator.EXISTS;
		}
		else
		{
			throw new RuntimeException("Unsupported comparator type: " + comparator + "!");
		}
	}
	
	//endregion Public Methods
	//####################################################################//
	//region Data

	//----------------------------------------------------------------------
	private enum Comparator
	{
		EQ,
		NE,
		LT,
		LE,
		GT,
		GE,
		EXISTS
	}

	//----------------------------------------------------------------------
	protected Comparator comparator;

	//----------------------------------------------------------------------
	public String key;
	
	//----------------------------------------------------------------------
	public String value;

	//----------------------------------------------------------------------
	public BehaviourTreeState succeed;
	
	//----------------------------------------------------------------------
	public BehaviourTreeState fail;
		
	//endregion Data
	//####################################################################//
}
