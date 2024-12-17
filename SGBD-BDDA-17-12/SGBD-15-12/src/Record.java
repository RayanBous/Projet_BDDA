import java.util.Arrays;
import java.util.List;

public class Record {
    private String[] colonnes;

    public Record(String [] colonnes)  {
        this.colonnes = colonnes;
    }

    public Record(int nbColonnes) {
        this.colonnes = new String[nbColonnes];
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
    
    @Override
    public String toString()    {
        StringBuilder content = new StringBuilder();
        for (int i=0; i<this.colonnes.length; i+=1) {
            content.append(this.colonnes[i]);
            content.append((i==this.colonnes.length-1) ? " ." : " ; ");
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
