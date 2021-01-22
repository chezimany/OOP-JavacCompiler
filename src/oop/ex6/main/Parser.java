package oop.ex6.main;

import oop.ex6.ifAndWhileBlocks.Block;
import oop.ex6.ifAndWhileBlocks.BlockException;
import oop.ex6.ifAndWhileBlocks.BlockFactory;
import oop.ex6.methods.Method;
import oop.ex6.methods.MethodException;
import oop.ex6.methods.MethodFactory;
import oop.ex6.variables.*;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * main class which is responsible for parsing a file
 */
public class Parser {
    /**
     * a pattern suit representing whitespaces
     */
    private static Pattern WHITESPACE = Pattern.compile("\\s*");
    /**
     * a pattern suit representing a comment line
     */
    private static Pattern COMMENT_LINE = Pattern.compile("//.*");
    /**
     * a pattern suit representing a method call
     */
    private static Pattern METHOD_CALL = Pattern.compile(MethodFactory.METHOD_NAME +
            BlockFactory.BRACKETS_CONTENT + ";\\s*");
    /**
     * a pattern suit representing a start of block
     */
    private static Pattern START_OF_BLOCK = Pattern.compile(".*\\{\\s*");
    /**
     * a pattern suit representing an end of block
     */
    private static Pattern END_OF_BLOCK = Pattern.compile("\\s*}\\s*");
    /**
     * a pattern suit representing a general action line
     */
    private static Pattern END_OF_ACTION = Pattern.compile(".+;\\s*");
    /**
     * a pattern suit representing a return line
     */
    private static Pattern RETURN_LINE = Pattern.compile("\\s*return\\s*;\\s*");
    /**
     * a list containing the programs global variables
     */
    private ArrayList<Variable> globalVariables = new ArrayList<>();
    /**
     * a list to contain the programs methods
     */
    private ArrayList<Method> allMethods = new ArrayList<>();
    /**
     * a stack to contain lists of each scopes variables
     */
    private Stack<ArrayList<Variable>> scopeVariables = new Stack<>();
    /**
     * a counter to follow the scope depth we are in
     */
    private int scopeCounter = 0;
    /**
     * a hash map which key is an int representing a line of the program, and it's value is another
     * hash map, where its key is a string representing the lines content, and its value is an int
     * representing the lines scope depth
     */
    private HashMap<Integer, HashMap<String, Integer> > linesMap = new HashMap<>();
    /**
     * a message when there is no return statement in a method
     */
    private static String NO_RETURN_STATEMENT = "no return statement at the end of the method";
    /**
     * a message when an illegal line appears in a method
     */
    private static String ILLEGAL_METHOD_LINE = "there is an illegal line inside a method";
    /**
     * a general message for illegal line
     */
    private static String GENERAL_ILLEGAL_LINE = "illegal action in line ";
    /**
     * a message for unbalanced number of parentheses
     */
    private static String UNBALANCED_PARENTHESES = "there is an unbalanced number of parentheses";
    /**
     * a message for a return statement in the global scope
     */
    private static String ILLEGAL_RETURN = "return statement in global scope in line ";
    /**
     * zero
     */
    private static int ZERO = 0;
    /**
     * one
     */
    private static int ONE = 1;
    /**
     * TWO
     */
    private static int TWO = 2;

    /**
     * iterate through a file and check if all lines are legal Sjavac code lines
     *
     * @param path - path of the file to check
     * @return - true if the file is legal, false otherwise
     * @throws IOException
     * @throws GeneralException
     */
    public boolean parse(String path) throws IOException, GeneralException {
        List<String> fileLines = Files.readAllLines(Paths.get(path));
        scopeVariables.push(globalVariables);
        firstParseOfFile(fileLines);
        try {
            secondCheckGlobalScope(fileLines);
        } catch (BlockException | VariableException | GeneralException e) {
            throw new GeneralException(e.getMessage());
        }
        return true;
    }

