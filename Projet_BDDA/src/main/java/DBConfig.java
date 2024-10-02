import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import org.json.JSONException;
import org.json.JSONObject;
import java.io.File;

public class DBConfig {
    public String dbpath;
    public long pagesize;
    public long dm_maxfilesize;

    public DBConfig(String dbpath, long pagesize, long dm_maxfilesize) { //Constructeur
        this.dbpath = dbpath;
        this.pagesize = pagesize;
        this.dm_maxfilesize = dm_maxfilesize;
    }

    public String getDbPath() {
        return dbpath;
    }

    public void setDbPath(String dbpath) {
        this.dbpath = dbpath;
    }

    public long getPagesize(){
        return pagesize;
    }

    public void setPagesize(long pagesize){
        this.pagesize = pagesize;
    }

    public long getDm_maxfilesize(){
        return dm_maxfilesize;
    }

    public void setDm_maxfilesize(long dm_maxfilesize){
        this.dm_maxfilesize = dm_maxfilesize;
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
                long pagesize = (long) js.getDouble("pagesize");
                long dm_maxfilesize = (long) js.getDouble(("dm_maxfilesize"));
                return new DBConfig(js.getString("dbpath"), pagesize, dm_maxfilesize);
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
