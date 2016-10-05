import java.io.DataOutputStream;

/**
 * Created by Tudor on 9/20/2016.
 */
public class Person {
    private String name;
    private DataOutputStream socket;

    public Person(String name, DataOutputStream socket) {
        this.name = name;
        this.socket = socket;
    }

    public String getName() {
        return name;
    }

    public DataOutputStream getStream() {
        return socket;
    }
}
