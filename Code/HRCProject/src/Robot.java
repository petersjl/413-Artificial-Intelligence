import java.util.*;
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
	private String name = "";
	private boolean awaitingName = false;

	private LinkedList<Position> targets;
	private LinkedList<Position> path;
	private boolean pathFound;
	private long openCount;
	private int pathLength;
	private Iterator<Position> pathWalk;

	private LinkedList<Action> currentPlan;
	private Map<String, LinkedList<Action>> allPlans;
	private Iterator<Action> planWalk;

	public RobotStatus status = RobotStatus.NormalRuntime;
	private boolean symmetricPlan = false;

	public enum RobotStatus{
		WalkingSearchPath,
		ExecutingPlan,
		AwaitingCoordinates,
		AwaitingPlanName,
		RecordingPlan, NormalRuntime
	}


	private Properties props;
	private StanfordCoreNLP pipeline;
	
	private Scanner sc;
	private ArrayList<Action> actionList;

	private static List<String> repeatWords = Arrays.asList("again", "more", "further");
	private static List<String> undoWords = Arrays.asList("undo", "back");

	private static List<String> intentPhrases = Arrays.asList(
			"I think you want me to ",
			"You might want me to ",
			"You're probably telling me to ",
			"That means I should probably ",
			"By that, I think I should ");

	private static List<String> uncertainPhrases = Arrays.asList(
			"I didn't quite understand that...",
			"Maybe say that a different way...",
			"Either my English is bad your yours is because that made no sense...",
			"Try that again please...",
			"Maybe try simpler words, I'm new to this...",
			"I haven't fully learned English, try different words...",
			"Only you understood that, try again...",
			"What did that even mean? How about a different phrase...",
			"New to English, try that differently...",
			"Can you say that any other way?");

	private static List<String> affirmativePhrases = Arrays.asList(
			"Sure thing",
			"I can do that",
			"Understood",
			"Right away",
			"I got it");

	private static List<String> appreciationPhrases = Arrays.asList(
			"Just doin my job",
			"Oh, thanks",
			"Why thank you",
			"You're the first to appreciate my work",
			"Finally some praise for all this",
			"Someone who actually understands my skill"
	);

	private static List<String> positivePhrases = Arrays.asList(
			"I appreciate your enthusiasm",
			"Your energy is infectious",
			"Thanks for the easy tone"
	);

	private static List<String> negativePhrases = Arrays.asList(
			"I'm sorry you seem to be having trouble",
			"I'll try to make this easier on you in the future",
			"Maybe we can work on this in the future"
	);
	
	/**
	    Initializes a Robot on a specific tile in the environment. 
	*/

	
	public Robot (Environment env, int posRow, int posCol) {
		this.env = env;
		this.posRow = posRow;
		this.posCol = posCol;
		
	    props = new Properties();
	    props.setProperty("annotators", "tokenize, ssplit, pos, lemma, ner, parse, sentiment");
	    pipeline = new StanfordCoreNLP(props);

		actionList = new ArrayList<>();

		this.path = new LinkedList<>();
		this.pathFound = false;
		this.openCount = 0;
		this.pathLength = 0;

		this.allPlans = new HashMap<>();
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
		switch(this.status){
			case WalkingSearchPath : {
				if(pathWalk == null) {
					pathWalk = path.iterator();
					//remove the start point
					pathWalk.next();
				}
				if(!pathWalk.hasNext()) {
					this.pathWalk = null;
					this.status = RobotStatus.NormalRuntime;
					return Action.DO_NOTHING;
				}
				Position pos = pathWalk.next();
				if(pos.row < this.posRow) {
					this.actionList.add(Action.MOVE_UP);
					return Action.MOVE_UP;
				}
				if(pos.row > this.posRow) {
					this.actionList.add(Action.MOVE_DOWN);
					return Action.MOVE_DOWN;
				}
				if(pos.col < this.posCol) {
					this.actionList.add(Action.MOVE_LEFT);
					return Action.MOVE_LEFT;
				}
				if(pos.col > this.posCol) {
					this.actionList.add(Action.MOVE_RIGHT);
					return Action.MOVE_RIGHT;
				}
				this.actionList.add(Action.CLEAN);
				return Action.CLEAN;
			}
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
						case MOVE_DOWN : return Action.MOVE_UP;
						case MOVE_UP: return Action.MOVE_DOWN;
						case MOVE_LEFT: return Action.MOVE_RIGHT;
						case MOVE_RIGHT: return Action.MOVE_LEFT;
						default: return Action.DO_NOTHING;
					}
				}else{
					return planWalk.next();
				}
			}
		}
	    Annotation annotation;
	    System.out.print("> ");
	    sc = new Scanner(System.in); 
        String name = sc.nextLine();
		name = name.toLowerCase();
