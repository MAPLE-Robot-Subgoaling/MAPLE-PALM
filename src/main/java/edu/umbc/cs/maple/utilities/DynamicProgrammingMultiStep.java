package edu.umbc.cs.maple.utilities;

import burlap.behavior.policy.EnumerablePolicy;
import burlap.behavior.policy.PolicyUtils;
import burlap.behavior.policy.support.ActionProb;
import burlap.behavior.singleagent.options.Option;
import burlap.behavior.singleagent.planning.stochastic.dpoperator.BellmanOperator;
import burlap.behavior.singleagent.planning.stochastic.dpoperator.DPOperator;
import burlap.behavior.valuefunction.ConstantValueFunction;
import burlap.behavior.valuefunction.QProvider;
import burlap.behavior.valuefunction.QValue;
import burlap.behavior.valuefunction.ValueFunction;
import burlap.mdp.core.action.Action;
import burlap.mdp.core.state.State;
import burlap.mdp.singleagent.SADomain;
import burlap.mdp.singleagent.model.FullModel;
import burlap.mdp.singleagent.model.SampleModel;
import burlap.mdp.singleagent.model.TransitionProb;
import burlap.statehashing.HashableState;
import burlap.statehashing.HashableStateFactory;
import org.yaml.snakeyaml.Yaml;

import java.io.*;
import java.util.*;

public class DynamicProgrammingMultiStep extends MultiStepMDPSolver implements ValueFunction, QProvider {

    protected Map<HashableState, Double> valueFunction;
    protected ValueFunction valueInitializer = new ConstantValueFunction();
    protected DPOperator operator = new BellmanOperator();

    public DynamicProgrammingMultiStep() {

    }

    public void DPPInit(SADomain domain, HashableStateFactory hashingFactory, DiscountProvider discountProvider) {
        this.solverInit(domain, hashingFactory, discountProvider);
        this.valueFunction = new HashMap();
    }

    public SampleModel getModel() {
        return this.model;
    }

    public void resetSolver() {
        this.valueFunction.clear();
    }

    public void setValueFunctionInitialization(ValueFunction vfInit) {
        this.valueInitializer = vfInit;
    }

    public ValueFunction getValueFunctionInitialization() {
        return this.valueInitializer;
    }

    public DPOperator getOperator() {
        return this.operator;
    }

    public void setOperator(DPOperator operator) {
        this.operator = operator;
    }

    public boolean hasComputedValueFor(State s) {
        HashableState sh = this.hashingFactory.hashState(s);
        return this.valueFunction.containsKey(sh);
    }

    public double value(State s) {
        if (this.model.terminal(s)) {
            return 0.0D;
        } else {
            HashableState sh = this.hashingFactory.hashState(s);
            return this.value(sh);
        }
    }

    public double value(HashableState sh) {
        if (this.model.terminal(sh.s())) {
            return 0.0D;
        } else {
            Double V = this.valueFunction.get(sh);
            double v = V == null ? this.getDefaultValue(sh.s()) : V.doubleValue();
            return v;
        }
    }

    public List<QValue> qValues(State s) {
        List<Action> gas = this.applicableActions(s);
        List<QValue> qs = new ArrayList(gas.size());
        Iterator var4 = gas.iterator();

        while(var4.hasNext()) {
            Action ga = (Action)var4.next();
            QValue q = new QValue(s, ga, this.qValue(s, ga));
            qs.add(q);
        }

        return qs;
    }

    public double qValue(State s, Action a) {
        double dq = this.computeQ(s, a);
        return dq;
    }

    public List<State> getAllStates() {
        List<State> result = new ArrayList(this.valueFunction.size());
        Set<HashableState> shs = this.valueFunction.keySet();
        Iterator var3 = shs.iterator();

        while(var3.hasNext()) {
            HashableState sh = (HashableState)var3.next();
            result.add(sh.s());
        }

        return result;
    }

    public double performBellmanUpdateOn(State s) {
        return this.performBellmanUpdateOn(this.stateHash(s));
    }

    public double performFixedPolicyBellmanUpdateOn(State s, EnumerablePolicy p) {
        return this.performFixedPolicyBellmanUpdateOn(this.stateHash(s), p);
    }

    public void writeValueTable(String path) {
        Yaml yaml = new Yaml();

        try {
            yaml.dump(this.valueFunction, new BufferedWriter(new FileWriter(path)));
        } catch (IOException var4) {
            var4.printStackTrace();
        }

    }

    public void loadValueTable(String path) {
        Yaml yaml = new Yaml();

        try {
            this.valueFunction = (Map)yaml.load(new FileReader(path));
        } catch (FileNotFoundException var4) {
            var4.printStackTrace();
        }

    }

    protected double performBellmanUpdateOn(HashableState sh) {
        if (this.model.terminal(sh.s())) {
            this.valueFunction.put(sh, 0.0D);
            return 0.0D;
        } else {
            List<Action> gas = this.applicableActions(sh.s());
            double[] qs = new double[gas.size()];
            int i = 0;

            for(Iterator var5 = gas.iterator(); var5.hasNext(); ++i) {
                Action ga = (Action)var5.next();
                double q = this.computeQ(sh.s(), ga);
                qs[i] = q;
            }

            double nv = this.operator.apply(qs);
            this.valueFunction.put(sh, nv);
            return nv;
        }
    }

    protected double performFixedPolicyBellmanUpdateOn(HashableState sh, EnumerablePolicy p) {
        if (this.model.terminal(sh.s())) {
            this.valueFunction.put(sh, 0.0D);
            return 0.0D;
        } else {
            double weightedQ = 0.0D;
            List<ActionProb> policyDistribution = p.policyDistribution(sh.s());
            List<Action> gas = this.applicableActions(sh.s());
            Iterator var7 = gas.iterator();

            while(var7.hasNext()) {
                Action ga = (Action)var7.next();
                double policyProb = PolicyUtils.actionProbGivenDistribution(ga, policyDistribution);
                if (policyProb != 0.0D) {
                    double q = this.computeQ(sh.s(), ga);
                    weightedQ += policyProb * q;
                }
            }

            this.valueFunction.put(sh, weightedQ);
            return weightedQ;
        }
    }

    protected double computeQ(State s, Action ga) {
        double q = 0.0D;
        List<TransitionProb> tps = ((FullModel)this.model).transitions(s, ga);
        Iterator var6;
        TransitionProb tp;
        double vp;
        double discount;
        double r;
        if (ga instanceof Option) {
            q += ((TransitionProb)tps.get(0)).eo.r;

            for(var6 = tps.iterator(); var6.hasNext(); q += tp.p * vp) {
                tp = (TransitionProb)var6.next();
                vp = this.value(tp.eo.op);
            }
        } else {
            for(var6 = tps.iterator(); var6.hasNext(); q += tp.p * (r + discount * vp)) {
                tp = (TransitionProb)var6.next();
                vp = this.value(tp.eo.op);
                discount = discountProvider.yield(tp.eo.o, tp.eo.a, tp.eo.op, false);
                double rewardDiscount = discountProvider.yield(tp.eo.o, tp.eo.a, tp.eo.op, true);
                r = tp.eo.r * rewardDiscount;
//                r = tp.eo.r;
            }
        }

        return q;
    }

    protected double getDefaultValue(State s) {
        return this.valueInitializer.value(s);
    }
}
