package oop.ex6.variables;

/**
 * an object representing a variable, which has a name, value, type and a boolean field if the variable
 * is final or not
 */
public class Variable {
    /**
     * a string representing the variables name
     */
    private String name;
    /**
     * the variables type
     */
    private VariableFactory.Type type;
    /**
     * a string representing the variables value
     */
    private String value;
    /**
     * is the variable final
     */
    private boolean isFinal;

    /**
     * a constructor for a variable that has all elements
     *
     * @param name    - name of the variable
     * @param value   - value of the variable
     * @param isFinal - is the variable final or not
     * @param type    - type of the variable
     */
    public Variable(String name, String value, boolean isFinal, VariableFactory.Type type) {
        this.name = name;
        this.value = value;
        this.type = type;
        this.isFinal = isFinal;
    }

    /**
     * a constructor for a variable which value isn't assigned yet
     *
     * @param name    - name of the variable
     * @param isFinal - is the variable final or not
     * @param type    - type of the variable
     */
    public Variable(String name, boolean isFinal, VariableFactory.Type type) {
        this.name = name;
        this.type = type;
        this.isFinal = isFinal;
    }

    /**
     * a getter for the variable name
     *
     * @return - vars name
     */
    public String getName() {
        return this.name;
    }

    /**
     * @return - the vars type
     */
    public VariableFactory.Type getType() {
        return type;
    }

    /**
     * @return - the vars value;
     */
    public String getValue() {
        return this.value;
    }

    /**
     * @return - is the variable final or not
     */
    public boolean isFinal() {
        return this.isFinal;
    }

}
