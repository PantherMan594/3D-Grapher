package com.pantherman594;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

// How the parser works:                              The expressions list will be here
// Input: x^sin(2)+y^(2+3)
// Parentheses: 2 gets stored as \1
//   x^sin\1+y^(2+3)                                   {2}
//     2 doesn't have any parentheses, so go on with main equation
// Parentheses again: 2+3 gets stored as \2
//   x^sin\1+y^\2                                      {2, 2+3}
//     2+3 doesn't have any parentheses, so go on with main equation
//   Main equation doesn't have any parentheses, so go on to functions
// Functions: first found is sin, that gets stored as \3
//   x^\3+y^\2                                         {2, 2+3, sin\1}
//     sin\2 doesn't have any other functinos, go back to main equation
// No more functions, now for exponents
//   \4+y^\2                                           {2, 2+3, sin\1, x^\3}
//   \4+\5                                             {2, 2+3, sin\1, x^\3, y^\2}
// And now to calculate, let's set x = 3 and y = 2:
//   \6                                                {2, 2+3, sin\1, x^\3, y^\2, \4+\5}
// Before = \4, symbol = +, after = \5
//   \4 can be broken down further to x^\3
//     \3 can be broken down further to sin\1
//       \1 = 2
//       sin(2) = 0.035
//     x^0.087 = 3^0.035 = 1.039
//   \5 can be broken down further to y^\2
//     \2 can be broken down further to 2+3
//       2+3 = 5
//     y^5 = 2^5 = 32
// \4+\5 = 1.039+32 = 33.039
// Rather than reparsing every time, this allows the program to reuse the parsed chunks, and simply replace the variables

public class MathParser {
    private Map<String, Float> variables = new TreeMap<>();
    private List<String> expressions = new ArrayList<>();
    private static List<List<String>> symbols = new ArrayList<>();
    private static List<String> allSymbols = new ArrayList<>();
    private String exp;
    private boolean parseError = false;

    static { // List all the functions, symbols, etc that will be used later
        // Divide them into groups, according to order
        List<String> funcs = new ArrayList<>();
        funcs.add("sin");
        funcs.add("cos");
        funcs.add("tan");
        funcs.add("arcsin");
        funcs.add("arccos");
        funcs.add("arctan");
        funcs.add("csc");
        funcs.add("sec");
        funcs.add("cot");
        funcs.add("arccsc");
        funcs.add("arcsec");
        funcs.add("arccot");
        funcs.add("ln");
        symbols.add(funcs);
        allSymbols.addAll(funcs);

        List<String> exp = new ArrayList<>();
        exp.add("^");
        symbols.add(exp);
        allSymbols.addAll(exp);

        List<String> multDiv = new ArrayList<>();
        multDiv.add("*");
        multDiv.add("/");
        symbols.add(multDiv);
        allSymbols.addAll(multDiv);

        List<String> addSub = new ArrayList<>();
        addSub.add("+");
        addSub.add("-");
        symbols.add(addSub);
        allSymbols.addAll(addSub);
    }

    /*public static void main(String[] args) throws ParseException {
        MathParser parser = new MathParser();
        parser.parse("x^2+y");
        parser.addVariable("x", 6);
        parser.addVariable("y", 2);
        System.out.println(parser.exp);
        System.out.println(parser.expressions);
        System.out.println(parser.evaluate());
    }*/

    // Parse the expression. This essentially splits it up into tiny chunks, each with 1 operation
    void parse(String exp) throws ParseException {
        try {
            exp = exp.toLowerCase() // Make it all lower case
                    .replace("x", "(x)") // Add parentheses to the x and y
                    .replace("y", "(y)") // variables, in case they are negative
                    .replace(")(",")*(") // Add a multiplication sign between parentheses
                    .replaceAll("([0-9])\\(", "$1*(") //      and/or numbers: declare it explicitly
                    .replace("-(", "-1*(");
            this.exp = parseParen(exp);
            parseError = false;
        } catch(Exception e) {
            parseError = true; // If there was an error with parsing, don't even try calculating
            throw new ParseException("Malformed equation: " + e.getMessage());
        }
    }

    // Store the variables in a map (right now only supports x and y)
    void addVariable(String variable, float value) {
        variables.put(variable, value);
    }

