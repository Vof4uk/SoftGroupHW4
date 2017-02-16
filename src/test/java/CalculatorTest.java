import org.junit.*;

import java.io.*;


public class CalculatorTest {

    private static Calculator calculator;
    private static final PrintStream SOUT = System.out;
    private static final InputStream SIN = System.in;

    @BeforeClass
    public static void before(){
        calculator = new Calculator();

    }

    @AfterClass
    public static void after(){
        System.setOut(SOUT);
        System.setIn(SIN);
    }

    @Test
    public void readFromConsoleSuccessTest(){

        ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(String.format("2+2%n").getBytes());
        System.setIn(new DataInputStream(byteArrayInputStream));
        String s = calculator.readFromConsoleAndCheck();

        Assert.assertEquals("2+2", s);
    }

    @Test
    public void addTest(){
        ByteArrayOutputStream arrayOutputStream = new ByteArrayOutputStream();
        System.setOut(new PrintStream(arrayOutputStream));

        byte[] arr = arrayOutputStream.toByteArray();
    }

    @Test
    public void splitInputTest(){
        String[] s0 ={"2", "4.5"};
        String[] s1 = calculator.splitInput("2+4.5");
        String[] s2 = calculator.splitInput("2-4.5");
        String[] s3 = calculator.splitInput("2*4.5");
        String[] s4 = calculator.splitInput("2/4.5");
        Assert.assertArrayEquals(s0, s1);
        Assert.assertArrayEquals(s0, s2);
        Assert.assertArrayEquals(s0, s3);
        Assert.assertArrayEquals(s0, s4);
    }

    @Test(expected = RuntimeException.class)
    public void readFromConsoleFailTest(){
        ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(String.format("2p2%n").getBytes());
        System.setIn(new DataInputStream(byteArrayInputStream));
        calculator.readFromConsoleAndCheck();
    }

    @Test
    public void getOperationTest(){
        Assert.assertEquals(Calculator.Operation.PLUS, calculator.getOperation("2+2"));
        Assert.assertEquals(Calculator.Operation.MINUS, calculator.getOperation("2-2"));
        Assert.assertEquals(Calculator.Operation.MULTIPLY, calculator.getOperation("2*2"));
        Assert.assertEquals(Calculator.Operation.DIVIDE, calculator.getOperation("2/2"));
    }

    @Test(expected = RuntimeException.class)
    public void getOperationTestFail(){
        calculator.getOperation("2**2");
    }

    @Test
    public void multiplyDoubleTest(){
        double actual = calculator.multiply(2.22, 2.401);
        double expected = 5.33022;
        Assert.assertEquals(expected, actual, 0.00000000001);
    }

    @Test(expected = RuntimeException.class)
    public void divideByZero(){
        calculator.divide(1., 0);
    }
}
