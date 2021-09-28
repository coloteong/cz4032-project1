public class ClassificationRule {
    private Rule rule;
    private int defaultClass;
    private int totalErrors;

    public ClassificationRule(Rule rule, int defaultClass, int totalErrors) {
        this.rule = rule;
        this.defaultClass = defaultClass;
        this.totalErrors = totalErrors;
    }

    public int getTotalError() {
        return totalErrors;
    }

    public int getDefaultClass() {
        return defaultClass;
    }

    public Rule getRule() {
        return rule;
    }
}