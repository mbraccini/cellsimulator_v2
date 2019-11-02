package utility;

import java.util.*;

public class ConvertInfixToRPN {

    static boolean isNumber(String str) {
        try{
            Double.valueOf(str);
            return true;
        } catch(Exception e){
            return false;
        }
    }

    Queue<String> convertInfixToRPN(String[] infixNotation) {
        Map<String, Integer> prededence = new HashMap<>();
        prededence.put("/", 5);
        prededence.put("*", 5);
        prededence.put("+", 4);
        prededence.put("-", 4);
        prededence.put("(", 0);

        Queue<String> Q = new LinkedList<>();
        Stack<String> S = new Stack<>();

        for (String token : infixNotation) {
            if ("(".equals(token)) {
                S.push(token);
                continue;
            }

            if (")".equals(token)) {
                while (!"(".equals(S.peek())) {
                    Q.add(S.pop());
                }
                S.pop();
                continue;
            }
            // an operator
            if (prededence.containsKey(token)) {
                while (!S.empty() && prededence.get(token) <= prededence.get(S.peek())) {
                    Q.add(S.pop());
                }
                S.push(token);
                continue;
            }

            if (isNumber(token)) {
                Q.add(token);
                continue;
            }

            throw new IllegalArgumentException("Invalid input");
        }
        // at the end, pop all the elements in S to Q
        while (!S.isEmpty()) {
            Q.add(S.pop());
        }

        return Q;
    }

    public static void main(String[] args) {
        String[] infixNotation = {"33", "+", "23"};
        System.out.println(new ConvertInfixToRPN().convertInfixToRPN(infixNotation));

        String[] infixNotation2 = {"2", "*", "(", "3", "+", "4", "/", "2", ")"};
        System.out.println(new ConvertInfixToRPN().convertInfixToRPN(infixNotation2));

        String[] infixNotation3 = {"(","!","(","(","S_1","==","1",")","||","(","S_5","==","1",")",")",")"};
        System.out.println(new ConvertInfixToRPN().convertInfixToRPN(infixNotation3));


    }
}
