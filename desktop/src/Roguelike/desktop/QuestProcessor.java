package Roguelike.desktop;

import Roguelike.Quests.Input.AbstractQuestInput;
import Roguelike.Quests.Output.QuestOutput;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.ObjectMap;
import com.badlogic.gdx.utils.XmlReader;

import java.io.File;
import java.io.IOException;

/**
 * Created by Philip on 24-Jan-16.
 */
public class QuestProcessor
{
	public Array<String> questPaths = new Array<String>(  );
	public ObjectMap<String, OutputData> output = new ObjectMap<String, OutputData>(  );
	public Array<InputData> input = new Array<InputData>(  );

	public QuestProcessor()
	{
		findFilesRecursive( new File("Quests") );

		for (InputData data : input)
		{
			OutputData od = output.get( data.key );

			if (od == null)
			{
				System.err.println("Input key '"+data.key+"' is never set!  File: "+data.path);
			}
			else if (data.value != null)
			{
				if (!od.values.contains( data.value, false ))
				{
					System.err.println("Input key '"+data.key+"' never has value '"+data.value+"'!  File: "+data.path);
				}
			}
		}

		for (OutputData data : output.values())
		{
			for (String value : data.values)
			{
				boolean found = false;

				for (InputData id : input)
				{
					if (id.key.equals( data.key ) && value.equals( id.value ))
					{
						found = true;
						break;
					}
				}

				if (!found)
				{
					System.err.println( "Output key '"+data.key+"' with value '"+value+"' is never used." );
				}
			}
		}

		String questListContents = "<Quests>\n";
		for (String path : questPaths)
		{
			questListContents += "\t<Quest>"+path+"</Quest>\n";
		}
		questListContents += "</Quests>";

		FileHandle questList = new FileHandle( new File("Quests/QuestList.xml") );
		questList.writeString( questListContents, false );


		String flagListContents = "<Flags>\n";
		for (OutputData data : output.values())
		{
			flagListContents += "\t<"+data.key+">\n";

			for (String val : data.values)
			{
				flagListContents += "\t\t<"+val+"/>\n";
			}

			flagListContents += "\t</"+data.key+">\n";
		}
		flagListContents += "</Flags>";

		FileHandle flagList = new FileHandle( new File("Quests/FlagList.xml") );
		flagList.writeString( flagListContents, false );
	}

	private void findFilesRecursive( File dir )
	{
		File[] contents = dir.listFiles();

		if ( contents == null )
		{
			return;
		}

		for ( File file : contents )
		{
			if ( file.isDirectory() )
			{
				findFilesRecursive( file );
			}
			else if ( file.getPath().endsWith( ".xml" ) )
			{
				parseXml( file.getPath() );
			}
		}
	}

	public void parseXml( String path )
	{
		XmlReader reader = new XmlReader();
		XmlReader.Element xml = null;

		try
		{
			xml = reader.parse( Gdx.files.internal( path ) );
		}
		catch ( IOException e )
		{
			e.printStackTrace();
		}

		if (!xml.getName().equals( "Quest" ))
		{
			return;
		}

		path = path.replace( "\\", "/" );
		path = path.replace( "Quests/", "" );
		path = path.replace( ".xml", "" );

		questPaths.add( path );

		XmlReader.Element inputsElement = xml.getChildByName( "Inputs" );
		if (inputsElement != null)
		{
			for (int i = 0; i < inputsElement.getChildCount(); i++)
			{
				AbstractQuestInput qi = AbstractQuestInput.load( inputsElement.getChild( i ) );

				InputData data = new InputData();
				data.key = qi.key;
				data.value = qi.value;
				data.path = path;

				input.add( data );
			}
		}

		XmlReader.Element outputsElement = xml.getChildByName( "Outputs" );
		if (outputsElement != null)
		{
			for ( int i = 0; i < outputsElement.getChildCount(); i++ )
			{
				XmlReader.Element outputElement = outputsElement.getChild( i );

				QuestOutput qo = new QuestOutput();
				qo.parse( outputElement );

				OutputData data = output.get( qo.key );
				if (data == null)
				{
					data = new OutputData();
					data.key = qo.key;
					output.put( qo.key, data );
				}

				if (!data.values.contains( qo.data, false ))
				{
					data.values.add( qo.data );
				}
			}
		}
	}

	private static class InputData
	{
		public String key;
		public String value;

		public String path;
	}

	private static class OutputData
	{
		public String key;
		public Array<String> values = new Array<String>(  );
	}
}
