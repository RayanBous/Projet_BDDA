import java.nio.ByteBuffer;
import java.util.List;
import java.util.Arrays;
import java.util.ArrayList;

public class RecorderTest {
    public static void main (String [] args)    {

        ColInfo[] colonnes = {
                new ColInfo("Code", "CHAR", 50),
                new ColInfo("Age", "INT", 1),
                new ColInfo("Note", "REAL", 1),
                new ColInfo("Commentaire", "CHAR", 50)
        };

        Relation table = new Relation("Etudiant", 4, colonnes);

        String[] valeursRecord = {"azerty", "19", "12.3", "assez bien"};
        Record record = new Record(valeursRecord);

        ByteBuffer buffer = ByteBuffer.allocate(1024);
        System.out.println("Ecriture du record dans le buffer...");
        int nbBytesEcrits = table.writeRecordToBuffer(record, buffer, 102);

        Record recordLu = new Record(4);
        System.out.println("Lécture du record depuis le buffer...");
        int nbBytesLus = table.readFromBuffer(recordLu, buffer, 102);

        System.out.println("Nb d'octets écrits = " + nbBytesEcrits + " Octets.");
        System.out.println("Nb d'octets lus = " + nbBytesLus + " Octets.");

        System.out.println("Record original = " + record.toString());
        System.out.println("Record lu = " + recordLu.toString());

        if (record.equals(recordLu)){
            System.out.println("Test réussi : les données sont identiques !");
        }   else {
            System.out.println("Test échoué : les données ne sont pas identiques !");
        }

    }
}