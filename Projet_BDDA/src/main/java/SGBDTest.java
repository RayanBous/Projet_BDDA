import java.io.IOException;

public class SGBDTest {
    public static void main(String[] args) throws IOException {
        DBConfig dbconfig = DBConfig.loadDBConfig("src/main/java/config.json");
        SGBD sgbd = new SGBD(dbconfig);
        try{
            sgbd.Run();
        }catch(IOException e){
            e.printStackTrace();
        }
    }
}
