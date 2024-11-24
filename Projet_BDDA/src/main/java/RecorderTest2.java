import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class RecorderTest2 {
    public static void main(String[] args) throws IOException {
        System.out.println("********* Initialisation de tout le programme *********");

        // Chargement de la configuration
        DBConfig config = DBConfig.loadDBConfig("src/main/java/config.json");
        DiskManager diskManager = new DiskManager(config);
        BufferManagerBis bufferManager = new BufferManagerBis(config, diskManager);

        // Initialisation de la Header Page
        PageId headerPageId = ajouteHeaderPage(diskManager, bufferManager);

        // Définition des colonnes pour la relation
        List<ColInfo> listeColonnesInfo = new ArrayList<>();
        listeColonnesInfo.add(new ColInfo("Nom", "CHAR", 12));
        listeColonnesInfo.add(new ColInfo("Prenom", "CHAR", 6));
        listeColonnesInfo.add(new ColInfo("Age", "INT", 4));

        // Création de la relation avec le nouveau constructeur
        RelationBis relation = new RelationBis("Etudiant", listeColonnesInfo.size(), headerPageId, diskManager, bufferManager, listeColonnesInfo);

        // Test d'ajout de pages de données
        ajouteDataPageTest(relation);

        // Test d'insertion de records
        InsertRecordTest(relation);

        // Test d'obtention des pages de données
        GetDataPagesTest(relation);

        // Test d'écriture et de récupération des records dans les pages de données
        WriteRecordDataPageTest(relation);
        GetRecordsInDataPageTest(relation);

        // Vidage des buffers pour synchroniser avec le disque
        bufferManager.FlushBuffers();

        System.out.println("********* Fin des tests *********");
    }

    public static void ajouteDataPageTest(RelationBis relation) throws IOException {
        System.out.println("\n************** Test : Ajout de pages de données **************");
        for (int i = 0; i < 5; i++) {
            relation.addDataPage();
            System.out.println("Page de données ajoutée n°" + (i + 1));
        }
    }

    public static void InsertRecordTest(RelationBis relation) throws IOException {
        System.out.println("\n************** Test : Insertion de records **************");

        for (int i = 0; i < 20; i++) {
            ArrayList<Object> a2 = new ArrayList<>(Arrays.asList("Boussad", "Rayan", 20 + i));
            String[] colonnes = new String[a2.size()];
            for (int j = 0; j < a2.size(); j++) {
                colonnes[j] = a2.get(j).toString();
            }

            Record record = new Record(colonnes);
            relation.InsertRecord(record);
            System.out.println("Record inséré : " + record);
        }

        System.out.println("Tous les records insérés : " + relation.GetAllRecords());
    }

    public static void GetDataPagesTest(RelationBis relation) throws IOException {
        System.out.println("\n************** Test : Obtention des pages de données **************");
        List<PageId> listePage = relation.getDataPages();
        System.out.println("Pages de données disponibles : " + listePage);
    }

    public static void WriteRecordDataPageTest(RelationBis relation) throws IOException {
        System.out.println("\n************** Test : Écriture de records dans les pages de données **************");

        // Création de trois records pour le test
        Record r1 = new Record(new String[]{"Boussad", "Rayan", "20"});
        Record r2 = new Record(new String[]{"Nayar", "Bassoud", "21"});
        Record r3 = new Record(new String[]{"Chato", "dit", "22"});

        // Écriture des records dans des pages disponibles
        relation.writeRecordToDataPage(r1, relation.getFreeDataPageId(38));
        relation.writeRecordToDataPage(r2, relation.getFreeDataPageId(38));
        relation.writeRecordToDataPage(r3, relation.getFreeDataPageId(38));

        System.out.println("Records écrits dans les pages.");
    }

    public static void GetRecordsInDataPageTest(RelationBis relation) throws IOException {
        System.out.println("\n************** Test : Récupération des records dans une page de données **************");

        PageId pageId = new PageId(0, 1);
        List<Record> records = relation.getRecordsInDataPage(pageId);

        System.out.println("Records présents dans la page " + pageId + " :");
        for (int i = 0; i < records.size(); i++) {
            System.out.println("Record n°" + (i + 1) + " : " + records.get(i));
        }
    }

    public static PageId ajouteHeaderPage(DiskManager diskManager, BufferManagerBis bufferManager) throws IOException {
        System.out.println("\n************** Initialisation de la Header Page **************");
        PageId headerPage = diskManager.AllocPage(); // Allocation d'une page

        ByteBuffer buff = bufferManager.GetPage(headerPage);
        System.out.println("Header Page initialisée à l'emplacement : " + headerPage);

        bufferManager.FreePage(headerPage, true); // Libération après initialisation
        return headerPage;
    }
}
