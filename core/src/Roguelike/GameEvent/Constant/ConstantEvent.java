package Roguelike.GameEvent.Constant;

import java.util.EnumMap;
import java.util.HashMap;

import net.objecthunter.exp4j.Expression;
import net.objecthunter.exp4j.ExpressionBuilder;
import Roguelike.Global.Statistic;
import Roguelike.Global.Tier1Element;
import Roguelike.Entity.Entity;
import Roguelike.Entity.GameEntity;

import com.badlogic.gdx.utils.XmlReader.Element;
import com.badlogic.gdx.utils.reflect.ClassReflection;
import com.badlogic.gdx.utils.reflect.ReflectionException;

import exp4j.Functions.RandomFunction;
import exp4j.Helpers.EquationHelper;
import exp4j.Operators.BooleanOperators;

public class ConstantEvent
{
	public EnumMap<Statistic, String> equations = new EnumMap<Statistic, String>(Statistic.class);
	private String[] reliesOn = new String[0];
	
	public void parse(Element xml)
	{
		reliesOn = xml.getAttribute("ReliesOn", "").split(",");
		
		for (int i = 0; i < xml.getChildCount(); i++)
		{
			Element sEl = xml.getChild(i);
			
			if (sEl.getName().toUpperCase().equals("ATK"))
			{
				for (Tier1Element el : Tier1Element.values())
				{
					String expanded = sEl.getText().toLowerCase();
					expanded = expanded.replaceAll("(?<!_)atk", el.Attack.toString().toLowerCase());
					
					equations.put(el.Attack, expanded);
				}
			}
			else if (sEl.getName().toUpperCase().equals("DEF"))
			{
				for (Tier1Element el : Tier1Element.values())
				{
					String expanded = sEl.getText().toLowerCase();
					expanded = expanded.replaceAll("(?<!_)def", el.Defense.toString().toLowerCase());
					
					equations.put(el.Defense, expanded);
				}
			}
			else if (sEl.getName().toUpperCase().equals("PIERCE"))
			{
				for (Tier1Element el : Tier1Element.values())
				{
					String expanded = sEl.getText().toLowerCase();
					expanded = expanded.replaceAll("(?<!_)pierce", el.Pierce.toString().toLowerCase());
					
					equations.put(el.Pierce, expanded);
				}
			}
			else if (sEl.getName().toUpperCase().equals("HARDINESS"))
			{
				for (Tier1Element el : Tier1Element.values())
				{
					String expanded = sEl.getText().toLowerCase();
					expanded = expanded.replaceAll("(?<!_)hardiness", el.Hardiness.toString().toLowerCase());
					
					equations.put(el.Hardiness, expanded);
				}
			}
			else
			{
				Statistic el = Statistic.valueOf(sEl.getName().toUpperCase());
				equations.put(el, sEl.getText().toLowerCase());
			}
		}
	}
	
	public int getStatistic(HashMap<String, Integer> variableMap, Statistic stat)
	{
		String eqn = equations.get(stat);
		
		if (eqn == null)
		{
			return 0;
		}
		
		for (String name : reliesOn)
		{
			if (!variableMap.containsKey(name.toLowerCase()))
			{
				variableMap.put(name.toLowerCase(), 0);
			}
		}
		
		ExpressionBuilder expB = EquationHelper.createEquationBuilder(eqn);
		EquationHelper.setVariableNames(expB, variableMap, "");
				
		Expression exp = EquationHelper.tryBuild(expB);
		if (exp == null)
		{
			return 0;
		}
		
		EquationHelper.setVariableValues(exp, variableMap, "");
		
		int val = (int)exp.evaluate();
		
		return val;
	}
	
	public void putStatistic(Statistic stat, String eqn)
	{
		equations.put(stat, eqn);
	}
	
	public static ConstantEvent load(Element xml)
	{		
		ConstantEvent ce = new ConstantEvent();
		
		ce.parse(xml);
		
		return ce;
	}	
}
