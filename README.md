Tutorial-1--Build-Conway-s-Game-of-Life--Without-Visualization-
===============================================================

Tutorial 1: Build Conway's Game of Life (Without Visualization)In this tutorial we will build a basic version of Conway's Game of Life, a simple two-dimensional cellular automata. The simulation will run from the command line and will not be visualizable (that's Tutorial 2) so you'll have to take it on faith that it runs properly.
This tutorial teaches:

	* How to build a basic simulation, use the random number generator, use the schedule (the representation of time), and schedule agents.
	* How to add a basic field (an object neighborhood, or representation of space) to the simulation.
	* How to make an agent

Create a SimStateIn the sim/app/tutorial1and2 directory, create a file called Tutorial1.java In this file, add:
package sim.app.tutorial1and2;

import sim.engine.*;
import sim.field.grid.*;
import ec.util.*;

public class Tutorial1 extends SimState
    {
    private static final long serialVersionUID = 1;

    public Tutorial1(long seed)
        {
        super(seed);
        }

A sim.engine.SimState is the basic class for modelling a simulation. You can think of it as a singleton object which holds all of your simulation information. You create a model by subclassing SimState and adding things to it as you like.

What is serialVerUID? Why are you setting it to 1?Java has a serialization facility which allows you to write out a collection of objects to a file, then read them back in later on when you like. MASON uses this facility to allow you to write out checkpoint files of your model. Java's serialization facility has a misfeature: each class is assigned a ID number, computed by default by Java based on a combination of the class hash and the Java version. Notionally this is to verify that objects read from the file match the current classes. But it's far too conservative. If the class changes slightly (maybe a name change in a method), the ID mismatches and you can no longer use your old (perfectly good) serialized objects. The standard trick for getting around this is to hard-code the ID number (it doesn't matter what it's set to, so we set it to 1 here). It's not a big deal if you forget this: but you may run into some trouble when reading older checkpoint files or ones produced on other machines.
Because we've hard-coded the serialVersionUIDs, if we change the classes in any way, they'll be incompatible with the serialized versions of previous classes but Java won't know that. So: if you change and recompile your code, you should not then try to load an old checkpoint file created using the old code. Also serialization doesn't work across Java versions necessarily. Don't expect to be able to serialize code in Java 1.3.1 and load it successfully in Java 1.5.
The basic SimState already holds two important objects for you: an ec.util.MersenneTwisterFast random number generator, and a sim.engine.Schedule. The Schedule is a discrete event scheduler which represents time in the simulation and lets you schedule events to occur in specific times in the future. The MersenneTwisterFast is a fast, unsynchronized implementation of the Mersenne Twister random number generator, a very high quality and popular generator. MersenneTwisterFast has the same functions as java.util.Random, so you should feel at home with it. We'll talk more about the generator in Tutorial 3.SimState has various constructors for setting up the MersenneTwisterFast and the Schedule. We'll just use the simplest: we pass in a long value which is the random seed for the MersenneTwisterFast.

Why the ec package rather than the sim package?Because MersenneTwisterFast was originally part of GMU's ECJ Evolutionary Computation system
The serialVersionUID is not required, but it will make life easier for you if you eventually want to write out and read checkpoints (serialization) of your model. In general, all your model objects ought to have this variable defined (and set to 1 is fine).Make a GridIn this simulation we will implement Conway's Game of Life, a simple two-dimensional, two-state, eight-neighbor cellular automaton. The Game of Life is played out on a 2D grid of "live" (state=1) and "dead" (state=0) cells. It begins with some initial configuration of cells. Each timestep, all of the cells simultaneously (synchronously) update themselves. Each cell uses the same rule to update itself, based on its eight neighboring cells. That rule is:

	* If I am alive and have one or fewer neighbors who are alive, then I die (presumably of loneliness).
	* If I am alive and have four or more neighbors who are alive, then I die (of overcrowding).
	* If I am dead and have exactly three alive neighbors, then I become alive again.
	* Otherwise, I stay as I am

A good webpage on the Game of Life can be found here.We will use a two-dimensional toroidal grid that is 100 x 100 in size, consisting entirely of 1's and 0's. To do this, we'll borrow special Field called sim.field.grid.IntGrid2D. Fields are our simulation's representation of space: they relate objects or data using some neighborhood function. In this case, our field is simply a wrapper two-dimensional array of integers. Feel free to examine the IntGrid2D code: it's very simple and straightforward. The 2D integer array it contains is public, and you are strongly encouraged to directly access the data for speed.
Add the grid as follows:
    public IntGrid2D grid;
    
    // our own parameters for setting the grid size later on
    public int gridWidth = 100;
    public int gridHeight = 100;

Seed the Grid
What's a b-heptomino?A heptomino is seven live cells. Martin Gardner popularized the Game of Life by showing the dynamics of various heptominos, which he named the a-heptomino, b-heptomino, c-heptomino, etc.
Next we need to define the function which seeds the grid with some initial configuration. Let's use the b-heptomino seed popular in the Game of Life. We'll seed the grid by placing the b-heptomino right in the middle of the grid. Add the seeding code:
    // A b-heptomino looks like this:
    //  X
    // XXX
    // X XX
    public static final int[][] b_heptomino = new int[][]
        {{0, 1, 1},
         {1, 1, 0},
         {0, 1, 1},
         {0, 0, 1}};
    
    void seedGrid()
        {
        // we stick a b_heptomino in the center of the grid
        for(int x=0;x<b_heptomino.length;x++)
            for(int y=0;y<b_heptomino[x].length;y++)
                grid.field[x + grid.field.length/2 - b_heptomino.length/2]
                          [y + grid.field[x].length/2 - b_heptomino[x].length/2] =
                    b_heptomino[x][y];
        }

Notice that we're directly accessing the grid's 2D integer field array. IntGrid2D's require that their 2D array be rectangular, so we could have written grid.getWidth() and grid.getHeight() instead of grid.field.length and grid.field[x].length respectively.
Add the Start and Finish MethodsThe general structure of a simulation is as follows:

	* The SimState's start() method is called.
	* The SimState's schedule.step(state) method is called some number of times or until it returns false. This in turn fires events on agents to manipulate the simulation.
	* The SimState's finish() method is called.

We don't need a finish() method, but we do need a start() method. In this method, we need to call super.start() to let the SimState set up its Schedule. Then we create the grid and seed it. Last, we schedule our (in this case) single agent (called CA -- we'll come to that) to repeatedly manipulate the simulation:
    public void start()
        {
        super.start();  // very important!  This resets and cleans out the Schedule.
        grid = new IntGrid2D(gridWidth, gridHeight);
        seedGrid();
        schedule.scheduleRepeating(new CA());
        }


How do you stop an infinitely repeating schedule?schedule.scheduleRepeating(agent) returns a sim.engine.Stoppable object. Call stop() on that object.
The schedule has a number of methods for scheduling agents to have events fired once or repeating. We have chosen the simplest repeating schedule: schedule the agent to be fired once every time step forever.
Can't it get simpler than that?Actually, in Tutorial 2, we'll simplify the main(...) code down to just a single method call. But you should understand what's going on here before using that convenience method (which does it for you and a whole lot more).
Last we'll write a very simple main(String[] args) method which creates a Tutorial1 with a random seed, starts it, calls step(tutorial1) until the schedule steps to 5000, then finishes up. After every 500 steps have transpired, the number of steps transpired so far is printed out, plus the timestamp of the last step. Since timestamps start at 0.0, if the schedule is doing one step per unit of time, the timestamp of the 100th step is 99.0.
    public static void main(String[] args)
        {
        Tutorial1 tutorial1 = new Tutorial1(System.currentTimeMillis());
        tutorial1.start();
        long steps;
        do
            {
            if (!tutorial1.schedule.step(tutorial1))
                break;
            steps = tutorial1.schedule.getSteps();
            if (steps % 500 == 0)
                System.out.println("Steps: " + steps + " Time: " + tutorial1.schedule.getTime());
            }
        while(steps < 5000);
        tutorial1.finish();
        System.exit(0);  // make sure any threads finish up
        }
    }

