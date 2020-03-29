package demos;

public class Test {
    private int id;
    private String name;

    public Test(int id, String name) {
        this.id = id;
        this.name = name;
    }

    public static void main(String[] args) {
        Test test = new Test(23,"Marius");
        System.out.println(test.id + " "+ test.name);
    }
}