    /**
     * iterate through the file, collect the local variables and methods and look for illegal
     * actions in the global
     * scope
     *
     * @param fileLines - list of the files lines
     * @throws GeneralException
     */
    private void firstParseOfFile(List<String> fileLines) throws GeneralException {
        for (int i = 0; i < fileLines.size(); i++) {
            HashMap<String, Integer> currentMap = new HashMap<>();
            int rowNum = i + 1;
            String line = fileLines.get(i);
            Matcher m1 = START_OF_BLOCK.matcher(line);
            Matcher m2 = END_OF_BLOCK.matcher(line);
            Matcher m3 = END_OF_ACTION.matcher(line);
            Matcher m4 = COMMENT_LINE.matcher(line);
            Matcher m5 = WHITESPACE.matcher(line);
            Matcher m6 = RETURN_LINE.matcher(line);
            if (!(m1.matches() || m2.matches() || m3.matches() || m4.matches() || m5.matches()
                    || m6.matches())) {
                throw new GeneralException(GENERAL_ILLEGAL_LINE + rowNum);
            }
            if (m6.matches() && scopeCounter == ZERO) {
                throw new GeneralException(ILLEGAL_RETURN + rowNum);
            }
            if (m3.matches() && scopeCounter == ZERO) { // should be only global scope declaration /
                // assignment
                isDeclarationLines(line, rowNum);
                currentMap.put(line, scopeCounter);
                linesMap.put(rowNum, currentMap);
                continue;
            }
            if (m4.matches() || m5.matches()) {
                currentMap.put(line, scopeCounter);
                linesMap.put(rowNum, currentMap);
                continue;
            }
            if (m6.matches()) {
                if (scopeCounter > ONE) {
                    currentMap.put(line, scopeCounter);
                    linesMap.put(rowNum, currentMap);
                }
                if (scopeCounter == ONE) { // the return statement at the end of a method
                    currentMap.put(line, scopeCounter);
                    linesMap.put(rowNum, currentMap);
                }
                continue;
            }
            if (m1.matches()) {
                if (scopeCounter == ZERO) { // should be only a line of method creation
                    isMethodCreationLines(line, rowNum);
                }
                currentMap.put(line, scopeCounter);
                linesMap.put(rowNum, currentMap);
                scopeCounter++;
                continue;
            } else if (scopeCounter < ONE && m2.matches() || scopeCounter < ZERO) {
                throw new GeneralException(UNBALANCED_PARENTHESES);
            }
            if (m2.matches()) {
                currentMap.put(line, scopeCounter);
                linesMap.put(rowNum, currentMap);
                scopeCounter--;
                continue;
            }
            currentMap.put(line, scopeCounter);
            linesMap.put(rowNum, currentMap);
        }
        if (scopeCounter != ZERO) {
            throw new GeneralException(UNBALANCED_PARENTHESES);
        }
    }

    /**
     * check if a given line is a variable declaration line
     *
     * @param line   - line to check
     * @param rowNum - number of the row
     * @return -true if legal
     * @throws GeneralException
     */
    private boolean isDeclarationLines(String line, int rowNum) throws GeneralException {
        try {
            ArrayList<Variable> newVars = VariableFactory.parseLine(line, globalVariables, false);
            for (int i = 0; i < newVars.size(); i++) {
                if (VariableValidation.isContain(newVars.get(i).getName(), globalVariables) != null) {
                    globalVariables.set(i, newVars.get(i));
                } else {
                    globalVariables.add(newVars.get(i));
                }
            }
            return true;
        } catch (VariableException e1) {
            throw new GeneralException(GENERAL_ILLEGAL_LINE + rowNum + "\n" + e1.getMessage());
        }
    }

    /**
     * checks if a given line is a method creation line
     *
     * @param line   - line to check
     * @param rowNum - number of the line
     * @return - true if it is
     * @throws GeneralException
     */
    private boolean isMethodCreationLines(String line, int rowNum) throws GeneralException {
        try {
            Method method = MethodFactory.createMethod(line, rowNum, allMethods, new ArrayList<>());
            getAllMethods().add(method);
            return true;
        } catch (MethodException | VariableException e) {
            throw new GeneralException(+rowNum + "\n" + e.getMessage());
        }
    }

    /**
     * the method checks if a method that is being called already exists and it's arguments match the
     * methods parameters
     *
     * @param line             - a method call line
     * @param methodsVariables - list of the methods variables
     * @return - true if the line is legal
     */
    private boolean isMethodCallLegal(String line, ArrayList<Variable> methodsVariables) {
        Matcher m1 = METHOD_CALL.matcher(line);
        if (m1.matches()) {
            String name = m1.group(ONE);
            Method existedMethod = MethodFactory.isContain(name, allMethods);
            if (existedMethod != null) { // a method with this name already exists
                Matcher m = WHITESPACE.matcher(m1.group(TWO));
                String[] parameters = m1.group(TWO).split(",");
                List<Variable> methodParams = existedMethod.getParameters();
                if (methodParams.size() == ZERO && parameters.length == ONE && m.matches()) {
                    return true; // method has no parameters
                }
                if (methodParams.size() == parameters.length) { // checks if num of parameters is correct
                    boolean isOk = false;
                    for (int i = 0; i < parameters.length; i++) { // checks if the parameters are compatible
                        Variable param = methodParams.get(i);
                        isOk = VariableFactory.createVariable(param.getName(), parameters[i],
                                param.getType(), param.isFinal(), methodsVariables, new ArrayList<>());
                    }
                    return isOk;
                }
            }
        }
        return false;
    }

    /**
     * iterates through the file again, goes inside methods and if\while blocks and checks if all
     * lines are legal
     *
     * @param fileLines - list of the file lines
     * @returntrue if code is legal
     */
    private boolean secondCheckGlobalScope(List<String> fileLines) throws VariableException,
            GeneralException, BlockException {
        for (int i = 0; i < getAllMethods().size(); i++) {
            Method method = getAllMethods().get(i);
            scopeVariables.push(method.getMethodVariables());
            int startRow = method.getStartRow();
            scopeCounter++;
            checkLocalScope(fileLines, scopeVariables, startRow);
        }
        return true;
    }

