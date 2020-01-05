package sma.agents.customs;

import java.util.ArrayList;

import org.jpl7.Query;

import env.jme.NewEnv;
import env.jme.Situation;
import jade.core.behaviours.FSMBehaviour;
import sma.actionsBehaviours.PrologBehavior;
import sma.actionsBehaviours.customs.MyAttackBehavior;
import sma.actionsBehaviours.customs.MyDecisionBehavior;
import sma.actionsBehaviours.customs.MyExploreBehavior;
import sma.agents.FinalAgent;
import weka.classifiers.meta.FilteredClassifier;
import weka.classifiers.trees.J48;
import weka.core.Instance;
import weka.core.Instances;
import weka.core.converters.ConverterUtils.DataSource;
import weka.filters.unsupervised.attribute.Remove;

public class CustomAgent extends FinalAgent {
	private static final long serialVersionUID = -8733339932256877217L;
	
    private final String DECISION = "D";  // Decision state
    private final String EXPLORE = "E";   // Exploration state
    private final String ATTACK = "A";    // Attack state
    private final String DEAD = "DEAD";

    // File where reasoning rules are
    public static final String PROLOG_FILE = "./ressources/prolog/duel/customAgentRequete.pl";

    private static Situation sit;
    private DataSource dataSource;
    private Instances instances;
    private FilteredClassifier classifier;

    /**
     * Setup the agent.
     */
    @Override
    protected void setup() {
    	//Need to be init
        offPoints = new ArrayList<>();
        defPoints = new ArrayList<>();
        super.setup();

        // Get situation
        sit = Situation.getCurrentSituation(this);

        PrologBehavior.sit = sit;

        // load prolog file
        try {
            String prolog = "consult('" + PROLOG_FILE + "')";
            if (!Query.hasSolution(prolog)) {
                System.err.println("Cannot open file " + prolog);
                System.exit(0);
            }
        } catch(Exception e) {
            System.err.println("Behaviour file for Prolog agent not found");
            System.exit(0);
        }

        // Load resources & Create classifier
        try {
            dataSource = new DataSource("./ressources/learningBase/lb_status.arff");
            //dataSource = new DataSource("./ressources/learningBase/lb_victory_defeat.arff");
            instances = dataSource.getDataSet();
            
            J48 tree = new J48();
            Remove rm = new Remove();
            rm.setAttributeIndices("5");
            rm.setAttributeIndices("6");

            classifier = new FilteredClassifier();
            classifier.setFilter(rm);
            classifier.setClassifier(tree);

            instances.setClassIndex(instances.numAttributes() - 1);
            classifier.buildClassifier(instances);
            
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(1);
        }
    }

    @Override
	protected void deploiment() {
        // Deploy agent
        final Object[] args = getArguments();
        deployAgent((NewEnv) args[0], true);
        final FSMBehaviour fsm = new FSMBehaviour();

        // Create FSM

        // Register states
        fsm.registerFirstState(new MyDecisionBehavior(this), DECISION);
        fsm.registerState(new MyExploreBehavior(this), EXPLORE);
        fsm.registerState(new MyAttackBehavior(this), ATTACK);

        // Register transitions
        fsm.registerDefaultTransition(EXPLORE, DECISION);
        fsm.registerTransition(DECISION, EXPLORE, MyDecisionBehavior.EXPLORE);
        fsm.registerDefaultTransition(DECISION, DECISION);
        fsm.registerDefaultTransition(ATTACK, DECISION);
        fsm.registerTransition(DECISION, ATTACK, MyDecisionBehavior.ATTACK);
        fsm.registerTransition(DECISION, DEAD, MyDecisionBehavior.DEAD);

        addBehaviour(fsm);
    }

    public Situation getAgentSituation() {
        sit = Situation.getCurrentSituation(this);
        PrologBehavior.sit = sit;
        return sit;
    }

    public String eval(Instance testInstance) throws Exception {
        double result = classifier.classifyInstance(testInstance);
        //return result == 1.0 ? "NOTINSIGHT" : "INSIGHT";
        return result == 1.0 ? "DEFEAT" : "VICTORY";
    }
    
    public String prologQuery(String behavior, ArrayList<Object> terms) {
		String query = behavior + "(";
		for (Object t: terms) {
			query += t + ",";
		}
		
		if(terms.size() > 0)
			return query.substring(0,query.length() - 1) + ")";
		else
			return query.substring(0,query.length()) + ")";
	}

    public Instances getInstances() {
        return instances;
    }

    public DataSource getDataSet() {
        return dataSource;
    }

    public static boolean enemyInSight() {
        return sit.enemyInSight;
    }
}
