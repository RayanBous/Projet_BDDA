public class Main {
    public static void main(String[] args) {
        String fichierConfig = "src/config.json";

        DBConfig dbconfig = DBConfig.loadDBConfig(fichierConfig);

        if (dbconfig != null) {
            System.out.println("Chemin de la BDD : " + dbconfig.getDbPath());
            System.out.println("Taille d'une page : " + dbconfig.getPageSize());
            System.out.println("Taille maximale d’un fichier \".rsdb\" : " + dbconfig.getDm_maxFileSize());
            System.out.println("Nombre de buffers du gestionnaire de buffers : " + dbconfig.getBm_BufferCount());
            System.out.println("Politique de remplacement utilisée par le gestionnaire de buffers : " + dbconfig.getBm_Policy());
        } else {
            System.out.println("Échec de chargement de la configuration.");
        }
    }
}
