package Roguelike.Items;

import java.io.BufferedReader;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.utils.Array;

public class WordList
{
	Array<String> words;
	
	public String select()
	{
		return words.get(MathUtils.random(words.size-1));
	}
	
	public static WordList loadWordList(String name)
	{
		FileHandle file = Gdx.files.internal("WordLists/"+name+".wordlist");

		BufferedReader reader = new BufferedReader(file.reader());
		Array<String> lines = new Array<String>();

		try 
		{
			String line = reader.readLine();
			while( line != null ) 
			{
				lines.add(line);
				line = reader.readLine();
			}
		}
		catch (Exception e) {}
		
		WordList wl = new WordList();
		wl.words = lines;
		
		return wl;
	}
}