    // Evaluate the equation, given the variables
    float evaluate() throws ParseException {
        if (parseError) throw new ParseException("Malformed equation.");
        try {
            expressions.add(exp); //            Collapse the equation one last time before fully expanding again,
            exp = "\\" + expressions.size(); // but this time with the variables and actually calculating numbers
            return evaluateExp(exp);
        } catch(Exception e) {
            throw new ParseException("Malformed equation: " + e.getMessage());
        }
    }

    private float evaluateExp(String exp) throws ParseException {
        return evaluateExp(exp, 0);
    }

    // Evaluate the expression. num makes sure it doesn't loop too many times if it errs, leading to a stack overflow
    private float evaluateExp(String exp, int num) throws ParseException {
        for (Map.Entry<String, Float> var : variables.entrySet()) {
            exp = exp.replace(var.getKey(), String.valueOf(var.getValue()));
        }

        // Look for the first symbol in the equation. Store its position and value
        int firstIndex = -1;
        String symbolType = "";
        for (String symbol : allSymbols) {
            int index = exp.indexOf(symbol);
            if (index != -1 && index > firstIndex) {
                firstIndex = index;
                symbolType = symbol;
            }
        }

        if (firstIndex == -1) { //         If there is no symbol, check if it
            if (exp.startsWith("\\")) { // starts with a \ (denoting a collapsed/stored expression)
                // If that exists, get the value of that expression
                exp = String.valueOf(evaluateExp(expressions.get(Integer.parseInt(exp.substring(1)) - 1)));
                for (Map.Entry<String, Float> var : variables.entrySet()) {
                    exp = exp.replace(var.getKey(), String.valueOf(var.getValue()));
                }
            }

            // Once done, try to return it as a float. If it can't be turned into a number, try up to 10 times, then throw an error
            if (num > 10) throw new ParseException("Malformed equation.");
            try {
                return Float.parseFloat(exp);
            } catch (NumberFormatException e) {
                return evaluateExp(exp, num + 1);
            }
        }

        // Get and evaluate the string before the symbol
        String before = "";
        if (firstIndex != 0) {
            before = exp.substring(0, firstIndex);
            if (before.startsWith("\\")) before = String.valueOf(evaluateExp(expressions.get(Integer.parseInt(before.substring(1)) - 1)));
        }

        // If the expression should be negated, do it explicitly with a -1*
        if (before.equals("") && symbolType.equals("-")) {
            before = "-1";
            symbolType = "*";
        }

        // Get and evaluate the string after the symbol
        String after = "";
        if (firstIndex != exp.length()) {
            after = exp.substring(firstIndex + symbolType.length(), exp.length());
            if (after.startsWith("\\")) after = String.valueOf(evaluateExp(expressions.get(Integer.parseInt(after.substring(1)) - 1)));
        }

        // Convert the before and after strings into numbers. If that's not possible, something's wrong.
        if (before.equals("")) before = "0";
        if (after.equals("")) after = "0";

        double beforeF;
        double afterF;
        try {
            beforeF = Double.parseDouble(before);
            afterF = Double.parseDouble(after);
        } catch (NumberFormatException e) {
            throw new ParseException("Invalid equation at: " + exp);
        }

        // Now we get to calculate this expression!
        Double finalVal;
        switch(symbolType) {
            case "sin":
                finalVal = Math.sin(afterF);
                break;
            case "cos":
                finalVal = Math.cos(afterF);
                break;
            case "tan":
                finalVal = Math.tan(afterF);
                break;
            case "arcsin":
                finalVal = Math.asin(afterF);
                break;
            case "arccos":
                finalVal = Math.acos(afterF);
                break;
            case "arctan":
                finalVal = Math.atan(afterF);
                break;
            case "csc":
                finalVal = 1 / Math.sin(afterF);
                break;
            case "sec":
                finalVal = 1 / Math.cos(afterF);
                break;
            case "cot":
                finalVal = 1 / Math.tan(afterF);
                break;
            case "arccsc":
                finalVal = 1 / Math.asin(afterF);
                break;
            case "arcsec":
                finalVal = 1 / Math.acos(afterF);
                break;
            case "arccot":
                finalVal = 1 / Math.atan(afterF);
                break;
            case "ln":
                finalVal = Math.log(afterF);
                break;
            case "^":
                finalVal = Math.pow(beforeF, afterF);
                break;
            case "*":
                finalVal = beforeF * afterF;
                break;
            case "/":
                finalVal = beforeF / afterF;
                break;
            case "+":
                finalVal = beforeF + afterF;
                break;
            case "-":
                finalVal = beforeF - afterF;
                break;
            default:
                throw new ParseException("Unknown symbol: " + symbolType);
        }

        // Finally, return the value as a float
        return (float) (double) finalVal;
    }

