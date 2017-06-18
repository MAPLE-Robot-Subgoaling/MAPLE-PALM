package amdp.planning;

import java.util.List;

import burlap.behavior.policy.Policy;
import burlap.behavior.singleagent.planning.Planner;
import burlap.mdp.core.Domain;
import burlap.mdp.core.action.ActionType;
import burlap.mdp.core.state.State;
import burlap.mdp.singleagent.SADomain;
import burlap.mdp.singleagent.model.SampleModel;
import burlap.statehashing.HashableStateFactory;

public class AMDPPlanner implements Planner{

	@Override
	public void solverInit(SADomain domain, double gamma, HashableStateFactory hashingFactory) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void resetSolver() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void setDomain(SADomain domain) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void setModel(SampleModel model) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public SampleModel getModel() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Domain getDomain() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void addActionType(ActionType a) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void setActionTypes(List<ActionType> actionTypes) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public List<ActionType> getActionTypes() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void setHashingFactory(HashableStateFactory hashingFactory) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public HashableStateFactory getHashingFactory() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public double getGamma() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public void setGamma(double gamma) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void setDebugCode(int code) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public int getDebugCode() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public void toggleDebugPrinting(boolean toggle) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public Policy planFromState(State initialState) {
		// TODO Auto-generated method stub
		return null;
	}

}
