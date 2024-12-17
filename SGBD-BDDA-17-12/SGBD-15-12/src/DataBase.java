import java.io.Serializable;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public class DataBase implements Serializable {
    private static final long serialVersionUID = 1L;
    
    private String nom;
    private HashMap <String, Relation> tables;

    public HashMap <String, Relation> getRelations()    {
        return this.tables;
    }

    public DataBase (String nom){
        this.nom = nom;
        this.tables = new HashMap<>();
    }

    public void addTable (Relation t){
        if (!this.tables.containsKey(t.getNom()))    {
            this.tables.put(t.getNom(), t);
            System.out.println("La table (" + t.getNom() + ") ajoutée à la base de données {" + this.nom + "}");
        }   else    {
            System.out.println("La table : + " + t.getNom() + " existe déjà");
        }
    }

    public void removeTable(String nom){
        if(this.tables.remove(nom) == null){
            System.err.println("Erreur : Erreur produite lors de la suppression de la table ("+ nom + ")... la table n'existe pas !");
        } 
    }

    public void removeBDD () {
        this.tables.clear();
    }

    public Relation getTable (String nom)    {
        return this.tables.get(nom);
    }

    public Collection<Relation> getTables() {
        return this.tables.values();
    }

    public String getNom(){
        return nom;
    }

    public boolean containsTable (String nom) {
        return (this.tables !=null && this.tables.containsKey(nom));  // Assurez-vous que "tables" est un Map<String, Relation> dans votre classe Database
    }

    public String toString() {
        StringBuffer sb = new StringBuffer();
        sb.append("Base de donnée : " + nom + "\nTables : \n"); int i=1;
        for (Relation table : tables.values()) {
            sb.append(i+")  ");i+=1;
            sb.append(table.toString());
        }
        return sb.toString();
    }
}