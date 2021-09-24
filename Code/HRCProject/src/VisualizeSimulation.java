
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;
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
	
	/* Builds the environment; while not necessary for this problem set,
	 * this could be modified to allow for different types of environments,
	 * for example loading from a file, or creating multiple agents that
	 * can communicate/interact with each other.
	 */
	public VisualizeSimulation() {
		// TODO: change the following to run the simulation on different maps.
		String filename = "Map1.txt";
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
	    }	
		
	    Position robotPos = new Position(0,0); // Modify for multiple robots MIW
		Environment env = new Environment(map, robotPos); // Modify for multiple robots MIW
		Robot robot = new Robot(env, robotPos.row, robotPos.col);
	
		ArrayList<Robot> robots = new ArrayList<Robot>();
		robots.add(robot);
    	envPanel = new EnvironmentPanel(env, robots);
    	add(envPanel);
	}
	
	public static void main(String[] args) {
	    JFrame frame = new VisualizeSimulation();

	    frame.setTitle("CSSE 413: HRC Project");
	    frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
	    frame.pack();
	    frame.setVisible(true);
    }
}

@SuppressWarnings("serial")
class EnvironmentPanel extends JPanel{
	private Timer timer;
	private Environment env;
	private ArrayList<Robot> robots;
	private LinkedList<Position> targets;
	private int timesteps, timestepsStop;
	//TODO: Change TILESIZE if you want to enlarge the visualization.
	public static final int TILESIZE = 15;
	//TODO: Change the timeStepSpeed to speed-up or slow down the animation.
	// 500 millisecond time steps
	private int timeStepSpeed = 100;
	
	public EnvironmentPanel(Environment env, ArrayList<Robot> robots) {
	    setPreferredSize(new Dimension(env.getCols()*TILESIZE, env.getRows()*TILESIZE));
		this.env = env;
		this.robots = robots;
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
//				if (goalConditionMet()) {
//					timer.stop();
//					printPerformanceMeasure();
//				}
			}
			
//			public void printPerformanceMeasure() {
//				System.out.println("A solution has been found in: " + timesteps + " steps.");
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
//			}
			
//			public boolean goalConditionMet() {
//				if (targets.isEmpty()) return true;
//				boolean temp = true;
//				for (Robot robot : robots) {
//					if (robot.getPathFound()) temp = false;
//				}	
//				return temp;
//			}
			
//			public void remove(int row, int col) {
//				for (int i = 0; i < targets.size(); i++) {
//					if (targets.get(i).row == row && targets.get(i).col == col) {
//						targets.remove(i);
//					}
//				}
//			}

			// Gets the new state of the world after robot actions
			public void updateEnvironment() {
				timesteps++;
				for(Robot robot : robots) {
					Action action = robot.getAction();
					int row = robot.getPosRow();
					int col = robot.getPosCol();
//					remove(row, col);
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
		g.setColor(Properties.GREEN);
		for(Robot robot : robots) {
		    g.fillOval(robot.getPosCol() * TILESIZE+TILESIZE/4, 
    		            robot.getPosRow() * TILESIZE+TILESIZE/4,
    		            TILESIZE/2, TILESIZE/2);
		}
	}
}