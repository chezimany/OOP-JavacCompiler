package oop.ex6.ifAndWhileBlocks;

import oop.ex6.variables.Variable;

import java.util.ArrayList;

/**
 * class representing a general block that has a condition
 */
public abstract class Block {
    /**
     * a string representing the blocks boolean condition
     */
    private String condition;
    /**
     * number of the row the block starts in
     */
    private int startRow;

    private ArrayList<Variable> blockVariables = new ArrayList<>();

    public Block(String condition, int startRow) {
        this.condition = condition;
        this.startRow = startRow;
    }

    /**
     * @return the blocks variables
     */
    public ArrayList<Variable> getBlockVariables() {
        return this.blockVariables;
    }

}
