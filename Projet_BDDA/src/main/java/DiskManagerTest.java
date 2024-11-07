import java.io.IOException;
public class DiskManagerTest {
    public static void main (String [] args)    {
        try {
            // Charger la configuration depuis le fichier JSON
            DBConfig config = DBConfig.loadDBConfig("src/main/java/config.json");
            if (config == null) {
                System.err.println("Erreur lors du chargement de la configuration.");
                return;
            }

            DiskManager dm = new DiskManager(config);
            dm.loadState();
            int n=5;
            PageId[] pages= new PageId[n];
            while (n>0){
                // Test Allocation et Écriture Page
                TestAllocAndWritePage(dm);

                // Test Lecture Page
                //TestReadPage(dm);
                System.out.println("");
                // pages[n-1] = dm.AllocPage();
                --n;
            }
            dm.saveState();

        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
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

    public static void TestReadPage(DiskManager dm) throws IOException {
        System.out.println("Test de la lecture de la page");

        PageId pageId = dm.AllocPage();



        byte[] dataToWrite = new byte[(int) dm.getDBConfig().getPagesize()];
        for (int i = 0; i < dataToWrite.length; i++) {
            dataToWrite[i] = (byte) (i % 256);
        }
        dm.WritePage(pageId, dataToWrite);

        byte[] dataRead = new byte[dataToWrite.length];
        dm.ReadPage(pageId, dataRead);
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

}