    /**
     * goes inside a local scope and checks if its lines are legal
     *
     * @param fileLines    - list of the files lines
     * @param allVariables - all the variables of the program
     * @param startRow     - beginning of the scope
     * @return - true if legal, false otherwise
     * @throws GeneralException
     * @throws BlockException
     * @throws VariableException
     * @throws MethodException
     */
    private boolean checkLocalScope(List<String> fileLines, Stack<ArrayList<Variable>> allVariables,
                                    int startRow) throws GeneralException, BlockException,
            VariableException {
        Boolean[] isLastReturn = {false};
        for (int i = startRow; i < linesMap.size(); i++) {
            String line = fileLines.get(i);
            if (linesMap.get(i + ONE).get(line) == ZERO) {
                break;
            }
            if (line == null) {
                continue;
            }
            boolean res = localScopeHelper(line, i + ONE, allVariables, isLastReturn);
            Matcher blockStartMatcher = START_OF_BLOCK.matcher(line);
            if (blockStartMatcher.matches()) {
                Block newBlock = BlockFactory.createBlock(line, new ArrayList<>(), i, allVariables);
                allVariables.push(newBlock.getBlockVariables());
                scopeCounter++;
                res = true;
            }
            if (!res) {
                throw new GeneralException(ILLEGAL_METHOD_LINE);
            }
        }
        return true;
    }

    /**
     * a helper for the check local scope method. receives a line and checks if its legal or not
     *
     * @param line         - line to check
     * @param row          - number of the line
     * @param allVariables - stack with all the program variables
     * @param isLastReturn - did we pass the last return of the method we are in
     * @return - true if it is a legal line
     */
    private boolean localScopeHelper(String line, int row, Stack<ArrayList<Variable>> allVariables,
                                     Boolean[] isLastReturn) throws
            GeneralException, VariableException {
        Matcher returnMatcher = RETURN_LINE.matcher(line);
        Matcher commentMatcher = COMMENT_LINE.matcher(line);
        Matcher spaceMatcher = WHITESPACE.matcher(line);
        Matcher actionMatcher = END_OF_ACTION.matcher(line);
        Matcher blockStartMatcher = START_OF_BLOCK.matcher(line);
        Matcher blockEndMatcher = END_OF_BLOCK.matcher(line);
        int scopeDepth = (Integer) linesMap.get(row).get(line);
        if (scopeDepth == scopeCounter - ONE) { // we reached the end of the scope
            return true;
        }
        if (actionMatcher.matches() || blockStartMatcher.matches()) {
            isLastReturn[ZERO] = false;
        }
        if (blockEndMatcher.matches()) {
            if (!isLastReturn[ZERO] && scopeDepth == ONE) {
                throw new GeneralException(NO_RETURN_STATEMENT);
            }
            scopeCounter--;
            if (allVariables.size() > ONE) {
                allVariables.pop();
            }
            return true;
        }
        if (returnMatcher.matches() && scopeDepth == ONE) { // the last return of the methods scope,
            // next line should be only white-spaces or closing parentheses
            isLastReturn[ZERO] = true;
            return true;
        } else if (commentMatcher.matches() || spaceMatcher.matches() || returnMatcher.matches()) {
            return true;
        } else if (actionMatcher.matches()) {
            return innerAction(line, allVariables);
        }
        return false;
    }

    /**
     * checks if an action inside a method is legal or not
     *
     * @param line         - line of the action
     * @param allVariables - all of the programs variables
     * @return - true if legal
     */
    private boolean innerAction(String line, Stack<ArrayList<Variable>> allVariables)
            throws VariableException {
        ArrayList<Variable> res = new ArrayList<>();
        Matcher m1 = VariableFactory.DECLARATION_LINE_PATTERN.matcher(line);
        Matcher m2 = VariableFactory.ASSIGNMENT_PATTERN.matcher(line);
        Matcher m3 = METHOD_CALL.matcher(line);
        for (int j = allVariables.size() - 1; j >= 0; j--) {
            if (m1.matches()) {
                boolean isDeclaration = VariableFactory.parseDeclaration
                        (line, allVariables.get(j), res, false);
                if (isDeclaration) {
                    return true;
                }
            }
            if (m2.matches()) {
                boolean isAssignment = VariableFactory.parseAssignment(line, allVariables.get(j), res);
                if (isAssignment) {
                    Variable toReplace =
                            VariableValidation.isContain(res.get(ZERO).getName(), allVariables.get(j));
                    if (j != ZERO) {
                        int indexOfVar = allVariables.get(j).indexOf(toReplace);
                        allVariables.get(j).set(indexOfVar, res.get(ZERO));
                    } else {
                        allVariables.get(ZERO).add(res.get(ZERO));
                    }
                    return true;
                }
            }
            if (m3.matches()) {
                if (isMethodCallLegal(line, allVariables.get(j))) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * @return all of the programs methods
     */
    private ArrayList<Method> getAllMethods() {
        return allMethods;
    }

}


