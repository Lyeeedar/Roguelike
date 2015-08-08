package Roguelike.Lights;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.utils.Pools;
import com.badlogic.gdx.utils.XmlReader.Element;

public class Light
{
	public boolean copied;
	
	public Color colour;
	public float baseIntensity;
	public int flicker;
	public float flickerPeriod;
	
	public float actualIntensity;
	public int lx;
	public int ly;
	
	private float timeAccumulator;
	
	public void update(float delta)
	{
		if (flicker > 0)
		{
			timeAccumulator += delta;
			while(timeAccumulator > flickerPeriod)
			{
				timeAccumulator -= flickerPeriod;
			}
			
			float factor = 1.0f / (float)flicker;			
			float fraction = baseIntensity / 20;
			
			actualIntensity = fraction * 19;
			
			for (int i = 0; i < flicker; i++)
			{
				actualIntensity += fraction * (float)( Math.sin( timeAccumulator * (i+1) / (flickerPeriod/(2*Math.PI)) ) * factor );
			}
		}
		else
		{
			actualIntensity = baseIntensity;
		}
	}
	
	public Light copy()
	{
		Light l = Pools.obtain(Light.class);
		l.colour = new Color(colour);
		l.baseIntensity = baseIntensity;
		l.copied = true;
		
		return l;
	}
	
	public static Light load(Element xml)
	{
		Light l = new Light();
		
		Element colourElement = xml.getChildByName("Colour");
		l.colour = new Color(
				colourElement.getFloat("Red", 0),
				colourElement.getFloat("Green", 0),
				colourElement.getFloat("Blue", 0),
				colourElement.getFloat("Alpha", 1)
				);
		l.baseIntensity = xml.getFloat("Intensity");
		l.flicker = xml.getInt("Flicker", 0);
		l.flickerPeriod = xml.getFloat("FlickerPeriod", 1);
		l.timeAccumulator = MathUtils.random() * l.flickerPeriod;
		
		return l;
	}
}