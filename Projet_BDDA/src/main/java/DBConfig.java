import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import org.json.JSONException;
import org.json.JSONObject;
import java.io.File;

public class DBConfig {
    private String dbpath;

    public DBConfig(String dbpath) { //Constructeur
        this.dbpath = dbpath;
    }

    public String getDbPath() {
        return dbpath;
    }

    public void setDbPath(String dbpath) {
        this.dbpath = dbpath;
    }

    public static DBConfig loadDBConfig(String fichierConfig) {
        File fichier = new File(fichierConfig);

        if (!fichier.exists()) { //On vérifie ici si le fichier existe
            System.err.println("Erreur : Le fichier " + fichierConfig + " n'existe pas.");
            return null;
        }

        if (!fichierConfig.endsWith(".json")) { //Si le suffixe != .json, c'est pas bon
            System.err.println("Erreur : Le fichier doit être au format JSON.");
            return null;
        }

        try {
            StringBuffer sb = new StringBuffer();
            FileReader f = new FileReader(fichierConfig);
            BufferedReader br = new BufferedReader(f);
            String s = null;
            while ((s = br.readLine()) != null) {
                sb.append(s);
            }

            br.close();
            try {
                JSONObject js = new JSONObject(sb.toString());
                return new DBConfig(js.getString("dbpath"));
            }catch(JSONException e){
                e.printStackTrace();
                return null;
            }
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }
}
