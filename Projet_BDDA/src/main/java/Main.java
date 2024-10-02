import java.io.File;
import java.io.FileOutputStream;
import java.io.FileInputStream;
import java.io.IOException;

public class Main {
    public static void main(String[] args) {
        String fichierConfig = "src/main/java/config.json";
        DBConfig dbconfig = DBConfig.loadDBConfig(fichierConfig);

        if (dbconfig != null) {
            System.out.println("Chemin de la BDD : " + dbconfig.getDbPath());
            System.out.println("Taille de la page : " + dbconfig.getPagesize());
            System.out.println("Taille max de la page : " + dbconfig.getDm_maxfilesize());

            DiskManager diskManager = new DiskManager(dbconfig);

            try {
                File binDataDir = new File("DataBase/BinData");
                if (!binDataDir.exists()) {
                    binDataDir.mkdirs();
                    System.out.println("Répertoire 'BinData' créé avec succès.");
                }

                // Allouer une nouvelle page
                PageId pageId = diskManager.AllocPage();
                System.out.println("Page allouée : " + pageId);

                // Préparer les données à écrire
                byte[] dataToWrite = new byte[(int) dbconfig.getPagesize()];
                String message = "Rayan";
                System.arraycopy(message.getBytes(), 0, dataToWrite, 0, message.length());

                // Remplir le reste de la page avec des espaces
                for (int i = message.length(); i < dataToWrite.length; i++) {
                    dataToWrite[i] = ' ';
                }

                // Écrire les données sur la page avec DiskManager
                diskManager.WritePage(pageId, dataToWrite);
                System.out.println("Données écrites sur la page.");

                // Lire les données de la page allouée
                byte[] readData = diskManager.ReadPage(pageId);
                System.out.println("Contenu lu à partir de la page : " + new String(readData).trim());

                // Sauvegarder et charger l'état du DiskManager
                diskManager.SaveState();
                System.out.println("État sauvegardé.");

                diskManager.LoadState();
                System.out.println("État chargé avec succès.");

            } catch (IOException e) {
                e.printStackTrace();
            } catch (ClassNotFoundException e) {
                throw new RuntimeException(e);
            }
        } else {
            System.out.println("Échec de chargement de la configuration.");
        }
    }
}
