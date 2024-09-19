package Projet_BDDA;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

public class DBConfig {
    private String dbpath;

    // Constructeur prenant en argument le chemin de la base de données
    public DBConfig(String dbpath) {
        this.dbpath = dbpath;
    }

    // Getter pour obtenir le chemin de la base de données
    public String getDbPath() {
        return dbpath;
    }

    // Setter pour définir le chemin de la base de données
    public void setDbPath(String dbpath) {
        this.dbpath = dbpath;
    }

    // Méthode statique pour charger la configuration depuis un fichier texte
    public static DBConfig loadDBConfig(String fichierConfig) {
        String dbpath = null;
        try (BufferedReader br = new BufferedReader(new FileReader(fichierConfig))) {
            String ligne;
            while ((ligne = br.readLine()) != null) {
                if (ligne.startsWith("dbpath")) {
                    // Supposons que la ligne soit de la forme dbpath = '../DB'
                    dbpath = ligne.split("=")[1].trim().replace("'", "");
                }
            }
        } catch (IOException e) {
            System.err.println("Erreur lors de la lecture du fichier de configuration : " + e.getMessage());
        }

        if (dbpath != null) {
            return new DBConfig(dbpath);
        } else {
            System.err.println("Aucun chemin de base de données trouvé dans le fichier de configuration.");
            return null;
        }
    }

    public static void main(String[] args) {
        // Exemple d'utilisation
        DBConfig config = DBConfig.loadDBConfig("config.txt");
        if (config != null) {
            System.out.println("Le chemin de la base de données est : " + config.getDbPath());
        }
    }
}
