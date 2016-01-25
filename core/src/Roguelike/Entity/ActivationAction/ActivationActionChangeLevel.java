package Roguelike.Entity.ActivationAction;

import Roguelike.Entity.EnvironmentEntity;
import Roguelike.Global;
import com.badlogic.gdx.utils.XmlReader;

/**
 * Created by Philip on 25-Jan-16.
 */
public class ActivationActionChangeLevel extends AbstractActivationAction
{
	public String level;

	public ActivationActionChangeLevel()
	{

	}

	public ActivationActionChangeLevel(String level)
	{
		this.level = level;
	}

	@Override
	public void evaluate( EnvironmentEntity entity, float delta )
	{
		Global.save();
		Global.LevelManager.nextLevel( level );
	}

	@Override
	public void parse( XmlReader.Element xml )
	{
		level = xml.getText();
	}
}
