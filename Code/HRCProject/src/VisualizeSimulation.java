import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.Timer;

/**
 * A Visual Guide toward testing whether your robot
 * agent is operating correctly. This visualization
 * will run for 200 time steps. If the agent reaches
 * the target location before the 200th time step, the
 * simulation will end automatically.
 * You are free to modify the environment for test cases.
 * @author Adam Gaweda, Michael Wollowski
 */
public class VisualizeSimulation extends JFrame {
	private static final long serialVersionUID = 1L;
	private EnvironmentPanel envPanel;
	private static String filename = "";
	
	/* Builds the environment; while not necessary for this problem set,
	 * this could be modified to allow for different types of environments,
	 * for example loading from a file, or creating multiple agents that
	 * can communicate/interact with each other.
	 */
	public VisualizeSimulation() {
		// TODO: change the following to run the simulation on different maps.
		LinkedList<String> map = new LinkedList<> ();
	    try {
			File inputFile = new File(filename);
			FileReader fileReader = new FileReader(inputFile); 
			BufferedReader bufferedReader = new BufferedReader(fileReader); 
			String line;
			while ((line = bufferedReader.readLine()) != null) {
//				System.out.println(line);
				map.add(line);
			}
	    	fileReader.close();
	    } catch (Exception exception) {
	    	System.out.println(exception);
	    	System.exit(0);;
	    }	
		
		ArrayList<Robot> robots = new ArrayList<Robot>();
		Environment env = new Environment(map, robots);

    	envPanel = new EnvironmentPanel(env, robots);
    	add(envPanel);
	}
	
	public static void main(String[] args) {
		filename = args[0];
	    JFrame frame = new VisualizeSimulation();

	    frame.setTitle("CSSE 413: HRC Project");
	    frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
	    frame.pack();
	    frame.setVisible(true);
    }
}

@SuppressWarnings("serial")
class EnvironmentPanel extends JPanel{
	
	private ArrayList<Color> robotColors = new ArrayList<>();
	private Timer timer;
	private Environment env;
	private ArrayList<Robot> robots;
	private LinkedList<Position> targets;
	private int timesteps, timestepsStop;
	//TODO: Change TILESIZE if you want to enlarge the visualization.
	public static final int TILESIZE = 25;
	//TODO: Change the timeStepSpeed to speed-up or slow down the animation.
	// 500 millisecond time steps
	private int timeStepSpeed = 300;
	
