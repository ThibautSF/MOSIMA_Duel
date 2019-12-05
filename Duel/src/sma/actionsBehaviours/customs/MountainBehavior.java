package sma.actionsBehaviours.customs;

import java.util.ArrayList;
import java.util.Random;

import org.jpl7.Query;

import com.jme3.math.Quaternion;
import com.jme3.math.Vector3f;
import com.jme3.renderer.Camera;

import env.jme.Situation;
import jade.core.Agent;
import jade.core.behaviours.TickerBehaviour;
import sma.AbstractAgent;
import sma.InterestPoint;
import sma.InterestPoint.Type;
import sma.actionsBehaviours.LegalActions.LegalAction;
import sma.agents.FinalAgent;
import sma.agents.FinalAgent.MoveMode;
import weka.core.Debug;

public class MountainBehavior extends TickerBehaviour {

	
	private static final long serialVersionUID = 4958939169231338495L;
	
	public static final float RANDOM_MAX_DIST = 10f;
	public static final int RANDOM_REFRESH = 20;
	
	public static final float VISION_ANGLE = 360f;
	public static final float VISION_DISTANCE = 350f;
	public static final float CAST_PRECISION = 2f;
	
	
	
	public static boolean prlNextOffend;
	
	
	FinalAgent agent;
	
	private Vector3f target;
	private Type targetType;
	
	private long randDate;
	
	long time;
	
	public MountainBehavior(Agent a, long period) {
		super(a, period);
		agent = (FinalAgent)((AbstractAgent)a); // I know, i know ...
		target = null;
		randDate = 0;
		time = System.currentTimeMillis();
		prlNextOffend = true;
	}

	
	protected void onTick(){
		if (!setTarget()){
			randomMove();
			return;
		}
		
		agent.moveTo(target);
	}
	
	void addInterestPoint(){
		if(targetType == Type.Offensive){
			agent.offPoints.add(new InterestPoint(Type.Offensive,agent));
		}else{
			agent.defPoints.add(new InterestPoint(Type.Defensive,agent));
		}
	}
	
	boolean setTarget() {
		ArrayList<Vector3f> aroundPoints = agent.sphereCast(agent.getSpatial(), AbstractAgent.VISION_DISTANCE, AbstractAgent.FAR_PRECISION, VISION_ANGLE);
		
		if(target != null)
			aroundPoints.add(0, target);
		
		Vector3f heighestPoint = getHighest(aroundPoints);
		
		target = heighestPoint;
		
		if(target != null) {
			//agent.goTo(target);
			//targetType = t;
			agent.lastAction = (targetType==Type.Offensive)?Situation.EXPLORE_OFF:Situation.EXPLORE_DEF;
		}
		
		return target != null;
	}
	
	Vector3f getHighest(ArrayList<Vector3f> points){
		float maxHeight = -256;
		Vector3f best = null;
		
		for(Vector3f v3 : points){
			if (v3.getY() > maxHeight){
				best = v3;
				maxHeight = v3.getY();
			}
		}
		return best;
	}
	
	void randomMove(){
		long time = System.currentTimeMillis();
		if(time - randDate > RANDOM_REFRESH * getPeriod()){
			agent.randomMove(); // Should be something in the neighborhound of the agent, and not some random point in the all map
			randDate = time;
			//agent.getEnvironement().drawDebug(agent.getCurrentPosition(), agent.getDestination());
		}
	}
}























