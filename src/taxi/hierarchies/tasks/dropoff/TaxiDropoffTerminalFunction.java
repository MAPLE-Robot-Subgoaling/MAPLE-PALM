//package taxi.hierarchies.tasks.dropoff;
//
//import burlap.mdp.core.TerminalFunction;
//import burlap.mdp.core.state.State;
//import taxi.hierarchies.tasks.dropoff.state.TaxiDropoffState;
//
//public class TaxiDropoffTerminalFunction implements TerminalFunction {
//	@Override
//	public boolean isTerminal(State s) {
//		TaxiDropoffState state = (TaxiDropoffState) s;
//		for(String passenger : state.getPassengers()) {
//			String pass_loc = (String)state.getPassengerAtt(passenger, TaxiDropoffDomain.ATT_LOCATION);
//			if(!pass_loc.equals(TaxiDropoffDomain.NOT_IN_TAXI))
//				return false;
//		}
//		return true;
//	}
//
//}
