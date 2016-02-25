package Roguelike.UI;

import Roguelike.Tiles.Point;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.scenes.scene2d.EventListener;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.scenes.scene2d.ui.Button;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Pools;

/**
 * Created by Philip on 25-Feb-16.
 */
public class ButtonKeyboardHelper
{
	public Array<Array<Button>> grid = new Array<Array<Button>>(  );
	public Button cancel;

	public Point current = new Point(  );

	private boolean first = true;

	public ButtonKeyboardHelper()
	{

	}

	public ButtonKeyboardHelper( Button cancel )
	{
		this.cancel = cancel;
		add( cancel );
	}

	public void add(Button button )
	{
		add(button, 0);
	}

	public void add( Button button, int x )
	{
		if ( x >= grid.size )
		{
			int needed = x - grid.size + 1;
			for (int i = 0; i < needed; i++)
			{
				grid.add( new Array<Button>(  ) );
			}
		}

		Array<Button> column = grid.get( x );
		column.add( button );

		if (first)
		{
			current.set( x, column.size-1 );
			button.overrideIsOver = true;
			first = false;
		}
	}

	// ----------------------------------------------------------------------
	public void clear()
	{
		grid.get( current.x ).get( current.y ).overrideIsOver = false;
	}

	// ----------------------------------------------------------------------
	public void trySetCurrent(Point p)
	{
		grid.get( current.x ).get( current.y ).overrideIsOver = false;

		current.set( p );

		if (current.x >= grid.size)
		{
			current.x = grid.size-1;
		}

		if (current.y >= grid.get( current.x ).size)
		{
			current.y = grid.get( current.x ).size-1;
		}

		grid.get( current.x ).get( current.y ).overrideIsOver = true;
	}

	// ----------------------------------------------------------------------
	public boolean keyDown( int keycode )
	{
		if ( keycode == Input.Keys.ESCAPE )
		{
			if (cancel != null)
			{
				pressButton( cancel );
			}
		}
		else if ( keycode == Input.Keys.ENTER )
		{
			Button button = grid.get( current.x ).get( current.y );
			if (button != null)
			{
				pressButton( button );
			}
		}
		else if ( keycode == Input.Keys.LEFT )
		{
			if ( current.x > 0 )
			{
				grid.get( current.x ).get( current.y ).overrideIsOver = false;

				int sx = current.x;
				current.x--;
				while ( current.x >= 0 && grid.get( current.x ).size == 0 )
				{
					current.x--;
				}

				if (current.x == -1)
				{
					current.x = sx;
				}
				else
				{
					if ( current.y >= grid.get( current.x ).size )
					{
						current.y = grid.get( current.x ).size - 1;
					}
				}

				grid.get( current.x ).get( current.y ).overrideIsOver = true;
			}
		}
		else if ( keycode == Input.Keys.RIGHT )
		{
			if ( current.x < grid.size - 1 )
			{
				grid.get( current.x ).get( current.y ).overrideIsOver = false;

				int sx = current.x;
				current.x++;
				while ( current.x < grid.size && grid.get( current.x ).size == 0 )
				{
					current.x++;
				}

				if (current.x == grid.size)
				{
					current.x = sx;
				}
				else
				{
					if ( current.y >= grid.get( current.x ).size )
					{
						current.y = grid.get( current.x ).size - 1;
					}
				}

				grid.get( current.x ).get( current.y ).overrideIsOver = true;
			}
		}
		else if ( keycode == Input.Keys.UP )
		{
			if ( current.y > 0 )
			{
				grid.get( current.x ).get( current.y ).overrideIsOver = false;

				current.y--;

				grid.get( current.x ).get( current.y ).overrideIsOver = true;
			}
		}
		else if ( keycode == Input.Keys.DOWN )
		{
			if ( current.y < grid.get( current.x ).size - 1 )
			{
				grid.get( current.x ).get( current.y ).overrideIsOver = false;

				current.y++;

				grid.get( current.x ).get( current.y ).overrideIsOver = true;
			}
		}

		return true;
	}

	// ----------------------------------------------------------------------
	private boolean pressButton(Button button )
	{
		for ( EventListener listener : button.getListeners() )
		{
			if (listener instanceof ClickListener)
			{
				((ClickListener)listener).clicked( null, 0, 0 );
			}
		}
		return true;
	}
}
