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

	public boolean male = true;

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

		Element template = xml.getChildByName( "EntityTemplate" );

		for ( Element classElement : xml.getChildrenByName( "Class" ) )
		{
			ClassDesc desc = new ClassDesc();
			desc.parse( classElement, template );

			classes.add( desc );
		}

		chosen = classes.get( 0 );
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

		if (male)
		{
			desc.male.sprite.render( batch, x, y, width, height );
		}
		else
		{
			desc.female.sprite.render( batch, x, y, width, height );
		}
	}

	public static class ClassDesc
	{
		public String name;
		public String description;
		public GameEntity female;
		public GameEntity male;

		public void parse( Element xml, Element entityTemplate )
		{
			name = xml.get( "Name" );
			description = xml.get( "Description" );

			Element maleElement = xml.getChildByName( "Male" );
			Element femaleElement = xml.getChildByName( "Female" );
			Element bothElement = xml.getChildByName( "Both" );

			Array<Element> maleData = new Array<Element>(  );
			maleData.add( entityTemplate );
			maleData.add( bothElement );
			maleData.add( maleElement );

			Array<Element> femaleData = new Array<Element>(  );
			femaleData.add( entityTemplate );
			femaleData.add( bothElement );
			femaleData.add( femaleElement );

			male = GameEntity.load( maleData );
			female = GameEntity.load( femaleData );
		}
	}
}
