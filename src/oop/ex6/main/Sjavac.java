package oop.ex6.main;

import java.io.IOException;

/**
 * the main file of the program, which uses the parser to check each file
 */
public class Sjavac {

    public static void main(String[] args){
        String path = args[0];
        Parser parser = new Parser();
        try {
            parser.parse(path);
            System.out.println("0");
        } catch (IOException e) {
            System.out.println("2");
            System.err.println(e.getMessage());
        } catch (GeneralException e) {
            System.out.println("1");
            System.err.println(e.getMessage());
        }
    }
}
