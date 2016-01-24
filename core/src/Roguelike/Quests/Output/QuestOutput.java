package Roguelike.Quests.Output;

import Roguelike.Global;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.XmlReader;

/**
 * Created by Philip on 23-Jan-16.
 */
public class QuestOutput
{
	public boolean runFlag;
	public String key;
	public String data;

	public boolean defer;

	public Array<AbstractQuestOutputCondition> conditions = new Array<AbstractQuestOutputCondition>(  );

	public void evaluate()
	{
		for (AbstractQuestOutputCondition condition : conditions)
		{
			if (!condition.evaluate())
			{
				return;
			}
		}

		if (defer)
		{
			Global.QuestManager.deferredFlags.put( key, data );
		}
		else if (runFlag)
		{
			Global.RunFlags.put( key, data );
		}
		else
		{
			Global.WorldFlags.put( key, data );
		}
	}

	public void parse( XmlReader.Element xml )
	{
		key = xml.getName();
		data = xml.get( "Data", "true" );
		runFlag = xml.getBooleanAttribute( "RunFlag", false );
		defer = xml.getBooleanAttribute( "Defer", true );

		XmlReader.Element conditionsElement = xml.getChildByName( "Conditions" );
		if (conditionsElement != null)
		{
			for (int i = 0; i < conditionsElement.getChildCount(); i++ )
			{
				XmlReader.Element conditionElement = conditionsElement.getChild( i );
				AbstractQuestOutputCondition condition = AbstractQuestOutputCondition.load( conditionElement );
				conditions.add( condition );
			}
		}
	}
}
