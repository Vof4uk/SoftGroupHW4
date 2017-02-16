import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

public class Calculator {

    private BufferedReader reader;
    private boolean isOn;

    public static void main(String[] args) {
        Calculator calculator = new Calculator();
        calculator.on();
        calculator.off();
    }

    public void on(){
        isOn = true;
        while (isOn) {
            waitAndCalculate();
        }
    }

    public void waitAndCalculate(){
        String line = readFromConsoleAndCheck();
        if(line.equals("exit")){
            isOn = false;
            return;
        }
        parseAndCalculate(line);
    }

    String readFromConsoleAndCheck(){
        try{
            if(reader == null) {
                reader = new BufferedReader(new InputStreamReader(System.in));
            }
            String line = reader.readLine();
            if(line.trim().equalsIgnoreCase("exit")){
                return "exit";
            }
            if(!line.matches("[0-9]+.?[0-9]*[+/*-][0-9]+.?[0-9]*")){
                throw new RuntimeException("Bad input to console");
            }
            return line;
        }catch (IOException ex){
            throw new RuntimeException("Something wrong with console");
        }
    }

    String[] splitInput(String input){
        return input.split("[+/*-]");
    }

    Operation getOperation(String line){
         String shortenedLine = line.replaceAll("[0-9.]", "");
         if(shortenedLine.length() > 1){
             throw new RuntimeException("Bad input to console");
         }
         switch (shortenedLine){
             case "+": return Operation.PLUS;
             case "-": return Operation.MINUS;
             case "*": return Operation.MULTIPLY;
             case "/": return Operation.DIVIDE;
             default: throw new RuntimeException("Unknown operator");
         }
     }

    void parseAndCalculate(String line){
        String[] members = splitInput(line);
        Operation operation = getOperation(line);
        if(anyDouble(members)){
            doubleCalculations(members, operation);
        }else{
            integerCalculations(members, operation);
        }
    }

    private void integerCalculations(String[] members, Operation operation) {
        int x = Integer.parseInt(members[0]);
        int y = Integer.parseInt(members[1]);
        switch (operation){
            case PLUS:
                System.out.println(add(x, y));
                break;
            case MULTIPLY:
                System.out.println(multiply(x, y));
                break;
            case MINUS:
                System.out.println(minus(x, y));
                break;
            case DIVIDE:
                System.out.println(divide(x, y));
                break;
            default:
                System.out.println("unknown operation");
        }
    }

    private void doubleCalculations(String[] members, Operation operation) {
        double x = Double.parseDouble(members[0]);
        double y = Double.parseDouble(members[1]);
        switch (operation){
            case PLUS:
                System.out.println(add(x, y));
                break;
            case MULTIPLY:
                System.out.println(multiply(x, y));
                break;
            case MINUS:
                System.out.println(minus(x, y));
                break;
            case DIVIDE:
                System.out.println(divide(x, y));
                break;
            default:
                System.out.println("unknown operation");
        }
    }

    private boolean anyDouble(String[] members) {
        for (int i = 0; i < members.length; i++) {
            if(members[i].contains(".")){
                return true;
            }
        }
        return false;
    }

    public int multiply(int x, int y){
        return x * y;
    }

    public double multiply(double x, double y){
        return x * y;
    }

    public int add(int x, int y){
        return x + y;
    }
    public double add(double x, double y){
        return x + y;
    }

    public int divide(int x, int y){
        return x / y;
    }

    public double divide(double x, double y){
        return x / y;
    }

    public int minus(int x, int y){
        return add(x, -y);
}
    public double minus(double x, double y){
        return add(x, -y);
    }

    enum Operation {
        PLUS, MINUS, MULTIPLY, DIVIDE;
    }

    public void off(){
        if(reader != null){
            try {
                reader.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
