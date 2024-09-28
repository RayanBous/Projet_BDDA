import java.io.IOException;
/* JE VAIS VOUS INDIQUER CE QUI A ETE FAIT AVEC ET SANS CHATGPT !*/

public class Main {
    public static void main(String[] args) {
        String fichierConfig = "src/main/java/config.json";

        DBConfig dbconfig = DBConfig.loadDBConfig(fichierConfig);

        if (dbconfig != null) { //Dans le cas où le fichier est correctement reconnu
            System.out.println("Chemin de la BDD : " + dbconfig.getDbPath());
            System.out.println("Taille de la page : " + dbconfig.getPagesize());
            System.out.println("Taille max de la page : " + dbconfig.getDm_maxfilesize());
            DiskManager diskManager = new DiskManager(dbconfig);

            try {
                // Ici j'alloue directement la page, avec son identifiant qui est afficher
                PageId pageId = diskManager.AllocPage();
                System.out.println("Page allouée : " + pageId);

                // Là j'essaye d'écrire sur la page
                byte[] dataToWrite = new byte[(int) dbconfig.getPagesize()]; //Faut caster ici par rapport à Pagesize
                String message = "Hello, World!"; //C'est juste un test
                System.arraycopy(message.getBytes(), 0, dataToWrite, 0, message.length()); //CHATGPT
                for (int i = message.length(); i < dataToWrite.length; i++) { //CHATGPT
                    dataToWrite[i] = ' '; //Ici on met des espaces pour les paragraphes non utiliser, ça va faire des "----" sinon sur l'invite de commande
                }
                diskManager.WritePage(pageId, dataToWrite);

                // Là c'est l'étape de lecture
                byte[] readData = diskManager.ReadPage(pageId);
                System.out.println("Contenu lu à partir de la page : " + new String(readData).trim());

            } catch (IOException e) { //Là faut faire de la gestion d'erreur vu qu'on travaille sur les fichier
                e.printStackTrace();
            }
        } else {
            //Message d'erreur au cas où le fichier n'est pas récupérer, c'est pûrement optionnel
            System.out.println("Échec de chargement de la configuration.");
        }
    }
}
