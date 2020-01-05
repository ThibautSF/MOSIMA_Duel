package sma.actionsBehaviours.customs;

import env.jme.Situation;
import jade.core.Agent;
import jade.core.behaviours.OneShotBehaviour;
import sma.AbstractAgent;
import sma.agents.FinalAgent;
import sma.agents.customs.CustomAgent;

import org.jpl7.Query;

import java.util.ArrayList;

public class MyDecisionBehavior extends OneShotBehaviour {
	private static final long serialVersionUID = -682034175416147451L;

	public static final float SAVE_CURRENT_PROBA = 0.0f;

    // What this behaviour returns
    public static final int EXPLORE = 0;
    public static final int HUNT = 1;
    public static final int ATTACK = 2;
    public static final int DEAD = 3;
    
    private CustomAgent agent;

    /**
     * Internal enum representing available actions.
     */
    private enum Actions {
        EXPLORE(MyDecisionBehavior.EXPLORE) {
            @Override
            public String getPrologQuery(CustomAgent agent) {
				ArrayList<Object> terms = new ArrayList<Object>();
				
                return agent.prologQuery("explore", terms);
            }
        },
        ATTACK(MyDecisionBehavior.ATTACK) {
            @Override
            public String getPrologQuery(CustomAgent agent) {
				ArrayList<Object> terms = new ArrayList<Object>();

                return agent.prologQuery("attack", terms);
            }
        };

        private final int returnCode;

        Actions(int returnCode) {
            this.returnCode = returnCode;
        }

        public int getReturnCode() {
            return returnCode;
        }

        public abstract String getPrologQuery(CustomAgent agent);
    }

    private int returnValue;
    private boolean first;
    private Situation sit;

    /**
     * Create a new DecisionBehaviour, executed by Agent a.
     * @param a the Agent
     */
    public MyDecisionBehavior(Agent a) {
        super(a);
        first = true;
        agent = (CustomAgent) ((FinalAgent)((AbstractAgent) a));
        sit = agent.getAgentSituation();
    }

    @Override
    public void action() {
        if (first) {
        	// The agent kills dummy before hitting the ground
            first = false;
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                // Ignore
            }
        }
        
        Situation currentSituation = agent.getAgentSituation();

        if (agent.dead || sit.victory) {
            returnValue = DEAD;
        } else {
            for (Actions action : Actions.values()) {
                currentSituation = agent.getAgentSituation();
                if (Query.hasSolution(action.getPrologQuery(agent))) {
                    returnValue = action.getReturnCode();
                }
            }
        }
        
        sit = currentSituation;
    }

    @Override
    public int onEnd() {
        return returnValue;
    }
}
