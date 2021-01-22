package oop.ex6.variables;

import java.util.ArrayList;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * a factory for variables
 */
public class VariableFactory {
    /**
     * a string representing legal variable types
     */
    public static String VAR_TYPES = "\\s*(final\\s+)?((int|double|String|boolean|char)\\s+)(.+);\\s*";
    /**
     * a string representing legal declaration
     */
    private static String DECLARATION = "\\s*([a-zA-Z]\\w*|_\\w+)\\s*(=\\s*(.+))?\\s*";
    /**
     * a string representing legal assignment
     */
    private static String ASSIGNMENT = "\\s*([a-zA-Z]\\w*|_\\w+)\\s*(=)\\s*(.+);\\s*";
    /**
     * a string representing a legal variable name
     */
    public static String VAR_NAME = "\\s*([a-zA-Z]\\w*|_\\w+)\\s*";
    /**
     * a string representing a legal int value
     */
    public static String INT_VALUE = "\\s*(-?\\d+)\\s*";
    /**
     * a string representing a legal double value
     */
    public static String DOUBLE_VALUE = "\\s*(-?\\d+(\\.\\d+)?)\\s*";
    /**
     * a string representing a legal char value
     */
    public static String CHAR_VALUE = "\\s*('.')\\s*";
    /**
     * a string representing a legal string value
     */
    public static String STRING_VALUE = "\\s*\"(.*)\"\\s*";
    /**
     * a string representing a legal boolean value
     */
    public static String BOOLEAN_VALUE = "\\s*(true|false|-?\\d+|-?\\d+(\\.\\d+)?)\\s*";
    /**
     * pattern suits for variables names
     */
    public static Pattern VAR_NAME_PATTERN = Pattern.compile(VAR_NAME);
    /**
     * pattern suits for a declaration of an int
     */
    private static Pattern INT_PATTERN = Pattern.compile(INT_VALUE);
    /**
     * pattern suits for a declaration of a double
     */
    private static Pattern DOUBLE_PATTERN = Pattern.compile(DOUBLE_VALUE);
    /**
     * pattern suits for a declaration of a boolean
     */
    private static Pattern BOOLEAN_PATTERN = Pattern.compile(BOOLEAN_VALUE);
    /**
     * pattern suits for a declaration of a char
     */
    private static Pattern CHAR_PATTERN = Pattern.compile(CHAR_VALUE);
    /**
     * pattern suits for a declaration of a string
     */
    private static Pattern STRING_PATTERN = Pattern.compile(STRING_VALUE);
    /**
     * pattern suits for a variable declarations
     */
    private static Pattern DECLARATION_PATTERN = Pattern.compile(DECLARATION);
    /**
     * pattern suits for a whole line of multiple declarations
     */
    public static Pattern DECLARATION_LINE_PATTERN = Pattern.compile(VAR_TYPES);
    /**
     * pattern suits for a variable value assignment
     */
    public static Pattern ASSIGNMENT_PATTERN = Pattern.compile(ASSIGNMENT);
    /**
     * hash-map, mapping between a pattern and it's type
     */
    public static Map<VariableFactory.Type, Pattern> typeToPattern = Map.of(Type.INT, INT_PATTERN,
            Type.DOUBLE, DOUBLE_PATTERN, Type.BOOLEAN, BOOLEAN_PATTERN, Type.STRING, STRING_PATTERN,
            Type.CHAR, CHAR_PATTERN);
    /**
     * ONE
     */
    private static int ONE = 1;
    /**
     * THREE
     */
    private static int THREE = 3;
    /**
     * FOUR
     */
    private static int FOUR = 4;
    /**
     *
     */
    private static String ILLEGAL_DECLARATION_OR_ASSIGNMENT = "illegal declaration/ assignment line";
    /**
     * a message when trying to assign a value to final variable
     */
    private static String FINAL_ASSIGNMENT = "a final variable cannot be assigned with a new value";

    /**
     * enum classes representing the legal variable types
     */
    public enum Type {
        INT,
        DOUBLE,
        STRING,
        CHAR,
        BOOLEAN
    }


    /**
     * given a line, analyzes it and decides whether to create a new variable or assign an
     * existing variable a new value
     *
     * @param line   - a line to analyze
     * @param variables - list of existing variables
     * @param isParam - flag when a variable given is a method parameter
     * @return a list of variables created
     */
    public static ArrayList<Variable> parseLine(String line, ArrayList<Variable> variables, boolean isParam)
            throws VariableException {
        ArrayList<Variable> result = new ArrayList<>();
        boolean isAssignment = parseAssignment(line, variables, result);
        boolean isDeclaration = parseDeclaration(line, variables, result, isParam);
        if (!isAssignment && !isDeclaration) {
            throw new VariableException(ILLEGAL_DECLARATION_OR_ASSIGNMENT);
        }
        return result;
    }

