import java.io.Serializable;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public class Database implements Serializable {
    private static final long serialVersionUID = 1L;
    private String nom;
    private HashMap<String, Relation> table;

    public Database(String nom){
        this.nom = nom;
        this.table = new HashMap<>();
    }

    public void ajouterTable(Relation t){
        this.table.put(nom, t);
    }

    public void supprimerTable(String nom){
        if(table.remove(nom) == null){
            System.err.println("TABLE NON SUPPRIMER CAR ELLE EXISTE PAS");
        }else{
            System.out.println("TABLE CORRECTEMENT SUPPRIMER");
        }
    }

    public void supprimerBDD(){
        table.clear();
    }

    public Map<String,Relation> getTable(String nom){
        return table;
    }

    /*public Relation getTable(String nom){
        return table;
    }*/

    public Collection<Relation> getTables() {
        return table.values();
    }

    public String getNom(){
        return nom;
    }

    boolean containsTable(String nomTable) {
        return table.containsKey(nomTable);  // Assurez-vous que "tables" est un Map<String, Relation> dans votre classe Database
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
