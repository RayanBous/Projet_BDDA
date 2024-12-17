import java.nio.ByteBuffer;
import java.util.List;
import java.util.Arrays;
import java.util.ArrayList;


public class RelationTests {
    public static void main (String [] args)    {  
        System.out.println("**********  Initialisation de tout le programme  **********");

        DBConfig config = DBConfig.loadDBConfig("src/config.json");
        DiskManager disk = new DiskManager(config);
        disk.loadState();
        BufferManager buffer = new BufferManager(config, disk);

        ColInfo[] colonnes = {
            new ColInfo("Nom", "VARCHAR", 15),
            new ColInfo("Prenom", "VARCHAR", 15),
            new ColInfo("Age", "INT", 1),
            new ColInfo("Code", "CHAR", 4)
        };

        Relation relation = new Relation("Etudiant", 4, colonnes, null, disk, buffer);
        relation.initializeHeaderPage();

        String[] valeursRecord1 = {"RAAB", "Rayane", "21", "BDJZ"};
        Record record1 = new Record(valeursRecord1);
        String[] valeursRecord2 = {"Boussad", "Rayan", "20", "JDZD"};
        Record record2 = new Record(valeursRecord2);
        String[] valeursRecord3 = {"SI MEHAND", "Sedik", "22", "LDOC"};
        Record record3 = new Record(valeursRecord3);
        String[] valeursRecord4 = {"HATO", "Chatodi", "19", "MCZF"};
        Record record4 = new Record(valeursRecord4);
        String[] valeursRecord5 = {"ZIDANE", "Zinedine", "43", "JDLL"};
        Record record5 = new Record(valeursRecord5);
        String[] valeursRecord6 = {"MBAPPE", "Kylian", "25", "NVZD"};
        Record record6 = new Record(valeursRecord6);
        String[] valeursRecord7 = {"VINICIUS", "Junior", "22", "MMXB"};
        Record record7 = new Record(valeursRecord7);
        String[] valeursRecord8 = {"NEUER", "Manuel", "32", "TURN"};
        Record record8 = new Record(valeursRecord8);
        Record[] records = {record1, record2, record3, record4, record5, record6, record7, record8};

        for (int i=0; i<records.length; i+=1)    {
            RecordId rid = relation.InsertRecord(records[i]);
            if (rid != null)    {
                System.out.println("Record n°" + (i+1) + " inséré avec succés à l'emplacement " + rid);
            }   else  {
                System.out.println("Echec de l'insertion du record n°" + (i+1) + " " + records[i]);
            }
        }
        System.out.println("**********  Test : Vérification des record insérés  **********");
        relation.GetAllRecords().forEach(record -> System.out.println(record));

        disk.saveState();
        buffer.flushBuffers();
        System.out.println("**********  Fin des tests  **********");

    }
}


    //     public class RelationTests {
    //         public static void main (String [] args)    {
                
    //             ColInfo[] colonnes = {
    //                 new ColInfo("Code", "CHAR", 6),
    //                 new ColInfo("Age", "INT", 1),
    //                 new ColInfo("Note", "REAL", 1),
    //                 new ColInfo("Commentaire", "VARCHAR", 10)
    //             };
        
    //             Relation table = new Relation("Etudiant", 4, colonnes);
        
    //             String[] valeursRecord = {"azerty", "19", "12.3", "assez bien"};
    //             Record record = new Record(valeursRecord);
        
    //             ByteBuffer buffer = ByteBuffer.allocate(1024);
    //             System.out.println("Ecriture du record dans le buffer...");
    //             int nbBytesEcrits = table.writeRecordToBuffer(record, buffer, 102);
        
    //             Record recordLu = new Record(4);
    //             System.out.println("Lécture du record depuis le buffer...");
    //             int nbBytesLus = table.readFromBuffer(recordLu, buffer, 102);
        
    //             System.out.println("Nb d'octets écrits = " + nbBytesEcrits + " Octets.");
    //             System.out.println("Nb d'octets lus = " + nbBytesLus + " Octets.");
        
    //             System.out.println("Record original = " + record.toString());
    //             System.out.println("Record lu = " + recordLu.toString());
        
    //             if (record.equals(recordLu)){
    //                 System.out.println("Test réussi : les données sont identiques !");
    //             }   else {
    //                 System.out.println("Test échoué : les données ne sont pas identiques !");
    //             }
    
    //         }
    //     }
    // }
