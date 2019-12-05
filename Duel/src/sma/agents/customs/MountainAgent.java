package sma.agents.customs;
import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.regex.Matcher;

import com.jme3.math.Vector3f;
import com.jme3.renderer.Camera;

import env.jme.Environment;
import env.jme.NewEnv;
import jade.core.behaviours.Behaviour;
import sma.AbstractAgent;
import sma.InterestPoint;
import sma.actionsBehaviours.DumbBehavior;
import sma.actionsBehaviours.ExploreBehavior;
import sma.actionsBehaviours.HuntBehavior;
import sma.actionsBehaviours.PrologBehavior;
import sma.actionsBehaviours.TempSphereCast;
import sma.actionsBehaviours.customs.MountainPrologBehavior;
import sma.agents.FinalAgent;

public class MountainAgent extends FinalAgent {

	
	private static final long serialVersionUID = 5215165765928961044L;
	
	public static final long PERIOD = 1000;
	
	public enum MoveMode {
		
		NORMAL,
		LEGAL;
		
	}
	
	public boolean friendorFoe;// ?
	
	public ArrayList<InterestPoint> offPoints;
	public ArrayList<InterestPoint> defPoints;
	
	public ExploreBehavior explore;
	public HuntBehavior hunt;
	
	public boolean useProlog;
	
	public int life;
	public long lastHit;
	public boolean dead;
	
	public String lastAction = "idle";
	
	MoveMode mode = MoveMode.NORMAL;
	
	public Behaviour currentBehavior;
	
	protected void setup(){
		super.setup();
		
		
		
		deploiment();
		
		offPoints = new ArrayList<>();
		defPoints = new ArrayList<>();
		
		this.life = AbstractAgent.MAX_LIFE;
		this.dead = false;
		this.lastHit = 0;
		
		
		addToAgents(this);
		
		currentBehavior = null;
		
		
		
		
		teleport(getRandomPosition());
		
	}
	
	public void goTo(Vector3f target){
		if (mode == MoveMode.NORMAL){
			if (getDestination() != null && getDestination().equals(target)){
				return; 
			}
			moveTo(target);
		}else{
			moveTo(target); 
		}
	}
	
	public void lookAt(Vector3f target){
		if (mode == MoveMode.NORMAL){
			((Camera)getSpatial().getUserData("cam")).lookAt(target, Vector3f.UNIT_Y); 
		}else{
			((Camera)getSpatial().getUserData("cam")).lookAt(target, Vector3f.UNIT_Y);
		}
	}
	
	protected void deploiment(){
		final Object[] args = getArguments();
		if(args[0]!=null && args[1]!=null){
			
			
			useProlog = ((boolean)args[1]);
			
			if(useProlog) {
				addBehaviour(new MountainPrologBehavior(this,PERIOD));
			} else {
				addBehaviour(new DumbBehavior(this, PERIOD));
			}
			
			deployAgent((NewEnv) args[0], useProlog);
			
			System.out.println("Agent "+getLocalName()+" deployed !");
			
			
		}else{
			System.err.println("Malfunction during parameter's loading of agent"+ this.getClass().getName());
			System.exit(-1);
		}
	}
	
	public void saveCSV(String ressourceFolderPath, String fileName, String content) {
		//In case the user write wrong separator
		ressourceFolderPath = ressourceFolderPath.replaceAll("(\\\\+|/+)", Matcher.quoteReplacement(File.separator));
		if(!(ressourceFolderPath.lastIndexOf(File.separator)==ressourceFolderPath.length()-1)) ressourceFolderPath+=File.separator;
		
		try{
		    PrintWriter writer = new PrintWriter(ressourceFolderPath+fileName+".csv", "UTF-8");
		    writer.println(content);
		    writer.close();
		    System.out.println("Execution result saved in /ressources/simus/");
		} catch (IOException e) {
		  System.out.println(e);
		  System.out.println("Experiment saving failed");
		}
		
	}
}
