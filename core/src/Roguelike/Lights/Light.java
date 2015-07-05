package Roguelike.Lights;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.utils.XmlReader.Element;

public class Light
{
	public Color Colour;
	public float Intensity;
	
	public int lx;
	public int ly;
	
	public Light copy()
	{
		Light l = new Light();
		l.Colour = new Color(Colour);
		l.Intensity = Intensity;
		
		return l;
	}
	
	public static Light load(Element xml)
	{
		Light l = new Light();
		
		Element colourElement = xml.getChildByName("Colour");
		l.Colour = new Color(
				colourElement.getFloat("Red", 1),
				colourElement.getFloat("Green", 1),
				colourElement.getFloat("Blue", 1),
				colourElement.getFloat("Alpha", 1)
				);
		l.Intensity = xml.getFloat("Intensity");
		
		return l;
	}
}