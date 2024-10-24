import java.nio.ByteBuffer;

public class Relation {
    private String nom;
    private int nbColonnes;
    private ColInfo[] colonnes;

    public Relation(String nom, int nbColonnes, ColInfo[] colonnes) {
        this.nom = nom;
        this.nbColonnes = nbColonnes;
        this.colonnes = colonnes;
    }

    public Relation(String nom, int nbColonnes)	{
        this.nom = nom;
        this.nbColonnes = nbColonnes;
        this.colonnes = new ColInfo[this.nbColonnes];
    }

    public String getNom()  {
        return this.nom;
    }
    public void setNom(String nom)  {
        this.nom = nom;
    }

    public int getNbColonnes()  {
        return this.nbColonnes;
    }
    public void setNbColonnes(int nbColonnes) {
        this.nbColonnes = nbColonnes;
    }

    public ColInfo[] getColonnes()  {
        return this.colonnes;
    }
    public void setColonnes(ColInfo[] colonnes)   {
        this.colonnes = colonnes;
    }

    public int writeRecordToBuffer(Record record, ByteBuffer buffer, int pos) {
        //On commence par determiner le format d'écriture à utiliser et appeler directement la méthode d'écriture correspondante
        for (int i=0; i<this.nbColonnes; i+=1)  {
            if (this.colonnes[i].getType().equals("VARCHAR")){
                return writeRecordToBufferFormatVariable(record, buffer, pos);
            }
        }
        return writeRecordToBufferFormatFixe(record, buffer, pos);
    }

    // Cette méthode écrit dans un buffer en utilisant le format fixe
    public int writeRecordToBufferFormatFixe(Record record, ByteBuffer buffer, int pos)   {
        buffer.position(pos);
        for (int i=0; i<this.nbColonnes; i+=1)  {
            ColInfo colonne = this.colonnes[i];
            String value = record.getAttributs()[i];
            switch (colonne.getType()) {
                case "INT":
                    buffer.putInt(Integer.parseInt(value));
                    break;
                case "REAL":
                    buffer.putFloat(Float.parseFloat(value));
                    break;
                case "CHAR":
                    for (int j=0; j<value.length(); j+=1)    {
                        buffer.putChar(value.charAt(j));
                    }
                    break;
                default:
                    break;
            }
        }
        return buffer.position()-pos;
    }

    // Cette méthode écrit dans un buffer en utilisant le format variable
    public int writeRecordToBufferFormatVariable(Record record, ByteBuffer buffer, int pos)   {
        int positionIemeElement = pos + ((this.nbColonnes+1)*Integer.BYTES);
        buffer.position(pos);
        // Premierement on complete notre buffer avec le tableau de l'offset directory
        for (int i=0; i<this.nbColonnes; i+=1)  {
            buffer.putInt(positionIemeElement);
            switch (this.colonnes[i].getType()) {
                case "INT":
                    positionIemeElement += Integer.BYTES;
                    break;
                case "REAL":
                    positionIemeElement += Float.BYTES;
                    break;
                case "CHAR":
                    positionIemeElement += (Character.BYTES * this.colonnes[i].getTaille());
                    break;
                case "VARCHAR":
                    positionIemeElement += (record.getAttributs()[i].length() * Character.BYTES);
                    break;
                default:
                    break;
            }
        }
        buffer.putInt(positionIemeElement);
        // Maintenant on doit enregistrer nos attributs dans notre buffer
        for (int i=0; i<this.nbColonnes; i+=1)  {
            ColInfo colonne = this.colonnes[i];
            String value = record.getAttributs()[i];
            switch (colonne.getType()) {
                case "INT":
                    buffer.putInt(Integer.parseInt(value));
                    break;
                case "REAL":
                    buffer.putFloat(Float.parseFloat(value));
                    break;
                case "CHAR":
                case "VARCHAR":
                    for (int j=0; j<value.length(); j+=1)    {
                        buffer.putChar(value.charAt(j));
                    }
                    break;
                default:
                    break;
            }
        }
        return buffer.position()-pos;
    }

    public int readFromBuffer(Record record, ByteBuffer buffer, int pos)    {
        //On commence par determiner le format de lécture à utiliser et appeler directement la méthode lecture correspondante
        for (int i=0; i<this.nbColonnes; i+=1)  {
            if (this.colonnes[i].getType().equals("VARCHAR")){
                return readFromBufferFormatVariable(record, buffer, pos);
            }
        }
        return readFromBufferFormatFixe(record, buffer, pos);
    }

    public int readFromBufferFormatFixe(Record record, ByteBuffer buffer, int pos)  {
        buffer.position(pos);
        for (int i=0; i<this.nbColonnes; i+=1)  {
            ColInfo colonne = this.colonnes[i];
            switch(colonne.getType()){
                case "INT":
                    String attributInt = buffer.getInt()+"";
                    record.setAttribut(i, attributInt);
                    break;
                case "REAL":
                    String attributFloat = buffer.getFloat()+"";
                    record.setAttribut(i, attributFloat);
                    break;
                case "CHAR":
                    StringBuilder attributString = new StringBuilder();
                    for (int j=0; j<colonne.getTaille(); j+=1){
                        attributString.append(buffer.getChar());
                    }
                    record.setAttribut(i, attributString.toString());
                    break;
                default:
                    break;
            }
        }
        return buffer.position()-pos;
    }

    public int readFromBufferFormatVariable(Record record, ByteBuffer buffer, int pos)  {
        buffer.position(pos);
        int positionIemeElement;
        for (int i=0; i<this.nbColonnes; i+=1)  {
            ColInfo colonne = this.colonnes[i];
            positionIemeElement = buffer.getInt();
            switch(colonne.getType())  {
                case "INT":
                    String attributInt = buffer.getInt(positionIemeElement) + "";
                    record.setAttribut(i, attributInt);
                    break;
                case "REAL":
                    String attributFloat = buffer.getFloat(positionIemeElement) + "";
                    record.setAttribut(i, attributFloat);
                    break;
                case "CHAR":
                case "VARCHAR":
                    int positionIemePlusUnElement = buffer.getInt(pos+((i+1)*Integer.BYTES));
                    StringBuilder attributString = new StringBuilder();
                    for (int j=positionIemeElement; j<positionIemePlusUnElement; j+=Character.BYTES)  {
                        attributString.append(buffer.getChar(j));
                    }
                    record.setAttribut(i,  attributString.toString());
                    break;
                default:
                    break;
            }
        }
        return buffer.getInt()-pos;
    }

}