//	    System.out.println(name);
		if(awaitingName){
			this.name = name;
			System.out.println("Alright, my name is now " + name);
			this.awaitingName = false;
			return Action.DO_NOTHING;
		}
        annotation = new Annotation(name);
	    pipeline.annotate(annotation);
	    List<CoreMap> sentences = annotation.get(CoreAnnotations.SentencesAnnotation.class);
	    if (sentences != null && ! sentences.isEmpty()) {
			CoreMap sentence = sentences.get(0);
			SemanticGraph graph = sentence.get(SemanticGraphCoreAnnotations.CollapsedCCProcessedDependenciesAnnotation.class);
			//graph.prettyPrint();

			switch (sentence.get(SentimentCoreAnnotations.SentimentClass.class) ){
				case "Positive": System.out.println(positivePhrases.get((int)Math.floor(Math.random() * positivePhrases.size())));break;
				case "Negative": System.out.println(negativePhrases.get((int)Math.floor(Math.random() * negativePhrases.size())));break;
			}

			List<IndexedWord> li = graph.getAllNodesByPartOfSpeechPattern("RB|VB|RP|NN|UH|JJ");
			Direction direction = null;
			DirectAction action = null;
			boolean repeat = false;
			boolean undo = false;
			boolean assumed = false;
			if(!(this.status == RobotStatus.RecordingPlan)){
				for (IndexedWord w : li) {
					if(assumed) break;
					switch (w.word()){
						case "left": {
							assumed = true;
							sayIntent("move left");
							break;
						}
						case "right": {
							assumed = true;
							sayIntent("move right");
							break;
						}
						case "up": {
							assumed = true;
							sayIntent("move up");
							break;
						}
						case "down": {
							assumed = true;
							sayIntent("move down");
							break;
						}
						case "clean": {
							assumed = true;
							sayIntent("clean");
							break;
						}
					}
				}
			}
			for (IndexedWord w : li) {
				if (w.word().equals("name")){
				  if(this.name != "") {
					  System.out.println("My name is " + this.name);
				  }else{
					  System.out.println("I don't have one yet. Enter one now to give me a name!");
					  this.awaitingName = true;
				  }
				  return Action.DO_NOTHING;
				}
				if (w.word().equals("work") || w.word().equals("job")){
					if(name.contains("good") || name.contains("nice")){
						sayAppreciate();
						return Action.DO_NOTHING;
					}
				}
				if (w.word().equals("not")) {
					return processNegate(assumed);
				}
				if (w.tag().equals("RB") || w.tag().equals("RBR")) {
					if(!repeat) repeat = repeatWords.contains(w.word());
				}
				if (w.tag().equals("RB") || w.tag().equals("VB")) {
					if(!undo) undo = undoWords.contains(w.word());
				}
				if (w.tag().equals("RB") || w.tag().equals("RP") || w.tag().equals("UH") || w.tag().equals("NN")) {
					if(direction == null) direction = processDirection(w.word());
				}
				if (w.tag().equals("VB") || w.tag().equals("NN") || w.tag().equals("JJ")) {
					if(action == null) action = processVerb(w.word());
				}
			}
			if(undo) {
				if(this.status == RobotStatus.RecordingPlan){
					System.out.println("Cannot perform undo actions in a plan");
					return Action.DO_NOTHING;
				}
				return undo();
			}
			if(repeat) return repeat();
			if(action == DirectAction.CLEAN) {
				if(name.contains("all")){
					if(this.status == RobotStatus.RecordingPlan){
						System.out.println("Cannot perform search actions in a plan");
						return Action.DO_NOTHING;
					}
					this.targets = getDirtyTiles();
					this.bfsM();
					if(this.pathFound){
						System.out.println("I found a path to the targets and will now execute");
						this.status = RobotStatus.WalkingSearchPath;
					}else{
						System.out.println("No valid path to all targets was found");
					}
					return Action.DO_NOTHING;
				}
				if(name.contains("list")){
					if(this.status == RobotStatus.RecordingPlan){
						System.out.println("Cannot perform search actions in a plan");
						return Action.DO_NOTHING;
					}
					System.out.println("Please enter target coordinates as int pairs separated by a space, one per line, and end with a blank row");
					this.targets = new LinkedList<>();
					while(true){
						System.out.print("> ");
						Scanner lineReader = new Scanner(System.in);
						String line = lineReader.nextLine();
						if(line.equals("")) break;
						Scanner coordReader = new Scanner(line);
						if(!coordReader.hasNextInt()){
							System.out.println("Please enter int pairs separated by a space");
							continue;
						}
						int tempRow = coordReader.nextInt();
						if(!coordReader.hasNextInt()){
							System.out.println("Please enter int pairs separated by a space");
							continue;
						}
						int tempCol = coordReader.nextInt();
						if(tempRow >=0 && tempCol >= 0 && tempRow < env.getRows() && tempCol < env.getCols()){
							this.targets.add(new Position(tempRow,tempCol));
						}
					}
					if(this.targets.isEmpty()){
						System.out.println("No targets supplied. I will wait for a new command.");
						return Action.DO_NOTHING;
					}
					this.bfsM();
					if(this.pathFound){
						System.out.println("I found a path to the targets and will now execute");
						this.status = RobotStatus.WalkingSearchPath;
					}else{
						System.out.println("No valid path to all targets was found");
					}
					return Action.DO_NOTHING;
				}
				if(name.contains("rectangle")){
					if(this.status == RobotStatus.RecordingPlan){
						System.out.println("Cannot perform search actions in a plan");
						return Action.DO_NOTHING;
					}
					System.out.println("Please enter target coordinates (top left and bottom right) as int pairs separated by a space, one per line");
					this.targets = new LinkedList<>();
					Position topLeft;
					while(true){
						System.out.print("top left > ");
						Scanner coordReader = new Scanner(System.in);
						if(!coordReader.hasNextInt()){
							System.out.println("Please enter an int pair separated by a comma");
							continue;
						}
						int tempRow = coordReader.nextInt();
						if(!coordReader.hasNextInt()){
							System.out.println("Please enter int pairs separated by a comma");
							continue;
						}
						int tempCol = coordReader.nextInt();
						if(tempRow >=0 && tempCol >= 0 && tempRow < env.getRows() && tempCol < env.getCols()){
							topLeft = new Position(tempRow,tempCol);
							break;
						}
					}
					Position bottomRight;
					while(true){
						System.out.print("bottom right > ");
						Scanner coordReader = new Scanner(System.in);
						if(!coordReader.hasNextInt()){
							System.out.println("Please enter an int pair separated by a comma");
							continue;
						}
						int tempRow = coordReader.nextInt();
						if(!coordReader.hasNextInt()){
							System.out.println("Please enter int pairs separated by a comma");
							continue;
						}
						int tempCol = coordReader.nextInt();
						if(tempRow >=0 && tempCol >= 0 && tempRow < env.getRows() && tempCol < env.getCols()){
							bottomRight = new Position(tempRow,tempCol);
							break;
						}
					}
					for(int i = topLeft.row; i <= bottomRight.row; i++){
						for(int j = topLeft.col; j <= bottomRight.col; j++){
							if((env.getTileStatus(i, j) == TileStatus.DIRTY)){
								targets.add(new Position(i, j));
							}
						}
					}
					this.bfsM();
					if(this.pathFound){
						System.out.println("I found a path to the targets and will now execute");
						this.status = RobotStatus.WalkingSearchPath;
					}else{
						System.out.println("No valid path to all targets was found");
					}
					return Action.DO_NOTHING;
				}
				if(this.status == RobotStatus.RecordingPlan){
					this.currentPlan.add(Action.CLEAN);
					return Action.DO_NOTHING;
				}
				actionList.add(Action.CLEAN);
				return affirm(Action.CLEAN);
			}
			if(action == DirectAction.STAY) return Action.DO_NOTHING;
			if(action == DirectAction.MOVE || action == null){
				if(direction == null){
				  sayUncertain();
				  return Action.DO_NOTHING;
				}
				switch (direction){
					case UP: {
						if(this.status == RobotStatus.RecordingPlan){
							this.currentPlan.add(Action.MOVE_UP);
							return Action.DO_NOTHING;

						}else{
							actionList.add(Action.MOVE_UP);
							return affirm(Action.MOVE_UP);
						}
					}
					case DOWN: {
						if(this.status == RobotStatus.RecordingPlan){
							this.currentPlan.add(Action.MOVE_DOWN);
							return Action.DO_NOTHING;
						}else{
							actionList.add(Action.MOVE_DOWN);
							return affirm(Action.MOVE_DOWN);
						}
					}
					case LEFT: {
						if(this.status == RobotStatus.RecordingPlan){
							this.currentPlan.add(Action.MOVE_LEFT);
							return Action.DO_NOTHING;
						}else{
							actionList.add(Action.MOVE_LEFT);
							return affirm(Action.MOVE_LEFT);
						}
					}
					case RIGHT: {
						if(this.status == RobotStatus.RecordingPlan){
							this.currentPlan.add(Action.MOVE_RIGHT);
							return Action.DO_NOTHING;
						}else{
							actionList.add(Action.MOVE_RIGHT);
							return affirm(Action.MOVE_RIGHT);
						}
					}
				}
			}
			if(action == DirectAction.RECORD){
				if(name.contains("begin")){
					if(this.status == RobotStatus.RecordingPlan){
						System.out.println("Cannot record plans inside other plans");
						return Action.DO_NOTHING;
					}
					System.out.println("I will begin recording your commands. Please enter them one line at a time and end with \"end record\"");
					this.currentPlan = new LinkedList<>();
					this.status = RobotStatus.RecordingPlan;
					return Action.DO_NOTHING;
				}else if(name.contains("end")){
					if(!(this.status == RobotStatus.RecordingPlan)){
						System.out.println("I have not been recording. Try \"begin record\" first to start a recording.");
						return Action.DO_NOTHING;
					}
					System.out.println("Please enter a name for the plan. Please note only the first word will be recorded.");
					Scanner planNameScanner = new Scanner(System.in);
					String planName = planNameScanner.next();
					this.allPlans.put(planName, this.currentPlan);
					System.out.println("I have saved the plan as \"" + planName + "\"");
					this.status = RobotStatus.NormalRuntime;
					return Action.DO_NOTHING;
				}
			}
			if(action == DirectAction.EXECUTE){
				if(this.status == RobotStatus.RecordingPlan){
					System.out.println("Cannot execute plans while recording.");
					return Action.DO_NOTHING;
				}
				Scanner planNameScanner = new Scanner(name);
				planNameScanner.next();
				planNameScanner.next();
				if(name.contains("symmetric")){
					System.out.println("This is a symmetric plan");
					planNameScanner.next();
					this.symmetricPlan = true;
				}else{
					symmetricPlan = false;
				}
				String planName = planNameScanner.next();
				if(!this.allPlans.containsKey(planName)){
					System.out.println("No plan with name \"" + planName + "\"");
					return Action.DO_NOTHING;
				}
				System.out.println("Starting execution of plan " + planName);
				this.currentPlan = this.allPlans.get(planName);
				this.status = RobotStatus.ExecutingPlan;
				return Action.DO_NOTHING;
			}
	    }
  	  sayUncertain();
  	  return Action.DO_NOTHING;
	}

	static private void sayIntent(String action){
		System.out.println(intentPhrases.get((int)Math.floor(Math.random() * intentPhrases.size())) + action);
	}

	static private void sayUncertain(){
		System.out.println(uncertainPhrases.get((int)Math.floor(Math.random() * uncertainPhrases.size())));
	}

	static private void sayAppreciate(){
		System.out.println(appreciationPhrases.get((int)Math.floor(Math.random() * appreciationPhrases.size())));
	}

	static private Action affirm(Action a){
		System.out.println(affirmativePhrases.get((int)Math.floor(Math.random() * affirmativePhrases.size())));
		return a;
	}

	static private Action processNegate(boolean assumed) {
		if (assumed) System.out.println("Never mind, I won't do that");
		else System.out.println("Alright, I won't do that.");
		return Action.DO_NOTHING;
	}

	private Action repeat(){
		Action last = this.actionList.get(this.actionList.size() - 1);
		if(this.status == RobotStatus.RecordingPlan){
			this.currentPlan.add(last);
			return Action.DO_NOTHING;
		}
		this.actionList.add(last);
		return affirm(last);
	}
	
	private Action undo(){
		Action last = this.actionList.get(this.actionList.size() - 1);
		this.actionList.remove(this.actionList.size() - 1);
		switch (last){
			case MOVE_UP: return affirm(Action.MOVE_DOWN);
			case MOVE_DOWN: return affirm(Action.MOVE_UP);
			case MOVE_LEFT: return affirm(Action.MOVE_RIGHT);
			case MOVE_RIGHT: return affirm(Action.MOVE_LEFT);
			case CLEAN: {
				System.out.println("Sorry, I cannot unclean a space");
				return Action.DO_NOTHING;
			}
		}
		return Action.DO_NOTHING;
	}

	static public Direction processDirection(String word){
		switch (word) {
			case "left": return Direction.LEFT;
			case "right": return Direction.RIGHT;
			case "up": return Direction.UP;
			case "down": return Direction.DOWN;
			default: return null;
		}
	}
	
	static public Action processUH(String word){
		System.out.println(word);
	    return Action.DO_NOTHING;
	}

	static public DirectAction processVerb(String word){
		switch (word) {
			case "clean": case "wash": case "wipe": case "scrub": case "cleanse": case "mop": return DirectAction.CLEAN;
			case "move": case "proceed": case "advance": case "step": case "jump": case "walk": case "go": return DirectAction.MOVE;
			case "stay": case "sit": case "wait": case "hold": return DirectAction.STAY;
			case "record": return DirectAction.RECORD;
			case "execute": return DirectAction.EXECUTE;
			default: return null;
		}
	}

	private enum DirectAction{
		CLEAN,
		MOVE,
		STAY,
		RECORD,
		EXECUTE
	}

	private enum Direction{
		UP,
		DOWN,
		LEFT,
		RIGHT
	}

	public List<Position> bfsM() {
		LinkedList<BetterPosition> targets = BetterPosition.convertFromPositions(this.targets);
		BetterPosition startPos = new BetterPosition(getPosRow(), getPosCol());
		LinkedList<BetterPosition> startPath = new LinkedList<>();
		startPath.add(startPos);
		LinkedList<EnvState> open = new LinkedList<>();
		LinkedList<EnvState> closed = new LinkedList<>();
		EnvState start = new EnvState(startPos, startPath, targets);
		open.add(start);
		openCount++;
		while(!open.isEmpty()){
			EnvState current = open.pop();
			TileStatus currentTile = env.getTileStatus(current.pos.getRow() - 1, current.pos.getCol());
			BetterPosition newPos;
			if(!(currentTile == TileStatus.IMPASSABLE)){
				newPos = new BetterPosition(current.pos.getRow() - 1, current.pos.getCol());
				if(addNewState(open, closed, current, currentTile, newPos)) return foundPath(open);
			}
			currentTile = env.getTileStatus(current.pos.getRow() + 1, current.pos.getCol());
			if(!(currentTile == TileStatus.IMPASSABLE)){
				newPos = new BetterPosition(current.pos.getRow() + 1, current.pos.getCol());
				if(addNewState(open, closed, current, currentTile, newPos)) return foundPath(open);
			}
			currentTile = env.getTileStatus(current.pos.getRow(), current.pos.getCol() - 1);
			if(!(currentTile == TileStatus.IMPASSABLE)){
				newPos = new BetterPosition(current.pos.getRow(), current.pos.getCol() - 1);
				if(addNewState(open, closed, current, currentTile, newPos)) return foundPath(open);
			}
			currentTile = env.getTileStatus(current.pos.getRow(), current.pos.getCol() + 1);
			if(!(currentTile == TileStatus.IMPASSABLE)){
				newPos = new BetterPosition(current.pos.getRow(), current.pos.getCol() + 1);
				if(addNewState(open, closed, current, currentTile, newPos)) return foundPath(open);
			}
			closed.add(current);
		}
		pathFound = false;
		return null;
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

	private class EnvState{
		BetterPosition pos;
		LinkedList<BetterPosition> path;
		LinkedList<BetterPosition> targets;
		int score;
		public EnvState(BetterPosition pos, LinkedList<BetterPosition> path, LinkedList<BetterPosition> targets){
			this.pos = pos;
			this.path = path;
			this.targets = targets;
		}

		public EnvState(BetterPosition pos, LinkedList<BetterPosition> path, LinkedList<BetterPosition> targets, int score){
			this.pos = pos;
			this.path = path;
			this.targets = targets;
		}

		@Override
		public boolean equals(Object o){
			if(o.getClass() != getClass()) return false;
			EnvState other = (EnvState) o;
			if(this.pos.getRow() != other.pos.getRow()) return false;
			if(this.pos.getCol() != other.pos.getCol()) return false;
			return this.targets.equals(other.targets);
		}

	}

	private boolean addNewState(LinkedList<EnvState> open, LinkedList<EnvState> closed, EnvState current, TileStatus currentTile, BetterPosition newPos){
		LinkedList<BetterPosition> newTargets = (LinkedList<BetterPosition>) current.targets.clone();
		LinkedList<BetterPosition> newPath = ((LinkedList<BetterPosition>) current.path.clone());
		newPath.add(newPos);
		if (currentTile == TileStatus.DIRTY && newTargets.contains(newPos)){
			newTargets.remove(newPos);
			newPath.add(newPos);
		}
		EnvState newState = new EnvState(newPos, newPath, newTargets);
		if(closed.contains(newState) || open.contains(newState)) {
			return false;
		}
		open.add(newState);
		openCount++;
		return newTargets.size() == 0;
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

	private LinkedList<Position> foundPath(LinkedList<EnvState> open){
		LinkedList<Position> finalPath = BetterPosition.toPositions(open.getLast().path);
		pathFound = true;
		pathLength = finalPath.size() - 1;
		this.path = finalPath;
		return finalPath;
	}


}