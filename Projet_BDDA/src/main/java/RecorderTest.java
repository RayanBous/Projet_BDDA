/*import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.List;

public class RecorderTest {
    public static void main(String[] args) {

        // Création de la configuration de la base de données
        DBConfig dbconfig = new DBConfig("DataBase", 4096, 12287, 19, "LRU");

        DiskManager diskManager = new DiskManager(dbconfig);
        BufferManager bufferManager = new BufferManager(dbconfig, diskManager, 5);

        ColInfo[] colonnes = {
                new ColInfo("Code", "CHAR", 4),
                new ColInfo("Age", "INT", 1),
                new ColInfo("Note", "REAL", 1),
                new ColInfo("RayanBoussad", "VARCHAR", 50)
        };

        PageId headerPageId = diskManager.AllocPage();

        Relation table = new Relation("Etudiant", 4, colonnes, headerPageId, diskManager, bufferManager, true);

        String[] valeursRecord = {"azerty", "25", "14.5", "excellent"};
        Record record = new Record(valeursRecord);

        ByteBuffer buffer = ByteBuffer.allocate((int) dbconfig.getPagesize());

        System.out.println("Écriture du record dans le buffer...");
        int nbBytesEcrits = table.writeRecordToBuffer(record, buffer, 0);
        System.out.println("Nombre d'octets écrits = " + nbBytesEcrits + " octets.");

        Record recordLu = new Record(4);
        System.out.println("Lecture du record depuis le buffer...");
        int nbBytesLus = table.readFromBuffer(recordLu, buffer, 0);
        System.out.println("Nombre d'octets lus = " + nbBytesLus + " octets.");

        System.out.println("Record original : " + record);
        System.out.println("Record lu : " + recordLu);

        if (record.equals(recordLu)) {
            System.out.println("Test réussi : les données sont identiques !");
        } else {
            System.out.println("Test échoué : les données ne sont pas identiques !");
        }

        try {
            System.out.println("Insertion du record dans une page de données...");
            RecordId recordId = table.insertRecord(record);
            if (recordId != null) {
                System.out.println("Record inséré avec succès avec RecordId : " + recordId);
            } else {
                System.out.println("Échec de l'insertion du record.");
            }
        } catch (IOException e) {
            System.err.println("Erreur lors de l'insertion du record : " + e.getMessage());
        }

        try {
            System.out.println("Lecture de tous les enregistrements de la table...");
            List<Record> records = table.getAllRecords();
            for (Record r : records) {
                System.out.println(r);
            }
        } catch (IOException e) {
            System.err.println("Erreur lors de la lecture des enregistrements : " + e.getMessage());
        }
    }
}*/
