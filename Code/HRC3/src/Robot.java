import java.awt.*;
import java.lang.reflect.Array;
import java.util.*;
import java.util.List;
import java.util.Properties;

import edu.stanford.nlp.ling.CoreAnnotations;
import edu.stanford.nlp.ling.IndexedWord;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.pipeline.StanfordCoreNLP;
import edu.stanford.nlp.semgraph.SemanticGraph;
import edu.stanford.nlp.semgraph.SemanticGraphCoreAnnotations;
import edu.stanford.nlp.semgraph.SemanticGraphCoreAnnotations.CollapsedCCProcessedDependenciesAnnotation;
import edu.stanford.nlp.sentiment.SentimentCoreAnnotations;
import edu.stanford.nlp.trees.GrammaticalRelation;
import edu.stanford.nlp.util.CoreMap;
import edu.stanford.nlp.util.Pair;

/**
	Represents an intelligent agent moving through a particular room.	
	The robot only has one sensor - the ability to get the status of any  
	tile in the environment through the command env.getTileStatus(row, col).
	@author Adam Gaweda, Michael Wollowski
*/

public class Robot {
	private Environment env;
	private int posRow;
	private int posCol;
	public String name = "";
	public boolean isPlayer;
	public Color color;
	public String colorName;
	private int totalDirtyTiles;
	private int totalCleanedTiles;

	public LinkedList<Position> claimedTargets;
	private LinkedList<Position> path;
	private boolean pathFound;
	private long openCount;
	private int pathLength;
	private Iterator<Position> pathWalk;

	public Action command;
	public LinkedList<Action> currentPlan;
	public static Map<String, LinkedList<Action>> allPlans = new HashMap<>();;
	private Iterator<Action> planWalk;
	public boolean symmetricPlan = false;

	public Action[][] policy;

	public RobotStatus status = RobotStatus.NormalRuntime;
	private final Random rand = new Random();


	public enum RobotStatus{
		ExecutingPlan,
		FollowingCommand,
		Staying,
		NormalRuntime
	}


	private Properties props;
	private StanfordCoreNLP pipeline;
	
	private Scanner sc;
	public ArrayList<Action> actionList;

	
	/**
	    Initializes a Robot on a specific tile in the environment. 
	*/

	
	public Robot (Environment env, int posRow, int posCol, boolean isPlayer) {
		this.env = env;
		this.posRow = posRow;
		this.posCol = posCol;
		this.isPlayer = isPlayer;
		if(this.isPlayer) this.command = Action.DO_NOTHING;

		this.totalDirtyTiles = 0;
		this.totalCleanedTiles = 0;
		this.claimedTargets = new LinkedList<>();

		actionList = new ArrayList<>();

		this.path = new LinkedList<>();
		this.pathFound = false;
		this.openCount = 0;
		this.pathLength = 0;
	}
	
	public int getPosRow() { return posRow; }
	public int getPosCol() { return posCol; }
	public void incPosRow() { posRow++; }
	public void decPosRow() { posRow--; }
	public void incPosCol() { posCol++; }
    public void decPosCol() { posCol--; }
	
