package oop.ex6.methods;

import oop.ex6.variables.Variable;

import java.util.ArrayList;

/**
 * class representing a method object that has a name, list of parameters and variables, and a start
 * row of the method in the file
 */
public class Method {
    /**
     * methods name
     */
    private String name;
    /**
     * row of the file where the method is declared
     */
    private int startRow;
    /**
     * list of methods parameters
     */
    private ArrayList<Variable> parameters;
    /**
     * list of method variables
     */
    private ArrayList<Variable> methodVariables = new ArrayList<>();

    public Method(String name, ArrayList<Variable> parameters, int startRow) {
        this.name = name;
        this.parameters = parameters;
        methodVariables.addAll(parameters);
        this.startRow = startRow;
    }

    /**
     * @return - methods name
     */
    public String getName() {
        return this.name;
    }

    /**
     * @return - methods parameters
     */
    public ArrayList<Variable> getParameters() {
        return this.parameters;
    }

    /**
     * @return methods variables
     */
    public ArrayList<Variable> getMethodVariables() {
        return this.methodVariables;
    }

    /**
     * @return - the row where the method starts
     */
    public int getStartRow() {
        return startRow;
    }


}
