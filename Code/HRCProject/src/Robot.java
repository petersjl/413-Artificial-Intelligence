import java.util.*;
import java.util.Properties;

import edu.stanford.nlp.ling.CoreAnnotations;
import edu.stanford.nlp.ling.IndexedWord;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.pipeline.StanfordCoreNLP;
import edu.stanford.nlp.semgraph.SemanticGraph;
import edu.stanford.nlp.semgraph.SemanticGraphCoreAnnotations;
import edu.stanford.nlp.semgraph.SemanticGraphCoreAnnotations.CollapsedCCProcessedDependenciesAnnotation;
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
	
	/**
	    Initializes a Robot on a specific tile in the environment. 
	*/

	
	public Robot (Environment env, int posRow, int posCol) {
		this.env = env;
		this.posRow = posRow;
		this.posCol = posCol;
		
	    props = new Properties();
	    props.setProperty("annotators", "tokenize, ssplit, pos, lemma, ner, parse");
	    pipeline = new StanfordCoreNLP(props);

		actionList = new ArrayList<>();
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
	    Annotation annotation;
	    System.out.print("> ");
	    sc = new Scanner(System.in); 
        String name = sc.nextLine(); 
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
	      
	      List<IndexedWord> li = graph.getAllNodesByPartOfSpeechPattern("RB|VB|RP|NN|UH|JJ");
		  Direction direction = null;
		  DirectAction action = null;
		  boolean repeat = false;
		  boolean undo = false;
		  boolean assumed = false;
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
		  if(undo) return undo();
		  if(repeat) return repeat();
		  if(action == DirectAction.CLEAN) {
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
					  actionList.add(Action.MOVE_UP);
					  return affirm(Action.MOVE_UP);
				  }
				  case DOWN: {
					  actionList.add(Action.MOVE_DOWN);
					  return affirm(Action.MOVE_DOWN);
				  }
				  case LEFT: {
					  actionList.add(Action.MOVE_LEFT);
					  return affirm(Action.MOVE_LEFT);
				  }
				  case RIGHT: {
					  actionList.add(Action.MOVE_RIGHT);
					  return affirm(Action.MOVE_RIGHT);
				  }
			  }
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
			default: return null;
		}
	}

	private enum DirectAction{
		CLEAN,
		MOVE,
		STAY
	}

	private enum Direction{
		UP,
		DOWN,
		LEFT,
		RIGHT
	}


}