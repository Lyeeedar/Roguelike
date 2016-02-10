package Roguelike.Entity.ActivationAction;

import Roguelike.Entity.EnvironmentEntity;
import Roguelike.Global;
import Roguelike.Items.Item;
import Roguelike.Items.TreasureGenerator;
import Roguelike.Screens.GameScreen;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.XmlReader;

/**
 * Created by Philip on 10-Feb-16.
 */
public class ActivationActionAddItem extends AbstractActivationAction
{
	String item;
	int quality;

	@Override
	public void evaluate( EnvironmentEntity entity, float delta )
	{
		int quality = this.quality > 0 ? this.quality : Global.getQuality();
		Array<Item> items = TreasureGenerator.generateLoot( quality, item, MathUtils.random );
		GameScreen.Instance.pickupQueue.addAll( items );
	}

	@Override
	public void parse( XmlReader.Element xml )
	{
		item = xml.getText();
		quality = xml.getIntAttribute( "Quality", -1 );
	}
}