The number of steps of the schedule is a long. But the time used by the schedule is real-valued. This means you can schedule items to occur at some real-valued timestep (such as at time 3.14159). When it is stepped, the schedule grabs the minimum-time scheduled agents and sends them an event, then unschedules and removes them.
Create the Cellular AutomatonAll that's left is to write the actual agent which updates the cells in the grid. Since all the cells must be updated synchronously, at each time step this agent will dump the grid into a secondary grid; then it will update the original grid cells based on the secondary grid cell values. Create a new file called CA.java. In this file, put:
package sim.app.tutorial1and2;

import sim.engine.*;
import sim.field.grid.*;

public class CA implements Steppable
    {
    private static final long serialVersionUID = 1;

    // the width and height will change later
    public IntGrid2D tempGrid = new IntGrid2D(0,0);


What's an Agent?We define an Agent in a very specific manner. An Agent is an object which can be scheduled on a Schedule to be activated at some time step, ostensibly in order for it to change the simulation environment in some way. Agents don't have to be physically in the environment (that is, in any Field). They can be in a Field of course, and often are: we call such agents embodied agents.
So far we've spoken of Agents receiving events from the Schedule. In fact agents can receive only a single kind of event from the Schedule: the Agent can have its step(SimState) method called. All agents must implement this method, which in turn means that they must implement the single-method interface sim.engine.Steppable.Here's the step(SimState) method:
    public void step(SimState state)
        {
        Tutorial1 tut = (Tutorial1)state;
        tempGrid.setTo(tut.grid);   // first copy the grid into tempGrid
        
        // for each cell...
        int width = tempGrid.getWidth();
        int height = tempGrid.getHeight();
        for(int x=0;x<width;x++)
            for(int y=0;y<height;y++)
                {
                int count = 0;
                // count the number of live neighbors around the cell,
                // and to simplify the for-loop, just include the cell itself
                for(int dx = -1; dx < 2; dx++)
                    for(int dy = -1; dy < 2; dy++)
                        count += tempGrid.field[tempGrid.stx(x+dx)][tempGrid.sty(y+dy)];
                
                // since we're including the cell itself, the rule is slightly different:
                // if the count is 2 or less, or 5 or higher, the cell dies
                // else if the count is 3 exactly, a dead cell becomes live again
                // else the cell stays as it is
                        
                if (count <= 2 || count >= 5)  // death
                    tut.grid.field[x][y] = 0;
                else if (count == 3)           // birth
                    tut.grid.field[x][y] = 1;
                }
        }
    }


