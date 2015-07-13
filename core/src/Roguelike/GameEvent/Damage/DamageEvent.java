package Roguelike.GameEvent.Damage;

import java.util.EnumMap;

import net.objecthunter.exp4j.Expression;
import net.objecthunter.exp4j.ExpressionBuilder;

import com.badlogic.gdx.utils.XmlReader.Element;

import exp4j.Helpers.EquationHelper;
import Roguelike.Global.Tier1Element;
import Roguelike.GameEvent.IGameObject;

public class DamageEvent extends AbstractOnDamageEvent
{
	private String condition;
	private EnumMap<Tier1Element, String> equations = new EnumMap<Tier1Element, String>(Tier1Element.class);
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
		
		EnumMap<Tier1Element, Integer> els = Tier1Element.getElementBlock();
		
		for (Tier1Element el : Tier1Element.values())
		{
			if (equations.containsKey(el))
			{
				String eqn = equations.get(el);
				
				ExpressionBuilder expB = EquationHelper.createEquationBuilder(eqn);
				obj.writeVariableNames(expB, reliesOn);
									
				Expression exp = EquationHelper.tryBuild(expB);
				if (exp == null)
				{
					continue;
				}
				
				obj.writeVariableValues(exp, reliesOn);
									
				int raw = (int)exp.evaluate();
				
				els.put(el, raw);
			}
		}
		
		obj.modifyDamage(els);
		
		return true;
	}
	
	@Override
	public void parse(Element xml)
	{
		reliesOn = xml.getAttribute("ReliesOn", "").split(",");
		condition = xml.getAttribute("Condition", null);
		
		for (int i = 0; i < xml.getChildCount(); i++)
		{
			Element sEl = xml.getChild(i);
			
			Tier1Element el = Tier1Element.valueOf(sEl.getName().toUpperCase());
			equations.put(el, sEl.getText());
		}
	}
}
