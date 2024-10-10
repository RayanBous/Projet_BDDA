import java.nio.ByteBuffer;
import java.util.ArrayList;

public class Relation {
    private String nom_relation;
    private int nb_colonne;
    private ColInfo[] colinfo;

    public Relation(String nom_relation, int nb_colonne, ColInfo[] colinfo) {
        this.nom_relation = nom_relation;
        this.nb_colonne = nb_colonne;
        this.colinfo = colinfo;
    }

    public String getNom_relation() {
        return nom_relation;
    }

    public void setNom_relation(String nom_relation) {
        this.nom_relation = nom_relation;
    }

    public int getNb_colonne() {
        return nb_colonne;
    }

    public void setNb_colonne(int nb_colonne) {
        this.nb_colonne = nb_colonne;
    }

    public ColInfo[] getColinfo() {
        return colinfo;
    }

    public void setColinfo(ColInfo[] colinfo) {
        this.colinfo = colinfo;
    }

    public int writeRecordToBuffer(Record record, ByteBuffer buffer, int pos) {
        int position = pos;

        for (int i = 0; i < nb_colonne; i++) {
            ColInfo col = colinfo[i];
            Object value = record.getRecord().get(i);

            switch (col.getType_colonne()) {
                case "INT":
                    int intValue = Integer.parseInt(value.toString());
                    buffer.putInt(position, intValue);
                    position = position + Integer.BYTES; // INT takes 4 bytes
                    break;

                case "REAL":
                    float floatValue = Float.parseFloat(value.toString());
                    buffer.putFloat(position, floatValue);
                    position = position + Float.BYTES; // REAL takes 4 bytes
                    break;

                case "CHAR":
                    String charValue = value.toString();
                    if (charValue.length() > 0) {
                        buffer.putChar(position, charValue.charAt(0));
                        position = position + Character.BYTES; // CHAR takes 2 bytes
                    }
                    break;

                case "VARCHAR":
                    String varcharValue = value.toString();
                    byte[] varcharBytes = varcharValue.getBytes();
                    buffer.putInt(position, varcharBytes.length);
                    position += Integer.BYTES; // Ajouter la taille de l'entier qui stocke la longueur
                    buffer.position(position);
                    buffer.put(varcharBytes);
                    position += varcharBytes.length;
                    break;

                default:
                    System.err.println("Type de colonne non reconnu: " + col.getType_colonne());
            }
        }
        return position - pos;
    }

    public int readFromBuffer(Record record, ByteBuffer buffer, int pos) {
        int position = pos;
        ArrayList<Object> values = new ArrayList<>();

        for (int i = 0; i < nb_colonne; i++) {
            ColInfo col = colinfo[i];

            switch (col.getType_colonne()) {
                case "INT":
                    int intValue = buffer.getInt(position);
                    values.add(intValue);
                    position = position + Integer.BYTES;
                    break;
                case "REAL":
                    float floatValue = buffer.getFloat(position);
                    values.add(floatValue);
                    position = position + Float.BYTES;
                    break;
                case "CHAR":
                    char charValue = buffer.getChar(position);
                    values.add(charValue);
                    position = position + Character.BYTES;
                    break;
                case "VARCHAR" :
                    int varcharLength = buffer.getInt(position);
                    position = position + Integer.BYTES;
                    byte[] varcharBytes = new byte[varcharLength];
                    buffer.get(varcharBytes, 0, varcharLength);
                    String varcharValue = new String(varcharBytes);
                    values.add(varcharValue);
                    position += varcharLength;
                    break;
                default:
                    System.err.println("Type de colonne non reconnu: " + col.getType_colonne());
            }
        }

        // Stocker les valeurs lues dans le record
        record.setRecord(values);

        // Retourner le nombre total d'octets lus
        return position - pos;
    }
}