Can this be faster?In HotSpot, you can get a 15% improvement in speed by copying instance variables to final local variables, like this:
final int width = tempGrid.getWidth();
final int height = tempGrid.getHeight();
final int[][] field = tempGrid.field;
final int[][] field2 = tut.grid.field;
for(int x=0;x<width;x++)
    for(int y=0;y<height;y++)
        {
        count = 0;
        for(int dx = -1; dx < 2; dx++)
            for(int dy = -1; dy < 2; dy++)
                count += field[tempGrid.stx(x+dx)][tempGrid.sty(y+dy)];
        if (count <= 2 || count >= 5)
            field2[x][y] = 0;
        else if (count == 3)
            field2[x][y] = 1;
        }

Some other tricks can make it faster yet. For an extensive tutorial on how to speed up Java, see the sim/app/heatbugs/Diffuser.java file.
Some notes:
	* The step(SimState) method will pass in your SimState instance (or more specifically, whatever instance you had passed into the Schedule.step(SimState) method).
	* The setTo(IntGrid2D) method resizes the array in the original grid if necessary. Thus we can start with a tempGrid of unknown size and lazily set it.
	* The stx(x) and sty(y) methods are methods to simplify your toroidal math. For example, if the grid's width is 100, then stx(123) returns 23 to wrap the value back into the legal range. Similarly, stx(-23) returns 77. These methods are inlined and fairly fast: there's at most a 25% loss in speed (usually much less) to use them rather than doing your own toroidal computation here. The methods work properly if your values are no more than a dimension out of bounds: for example, stx(223) will not work correctly. For complete out-of-bounds toroidal locations regardless of bounds, use the slower tx(x) and ty(y) methods.

Run the simulationAs mentioned before, when we run the simulation from the command line, it won't do anything exciting. You can see the simulation by completing Tutorial 2.
Compile the simulation's two Java files. Then issue java sim.app.tutorial1and2.Tutorial1 from the command line. Java should run for a second, then silently finish.
