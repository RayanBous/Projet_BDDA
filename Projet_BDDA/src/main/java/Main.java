import java.io.File;
import java.io.IOException;

public class Main {
    public static void main(String[] args) {
        // Chemin vers le fichier de configuration
        String fichierConfig = "src/main/java/config.json";
        DBConfig dbconfig = DBConfig.loadDBConfig(fichierConfig);

        if (dbconfig != null) {
            System.out.println("Chemin de la BDD : " + dbconfig.getDbPath());
            System.out.println("Taille de la page : " + dbconfig.getPagesize());
            System.out.println("Taille max du fichier : " + dbconfig.getDm_maxfilesize());
            System.out.println("Nombre de buffers gérés par le BufferManager : " + dbconfig.getBmBufferCount());
            System.out.println("Politique de remplacement utilisée : " + dbconfig.getBmPolicy());

            DiskManager diskManager = new DiskManager(dbconfig);
            BufferManager bufferManager = new BufferManager(dbconfig, diskManager, (int) dbconfig.getBmBufferCount());

            try {
                // Créer le répertoire BinData s'il n'existe pas déjà
                File binDataDir = new File(dbconfig.getDbPath() + "/BinData");
                if (!binDataDir.exists()) {
                    binDataDir.mkdirs();
                    System.out.println("Répertoire 'BinData' créé avec succès.");
                }

                // Exécution des tests du DiskManager
                System.out.println("\nTEST POUR LA CLASSE DISKMANAGER");
                TestDiskManager(diskManager);

                // Exécution des tests du BufferManager
                System.out.println("\nTEST POUR LA CLASSE BUFFERMANAGER");
                TestBufferManager(bufferManager, diskManager);

            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                try {
                    if (diskManager.getFichierCourant() != null) {
                        diskManager.getFichierCourant().close(); // Fermer le fichier courant à la fin
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        } else {
            System.out.println("Échec de chargement de la configuration.");
        }
    }

    // Méthode de test pour le DiskManager
    public static void TestDiskManager(DiskManager dm) throws IOException {
        // Messages à écrire dans le fichier binaire
        String[] messages = {"Hello", "World", "Disk Manager"};
        byte[][] dataToWrite = new byte[messages.length][(int) dm.getDBConfig().getPagesize()];

        // Préparer les données à écrire
        for (int i = 0; i < messages.length; i++) {
            System.arraycopy(messages[i].getBytes(), 0, dataToWrite[i], 0, messages[i].length());
        }

        // Écrire les données dans le DiskManager
        for (int i = 0; i < messages.length; i++) {
            PageId pageId = dm.AllocPage();
            dm.WritePage(pageId, dataToWrite[i]);
        }
        System.out.println("Écrit : " + String.join(", ", messages));

        // Lire et afficher les pages écrites
        for (int i = 0; i < messages.length; i++) {
            PageId pageId = new PageId(dm.getIndexFichierActuel(), i); // Utiliser l'index du fichier courant
            byte[] dataRead = new byte[(int) dm.getDBConfig().getPagesize()]; // Préparer un buffer pour la lecture
            dm.ReadPage(pageId, dataRead); // Lire les données
            System.out.println("Lu depuis la page " + i + ": " + new String(dataRead).trim());
        }

        // Test supplémentaire d'allocation et écriture de page
        TestAllocAndWritePage(dm);
    }

    // Méthode de test pour le BufferManager
    public static void TestBufferManager(BufferManager bufferManager, DiskManager dm) throws IOException {
        // Test de base pour allouer une page et la mettre en cache
        TestSimplePageAllocation(bufferManager, dm);

        // Test de la politique LRU (Least Recently Used)
        TestLRUReplacement(bufferManager, dm);

        // Test de la politique MRU (Most Recently Used)
        TestMRUReplacement(bufferManager, dm);

        // Test de l'écriture et libération de page
        TestFlushAndFreePage(bufferManager, dm);
    }

    public static void TestAllocAndWritePage(DiskManager dm) throws IOException {
        System.out.println("Test de l'allocation et écriture de la page");

        // Allouer une page
        PageId pageId = dm.AllocPage();

        // Créer un tableau de données à écrire
        byte[] dataToWrite = new byte[(int) dm.getDBConfig().getPagesize()];
        String message = "Hello DiskManager!";
        System.arraycopy(message.getBytes(), 0, dataToWrite, 0, message.length());

        // Écrire des données dans la page
        dm.WritePage(pageId, dataToWrite);

        // Lire les données et vérifier qu'elles correspondent
        byte[] dataRead = new byte[dataToWrite.length];
        dm.ReadPage(pageId, dataRead);
        boolean success = new String(dataRead).trim().equals(message);

        if (success) {
            System.out.println("Test réussi, lecture et écriture de la page réussi");
        } else {
            System.out.println("Test échoué : Données incorrectes après la lecture.");
        }
    }

    public static void TestSimplePageAllocation(BufferManager bufferManager, DiskManager dm) throws IOException {
        System.out.println("Test de l'allocation d'une page simple.");

        // Allouer une nouvelle page dans le DiskManager
        PageId pageId = dm.AllocPage();

        // Lire cette page via le BufferManager (elle devrait être chargée en mémoire)
        NewBuffer buffer = bufferManager.GetPage(pageId);

        if (buffer != null) {
            System.out.println("Test réussi : la page a été correctement chargée en mémoire.");
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
        if (buffer3 == null) {
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
