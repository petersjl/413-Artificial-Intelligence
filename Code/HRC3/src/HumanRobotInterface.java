import edu.stanford.nlp.ling.CoreAnnotations;
import edu.stanford.nlp.ling.IndexedWord;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.pipeline.StanfordCoreNLP;
import edu.stanford.nlp.semgraph.SemanticGraph;
import edu.stanford.nlp.semgraph.SemanticGraphCoreAnnotations;
import edu.stanford.nlp.sentiment.SentimentCoreAnnotations;
import edu.stanford.nlp.util.CoreMap;

import java.util.*;
import java.util.Properties;

public class HumanRobotInterface {
    ArrayList<Robot> robots;
    Environment env;
    private HRIStatus status = null;
    private LinkedList<Action> currentPlan;
    private Robot focusedRobot;

    private boolean symmetricPlan = false;

    private Properties props;
    private StanfordCoreNLP pipeline;

    private Action lastAction;
    private HashMap<String, Robot> robotMapping = new HashMap<>();
    private static final List<String> repeatWords = Arrays.asList("again", "more", "further");
    private static final List<String> undoWords = Arrays.asList("undo", "back");

    private static final List<String> intentPhrases = Arrays.asList(
            "I think you want me to ",
            "You might want me to ",
            "You're probably telling me to ",
            "That means I should probably ",
            "By that, I think I should ");

