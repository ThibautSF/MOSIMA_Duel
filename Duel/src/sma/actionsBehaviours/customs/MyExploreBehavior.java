package sma.actionsBehaviours.customs;

import com.jme3.math.Vector3f;
import env.jme.Situation;
import jade.core.Agent;
import jade.core.behaviours.SimpleBehaviour;
import sma.AbstractAgent;
import sma.agents.FinalAgent;
import sma.agents.customs.CustomAgent;
import weka.core.Instance;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class MyExploreBehavior extends SimpleBehaviour {
	private static final long serialVersionUID = 1580134233049206276L;
	
    public static final int RANDOM_PROBABILITY = 20;
    public static final int PRECISION = 8;
    public static final int MAX_DISTANCE = 20;

    private final Random random;
    private boolean isDone;
    private Vector3f computedTarget;
    private Vector3f startPosition;
    
    private static CustomAgent agent;

    /**
     * Create a new Explore behaviour, executed by the provided agent.
     * @param a the agent
     */
    public MyExploreBehavior(Agent a) {
        super(a);
		agent = (CustomAgent) ((FinalAgent)((AbstractAgent) a));
        random = new Random(System.currentTimeMillis());
        isDone = false;
    }

    @Override
    public void action() {
        agent.lastAction = "explore";

        if (computedTarget == null) {
            startPosition = new Vector3f(agent.getCurrentPosition());
            
            computedTarget = getBestNeighbour();
            if (computedTarget == null) {
                computedTarget = getRandomTarget();
            }
            agent.moveTo(computedTarget);
        }

        // The behaviour runs until target is reached or max distance
        isDone = computedTarget.distance(agent.getCurrentPosition()) <= PRECISION
                || startPosition.distance(agent.getCurrentPosition()) >= MAX_DISTANCE
                || agent.getAgentSituation().enemyInSight
        ;
    }

    @Override
    public boolean done() {
        boolean done = isDone;
        if (done) {
            isDone = false;
            computedTarget = null;
        }
        return done;
    }

    private Vector3f getRandomTarget() {
        ArrayList<Vector3f> points = agent.sphereCast(
                agent.getSpatial(),
                6, //AbstractAgent.NEIGHBORHOOD_DISTANCE,
                AbstractAgent.CLOSE_PRECISION,
                //AbstractAgent.VISION_ANGLE
                (float) (2*Math.PI)
        );
        return points.get(random.nextInt(points.size()));
    }

    private Vector3f getBestNeighbour() {
        ArrayList<Vector3f> points = agent.sphereCast(
                agent.getSpatial(),
                6, //AbstractAgent.NEIGHBORHOOD_DISTANCE,
                AbstractAgent.CLOSE_PRECISION,
                //AbstractAgent.VISION_ANGLE
                (float) (2*Math.PI)
        );
        List<Vector3f> candidates = evaluatePoints(points);
        if (candidates.size() != 0) {
            return candidates.get(new Random().nextInt(candidates.size()));
        }
        return null;
    }

    private List<Vector3f> evaluatePoints(List<Vector3f> list) {
        Vector3f current = agent.getCurrentPosition();
        List<Vector3f> points = new ArrayList<>();

        for (Vector3f point : list) {
			ArrayList<Object> terms = new ArrayList<Object>();
			terms.add(point.x);
			terms.add(point.y);
			terms.add(point.z);
        	String query = agent.prologQuery("goodPosition", terms);
        	
        	//System.out.println(query);
        	//workaround query -> term error
        	if (MyExploreBehavior.isAGoodPosition(point.x, point.y, point.z)) {
        		points.add(point);
        	}
        	
        	/*
            if (Query.hasSolution(query)) {
                points.add(point);
            }
            */
        }
        
        //Go back original position
        agent.teleport(current);
        return points;
    }
    
    public static boolean isAGoodPosition(float x, float y, float z) {
    	Vector3f position = new Vector3f(x, y, z);
    	agent.teleport(position);

        Situation sit = agent.getAgentSituation();
        Instance instance = new Instance(agent.getInstances().numAttributes());

        instance.setDataset(agent.getInstances());
        instance.setValue(0, (double) sit.averageAltitude);
        instance.setValue(1, (double) sit.maxAltitude);
        instance.setValue(2, (double) sit.currentAltitude);
        instance.setValue(3, (double) sit.fovValue);
        instance.setValue(4, sit.lastAction);
        instance.setValue(5, (double) sit.life);
        instance.setValue(6, "NOTINSIGHT"); // Not used
        //instance.setValue(6, "VICTORY"); // Not used

        try {
            return agent.eval(instance).equals("INSIGHT");
            //return agent.eval(instance).equals("VICTORY");
        } catch (Exception e) {
            return false;
        }
    }
}
