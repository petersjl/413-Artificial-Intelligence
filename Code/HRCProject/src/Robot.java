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
	      
	      List<IndexedWord> li = graph.getAllNodesByPartOfSpeechPattern("RB|UH");
	      for (IndexedWord w : li) {
	    	  if (w.tag().equals("RB")) {
	    		  return processRB(w.word());
	    	  }
	    	  if (w.tag().equals("UH")) {
	    		  return processUH(w.word());
	    	  }
	      }
    	  System.out.println("Cannot identify sentence structure.");
    	  return Action.DO_NOTHING;
	    }
  	  System.out.println("Empty sentence.");
  	  return Action.DO_NOTHING;
	}

	static public Action processRB(String word){
		System.out.println(word);
	    return Action.DO_NOTHING;
	}
	
	static public Action processUH(String word){
		System.out.println(word);
	    return Action.DO_NOTHING;
	}


}