    // PEMDAS: First thing to do is separate all the equations in parentheses
    private String parseParen(String exp) throws ParseException {
        // First check if the parentheses are matching. If not, throw an error
        int openParen = exp.replace("(", "").length();
        int closedParen = exp.replace(")", "").length();
        if (openParen != closedParen) {
            int diff = openParen - closedParen;
            String error = "Mismatched Parentheses. ";

            if (diff > 0) error += diff + " parentheses were opened but not closed.";
            else error += -1*diff + " parentheses were closed but never opened.";
            throw new ParseException(error);
        }

        // Find the first parenthesis...
        int nextParen = exp.indexOf("(");

        // (if there is none, go on to the rest of the parsing)
        if (nextParen == -1) return parse(exp, symbols);

        // ... and its matching pair
        int numOpen = 1;
        int lastJ = -1;
        for (int j = nextParen + 1; numOpen != 0 && j < exp.length(); j++) {
            switch (exp.substring(j, j + 1)) {
                case "(":
                    numOpen++;
                    break;
                case ")":
                    numOpen--;
                    if (numOpen == 0) {
                        lastJ = j;
                    }
                    break;
            }
        }
        if (lastJ > 0) { // If the matching pair was found, get the equation inside of it and parse that, and parse this again (with a substitution)
            expressions.add(parseParen(exp.substring(nextParen + 1, lastJ)));
            exp = exp.replace("(" + exp.substring(nextParen + 1, lastJ) + ")", "\\" + expressions.size());
            return parseParen(exp);
        } else {
            throw new ParseException("Mismatched parentheses. Could not find closing parenthesis.");
        }
    }

    private String parse(String exp, List<List<String>> symbols) throws ParseException {
        if (symbols.isEmpty()) { // If there are no more symbols left to check,
            return exp; // the equation should be symbol-less: just return it
        }
        List<String> stopSymbols = new ArrayList<>(symbols.get(0)); // Include the first set of symbols in stopSymbols
        List<List<String>> remainingSymbols = new ArrayList<>();
        for (int i = 1; i < symbols.size(); i++) {
            stopSymbols.addAll(symbols.get(i)); // Add all the symbols after the first one to stopSymbols (one giant list)
            remainingSymbols.add(symbols.get(i)); // as well as remainingSymbols (a couple small lists)
        }

        // Look to the first occurrence of a symbol in the first list (the one we're looking at right now)
        List<String> findSymbols = new ArrayList<>(symbols.get(0));
        int firstIndex = -1;
        for (String symbol : findSymbols) {
            int index = exp.indexOf(symbol);
            if (index != -1 && index > firstIndex) {
                firstIndex = index;
            }
        }

        if (firstIndex == -1) return parse(exp, remainingSymbols);

        // Look for the closest stopSymbol on both sides
        int firstI = -1;
        int lastI = -1;
        int i = firstIndex - 1;
        while (i < exp.length()) {
            if (i == -1) {
                firstI = 0;
                i = firstIndex + 1;
            }
            String s = exp.substring(i, i + 1);
            if (firstI == -1) {
                if (stopSymbols.contains(s)) {
                    firstI = i + 1;
                    i = firstIndex + 2;
                }
                i--;
            } else {
                if (stopSymbols.contains(s)) {
                    lastI = i;
                    break;
                }
                i++;
            }
        }
        if (lastI == -1) lastI = exp.length();

        // Break down the expression; rinse and repeat
        if (exp.substring(firstI, lastI).equals(exp)) {
            return exp;
        }
        expressions.add(parse(exp.substring(firstI, lastI), symbols));
        exp = exp.substring(0, firstI) + "\\" + expressions.size() + exp.substring(lastI, exp.length());

        return parse(exp, symbols);
    }

    class ParseException extends Exception {
        private static final long serialVersionUID = 300L;

        public ParseException() {
        }

        public ParseException(String var1) {
            super(var1);
        }

        public ParseException(String var1, Throwable var2) {
            super(var1, var2);
        }

        public ParseException(Throwable var1) {
            super(var1);
        }

    }
}
