// THIS IS MY VERSION

package sim.app.tutorial1and2;

import sim.engine.*;
import sim.field.grid.*;

public class CA implements Steppable	{
	private static final long serialVersionUID = 1;
	// the width and height will change later
	public IntGrid2D tempGrid = new IntGrid2D (0,0);
	
	public void step(SimState state)	{
		Tutorial1 tut = (Tutorial1)state;
		// first copy the grid into TempGrid
		tempGrid.setTo(tut.grid);
		
		// Now apply the Game of Life!
		
		// for each cell...
		int width = tempGrid.getWidth();
		int height = tempGrid.getHeight();
		for(int x=0; x<width; x++)
			for(int y=0; y<height; y++)	{
				int count = 0;
				// count the number of live neighbours around the cell,
				//and to simplify for the for-loop, just include the cell itself
				for(int dx = -1; dx < 2; dx++)
					for(int dy = -1; dy < 2; dy++)
						count += tempGrid.field[tempGrid.stx(x+dx)][tempGrid.sty(y+dy)];
				
				// since we're including the cell itself, the rule is slightly different:
				//if the count is 2 or less, or 5 or higher, the cell dies
				//else if the count is 3 exactly, a dead cell becomes libe again
				//else the cell stays as it is
				
				if (count <= 2 || count >= 5)	// death
					tut.grid.field[x][y] = 0;
				else if (count == 3)			// birth
					tut.grid.field[x][y] = 1;
				}
		}
	}
