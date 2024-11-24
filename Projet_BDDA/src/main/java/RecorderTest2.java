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
        List<ColInfo> listeColonnesInfo = Arrays.asList(
                new ColInfo("Nom", "CHAR", 12),
                new ColInfo("Prenom", "CHAR", 6),
                new ColInfo("Age", "INT", 4)
        );

        // Création de la relation
        RelationBis relation = new RelationBis("Etudiant", listeColonnesInfo.size(), diskManager, bufferManager);
        relation.setColonnes(listeColonnesInfo);

        // Tests
        ajouteDataPageTest(relation);
        InsertRecordTest(relation);
        GetDataPagesTest(relation);
        WriteRecordDataPageTest(relation);
        GetRecordsInDataPageTest(relation);

        // Vidage des buffers pour synchronisation
        bufferManager.FlushBuffers();

        System.out.println("********* Fin des tests *********");
    }

    // Test d'ajout de pages de données
    public static void ajouteDataPageTest(RelationBis relation) throws IOException {
        System.out.println("\n************** Test : Ajout de pages de données **************");
        for (int i = 0; i < 5; i++) {
            relation.addDataPage();
            System.out.println("Page de données ajoutée n°" + (i + 1));
        }
    }

    // Test d'insertion de records
    public static void InsertRecordTest(RelationBis relation) throws IOException {
        System.out.println("\n************** Test : Insertion de records **************");

        // Insertion des records
        for (int i = 0; i < 20; i++) {
            Record record = createTestRecord(i);
            relation.InsertRecord(record);
            System.out.println("Record inséré : " + record);
        }

        // Vérification des records insérés
        System.out.println("\n************** Test : Vérification des records insérés **************");
        relation.GetAllRecords().forEach(record -> System.out.println("Record : " + record));
    }

    // Création d'un record pour le test
    public static Record createTestRecord(int i) {
        List<Object> values = Arrays.asList("Boussad", "Rayan", 20 + i);
        String[] colonnes = values.stream().map(Object::toString).toArray(String[]::new);
        return new Record(colonnes);
    }

    // Test d'obtention des pages de données
    public static void GetDataPagesTest(RelationBis relation) throws IOException {
        System.out.println("\n************** Test : Obtention des pages de données **************");
        List<PageId> listePage = relation.getDataPages();
        System.out.println("Pages de données disponibles : " + listePage);
    }

    // Test d'écriture de records dans les pages de données
    public static void WriteRecordDataPageTest(RelationBis relation) throws IOException {
        System.out.println("\n************** Test : Écriture de records dans les pages de données **************");

        // Création de records
        Record[] records = {
                new Record(new String[]{"Boussad", "Rayan", "20"}),
                new Record(new String[]{"Nayar", "Bassoud", "21"}),
                new Record(new String[]{"Chato", "dit", "22"})
        };

        // Récupérer une page disponible
        PageId pageId = relation.getFreeDataPageId(40);

        if (pageId != null) {
            Arrays.stream(records).forEach(record -> {
                try {
                    relation.writeRecordToDataPage(record, pageId);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            });
            System.out.println("Records écrits dans la page.");
        } else {
            System.out.println("ERREUR : Aucune page de données disponible pour écrire les records.");
        }
    }

    // Test de récupération des records dans une page de données
    public static void GetRecordsInDataPageTest(RelationBis relation) throws IOException {
        System.out.println("\n************** Test : Récupération des records dans une page de données **************");

        PageId pageId = new PageId(0, 1);  // Exemple avec une page spécifique
        List<Record> records = relation.getRecordsInDataPage(pageId);

        System.out.println("Records présents dans la page " + pageId + " :");
        for (int i = 0; i < records.size(); i++) {
            System.out.println("Record n°" + (i + 1) + " : " + records.get(i));
        }
    }

    // Initialisation de la Header Page
    public static PageId ajouteHeaderPage(DiskManager diskManager, BufferManagerBis bufferManager) throws IOException {
        System.out.println("\n************** Initialisation de la Header Page **************");
        PageId headerPage = diskManager.AllocPage();
        System.out.println("Header Page initialisée à l'emplacement : " + headerPage);
        ByteBuffer buffHeader = bufferManager.GetPage(headerPage);
        buffHeader.putInt(0); // nbDataPage = 0

        // Réservation d'espaces pour 10 pages
        for (int i = 0; i < 10; i++) {
            buffHeader.putInt(0); // fileId = 0
            buffHeader.putInt(0); // pageId = 0
        }

        // Libération de la page d'en-tête après modification
        bufferManager.FreePage(headerPage, true);

        System.out.println("Page d'en-tête initialisée et libérée avec succès.");
        return headerPage;
    }
}
