// THIS IS MY VERSION

package sim.app.tutorial1and2;

import sim.engine.*;
import sim.field.grid.*;
import ec.util.*;

public class Tutorial1 extends SimState	{
	private static final long serialVersionUID = 1;
	
	public Tutorial1(long seed)	{
		super(seed);	}

	public IntGrid2D grid;

	// our own parameters for setting the grid size later on
	public int gridWidth = 100;
	public int gridHeight = 100;

	// A b-heptomino looks like this:
	//	X
	// XXX
	// X XX
	public static final int[][] b_heptomino = new int[][]	
		{{0, 1, 1},
		{1, 1, 0},
		{0, 1, 1},
		{0, 0, 1}};
		
	void seedGrid()	{
		// we stick a b_heptomino in the centre of the grid
		for(int x=0; x<b_heptomino.length; x++)
			for(int y=0; y<b_heptomino[x].length; y++)
				grid.field[x + grid.field.length/2 - b_heptomino.length/2]
						[y + grid.field[x].length/2 - b_heptomino[x].length/2]	=
						b_heptomino[x][y];
		}
	
	public void start()	{
		super.start();	// very important! This resets and cleans out the Schedule
		grid = new IntGrid2D(gridWidth, gridHeight);
		seedGrid();
		schedule.scheduleRepeating(new CA());	}

	// doLoop(...) needs two things: the class that it should instantiate a
	//simulation from, and the command line arguments. That's it.
    public static void main(String[] args)	{
        doLoop(Tutorial1.class, args);
        System.exit(0);	}    
    }		
/*	
	// THIS VERSION IS FOR TUTORIAL 2
	//	To checkpoint out from Tutorial1, we need to add some features to the main() method.		
    public static void main(String[] args)
        {
        Tutorial1 tutorial1 = null;
        
        // should we load from checkpoint?
        for(int x=0;x<args.length-1;x++)  // "-fromcheckpoint" can't be the last string
            if (args[x].equals("-fromcheckpoint"))
                {
                SimState state = SimState.readFromCheckpoint(new java.io.File(args[x+1]));
                if (state == null)   // there was an error -- quit (error will be displayed)
                    System.exit(1);
                else if (!(state instanceof Tutorial1))  // uh oh, wrong simulation stored in the file!
                    {
                    System.out.println("Checkpoint contains some other simulation: " + state);
                    System.exit(1);
                    }
                else // we're ready to lock and load!
                    tutorial1 = (Tutorial1)state;
                }
		// This little chunk of code lets us start a Tutorial1 simulation on the command line, loading from
		//an existing checkpoint file using the -fromcheckpoint parameter.
		//...or should we start fresh?
        if (tutorial1==null)  // no checkpoint file requested
            {
            tutorial1 = new Tutorial1(System.currentTimeMillis());
            tutorial1.start();
            }		
		// If no -fromcheckpoint argument was provided, we just start a brand-spanking-new Tutorial1.
		//We run the main loop as before, but every 500 steps we write out a checkpoint file of the model.
		long steps;
        do
            {
            if (!tutorial1.schedule.step(tutorial1))
                break;
            steps = tutorial1.schedule.getSteps();
            if (steps % 500 == 0)
                {
                System.out.println("Steps: " + steps + " Time: " + tutorial1.schedule.getTime());
                String s = steps + ".Tutorial1.checkpoint";
                System.out.println("Checkpointing to file: " + s);
                tutorial1.writeToCheckpoint(new java.io.File(s));
                }
            }
        while(steps < 5000);
        tutorial1.finish();
        System.exit(0);  // make sure any threads finish up
        }
		*/
