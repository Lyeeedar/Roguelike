package Roguelike.UI;

import java.io.IOException;

import Roguelike.Entity.GameEntity;
import Roguelike.Sprite.Sprite;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.XmlReader;
import com.badlogic.gdx.utils.XmlReader.Element;

public class ClassList extends TilePanel
{
	public ClassList( Skin skin, Stage stage, Sprite tileBackground, Sprite tileBorder )
	{
		super( skin, stage, tileBackground, tileBorder, 1, 1, 64, true );

		parse( "Classes/ClassList.xml" );
	}

	public Array<ClassDesc> classes = new Array<ClassDesc>();
	public ClassDesc chosen;

	public void reparse()
	{
		classes.clear();
		parse( "Classes/ClassList.xml" );
	}

	public void parse( String path )
	{
		XmlReader xmlReader = new XmlReader();
		Element xml = null;

		try
		{
			xml = xmlReader.parse( Gdx.files.internal( path ) );
		}
		catch ( IOException e )
		{
			e.printStackTrace();
		}

		for ( Element classElement : xml.getChildrenByName( "Class" ) )
		{
			ClassDesc desc = new ClassDesc();
			desc.parse( classElement );

			classes.add( desc );
		}

		chosen = classes.get( 0 );
	}

	public static class ClassDesc
	{
		public String name;
		public String description;
		public String lines;
		public GameEntity entity;

		public void parse( Element xml )
		{
			name = xml.get( "Name" );
			description = xml.get( "Description" );
			lines = xml.get( "Lines" );
			entity = GameEntity.load( xml.get( "EntityPath" ) );
		}
	}

	@Override
	public void populateTileData()
	{
		this.tileData.clear();
		tileData.addAll( classes );

		dataHeight = tileData.size;
	}

	@Override
	public Sprite getSpriteForData( Object data )
	{
		// ClassDesc desc = (ClassDesc) data;
		return null;// desc.entity.sprite;
	}

	@Override
	public void handleDataClicked( Object data, InputEvent event, float x, float y )
	{
		chosen = (ClassDesc) data;
	}

	@Override
	public Table getToolTipForData( Object data )
	{
		ClassDesc desc = (ClassDesc) data;

		Table table = new Table();

		table.add( new Label( desc.name, skin, "title" ) ).expandX().left();
		table.row();

		Label descLabel = new Label( desc.description, skin );
		descLabel.setWrap( true );
		table.add( descLabel ).expand().left().width( com.badlogic.gdx.scenes.scene2d.ui.Value.percentWidth( 1, table ) );
		table.row();

		return table;
	}

	@Override
	public Color getColourForData( Object data )
	{
		return data == chosen ? Color.GREEN : Color.WHITE;
	}

	@Override
	public void onDrawItemBackground( Object data, Batch batch, int x, int y, int width, int height )
	{
	}

	@Override
	public void onDrawItem( Object data, Batch batch, int x, int y, int width, int height )
	{
	}

	@Override
	public void onDrawItemForeground( Object data, Batch batch, int x, int y, int width, int height )
	{
		batch.setColor( Color.WHITE );

		ClassDesc desc = (ClassDesc) data;
		desc.entity.sprite.render( batch, x, y, width, height );
	}
}
