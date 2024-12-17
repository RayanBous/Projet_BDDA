import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.Serializable;

import org.json.JSONException;
import org.json.JSONObject;

public class DBConfig implements Serializable {
    private static final long serialVersionUID = 1L;

    private String dbpath;
    private long pageSize;
    private long dm_maxFileSize;
    private int bm_buffercount;
    private String bm_policy;

    public DBConfig(String dbpath, long pageSize, long dm_maxFileSize, int bm_buffercount, String bm_policy) { //Constructeur
        this.dbpath = dbpath;
        this.pageSize = pageSize;
        this.dm_maxFileSize = dm_maxFileSize;
        this.bm_buffercount = bm_buffercount;
        this.bm_policy = bm_policy;
    }

    public int getBm_BufferCount()  {
        return this.bm_buffercount;
    }

    public String getBm_Policy()    {
        return this.bm_policy;
    }

    public void setBm_bufferCount(int bm_buffercount) {
        this.bm_buffercount = bm_buffercount;
    }

    public void setBm_Policy(String bm_policy)  {
        this.bm_policy = bm_policy;
    }

    public String getDbPath() {
        return dbpath;
    }

    public void setDbPath(String dbpath) {
        this.dbpath = dbpath;
    }

    public long getPageSize() {
        return pageSize;
    }

    public void setPageSize(long pageSize) {
        this.pageSize = pageSize;
    }

    public long getDm_maxFileSize() {
        return dm_maxFileSize;
    }

    public void setDm_maxFileSize(long dm_maxFileSize) {
        this.dm_maxFileSize = dm_maxFileSize;
    }    

    public static DBConfig loadDBConfig (String fichierConfig) {
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
                return new DBConfig(js.getString("dbpath"), js.getLong("pageSize"), js.getLong("dm_maxFileSize"), js.getInt("bm_bufferCount"), js.getString("bm_policy"));
            }catch(JSONException e){
                e.printStackTrace();
                return null;
            }
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("DBConfig{");
        sb.append("dbpath=").append(this.dbpath);
        sb.append(", pageSize=").append(this.pageSize);
        sb.append(", dm_maxFileSize=").append(this.dm_maxFileSize);
        sb.append('}');
        return sb.toString();
    }



}
