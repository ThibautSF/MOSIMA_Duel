package sma.actionsBehaviours.customs;

import org.jpl7.Query;
import org.lwjgl.Sys;

import com.jme3.math.Vector3f;

import dataStructures.tuple.Tuple2;
import env.jme.NewEnv;
import env.jme.Situation;
import jade.core.Agent;
import jade.core.behaviours.Behaviour;
import jade.core.behaviours.TickerBehaviour;
import sma.AbstractAgent;
import sma.InterestPoint;
import sma.actionsBehaviours.*;
import sma.agents.FinalAgent;
import sma.agents.customs.MountainAgent;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

public class MountainPrologBehavior extends TickerBehaviour {
	private static final long serialVersionUID = 5739600674796316846L;
	private static final int HEIGHT_EXPLORE_RATE = 80;
	private static final int EXPLORE_IDLE_MIN = 5; //Minimal time (in seconds) to stay in the same exploration behavior

	public static FinalAgent agent;
	public static Class<?> exploreBehavior;
	public static long dateLastChooseExploration = 0;
	public static Class<?> nextBehavior;
	
	public static final Class<?> DEFAULT_EXPLORE_BEHAVIOR = MountainBehavior.class;

	public static Situation sit;
	private int lastLife = -1;


	public MountainPrologBehavior(Agent a, long period) {
		super(a, period);
		agent = (FinalAgent)((AbstractAgent) a);
		exploreBehavior = DEFAULT_EXPLORE_BEHAVIOR;
	}



	@Override
	protected void onTick() {
		sit = Situation.getCurrentSituation(agent);
		if (sit.victory) {
			//Victory
			System.out.println("victory");
		} else if (sit.life <= 0) {
			//Loose
			System.out.println("i loose");
			//NOT WORKING
		} else {
			
			if (sit.enemyInSight) {
				System.out.println("I see " + sit.enemy);
				agent.saveCSV("ressources/learningBase/vision/", "ennemy_vision_historic", sit.toCSVFile());
			}
			
			if(lastLife > sit.life) {
				System.out.println("Was shooted last tick");
				agent.saveCSV("ressources/learningBase/shoot/", "shoot_recieved_historic", sit.toCSVFile());
			}
			lastLife = sit.life;
			
			try {
				String prolog = "consult('./ressources/prolog/duel/mountainAgentRequete.pl')";
	
				if (!Query.hasSolution(prolog)) {
					System.out.println("Cannot open file " + prolog);
				}
				else {
					
					List<String> behavior = Arrays.asList("explore", "hunt", "attack");
					ArrayList<Object> terms = new ArrayList<Object>();
	
					for (String b : behavior) {
						terms.clear();
						// Get parameters 
						if (b.equals("explore")) {
							terms.add(sit.timeSinceLastShot);
							int time = (int) Math.max(0, Math.min(Integer.MAX_VALUE,(System.currentTimeMillis() - dateLastChooseExploration)));
							//System.out.println(time);
							terms.add(time);
							terms.add(EXPLORE_IDLE_MIN);
							/*
							terms.add(((ExploreBehavior.prlNextOffend)?sit.offSize:sit.defSize ));
							terms.add(InterestPoint.INFLUENCE_ZONE);
							terms.add(NewEnv.MAX_DISTANCE);
							*/
						}
						else if (b.equals("hunt")) {
							terms.add(sit.life);
							terms.add(sit.timeSinceLastShot);
							terms.add(sit.offSize);
							terms.add(sit.defSize);
							terms.add(InterestPoint.INFLUENCE_ZONE);
							terms.add(NewEnv.MAX_DISTANCE);
							terms.add(sit.enemyInSight);
						}else if(b.equals("attack")){
							//terms.add(sit.life);
							terms.add(sit.enemyInSight);
							//terms.add(sit.impactProba);
						}
						else { // RETREAT
							terms.add(sit.life);
							terms.add(sit.timeSinceLastShot);
						}
	
						String query = prologQuery(b, terms);
						//System.out.println(nextBehavior);
						if (Query.hasSolution(query)) {
							//System.out.println("has solution");
							setNextBehavior();
	
						}
					}
				}
			}catch(Exception e) {
				System.err.println("Behaviour file for Prolog agent not found");
				System.exit(0);
			}
		}
	}



	public void setNextBehavior(){

		if(agent.currentBehavior != null && nextBehavior == agent.currentBehavior.getClass()){
			return;
		}
		if (agent.currentBehavior != null){
			agent.removeBehaviour(agent.currentBehavior);
		}

		if (nextBehavior == MyExploreBehavior.class){
			ExploreBehavior ex = new ExploreBehavior(agent, FinalAgent.PERIOD);
			agent.addBehaviour(ex);
			agent.currentBehavior = ex;

		}else if (nextBehavior == MountainBehavior.class){
			MountainBehavior m = new MountainBehavior(agent, FinalAgent.PERIOD);
			agent.addBehaviour(m);
			agent.currentBehavior = m;

		}else if(nextBehavior == HuntBehavior.class){
			HuntBehavior h = new HuntBehavior(agent, FinalAgent.PERIOD);
			agent.currentBehavior = h;
			agent.addBehaviour(h);

		}else if(nextBehavior == Attack.class){

			Attack a = new Attack(agent, FinalAgent.PERIOD, sit.enemy);
			agent.currentBehavior = a;
			agent.addBehaviour(a);

		}


	}


	public String prologQuery(String behavior, ArrayList<Object> terms) {
		String query = behavior + "(";
		for (Object t: terms) {
			query += t + ",";
		}
		return query.substring(0,query.length() - 1) + ")";
	}
	
	public static boolean heightExplore() {
		Random r = new Random();
		int x=r.nextInt(100);
		System.out.println("MountainAgent : Do I explore ?; succesRate = "+HEIGHT_EXPLORE_RATE+"; v= "+x);
		return (x<HEIGHT_EXPLORE_RATE) ? true: false;
	}

	public static void executeExplore() {
		System.out.println("explore");
		nextBehavior = MyExploreBehavior.class;
		dateLastChooseExploration = System.currentTimeMillis();
		exploreBehavior = nextBehavior;
	}
	
	public static void executeMountain() {
		System.out.println("mountain");
		nextBehavior = MountainBehavior.class;
		dateLastChooseExploration = System.currentTimeMillis();
		exploreBehavior = nextBehavior;
	}
	
	public static void executeLastExplore() {
		//System.out.println("Execute last " + exploreBehavior);
		if (agent.currentBehavior == null) {
			if (exploreBehavior == null)
				nextBehavior = DEFAULT_EXPLORE_BEHAVIOR;
			else
				nextBehavior = exploreBehavior;
		}
	}

	public static void executeHunt() {
		System.out.println("hunt");
		nextBehavior = HuntBehavior.class;
	}

	public static void executeAttack() {
		System.out.println("attack");
		System.out.println(sit.enemyInSight);
		System.out.println(sit.enemy);
		nextBehavior = Attack.class;
	}


	public static void executeRetreat() {
		//System.out.println("retreat");
		//nextBehavior = RetreatBehavior.class;
	}
}