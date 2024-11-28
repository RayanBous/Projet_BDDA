import java.util.HashMap;
import java.util.Map;

public class Database {
    private String nom;
    private HashMap<String, RelationBis> table;

    public Database(String nom){
        this.nom = nom;
        this.table = new HashMap<>();
    }

    public void ajouterTable(RelationBis t){
        this.table.put(nom, t);
    }

    public void supprimerTable(String nom){
        this.table.remove(nom);
    }

    public void supprimerBDD(){
        table.clear();
    }

    public Map<String,RelationBis> getTable(String nom){
        return table;
    }

    /*public RelationBis getTable(String nom){
        return table;
    }*/

    public String getNom(){
        return nom;
    }

    public String toString() {
        StringBuffer sb = new StringBuffer();
        sb.append("Nom : " + nom);
        for (String tabl : table.keySet()) {
            sb.append("Table : " + tabl);
        }
        return sb.toString();
    }

}
