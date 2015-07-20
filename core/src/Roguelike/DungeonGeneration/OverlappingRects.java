package Roguelike.DungeonGeneration;

import java.util.Random;

import Roguelike.DungeonGeneration.DungeonFileParser.Symbol;

public class OverlappingRects
{
	private static final float V_WIDTH_MIN = 0.3f;
	private static final float V_WIDTH_MAX = 0.9f;
	private static final float V_WIDTH_DIFF = V_WIDTH_MAX - V_WIDTH_MIN;
	private static final float V_HEIGHT_MIN = 0.7f;
	private static final float V_HEIGHT_MAX = 1.0f;
	private static final float V_HEIGHT_DIFF = V_HEIGHT_MAX - V_HEIGHT_MIN;
	
	private static final float H_WIDTH_MIN = 0.9f;
	private static final float H_WIDTH_MAX = 1.0f;
	private static final float H_WIDTH_DIFF = H_WIDTH_MAX - H_WIDTH_MIN;
	private static final float H_HEIGHT_MIN = 0.3f;
	private static final float H_HEIGHT_MAX = 0.9f;
	private static final float H_HEIGHT_DIFF = H_HEIGHT_MAX - H_HEIGHT_MIN;
	
	public static void process(Symbol[][] grid, Symbol floor, Symbol wall, Random ran)
	{
		int width = grid.length-2;
		int height = grid[0].length-2;
		
		// calculate vertical rect
		float vwidthP = ran.nextFloat() *  V_WIDTH_DIFF + V_WIDTH_MIN;
		float vheightP = ran.nextFloat() * V_HEIGHT_DIFF + V_HEIGHT_MIN;
		
		int vwidth = (int)(width*vwidthP);
		int vwidthdiff = width - vwidth;
		int vxoffset = vwidthdiff > 1 ? ran.nextInt(vwidthdiff / 2) : 0;
		
		int vheight = (int)(height*vheightP);
		int vheightdiff = height - vheight;
		int vyoffset = vheightdiff > 1 ? ran.nextInt(vheightdiff / 2) : 0;
				
		// calculate horizontal rect
		float hwidthP = ran.nextFloat() *  H_WIDTH_DIFF + H_WIDTH_MIN;
		float hheightP = ran.nextFloat() * H_HEIGHT_DIFF + H_HEIGHT_MIN;
		
		int hwidth = (int)(width*hwidthP);
		int hwidthdiff = width - hwidth;
		int hxoffset = hwidthdiff > 1 ? ran.nextInt(hwidthdiff / 2) : 0;
		
		int hheight = (int)(height*hheightP);
		int hheightdiff = height - hheight;
		int hyoffset = hheightdiff > 1 ? ran.nextInt(hheightdiff / 2) : 0;
				
		// intialise to solid
		for (int x = 0; x < width+2; x++)
		{
			for (int y = 0; y < height+2; y++)
			{
				grid[x][y] = wall;
			}
		}
		
		// place vertical rect
		for (int x = vxoffset; x < vxoffset+vwidth; x++)
		{
			for (int y = vyoffset; y < vyoffset+vheight; y++)
			{
				grid[x+1][y+1] = floor;
			}
		}
		
		// place horizontal rect
		for (int x = hxoffset; x < hxoffset+hwidth; x++)
		{
			for (int y = hyoffset; y < hyoffset+hheight; y++)
			{
				grid[x+1][y+1] = floor;
			}
		}
	}
}