	/**
	   Returns the next action to be taken by the robot. A support function 
	   that processes the path LinkedList that has been populates by the
	   search functions.
	*/
	public Action getAction () {
		Action currentAction = null;
		switch(this.status){
			case Staying: return Action.DO_NOTHING;
			case ExecutingPlan: {
				if(planWalk == null) {
					planWalk = currentPlan.iterator();
				}
				if(!planWalk.hasNext()) {
					System.out.println("Finished executing plan. Returning to normal runtime.");
					this.planWalk = null;
					this.status = RobotStatus.NormalRuntime;
					return Action.DO_NOTHING;
				}
				if(symmetricPlan){
					switch (planWalk.next()){
						case MOVE_DOWN : currentAction = Action.MOVE_UP; break;
						case MOVE_UP: currentAction = Action.MOVE_DOWN; break;
						case MOVE_LEFT: currentAction = Action.MOVE_RIGHT; break;
						case MOVE_RIGHT: currentAction = Action.MOVE_LEFT; break;
						default: return Action.DO_NOTHING;
					}
				}else{
					currentAction = planWalk.next();
				}
				break;
			}
			case FollowingCommand: {
				this.status = RobotStatus.NormalRuntime;
				currentAction = this.command;
				break;
			}
			case NormalRuntime: {
				Position currentPos = new Position(posRow, posCol);
				if(claimedTargets.remove(currentPos)){
					return Action.CLEAN;
				}
				if(this.totalDirtyTiles != env.getTotalDirtyTiles() || this.totalCleanedTiles != env.getTotalCleanedTiles() || this.policy == null) {
					this.totalDirtyTiles = env.getTotalDirtyTiles();
					this.totalCleanedTiles = env.getTotalCleanedTiles();
					this.claimedTargets.clear();
					LinkedList<Position> targets = env.getDirtyTiles();
					ArrayList<Robot> robots = env.getRobots();
					for (Robot r : robots){
						LinkedList<Position> claimed = r.claimedTargets;
						for (Position claim : claimed){
							targets.remove(claim);
						}
					}
					if(targets.size() == 0) return Action.DO_NOTHING;
					Position target = findClosestTarget(targets);
					this.claimedTargets.add(target);
					this.policy = env.generatePolicy(target);
				}
				currentAction = this.policy[this.posRow][this.posCol];
			}
		}
		double randomDouble = rand.nextDouble();
		if(randomDouble >= 0.9){
			currentAction = Action.MOVE_LEFT;
		} else if (randomDouble >= 0.8){
			currentAction = Action.MOVE_RIGHT;
		}
		switch (currentAction){
			case MOVE_UP : if(env.getTileStatusWithRobots(posRow - 1, posCol) == TileStatus.IMPASSABLE) return Action.DO_NOTHING; else return currentAction;
			case MOVE_DOWN: if(env.getTileStatusWithRobots(posRow + 1, posCol) == TileStatus.IMPASSABLE) return Action.DO_NOTHING; else return currentAction;
			case MOVE_LEFT: if(env.getTileStatusWithRobots(posRow, posCol - 1) == TileStatus.IMPASSABLE) return Action.DO_NOTHING; else return currentAction;
			case MOVE_RIGHT: if(env.getTileStatusWithRobots(posRow, posCol + 1) == TileStatus.IMPASSABLE) return Action.DO_NOTHING; else return currentAction;
			default: return Action.DO_NOTHING;
		}
	}

	private Position findClosestTarget(LinkedList<Position> targets) {
		int minDistance = Integer.MAX_VALUE;
		Position minTarget = null;
		for(Position target: targets){
			int distance = Math.abs(this.posRow - target.row) + Math.abs(this.posCol - target.col);
			if( minDistance > distance ){
				minDistance = distance;
				minTarget = target;
			}
		}
		return minTarget;
	}



	private static class BetterPosition{
		private int row;
		private int col;

		public BetterPosition(int row, int col){
			this.row = row;
			this.col = col;
		}

		public BetterPosition(Position pos){
			this.row = pos.row;
			this.col = pos.col;
		}

		int getRow() {
			return row;
		}

		int getCol() {
			return col;
		}

		void setRow(int row) {
			this.row = row;
		}

		void setCol(int col) {
			this.col = col;
		}

		public static LinkedList<BetterPosition> convertFromPositions(LinkedList<Position> positions){
			Iterator<Position> iterator = positions.iterator();
			LinkedList<BetterPosition> betterPositions = new LinkedList<>();
			while(iterator.hasNext()){
				Position pos = iterator.next();
				betterPositions.add(new BetterPosition(pos.row, pos.col));
			}
			return betterPositions;
		}

		public static LinkedList<Position> toPositions(LinkedList<BetterPosition> betterPositions){
			Iterator<BetterPosition> iterator = betterPositions.iterator();
			LinkedList<Position> positions = new LinkedList<>();
			while(iterator.hasNext()){
				BetterPosition pos = iterator.next();
				positions.add(new Position(pos.getRow(), pos.getCol()));
			}
			return positions;
		}

		@Override
		public boolean equals(Object o){
			if(o.getClass() != getClass()) return false;
			BetterPosition other = (BetterPosition) o;
			if(this.getRow() != other.getRow()) return false;
			return this.getCol() == other.getCol();
		}
	}

	private LinkedList<Position> getDirtyTiles(){
		LinkedList<Position> dirtyTiles = new LinkedList<>();
		for(int i = 0; i < env.getRows(); i++){
			for(int j = 0; j < env.getCols(); j++){
				if(env.getTileStatus(i, j) == TileStatus.DIRTY){
					dirtyTiles.add(new Position(i, j));
				}
			}
		}
		return dirtyTiles;
	}

}