	public EnvironmentPanel(Environment env, ArrayList<Robot> robots) {
		robotColors.add(Properties.RED);
		robotColors.add(Properties.GREEN);
		robotColors.add(Properties.BLUE);
		robotColors.add(Properties.ORANGE);
		robotColors.add(Properties.YELLOW);
		robotColors.add(Properties.WHITE);
		robotColors.add(Properties.INDIGO);
	    setPreferredSize(new Dimension(env.getCols()*TILESIZE, env.getRows()*TILESIZE));
		this.env = env;
		this.robots = robots;

		//Tell robots what their color is
		for(int i = 0; i < this.robots.size(); i++){
			this.robots.get(i).color = robotColors.get(i);
		}
		// number of time steps since the beginning
//		this.timesteps = -1; // -1 to account for displaying initial state.
		// number of time steps before stopping simulation
//		this.timestepsStop = 200;
		this.targets = env.getTargets();
		
		this.timer = new Timer(timeStepSpeed, new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				updateEnvironment();
				repaint();
//				if (timesteps == timestepsStop) {
//					timer.stop();
//					printPerformanceMeasure();
//				}
				if (goalConditionMet()) {
					timer.stop();
					printPerformanceMeasure();
				}
			}
			
			public void printPerformanceMeasure() {
				System.out.println("A solution has been found in: " + timesteps + " steps.");
//				int num = 0;
//				for(Robot robot : robots) {
//					if (robot.getPathFound()) {
//						System.out.println("Robot " + num + " found a path to the goal state in: " + robot.getPathLength() + " steps.");
//						System.out.println("Robot " + num + " search placed on open: " + robot.getOpenCount() + " states.");		
//					} else {
//						System.out.println("Robot " + num + " did not find a path to the goal state.");
//						System.out.println("Robot " + num + " search placed on open: " + robot.getOpenCount() + " states.");	
//					}
//					num++;
//				}
			}
			
//			public boolean goalConditionMet() {
//				if (targets.isEmpty()) return true;
//				boolean temp = true;
//				for (Robot robot : robots) {
//					if (robot.getPathFound()) temp = false;
//				}	
//				return temp;
//			}
			
			public boolean goalConditionMet() {
				return env.getNumDirtyTiles() == 0;

		}
			

			// Gets the new state of the world after robot actions
			public void updateEnvironment() {
				timesteps++;
				if (((int)(Math.random()*5)) == 0) {
					int row = (int)(Math.random()* env.getRows());
					int col = (int)(Math.random()* env.getCols());
					if (env.validPos(row, col)) env.soilTile(row, col);
				}
				//TODO: the following screws up the id numbers.
				if (((int)(Math.random()*100)) == 0)
					robots.remove((int)(Math.random()*robots.size()));
				if (robots.size() == 0) {
					System.out.println("All robots broke. No solution found.");
					System.exit(0);
				}
				for(Robot robot : robots) {
					if (robot != null) {
						Action action = robot.getAction();
						int row = robot.getPosRow();
						int col = robot.getPosCol();
						switch(action) {
	                    case CLEAN:
	                        env.cleanTile(row, col);
	                        break;
			            case MOVE_DOWN:
			                if (env.validPos(row+1, col))
			                    robot.incPosRow();
			                break;
			            case MOVE_LEFT:
			                if (env.validPos(row, col-1))
			                    robot.decPosCol();
			                break;
			            case MOVE_RIGHT:
			                if (env.validPos(row, col+1))
			                    robot.incPosCol();
			                break;
			            case MOVE_UP:
			                if (env.validPos(row-1, col))
			                    robot.decPosRow();
			                break;
			            case DO_NOTHING: // pass to default
			            default:
			                break;
						}
					}
				}
			}
		});
		this.timer.start();
	}
	
	/*
	 * The paintComponent method draws all of the objects onto the
	 * panel. This is updated at each time step when we call repaint().
	 */
	@Override
	protected void paintComponent(Graphics g) {
		super.paintComponent(g);
		// Paint Environment Tiles
		Tile[][] tiles = env.getTiles();
		for(int row = 0; row < env.getRows(); row++)
		    for(int col = 0; col < env.getCols(); col++) {
		        if(tiles[row][col].getStatus() == TileStatus.CLEAN) {
                    g.setColor(Properties.SILVER);
		        } else if(tiles[row][col].getStatus() == TileStatus.DIRTY) {
                    g.setColor(Properties.BROWN);
                } else if(tiles[row][col].getStatus() == TileStatus.IMPASSABLE) {
                    g.setColor(Properties.BLACK);
                } else if(tiles[row][col].getStatus() == TileStatus.TARGET) {
                    g.setColor(Properties.LIGHTGREEN);
                }
		        // fillRect(int x, int y, int width, int height)
		        g.fillRect( col * TILESIZE, 
                            row * TILESIZE,
                            TILESIZE, TILESIZE);
		        
		        g.setColor(Properties.BLACK);
                g.drawRect( col * TILESIZE, 
                            row * TILESIZE,
                            TILESIZE, TILESIZE);
		    }
		// Paint Robot
//		g.setColor(Properties.BLACK);
		for(int i = 0; i < robots.size(); i++) {
			g.setColor(robots.get(i).color);
//			g.drawString(String.valueOf(i), robots.get(i).getPosCol() * TILESIZE+TILESIZE/4, (int) (robots.get(i).getPosRow() * TILESIZE+TILESIZE/1.5));
			g.fillOval(robots.get(i).getPosCol() * TILESIZE+TILESIZE/4,
						robots.get(i).getPosRow() * TILESIZE+TILESIZE/4,
						TILESIZE/2, TILESIZE/2);
		}
	}
}
