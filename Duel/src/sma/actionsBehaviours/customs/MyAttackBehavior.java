package sma.actionsBehaviours.customs;

import com.jme3.math.Vector3f;
import env.jme.Situation;
import jade.core.Agent;
import jade.core.behaviours.OneShotBehaviour;

import java.util.ArrayList;

import org.jpl7.Query;
import sma.AbstractAgent;
import sma.agents.FinalAgent;
import sma.agents.customs.CustomAgent;

public class MyAttackBehavior extends OneShotBehaviour {
	private static final long serialVersionUID = 581439695745435133L;
	
	private Vector3f enemyPos;
    private String enemy;
    private Situation sit;
    
    private CustomAgent agent;

    public MyAttackBehavior(Agent a) {
        super(a);
        agent = (CustomAgent) ((FinalAgent)((AbstractAgent) a));
    }

    @Override
    public void action() {
        this.sit = agent.getAgentSituation();
        this.enemy = sit.enemy;
        enemyPos = agent.getEnemyLocation(enemy);

        boolean openFire = askForFirePermission(agent);

        //System.out.println(openFire);
        if (!openFire) return;

        agent.goTo(enemyPos);

        if (agent.isVisible(enemy, AbstractAgent.VISION_DISTANCE)){
            enemyPos = agent.getEnemyLocation(enemy);
            agent.lookAt(enemyPos);

            System.out.println("Enemy visible, FIRE !");
            agent.lastAction = Situation.SHOOT;
            agent.shoot(enemy);
        }
        
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private boolean askForFirePermission(CustomAgent a) {
    	ArrayList<Object> terms = new ArrayList<Object>();
		terms.add(sit.impactProba);
    	String query = a.prologQuery("toOpenFire", terms);
        return Query.hasSolution(query);
    }
}
