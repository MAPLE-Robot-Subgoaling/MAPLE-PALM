//package edu.umbc.cs.maple.cleanup.hierarchies.tasks.alt;
//
//import burlap.mdp.core.oo.propositional.PropositionalFunction;
//import burlap.mdp.core.oo.state.OOState;
//import burlap.mdp.core.oo.state.ObjectInstance;
//import edu.umbc.cs.maple.cleanup.state.CleanupDoor;
//import edu.umbc.cs.maple.cleanup.state.CleanupState;
//
//public class AgentFromDoorGoalPF extends PropositionalFunction {
//
//    public AgentFromDoorGoalPF(){
//        super("AgentFromDoorGoalPF", new String[]{});
//    }
//
//    public AgentFromDoorGoalPF(String name, String[] parameterClasses) {
//        super(name, parameterClasses);
//    }
//
//    @Override
//    public boolean isTrue(OOState s, String[] params) {
//        CleanupState state = (CleanupState) s;
//        return isTrue(state, params);
//    }
//
//    public static boolean isTrue(CleanupState state, String[] params) {
//        String doorName = params[0];
//        ObjectInstance agent = state.getAgent();
//        ObjectInstance door = state.object(doorName);
//        if (door == null) {
//            return false;
//        }
//        return state.isObjectInDoor(agent, (CleanupDoor) door);
//    }
//
//}