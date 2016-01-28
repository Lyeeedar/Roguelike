package Roguelike.Entity.ActivationAction;

import Roguelike.Entity.EnvironmentEntity;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.XmlReader;

/**
 * Created by Philip on 25-Jan-16.
 */
public class ActivationActionGroup
{
	// ----------------------------------------------------------------------
	public String name;
	public boolean enabled;
	public Array<AbstractActivationCondition> conditions = new Array<AbstractActivationCondition>(  );
	public Array<AbstractActivationAction> actions = new Array<AbstractActivationAction>(  );

	// ----------------------------------------------------------------------
	public ActivationActionGroup()
	{

	}

	// ----------------------------------------------------------------------
	public ActivationActionGroup(String name)
	{
		this.name = name;
		enabled = true;
	}

	// ----------------------------------------------------------------------
	public ActivationActionGroup(String name, boolean enabled)
	{
		this.name = name;
		this.enabled = enabled;
	}

	// ----------------------------------------------------------------------
	public boolean checkCondition( EnvironmentEntity entity, float delta )
	{
		for (AbstractActivationCondition condition : conditions)
		{
			if (!condition.evaluate( entity, delta ))
			{
				return false;
			}
		}

		return true;
	}

	// ----------------------------------------------------------------------
	public void activate( EnvironmentEntity entity, float delta )
	{
		if (!checkCondition( entity, delta )) { return; }

		for (int i = 0; i < actions.size; i++)
		{
			actions.get( i ).evaluate( entity, delta );
		}
	}

	// ----------------------------------------------------------------------
	public void parse( XmlReader.Element xml )
	{
		name = xml.getAttribute( "Name", "UNNAMED" );
		enabled = xml.getBooleanAttribute( "Enabled", true );

		XmlReader.Element conditionsElement = xml.getChildByName( "Conditions" );
		XmlReader.Element actionsElement = xml.getChildByName( "Actions" );

		if (conditionsElement != null || actionsElement != null)
		{
			if (conditionsElement != null)
			{
				for (int i = 0; i < conditionsElement.getChildCount(); i++)
				{
					XmlReader.Element el = conditionsElement.getChild( i );

					AbstractActivationCondition condition = AbstractActivationCondition.load( el );
					conditions.add( condition );
				}
			}

			for (int i = 0; i < actionsElement.getChildCount(); i++)
			{
				XmlReader.Element el = actionsElement.getChild( i );

				AbstractActivationAction action = AbstractActivationAction.load( el );
				actions.add( action );
			}
		}
		else
		{
			for (int i = 0; i < xml.getChildCount(); i++)
			{
				XmlReader.Element el = xml.getChild( i );

				AbstractActivationAction action = AbstractActivationAction.load( el );
				actions.add( action );
			}
		}
	}
}