    private static final List<String> uncertainPhrases = Arrays.asList(
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

    private static final List<String> affirmativePhrases = Arrays.asList(
            "Sure thing",
            "I can do that",
            "Understood",
            "Right away",
            "I got it");

    private static final List<String> appreciationPhrases = Arrays.asList(
            "Just doin my job",
            "Oh, thanks",
            "Why thank you",
            "You're the first to appreciate my work",
            "Finally some praise for all this",
            "Someone who actually understands my skill"
    );

    private static final List<String> positivePhrases = Arrays.asList(
            "I appreciate your enthusiasm",
            "Your energy is infectious",
            "Thanks for the easy tone"
    );

    private static final List<String> negativePhrases = Arrays.asList(
            "I'm sorry you seem to be having trouble",
            "I'll try to make this easier on you in the future",
            "Maybe we can work on this in the future"
    );

    private static final List<String> commonSpellingErrors = Arrays.asList(
            "moce", "plese", "clewn", "lftr", "rift", "travl"
    );

    public HumanRobotInterface(ArrayList<Robot> robots, Environment environment){
        this.robots = robots;
        this.env = environment;

        props = new Properties();
        props.setProperty("annotators", "tokenize, ssplit, pos, lemma, ner, parse, sentiment");
        pipeline = new StanfordCoreNLP(props);
    }

    public boolean getDialog(){
        Annotation annotation;
        System.out.print("> ");
        Scanner sc = new Scanner(System.in);
        String name = sc.nextLine();
        if (name.equals("run")) return false;
        focusedRobot = null;
        name = processSentence(name);
        annotation = new Annotation(name);
        pipeline.annotate(annotation);
        List<CoreMap> sentences = annotation.get(CoreAnnotations.SentencesAnnotation.class);
        if (sentences != null && ! sentences.isEmpty()) {
            CoreMap sentence = sentences.get(0);
            SemanticGraph graph = sentence.get(SemanticGraphCoreAnnotations.CollapsedCCProcessedDependenciesAnnotation.class);
            //graph.prettyPrint();
            if (this.status != HRIStatus.RecordingPlan) {
                switch (sentence.get(SentimentCoreAnnotations.SentimentClass.class)) {
                    case "Positive":
                        System.out.println(positivePhrases.get((int) Math.floor(Math.random() * positivePhrases.size())));
                        break;
                    case "Negative":
                        System.out.println(negativePhrases.get((int) Math.floor(Math.random() * negativePhrases.size())));
                        break;
                }
            }

            List<IndexedWord> li = graph.getAllNodesByPartOfSpeechPattern("RB|VB|RP|NN|UH|JJ");
            Direction direction = null;
            DirectAction action = null;
            boolean repeat = false;
            boolean undo = false;
            boolean assumed = false;
            if(!(this.status == HRIStatus.RecordingPlan)){
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
            if(name.contains("plans")){
                if (name.contains("list")){
                    System.out.println("Plan list:");
                    System.out.println(Robot.allPlans.keySet());
                    return true;
                }
            }
            for (IndexedWord w : li) {
                if (w.word().equals("name")){
                    if(focusedRobot.name != "") {
                        System.out.println("My name is " + focusedRobot.name);
                    }else{
                        System.out.println("I don't have one yet. Enter one now to give me a name!\n(name)>");
                        Scanner s2 = new Scanner(System.in);
                        focusedRobot.name = s2.next();
                    }
                    return true;
                }
                if (w.word().equals("combine")){
                    Scanner combiner = new Scanner(name);
                    if(!combiner.hasNext()) return true;
                    combiner.next();
                    if(!combiner.hasNext()) return true;
                    String planA = combiner.next();
                    if(!combiner.hasNext()) return true;
                    combiner.next();
                    if(!combiner.hasNext()) return true;
                    String planB = combiner.next();
                    if (!Robot.allPlans.containsKey(planA)) {
                        System.out.println("There is no plan " + planA);
                        return true;
                    }
                    if (!Robot.allPlans.containsKey(planB)) {
                        System.out.println("There is no plan " + planB);
                        return true;
                    }
                    System.out.print("Enter a name for the new plan\n>");
                    String newName = "";
                    while(newName.equals("")) {
                        Scanner newNameScanner = new Scanner(System.in);
                        newName = newNameScanner.next();
                    }
                    LinkedList<Action> newPlan = new LinkedList<>();
                    Iterator<Action> planAIterator = Robot.allPlans.get(planA).iterator();
                    Iterator<Action> planBIterator = Robot.allPlans.get(planB).iterator();
                    while(planAIterator.hasNext()) newPlan.add(planAIterator.next());
                    while(planBIterator.hasNext()) newPlan.add(planBIterator.next());
                    Robot.allPlans.put(newName, newPlan);
                    return true;
                }
                if (w.word().equals("work") || w.word().equals("job")){
                    if(name.contains("good") || name.contains("nice")){
                        sayAppreciate();
                        focusedRobot = null;
                        return true;
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
                if(this.status == HRIStatus.RecordingPlan){
                    System.out.println("Cannot perform undo actions in a plan");
                    return true;
                }
                return undo();
            }
            if(repeat) return repeat();
            if(action == DirectAction.CLEAN) {
                LinkedList<Position> targets;
                if(name.contains("all")){
                    if(this.status == HRIStatus.RecordingPlan){
                        System.out.println("Cannot perform search actions in a plan");
                        return true;
                    }
                    focusedRobot.status = Robot.RobotStatus.NormalRuntime;
                    return affirm(Action.DO_NOTHING);
                }
                if(name.contains("list")){
                    if(this.status == HRIStatus.RecordingPlan){
                        System.out.println("Cannot perform search actions in a plan");
                        return true;
                    }
                    printName();
                    System.out.println("Please enter target coordinates as int pairs separated by a space, one per line, and end with a blank row");
                    targets = new LinkedList<>();
                    while(true){
                        System.out.print("> ");
                        Scanner lineReader = new Scanner(System.in);
                        String line = lineReader.nextLine();
                        if(line.equals("")) break;
                        Scanner coordReader = new Scanner(line);
                        if(!coordReader.hasNextInt()){
                            printName();
                            System.out.println("Please enter int pairs separated by a space");
                            continue;
                        }
                        int tempRow = coordReader.nextInt();
                        if(!coordReader.hasNextInt()){
                            printName();
                            System.out.println("Please enter int pairs separated by a space");
                            continue;
                        }
                        int tempCol = coordReader.nextInt();
                        if(tempRow >=0 && tempCol >= 0 && tempRow < env.getRows() && tempCol < env.getCols()){
                            if(env.getTileStatus(tempRow, tempCol) == TileStatus.DIRTY) targets.add(new Position(tempRow,tempCol));
                        }
                    }
                    if(targets.isEmpty()){
                        printName();
                        System.out.println("No targets supplied. I will wait for a new command.");
                        return true;
                    }
                    focusedRobot.policy = env.generatePolicy(targets);
                    focusedRobot.claimedTargets = targets;
                    printName();
                    System.out.println("I will find those targets");
                    return true;
                }
                if(name.contains("rectangle")){
                    if(this.status == HRIStatus.RecordingPlan){
                        System.out.println("Cannot perform search actions in a plan");
                        return true;
                    }
                    printName();
                    System.out.println("Please enter target coordinates (top left and bottom right) as int pairs separated by a space, one per line");
                    targets = new LinkedList<>();
                    Position topLeft;
                    while(true){
                        System.out.print("top left > ");
                        Scanner coordReader = new Scanner(System.in);
                        if(!coordReader.hasNextInt()){
                            printName();
                            System.out.println("Please enter an int pair separated by a comma");
                            continue;
                        }
                        int tempRow = coordReader.nextInt();
                        if(!coordReader.hasNextInt()){
                            printName();
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
                            printName();
                            System.out.println("Please enter an int pair separated by a comma");
                            continue;
                        }
                        int tempRow = coordReader.nextInt();
                        if(!coordReader.hasNextInt()){
                            printName();
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
                    focusedRobot.policy = env.generatePolicy(targets);
                    focusedRobot.claimedTargets = targets;
                    printName();
                    System.out.println("I will find those targets");
                    return true;
                }
                if(this.status == HRIStatus.RecordingPlan){
                    lastAction = Action.CLEAN;
                    this.currentPlan.add(Action.CLEAN);
                    return true;
                }
                lastAction = Action.CLEAN;
                return affirm(Action.CLEAN);
            }
            if(action == DirectAction.STAY) {
                if(hasNoFocus()) return true;
                focusedRobot.status = Robot.RobotStatus.Staying;
                return true;
            }
            if(action == DirectAction.MOVE || action == null){
                if(direction == null){
                    sayUncertain();
                    return true;
                }
                switch (direction){
                    case UP: {
                        if(this.status == HRIStatus.RecordingPlan){
                            lastAction = Action.MOVE_UP;
                            this.currentPlan.add(Action.MOVE_UP);
                            return true;

                        }else{
                            lastAction = Action.MOVE_UP;
                            focusedRobot.command = lastAction;
                            focusedRobot.status = Robot.RobotStatus.FollowingCommand;
                            return affirm(Action.MOVE_UP);
                        }
                    }
                    case DOWN: {
                        if(this.status == HRIStatus.RecordingPlan){
                            lastAction = Action.MOVE_DOWN;
                            this.currentPlan.add(Action.MOVE_DOWN);
                            return true;
                        }else{
                            lastAction = Action.MOVE_DOWN;
                            focusedRobot.command = Action.MOVE_DOWN;
                            focusedRobot.status = Robot.RobotStatus.FollowingCommand;
                            return affirm(Action.MOVE_DOWN);
                        }
                    }
                    case LEFT: {
                        if(this.status == HRIStatus.RecordingPlan){
                            lastAction = Action.MOVE_LEFT;
                            this.currentPlan.add(Action.MOVE_LEFT);
                            return true;
                        }else{
                            lastAction = Action.MOVE_LEFT;
                            focusedRobot.command = Action.MOVE_LEFT;
                            focusedRobot.status = Robot.RobotStatus.FollowingCommand;
                            return affirm(Action.MOVE_LEFT);
                        }
                    }
                    case RIGHT: {
                        if(this.status == HRIStatus.RecordingPlan){
                            lastAction = Action.MOVE_RIGHT;
                            this.currentPlan.add(Action.MOVE_RIGHT);
                            return true;
                        }else{
                            lastAction = Action.MOVE_RIGHT;
                            focusedRobot.command = Action.MOVE_RIGHT;
                            focusedRobot.status = Robot.RobotStatus.FollowingCommand;
                            return affirm(Action.MOVE_RIGHT);
                        }
                    }
                }
            }
            if(action == DirectAction.RECORD){
                if(name.contains("begin")){
                    if(this.status == HRIStatus.RecordingPlan){
                        System.out.println("Cannot record plans inside other plans");
                        return true;
                    }
                    System.out.println("We will begin recording your commands. Please enter them one line at a time and end with \"end record\"");
                    this.currentPlan = new LinkedList<>();
                    this.status = HRIStatus.RecordingPlan;
                    return true;
                }else if(name.contains("end")){
                    if(!(this.status == HRIStatus.RecordingPlan)){
                        System.out.println("We have not been recording. Try \"begin record\" first to start a recording.");
                        return true;
                    }
                    System.out.println("Please enter a name for the plan. Please note only the first word will be recorded.");
                    String planName = "";
                    while (planName.equals("")) {
                        Scanner planNameScanner = new Scanner(System.in);
                        planName = planNameScanner.next();
                    }
                    Robot.allPlans.put(planName, this.currentPlan);
                    System.out.println("I have saved the plan as \"" + planName + "\"");
                    this.status = HRIStatus.NormalRuntime;
                    return true;
                }
            }
            if(action == DirectAction.EXECUTE){
                if(this.status == HRIStatus.RecordingPlan){
                    System.out.println("Cannot execute plans while recording.");
                    return true;
                }
                Scanner planNameScanner = new Scanner(name);
                planNameScanner.next();
                planNameScanner.next();
                planNameScanner.next();
                if(name.contains("symmetric")){
                    System.out.println("This is a symmetric plan");
                    planNameScanner.next();
                    focusedRobot.symmetricPlan = true;
                }else{
                    symmetricPlan = false;
                }
                String planName = planNameScanner.next();
                if(!Robot.allPlans.containsKey(planName)){
                    System.out.println("No plan with name \"" + planName + "\"");
                    return true;
                }
                printName();
                System.out.println("I will execute plan " + planName);
                focusedRobot.status = Robot.RobotStatus.ExecutingPlan;
                focusedRobot.currentPlan = Robot.allPlans.get(planName);
                return true;
            }
        }
        sayUncertain();
        return true;
    }

    public void getNames() {
        System.out.println("Please enter the names of the robots that appear above.");
        if (env.hasPlayer()) {
            System.out.println(
                "Please note that the red one will be controlled by you manually, and cannot be spoken to; \n" +
                "Because of that, it is omitted below"
            );
        }

        for (Robot robot: robots){
            String robotColor = robot.colorName;
            if(robotColor.equalsIgnoreCase("red")) continue;
            System.out.print(robotColor + " Robot: -> ");
            String name = "";
            while(name.equals("")) {
                Scanner sc = new Scanner(System.in);
                name = sc.nextLine();
            }
            robotMapping.put(name, robot);
            robot.name = name;
        }
    }

    private boolean hasNoFocus(){
        if(focusedRobot == null){
            printWho();
            return false;
        }
        return true;
    }

    private void printName(){
        System.out.print("(" + focusedRobot.name + ") ");
    }

    private void printWho(){
        System.out.println("Please say who you are talking to and try again...");
    }

    private void sayIntent(String action){
        printName();
        System.out.println(intentPhrases.get((int)Math.floor(Math.random() * intentPhrases.size())) + action);
    }

    private void sayUncertain(){
        System.out.println(uncertainPhrases.get((int)Math.floor(Math.random() * uncertainPhrases.size())));
    }

    private void sayAppreciate(){
        printName();
        System.out.println(appreciationPhrases.get((int)Math.floor(Math.random() * appreciationPhrases.size())));
    }

    private boolean affirm(Action a){
        printName();
        System.out.println(affirmativePhrases.get((int)Math.floor(Math.random() * affirmativePhrases.size())));
        return true;
    }

    private Boolean processNegate(boolean assumed) {
        printName();
        if (assumed) System.out.println("Never mind, I won't do that");
        else System.out.println("Alright, I won't do that.");
        return true;
    }

    private Boolean repeat(){
        if(this.status == HRIStatus.RecordingPlan){
            this.currentPlan.add(lastAction);
            return true;
        }
        currentPlan.pollLast();
        return true;
    }

    private Boolean undo(){
        currentPlan.pollLast();
        return true;
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

    static public Boolean processUH(String word){
        System.out.println(word);
        return true;
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

    private String processSentence(String inputSentence) {
        inputSentence = inputSentence.toLowerCase(Locale.ROOT);

        for (String misspelling : commonSpellingErrors) {

            if (inputSentence.contains(misspelling)) {
                String correctSpelling = getCorrectSpelling(misspelling);
                inputSentence = inputSentence.replace(misspelling, correctSpelling);
            }
        }

        findFocusedRobot(inputSentence);

        return inputSentence;
    }

    private void findFocusedRobot(String inputSentence) {
        for (String word : inputSentence.split(" ")){
            if (robotMapping.containsKey(word)){
                focusedRobot = robotMapping.get(word);
            }
        }
    }

    private String getCorrectSpelling(String misspelling) {
        switch (misspelling) {
            case "moce":
                return "move";
            case "plese":
                return "please";
            case "clewn":
                return "clean";
            case "lftr":
                return "left";
            case "rift":
                return "right";
            case "travl":
                return "travel";
            default:
                return "";
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

    public enum HRIStatus{
        WalkingSearchPath,
        ExecutingPlan,
        AwaitingCoordinates,
        AwaitingPlanName,
        RecordingPlan,
        NormalRuntime
    }
}
