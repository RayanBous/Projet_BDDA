import java.io.IOException;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileInputStream;

public class Main {
    public static void main(String[] args) {
        // Spécification du chemin du fichier de configuration
        String fichierConfig = "src/main/java/config.json";

        // Chargement de la configuration
        DBConfig dbconfig = DBConfig.loadDBConfig(fichierConfig);

        if (dbconfig != null) {
            // Affichage des paramètres de configuration
            System.out.println("Chemin de la BDD : " + dbconfig.getDbPath());
            System.out.println("Taille de la page : " + dbconfig.getPagesize());
            System.out.println("Taille max de la page : " + dbconfig.getDm_maxfilesize());

            // Initialisation du DiskManager avec la configuration
            DiskManager diskManager = new DiskManager(dbconfig);

            try {
                // Vérification si le dossier BinData existe ou création si nécessaire
                File binDataDir = new File("DataBase/BinData");
                if (!binDataDir.exists()) {
                    binDataDir.mkdirs();
                    System.out.println("Répertoire 'BinData' créé avec succès.");
                }

                // Allocation de page
                PageId pageId = diskManager.AllocPage();
                System.out.println("Page allouée : " + pageId);

                // Préparation des données à écrire sur la page
                byte[] dataToWrite = new byte[(int) dbconfig.getPagesize()];
                String message = "Rayan";  // Juste un exemple de test d'écriture
                System.arraycopy(message.getBytes(), 0, dataToWrite, 0, message.length());

                // Remplissage des espaces inutilisés avec des espaces vides
                for (int i = message.length(); i < dataToWrite.length; i++) {
                    dataToWrite[i] = ' ';
                }

                // Écriture des données sur la page
                diskManager.WritePage(pageId, dataToWrite);
                System.out.println("Données écrites sur la page.");

                // Création d'un fichier binaire et écriture des données
                String binFileName = "DataBase/BinData/page_" + pageId.getPageIdx() + ".bin"; // Nom du fichier binaire
                try (FileOutputStream fos = new FileOutputStream(binFileName)) {
                    fos.write(dataToWrite);
                    System.out.println("Fichier binaire créé : " + binFileName);
                }

                // Lecture des données depuis la page
                byte[] readData = diskManager.ReadPage(pageId);
                System.out.println("Contenu lu à partir de la page : " + new String(readData).trim());

                // Afficher le contenu du dernier fichier binaire créé
                try (FileInputStream fis = new FileInputStream(binFileName)) {
                    byte[] fileData = new byte[(int) dbconfig.getPagesize()];
                    fis.read(fileData);
                    System.out.println("Contenu du fichier binaire créé : " + new String(fileData).trim());
                }

                // Sauvegarde de l'état du DiskManager
                diskManager.SaveState();
                System.out.println("État sauvegardé.");

                // Chargement de l'état précédemment sauvegardé
                diskManager.LoadState();
                System.out.println("État chargé avec succès.");

            } catch (IOException e) {
                // Gestion des erreurs liées aux fichiers ou autres exceptions
                e.printStackTrace();
            } catch (ClassNotFoundException e) {
                throw new RuntimeException(e);
            }
        } else {
            // Message d'erreur en cas d'échec de chargement de la configuration
            System.out.println("Échec de chargement de la configuration.");
        }
    }
}
