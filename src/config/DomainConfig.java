package config;

import burlap.mdp.core.state.State;

public abstract class DomainConfig {
    public String state;
    public abstract State generateState();
}
