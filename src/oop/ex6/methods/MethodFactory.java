package oop.ex6.methods;

import oop.ex6.ifAndWhileBlocks.BlockFactory;
import oop.ex6.variables.Variable;
import oop.ex6.variables.VariableException;
import oop.ex6.variables.VariableFactory;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * factory for method objects
 */
public class MethodFactory {

    /**
     * a string representing legal method name
     */
    public static String METHOD_NAME = "\\s*([a-zA-Z]+\\w*)";
    /**
     * a string representing a method line declaration
     */
    private static Pattern METHOD_LINE = Pattern.compile("\\s*void\\s+" + MethodFactory.METHOD_NAME +
            BlockFactory.BRACKETS_CONTENT + "\\s*\\{\\s*");
    /**
     * string for empty parameters of method given
     */
    private static Pattern EMPTY_PARAMETERS = Pattern.compile("\\s*");
    /**
     * a message for illegal parameters given
     */
    private static String ILLEGAL_PARAMETERS = "illegal method assignment";
    /**
     * general message for illegal method declaration
     */
    private static String ILLEGAL_METHOD  = "illegal method's declaration line";
    /**
     * ZERO
     */
    private static int ZERO = 0;
    /**
     * ONE
     */
    private static int ONE = 1;
    /**
     * TWO
     */
    private static int TWO = 2;


    /**
     * create a new method matching the given details
     *
     * @param line  - a string representing the details of the method
     * @param start - row index where the method is created
     * @return - a new method matching the details
     */
    public static Method createMethod(String line, int start, ArrayList<Method> methods,
                                      ArrayList<Variable> globalVariables)
            throws VariableException, MethodException {
        Matcher m = METHOD_LINE.matcher(line);
        if (m.matches()) {
            if (isContain(m.group(ONE), methods) == null) {
                ArrayList<Variable> paramVars = new ArrayList<>();
                String[] params = m.group(TWO).split(",");
                Matcher m1 = EMPTY_PARAMETERS.matcher(params[ZERO]);
                if (m1.matches() && params.length == ONE) {
                    return new Method(m.group(ONE), paramVars, start);
                } else {
                    for (String param : params) {
                        ArrayList<Variable> res = new ArrayList<>();
                        VariableFactory.parseDeclaration(param + ";", globalVariables, res,
                                true);
                        if (res.size() < ONE) {
                            throw new MethodException(ILLEGAL_PARAMETERS);
                        }
                        Variable var = res.get(ZERO);
                        paramVars.add(var);
                        globalVariables.add(var);
                    }
                    return new Method(m.group(ONE), paramVars, start);
                }
            }
        }
        throw new MethodException(ILLEGAL_METHOD);
    }

    /**
     * checks if a method was already declared, while trying to create a new one
     *
     * @param name name of the method
     * @return the method, null if the method was not declared before
     */
    public static Method isContain(String name, List<Method> methods) {
        for (Method method : methods) {
            if (method.getName().equals(name)) {
                return method;
            }
        }
        return null;
    }
}
