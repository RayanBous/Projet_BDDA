import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.List;

public class RelationTest2 {
    public static void main(String[] args) throws IOException {
        System.out.println("********* Initialisation de tout le programme *********");

        DBConfig config = DBConfig.loadDBConfig("src/main/java/config.json");  // Chemin vers config.json
        DiskManager diskManager = new DiskManager(config);
        BufferManagerBis bufferManager = new BufferManagerBis(config, diskManager);

        diskManager.creerNouveauFichier();

        PageId headerPageId = ajouteHeaderPage(diskManager, bufferManager);

        List<ColInfo> listeColonnesInfo = Arrays.asList(
                new ColInfo("Nom", "CHAR", 12),
                new ColInfo("Prenom", "CHAR", 6),
                new ColInfo("Age", "INT", 4)
        );

        RelationBis relation = new RelationBis("Etudiant", listeColonnesInfo.size(), diskManager, bufferManager);
        relation.setColonnes(listeColonnesInfo);

        relation.setHeaderPageId(headerPageId);

        ajouteDataPageTest(relation);
        InsertRecordTest(relation);
        GetDataPagesTest(relation);
        WriteRecordDataPageTest(relation);
        GetRecordsInDataPageTest(relation);

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
            Record record = createTestRecord(i);
            RecordId rid = relation.InsertRecord(record); // Utilisation de la valeur retournée
            if (rid != null) {
                System.out.println("Record inséré avec succès : " + record + " à l'emplacement : " + rid);
            } else {
                System.out.println("Échec de l'insertion pour le record : " + record);
            }
        }

        System.out.println("\n************** Test : Vérification des records insérés **************");
        relation.GetAllRecords().forEach(record -> System.out.println("Record : " + record));
    }

    public static Record createTestRecord(int i) {
        List<Object> values = Arrays.asList("Boussad", "Rayan", 20 + i);
        String[] colonnes = values.stream().map(Object::toString).toArray(String[]::new);
        return new Record(colonnes);
    }

    public static void GetDataPagesTest(RelationBis relation) throws IOException {
        System.out.println("\n************** Test : Obtention des pages de données **************");
        List<PageId> listePage = relation.getDataPages();
        System.out.println("Pages de données disponibles : " + listePage);
    }

    public static void WriteRecordDataPageTest(RelationBis relation) throws IOException {
        System.out.println("\n************** Test : Écriture de records dans les pages de données **************");

        Record[] records = {
                new Record(new String[]{"Boussad", "Rayan", "20"}),
                new Record(new String[]{"Nayar", "Bassoud", "21"}),
                new Record(new String[]{"Chato", "dit", "22"})
        };

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

    public static void GetRecordsInDataPageTest(RelationBis relation) throws IOException {
        System.out.println("\n************** Test : Récupération des records dans une page de données **************");

        PageId pageId = new PageId(0, 1);  // Exemple avec une page spécifique
        List<Record> records = relation.getRecordsInDataPage(pageId);

        System.out.println("Records présents dans la page " + pageId + " :");
        for (int i = 0; i < records.size(); i++) {
            System.out.println("Record n°" + (i + 1) + " : " + records.get(i));
        }
    }

    public static PageId ajouteHeaderPage(DiskManager diskManager, BufferManagerBis bufferManager) throws IOException {
        System.out.println("\n************** Initialisation de la Header Page **************");
        PageId headerPage = diskManager.AllocPage();
        System.out.println("Header Page initialisée à l'emplacement : " + headerPage);
        ByteBuffer buffHeader = bufferManager.GetPage(headerPage);
        buffHeader.putInt(0);

        for (int i = 0; i < 10; i++) {
            buffHeader.putInt(0); // fileId = 0
            buffHeader.putInt(0); // pageId = 0
        }

        bufferManager.FreePage(headerPage, true);

        System.out.println("Page d'en-tête initialisée et libérée avec succès.");
        return headerPage;
    }
}
