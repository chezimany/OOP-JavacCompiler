package oop.ex6.variables;

import java.util.ArrayList;
import java.util.regex.Matcher;

/**
 * class responsible for variable validation - checking type and values
 */
public class VariableValidation {

    /**
     * a message when trying to declare a variable with an existed var name
     */
    private static String USED_VAR_NAME = "declaration attempt with a used variable name";
    /**
     * a message when a final value does not have a value
     */
    private static String FINAL_NOT_ASSIGNED = "final variable is not initialized";
    /**
     * a message when trying to assign a value to method's parameters
     */
    private static String ASSIGNED_PARAMETERS = "assignment is not allowed to methods' parameters";

    /**
     * checks if a variable was already declared
     * @param name - name of the variable
     * @param variableDeclarations - list of existing variables in the program
     * @return the variable, null if it doesn't exist in the list
     */
    public static Variable isContain(String name, ArrayList<Variable> variableDeclarations) {
        for (Variable var : variableDeclarations) {
            if (var.getName().equals(name)) {
                return var;
            }
        }
        return null;
    }

    /**
     * checks if a variable declaration is legal
     *
     * @param name                 - name of the variable to check
     * @param value                - value to be assigned to the variable
     * @param isFinal              - is the variable going to be final
     * @param variableDeclarations - list other variables to check if the new variables name is included
     * @param isParam - is the variable a methods parameter
     * @return true if legal
     * @throws VariableException
     */
    public static boolean isCompatible(String name, String value, boolean isFinal,
                                       ArrayList<Variable> variableDeclarations, boolean isParam)
            throws VariableException {
        if (VariableValidation.isContain(name, variableDeclarations) != null) {
            throw new VariableException(USED_VAR_NAME);
        }
        if (isFinal && value == null && !isParam) {
            throw new VariableException(FINAL_NOT_ASSIGNED);
        }
        if (isParam && value != null) {
            throw new VariableException(ASSIGNED_PARAMETERS);
        }
        return true;
    }

    /**
     * checks if a given value matches a given type
     *
     * @param value - a given value to check
     * @param type  - type to check
     * @return true if the value fits the type
     */
    public static boolean checkValue(String value, VariableFactory.Type type) {
        Matcher m1 = VariableFactory.typeToPattern.get(type).matcher(value);
        return m1.matches();
    }

    /**
     * checks if a given variable's value can be assigned to a given type
     *
     * @param value     - the name of the variable which value is being used
     * @param type      - type to check
     * @param variables - list of variables, to check if the variable exists in
     * @return true if the assignment is legal
     */
    public static boolean checkVariableValue(String value, VariableFactory.Type type,
                                             ArrayList<Variable> variables) {
        Matcher m2 = VariableFactory.VAR_NAME_PATTERN.matcher(value);
        if (m2.matches()) {
            return isTypeLegal(value, type, variables);
        }
        return false;
    }

    /**
     * in case of an assignment of an existed variable to a new variable, checks if the types are compatible
     * with each other - a double can also be assigned with an int, and a boolean can
     * also be assigned with an int and a double.
     *
     * @param existedVar - an existed variable name in the file
     * @param type       - type of the new variable
     * @param variables  - list of existing variables
     * @return true if the assigned variable's type is legal
     */
    public static boolean isTypeLegal(String existedVar, VariableFactory.Type type,
                                      ArrayList<Variable> variables) {
        Variable existingVar = isContain(existedVar, variables);
        if (existingVar == null || existingVar.getValue() == null) { // trying to assign a variable to a
            // non-existing variable or uninitialized variable
            return false;
        }
        return existingVar.getType() == type || ((type == VariableFactory.Type.DOUBLE ||
                type == VariableFactory.Type.BOOLEAN) &&
                (existingVar.getType() == VariableFactory.Type.DOUBLE ||
                existingVar.getType() == VariableFactory.Type.INT)); // check if the types are compatible
    }

}
