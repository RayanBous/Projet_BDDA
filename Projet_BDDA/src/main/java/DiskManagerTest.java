import java.io.IOException;

public class DiskManagerTest {
    public static void main(String[] args) {
        try {
            // Charger la configuration depuis le fichier JSON
            DBConfig config = DBConfig.loadDBConfig("src/main/java/config.json");
            if (config == null) {
                System.err.println("Erreur lors du chargement de la configuration.");
                return;
            }

            DiskManager dm = new DiskManager(config);

            // Test Allocation et Écriture Page
            TestAllocAndWritePage(dm);

            // Test Lecture Page
            TestReadPage(dm);

            // Test Sauvegarde et Chargement de l'État
            TestSaveAndLoadState(dm);

        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    public static void TestAllocAndWritePage(DiskManager dm) throws IOException {
        System.out.println("Test de l'allocation et écriture de la page");

        // Allouer une page
        PageId pageId = dm.AllocPage();

        // Créer un tableau de données à écrire
        byte[] dataToWrite = new byte[dm.getPageSize()];
        for (int i = 0; i < dataToWrite.length; i++) {
            dataToWrite[i] = (byte) i;
        }

        // Écrire des données dans la page
        dm.WritePage(pageId, dataToWrite);

        // Lire les données et vérifier qu'elles correspondent
        byte[] dataRead = dm.ReadPage(pageId);
        boolean success = true;
        for (int i = 0; i < dataToWrite.length; i++) {
            if (dataToWrite[i] != dataRead[i]) {
                success = false;
                break;
            }
        }

        if (success) {
            System.out.println("Test réussi, lecture et écriture de la page réussi");
        } else {
            System.out.println("Test échoué : Données incorrectes après la lecture.");
        }
    }

    public static void TestReadPage(DiskManager dm) throws IOException {
        System.out.println("Test de la lecture de la page");

        // Allouer une nouvelle page
        PageId pageId = dm.AllocPage();

        // Écrire des données
        byte[] dataToWrite = new byte[dm.getPageSize()];
        for (int i = 0; i < dataToWrite.length; i++) {
            dataToWrite[i] = (byte) (i * 2);
        }
        dm.WritePage(pageId, dataToWrite);

        // Lire les données et vérifier
        byte[] dataRead = dm.ReadPage(pageId);
        boolean success = true;
        for (int i = 0; i < dataToWrite.length; i++) {
            if (dataToWrite[i] != dataRead[i]) {
                success = false;
                break;
            }
        }

        if (success) {
            System.out.println("Test réussi : lecture de la page réussi.");
        } else {
            System.out.println("Test échoué : Données incorrectes après la lecture.");
        }
    }

    public static void TestSaveAndLoadState(DiskManager dm) throws IOException, ClassNotFoundException {
        System.out.println("Test de sauvegarde et changement de la page");

        // Sauvegarder l'état
        dm.SaveState();
        System.out.println("Le fichier d'état a été créé avec succès.");

        // Charger l'état
        dm.LoadState();
        System.out.println("L'état a été chargé avec succès.");
    }
}
