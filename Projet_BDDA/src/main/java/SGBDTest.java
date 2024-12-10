import java.io.IOException;

public class SGBDTest {
    public static void main (String [] args) throws IOException {
        DBConfig config = DBConfig.loadDBConfig("src/main/java/config.json");
        SGBD mySGBD = new SGBD (config);
        mySGBD.Run();
    }
}
