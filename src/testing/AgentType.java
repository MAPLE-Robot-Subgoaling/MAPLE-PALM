package testing;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public enum AgentType {

    PALM_EXPERT("palmExpert", "PALM-Expert"),
    PALM_HIERGEN("palmHierGen", "PALM-HierGen"),
    PALM_EXPERT_NAV_GIVEN("palmExpertWithNavGiven", "PALM-Expert w/ Nav"),

    RMAXQ_EXPERT("rmaxqExpert", "RMAXQ-Expert"),
    RMAXQ_HIERGEN("rmaxqHierGen", "RMAXQ-HierGen"),

    KAPPA_EXPERT("kappaExpert", "κ-Expert"),
    KAPPA_HIERGEN("kappaHierGen", "κ-HierGen"),



    Q_LEARNING("qLearning", "QL"),

    ;

    private String type;
    private String plotterDisplayName;

    AgentType(String type, String plotterDisplayName) {
        this.type = type;
        this.plotterDisplayName = plotterDisplayName;
    }

    public String getType() {
        return type;
    }

    public String getPlotterDisplayName() {
        return plotterDisplayName;
    }

    public static List<String> getTypes() {
        return Arrays.stream(AgentType.values()).map(AgentType::getType).collect(Collectors.toList());
    }
}
