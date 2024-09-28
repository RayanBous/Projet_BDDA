public class Main {
    public static void main(String[] args) {
        String fichierConfig = "src/main/java/config.json";

        DBConfig dbconfig = DBConfig.loadDBConfig(fichierConfig);

        if (dbconfig != null) {
            System.out.println("Chemin de la BDD : " + dbconfig.getDbPath());
            System.out.println("Taille de la page : " + dbconfig.getPagesize());
            System.out.println("Taille max de la page : " +dbconfig.getDm_maxfilesize());
        } else {
            System.out.println("Échec de chargement de la configuration.");
        }
    }
}
