import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Record {
    private String[] colonnes;
    private RecordId recordId;

    public Record(String[] colonnes)  {
        this.colonnes = colonnes;
        this.recordId = null;
    }

    public Record(){
        this.colonnes = new String[0];
        this.recordId = null;
    }

    public Record(int nbColonnes) {
        this.colonnes = new String[nbColonnes];
    }

    public void setRecordId(RecordId recordId) {
        this.recordId = recordId;
    }

    public void setTuple(ArrayList<Object> a1) {
        // Initialisation du tableau de colonnes avec la taille de l'ArrayList
        this.colonnes = new String[a1.size()];

        // Parcours de l'ArrayList et conversion des objets en String
        for (int i = 0; i < a1.size(); i++) {
            Object obj = a1.get(i);
            this.colonnes[i] = obj != null ? obj.toString() : "null";  // Convertir en chaîne de caractères
        }
    }

    public RecordId getRecordId() {
        return recordId;
    }

    public void setAttributs(String[] colonnes)  {
        this.colonnes = colonnes;
    }

    public String[] getAttributs(){
        return this.colonnes;
    }

    public void setAttribut(int i, String colonne){
        this.colonnes[i] = colonne;
    }

    public String[] getColonnes(){
        return colonnes;
    }

    public int getSize(){
        int taille = 0;
        for(String col : colonnes){
            if(col != null){
                taille += col.length();
            }else{
                taille += 4;
            }
        }
        return taille;
    }

    @Override
    public String toString()    {
        StringBuilder content = new StringBuilder();
        content.append("[");
        for (int i=0; i<this.colonnes.length; i+=1) {
            content.append(this.colonnes[i]);
            content.append((i==this.colonnes.length-1) ? "]" : " | ");
        }
        return content.toString();
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        Record other = (Record) obj;
        if (!Arrays.equals(colonnes, other.getAttributs()))
            return false;
        return true;
    }
}