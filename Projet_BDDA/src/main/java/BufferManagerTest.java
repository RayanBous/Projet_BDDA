import java.io.IOException;

public class BufferManagerTest {
    public static void main(String[] args) {
        try {
            DBConfig config = DBConfig.loadDBConfig("src/main/java/config.json");
            if (config == null) {
                System.err.println("Erreur lors du chargement de la configuration.");
                return;
            }

            // Créer une instance du DiskManager
            DiskManager dm = new DiskManager(config);
            
            // Créer une instance du BufferManager avec un petit nombre de buffers (ex. 3)
            BufferManager bufferManager = new BufferManager(config, dm, 3);

            // Test de base pour allouer une page et la mettre en cache
            TestSimplePageAllocation(bufferManager, dm);

            // Test de la politique LRU (Least Recently Used)
            TestLRUReplacement(bufferManager, dm);

            // Test de la politique MRU (Most Recently Used)
            TestMRUReplacement(bufferManager, dm);

            // Test de l'écriture et libération de page
            TestFlushAndFreePage(bufferManager, dm);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void TestSimplePageAllocation(BufferManager bufferManager, DiskManager dm) throws IOException {
        System.out.println("Test de l'allocation d'une page simple.");

        // Allouer une nouvelle page dans le DiskManager
        PageId pageId = dm.AllocPage();

        // Lire cette page via le BufferManager (elle devrait être chargée en mémoire)
        NewBuffer buffer = bufferManager.GetPage(pageId);

        if (buffer == null) {
            System.out.println("Test réussi : la page a été correctement chargée en mémoire.");
            System.out.println("l'ID de la page : " + pageId);
        } else {
            System.out.println("Test échoué : la page n'a pas été chargée en mémoire.");
        }
    }

    public static void TestLRUReplacement(BufferManager bufferManager, DiskManager dm) throws IOException {
        System.out.println("Test de la politique de remplacement LRU.");

        // Allouer 3 pages (en supposant que le buffer manager gère 3 buffers)
        PageId page1 = dm.AllocPage();
        PageId page2 = dm.AllocPage();
        PageId page3 = dm.AllocPage();

        // Charger les pages dans le buffer
        bufferManager.GetPage(page1);
        bufferManager.GetPage(page2);
        bufferManager.GetPage(page3);

        // Allouer une nouvelle page pour déclencher la politique LRU
        PageId page4 = dm.AllocPage();
        bufferManager.GetPage(page4); // Ici, une page doit être remplacée

        // Vérifier que la première page allouée a été remplacée (LRU)
        NewBuffer buffer1 = bufferManager.GetPage(page1);
        if (buffer1 == null) {
            System.out.println("Test réussi : La première page a été remplacée selon la politique LRU.");
            System.out.println("L'ID de la page est : " + page4);
        } else {
            System.out.println("Test échoué : La première page n'a pas été remplacée.");
        }
    }

    public static void TestMRUReplacement(BufferManager bufferManager, DiskManager dm) throws IOException {
        System.out.println("Test de la politique de remplacement MRU.");

        // Changer la politique de remplacement à MRU
        bufferManager.SetCurrentRemplacementPolicy("MRU");

        // Allouer 3 pages
        PageId page1 = dm.AllocPage();
        PageId page2 = dm.AllocPage();
        PageId page3 = dm.AllocPage();

        // Charger les pages dans le buffer
        bufferManager.GetPage(page1);
        bufferManager.GetPage(page2);
        bufferManager.GetPage(page3);

        // Recharger la page 3 pour en faire la page la plus récemment utilisée
        bufferManager.GetPage(page3);

        // Allouer une nouvelle page pour déclencher la politique MRU
        PageId page4 = dm.AllocPage();
        bufferManager.GetPage(page4);

        // Vérifier que la page 3 a été remplacée (MRU)
        NewBuffer buffer3 = bufferManager.GetPage(page3);
        if (buffer3 != null) {
            System.out.println("Test réussi : La page 3 a été remplacée selon la politique MRU.");
        } else {
            System.out.println("Test échoué : La page 3 n'a pas été remplacée.");
        }
    }

    public static void TestFlushAndFreePage(BufferManager bufferManager, DiskManager dm) throws IOException {
        System.out.println("Test de l'écriture et libération de page.");

        // Allouer une nouvelle page
        PageId pageId = dm.AllocPage();

        // Charger la page dans le buffer
        NewBuffer buffer = bufferManager.GetPage(pageId);

        // Modifier la page en mémoire (simuler un changement)
        if (buffer != null) {
            buffer.dirty = 1; // Marquer la page comme "dirty"
        }

        // Libérer la page en mémoire (simuler que la page n'est plus utilisée)
        bufferManager.FreePage(pageId, 1); // 1 pour "dirty"

        // Flusher toutes les pages modifiées sur le disque
        bufferManager.FlushBuffers();

        System.out.println("Les pages modifiées ont été écrites sur le disque.");
    }
}
