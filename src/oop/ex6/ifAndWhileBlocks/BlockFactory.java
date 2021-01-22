package oop.ex6.ifAndWhileBlocks;

import oop.ex6.variables.*;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * a factory for blocks
 */
public class BlockFactory {
    /**
     * a string representing block brackets content
     */
    public static String BRACKETS_CONTENT = "\\s*\\((.*)\\)\\s*";
    /**
     * pattern suit for a true or false phrase
     */
    private static Pattern TRUE_FALSE = Pattern.compile("\\s*(true|false)\\s*");
    /**
     * pattern suit for int or double value
     */
    private static Pattern INT_DOUBLE = Pattern.compile(VariableFactory.INT_VALUE + "|" +
            VariableFactory.DOUBLE_VALUE);
    /**
     * pattern suit for an illegal boolean condition
     */
    private static Pattern ILLEGAL_CONDITION = Pattern.compile("\\s*(\\|\\||&&)(.*)|(.*)(\\|\\||&&)\\s*");
    /**
     * pattern suit for an if condition
     */
    private static Pattern IF_CONDITION_LINE = Pattern.compile("\\s*(if)" + BlockFactory.BRACKETS_CONTENT +
            "\\{\\s*");
    /**
     * pattern suit for a while condition
     */
    private static Pattern WHILE_CONDITION_LINE = Pattern.compile("\\s*(while)" +
            BlockFactory.BRACKETS_CONTENT + "\\{\\s*");
    /**
     * a message when an illegal block is given
     */
    private static String ILLEGAL_CONDITION_LINE = "illegal if/while block";
    /**
     * a message of consecutive operators in a block condition
     */
    private static String CONSECUTIVE_OPERATORS = "illegal condition - there are two consecutive operators";
    /**
     * a block condition contains an operator in the start or at the end of a block condition
     */
    private static String CONDITION_START_OR_END = "illegal condition - the brackets start with || or &&";
    /**
     * a block condition contains a non-boolean expression
     */
    private static String NOT_BOOLEAN_CONDITION = "The condition is not containing a boolean expression";
    /**
     * ONE
     */
    private static int ONE = 1;
    /**
     * TWO
     */
    private static int TWO = 2;

    /**
     * checks if a block creation attempt has a legal boolean condition and creates one if it does
     *
     * @param line           - line of the block creation attempt
     * @param variables      - list of all existing variables to iterates through
     * @param startRow       - row of the file where the attempt occurs
     * @param scopeVariables - list of the local scope variables to iterates through
     * @return - a new block if condition was legal
     */
    public static Block createBlock(String line, ArrayList<Variable> variables, int startRow,
                                    Stack<ArrayList<Variable>> scopeVariables) throws BlockException {
        Matcher m1 = IF_CONDITION_LINE.matcher(line);
        Matcher m2 = WHILE_CONDITION_LINE.matcher(line);
        if (m1.matches() && checkBrackets(m1.group(TWO), scopeVariables)) {
            return new IfBlock(m1.group(TWO), startRow);
        } else if (m2.matches() && checkBrackets(m2.group(TWO), scopeVariables)) {
            return new WhileBlock(m2.group(TWO), startRow);
        }
        throw new BlockException(ILLEGAL_CONDITION_LINE);
    }

    /**
     * checks if the content of given brackets is a legal boolean phrase
     *
     * @param bracketsContent - a string representing the content
     * @param scopeVariables  - local variables to iterate through in case the brackets contain
     *                        a variable name
     * @return - true if legal, false otherwise
     * @throws BlockException
     */
    private static boolean checkBrackets(String bracketsContent,
                                         Stack<ArrayList<Variable>> scopeVariables) throws BlockException {
        ArrayList<String> allConditions = new ArrayList<>();
        Matcher m = ILLEGAL_CONDITION.matcher(bracketsContent);
        if (m.matches()) {
            throw new BlockException(CONDITION_START_OR_END);
        }
        String[] conditions = bracketsContent.split("\\|\\|");
        for (String condition : conditions) {
            if (m.matches()) {
                throw new BlockException(CONSECUTIVE_OPERATORS);
            } else {
                String[] vars = condition.split("&&");
                for (String var : vars) {
                    var = var.replaceAll(" ", "");
                    allConditions.add(var);
                }
            }
        }
        return isBoolean(allConditions, scopeVariables);
    }

    /**
     * checks if all conditions given are boolean or not
     *
     * @param allConditions  - list oof conditions to check
     * @param scopeVariables -list of the local scope variables to check in case one of the
     *                       condition is a variable name
     * @return - true if all conditions are legal boolean conditions
     */
    private static boolean isBoolean(ArrayList<String> allConditions,
                                     Stack<ArrayList<Variable>> scopeVariables)
            throws BlockException {
        for (String condition : allConditions) {
            Matcher m1 = TRUE_FALSE.matcher(condition);
            Matcher m2 = Pattern.compile(VariableFactory.VAR_NAME).matcher(condition);
            Matcher m3 = INT_DOUBLE.matcher(condition);
            if (m1.matches() || m3.matches()) {
                continue;
            }
            if (m2.matches()) {
                for (ArrayList<Variable> list : scopeVariables) {
                    Variable var = VariableValidation.isContain(m2.group(ONE), list);
                    if (var != null && (var.getType() == VariableFactory.Type.INT ||
                            var.getType() == VariableFactory.Type.DOUBLE ||
                            var.getType() == VariableFactory.Type.BOOLEAN) && var.getValue() != null) {
                        return true;
                    }
                }
                throw new BlockException(NOT_BOOLEAN_CONDITION);
            } else {
                throw new BlockException(NOT_BOOLEAN_CONDITION);
            }
        }
        return true;
    }
}
