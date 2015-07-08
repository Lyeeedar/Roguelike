package Roguelike.GameEvent.OnTurn;

import net.objecthunter.exp4j.Expression;
import net.objecthunter.exp4j.ExpressionBuilder;
import Roguelike.Entity.Entity;

import com.badlogic.gdx.utils.XmlReader.Element;

public class DamageOverTimeEvent extends AbstractOnTurnEvent
{
	String dps;
	float remainder;
	
	@Override
	public boolean handle(Entity entity, float time)
	{
		ExpressionBuilder expB = new ExpressionBuilder(dps);
		Expression exp = entity.fillExpressionWithValues(expB, "");
		
		float rawDamage = (float)exp.evaluate() * time + remainder;
		
		int rounded = (int)Math.floor(rawDamage);
		
		remainder = rawDamage - rounded;
		
		entity.applyDamage(rounded);
		
		return true;
	}

	@Override
	public void parse(Element xml)
	{
		dps = xml.get("Damage");
	}

}
