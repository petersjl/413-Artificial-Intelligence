import java.util.List;
import java.util.Properties;
import java.util.Scanner;

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

	private Properties props;
	private StanfordCoreNLP pipeline;
	
	private Scanner sc;
	
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
        annotation = new Annotation(name);
	    pipeline.annotate(annotation);
	    List<CoreMap> sentences = annotation.get(CoreAnnotations.SentencesAnnotation.class);
	    if (sentences != null && ! sentences.isEmpty()) {
	      CoreMap sentence = sentences.get(0);
	      SemanticGraph graph = sentence.get(SemanticGraphCoreAnnotations.CollapsedCCProcessedDependenciesAnnotation.class);
	      graph.prettyPrint();
	      
	      List<IndexedWord> li = graph.getAllNodesByPartOfSpeechPattern("RB|VB|RP|NN|UH|JJ");
		  Direction direction = null;
		  DirectAction action = null;
	      for (IndexedWord w : li) {
	    	  if (w.tag().equals("RB") || w.tag().equals("RP") || w.tag().equals("UH") || w.tag().equals("NN")) {
	    		  if(direction == null) direction = processRB(w.word());
	    	  }
			  if (w.tag().equals("VB") || w.tag().equals("NN") || w.tag().equals("JJ")) {
				  if(action == null) action = processVB(w.word());
			  }
	      }
		  if(action == DirectAction.CLEAN) return Action.CLEAN;
		  if(action == DirectAction.STAY) return Action.DO_NOTHING;
		  if(action == DirectAction.MOVE || action == null){
			  if(direction == null){
				  System.out.println("Cannot identify sentence structure.");
				  return Action.DO_NOTHING;
			  }
			  switch (direction){
				  case UP: return Action.MOVE_UP;
				  case DOWN: return Action.MOVE_DOWN;
				  case LEFT: return Action.MOVE_LEFT;
				  case RIGHT: return Action.MOVE_RIGHT;
			  }
		  }
	    }
  	  System.out.println("Empty sentence.");
  	  return Action.DO_NOTHING;
	}

	static public Direction processRB(String word){
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

	static public DirectAction processVB(String word){
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