    /**
     * given a variable declaration line, iterates through it, creates new variables
     * and adds them to the lists
     *
     * @param line      - a string representing the declaration
     * @param variables - list of existing variables
     * @param result    - a list to add the new variables declared
     * @return - true if the line was legal, false otherwise
     */
    public static boolean parseDeclaration(String line, ArrayList<Variable> variables,
                                           ArrayList<Variable> result, boolean isParam)
            throws VariableException {
        Matcher variablesLine = DECLARATION_LINE_PATTERN.matcher(line);
        if (variablesLine.matches()) {
            boolean isFinal = variablesLine.group(ONE) != null;
            VariableFactory.Type type = getType(variablesLine.group(THREE));
            String varsLine = variablesLine.group(FOUR);
            if (varsLine.startsWith(",") || varsLine.endsWith(",")) {
                return false;
            }
            String[] singleVar = varsLine.split(",");
            boolean isOk = false;
            for (String s : singleVar) {
                if (isParam) {
                    isOk = varDeclaration(s, type, isFinal, variables, true, result);
                } else {
                    isOk = varDeclaration(s, type, isFinal, variables, false, result);
                }
            }
            return isOk;
        }
        return false;
    }

    /**
     * given a variable declaration line, breaks it into name, value etc. and creates a matching variable
     *
     * @param variable             - line of variable to create
     * @param type                 - type of the variable to create
     * @param isFinal              - is the variable supposed to be final
     * @param variableDeclarations - list of existing variables in the program
     * @param isParam              - is the variable a methods parameter
     * @param result               - a list to add the newly created variable to
     * @return - true if succeeded, false otherwise
     */
    private static boolean varDeclaration(String variable, VariableFactory.Type type, boolean isFinal,
                                          ArrayList<Variable> variableDeclarations, boolean isParam,
                                          ArrayList<Variable> result) throws VariableException {
        Matcher m = DECLARATION_PATTERN.matcher(variable);
        if (m.matches()) {
            String name = m.group(ONE);
            String value = m.group(THREE);
            VariableValidation.isCompatible(name, value, isFinal, variableDeclarations, isParam);
            Variable var;
            if (value == null) { // declaration of a variable without a value
                if (isParam) {
                    var = createVariableWithValue(name, "", type, isFinal);
                    result.add(var);
                    variableDeclarations.add(var);
                    return true;
                }
                var = createValuelessVariable(name, type, isFinal);
                result.add(var);
                variableDeclarations.add(var);
                return true;
            }
            return createVariable(name, value, type, isFinal, variableDeclarations, result);
        }
        return false;
    }

    /**
     * @param line                 -  given a variable assignment line, assigns the variable with the
     *                             given value
     * @param variableDeclarations - list of existing variables to check a variable with the same name and
     *                             type was created.
     * @param result               - a list to a new variable if needed
     * @return - true if succeeded, false otherwise
     */
    public static boolean parseAssignment(String line, ArrayList<Variable> variableDeclarations,
                                          ArrayList<Variable> result)
            throws VariableException {
        Matcher m = ASSIGNMENT_PATTERN.matcher(line);
        if (m.matches()) {
            String name = m.group(ONE);
            String value = m.group(THREE);
            Variable var = VariableValidation.isContain(name, variableDeclarations);
            if (var == null) {
                return false;
            }
            boolean isFinal = var.isFinal();
            if (isFinal) {
                throw new VariableException(FINAL_ASSIGNMENT);
            }
            return createVariable(name, value, var.getType(), false, variableDeclarations, result);
        }
        return false;
    }

    /**
     * returns the type of variable matching a given string
     *
     * @param type - a string representing a variables type
     * @return the type matching the given string
     */
    private static VariableFactory.Type getType(String type) {
        switch (type) {
            case ("int"):
                return Type.INT;
            case ("double"):
                return Type.DOUBLE;
            case ("char"):
                return Type.CHAR;
            case ("boolean"):
                return Type.BOOLEAN;
            default:
                return Type.STRING;
        }
    }

    /**
     * creates a variable matching all given elements
     *
     * @param name                 - name of the new variable to create
     * @param value                - value of the new variable to create
     * @param varType              - type of the new variable to create
     * @param isFinal              - is the variable final or not
     * @param variableDeclarations - list of existing variables to check a variable with the same name and
     *                             type was created.
     * @return a new variable with all it's elements
     */
    public static boolean createVariable(String name, String value, VariableFactory.Type varType,
                                         boolean isFinal, ArrayList<Variable> variableDeclarations,
                                         ArrayList<Variable> result) {
        String varValue = value;
        Variable var;
        if (VariableValidation.checkValue(varValue, varType)) {
            var = createVariableWithValue(name, varValue, varType, isFinal);
            result.add(var);
            variableDeclarations.add(var);
            return true;
        } else if (VariableValidation.checkVariableValue(value, varType, variableDeclarations)) {
            varValue = VariableValidation.isContain(value, variableDeclarations).getValue();
            var = createVariableWithValue(name, varValue, varType, isFinal);
            result.add(var);
            variableDeclarations.add(var);
            return true;
        }
        return false;
    }

    /**
     * creates a variable without a value
     *
     * @param name    - name of the variable
     * @param type    - type of the variable
     * @param isFinal - flag, true if the variable is final
     * @return a new variable with all it's elements
     */
    private static Variable createValuelessVariable(String name, VariableFactory.Type type,
                                                    boolean isFinal) {
        return new Variable(name, isFinal, type);
    }

    /**
     * creates a variable with a value
     *
     * @param name    - name of the variable
     * @param value   - value of the variable
     * @param type    - type of the variable
     * @param isFinal - flag, true if the variable is final
     * @return a new variable with all it's elements
     */
    private static Variable createVariableWithValue(String name, String value, VariableFactory.Type type,
                                                    boolean isFinal) {
        return new Variable(name, value, isFinal, type);
    }


}
