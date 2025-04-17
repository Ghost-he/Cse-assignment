class A {
    protected int i;

    public A() {
        multiply(15);
        System.out.println(i);
    }

    public void multiply(int i) {
        this.i = 5 * i;
    }
}

class B extends A {
    public B() {
        super();
    }

    public void multiply(int i) {
        this.i = 3 * i;
    }
}

public class Main {
    public static void main(String[] args) {
        B obj = new B();
    }
}