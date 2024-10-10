import java.io.File;
import java.io.FileWriter;
import java.io.FileReader;
import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.RandomAccessFile;
import java.io.Serializable;
import java.util.ArrayList;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.util.List;
import java.util.LinkedList;

public class DiskManager implements Serializable{

    private static final long serialVersion = 1L;

    private DBConfig dbconfig;
    private LinkedList <PageId> pagesLibres; // Pour enregistrer la liste des pages libres pour les réutiliser
    private RandomAccessFile fichierCourant; // Pour enregistrer le fichier actuel sur lequel on travaille et on insère de nouvelles pages
    private int indexFichierActuel; // Pour enregistrer l'index du fichier actuel
    private int indexPageActuelle;

    public DiskManager(DBConfig dbconfig)   {
        this.dbconfig = dbconfig;
        this.pagesLibres = new LinkedList<>();
        this.fichierCourant = null;
        this.indexFichierActuel = -1;
        this.indexPageActuelle = -1;
    }

    public int getIndexPageActuelle() {
        return indexPageActuelle;
    }

    public void setIndexPageActuelle(int indexPageActuelle) {
        this.indexPageActuelle = indexPageActuelle;
    }

    public DBConfig getDBConfig()   {
        return this.dbconfig;
    }

    public void setDBConfig(DBConfig dbconfig)  {
        this.dbconfig = dbconfig;
    }

    public LinkedList<PageId> getPagesLibres()   {
        return this.pagesLibres;
    }

    public RandomAccessFile getFichierCourant()  {
        return this.fichierCourant;
    }

    public void setFichierCourant(RandomAccessFile fichierCourant) {
        this.fichierCourant = fichierCourant;
    }

    public int getIndexFichierActuel() {
        return indexFichierActuel;
    }

    public void setIndexFichierActuel(int nbFichiers) {
        this.indexFichierActuel = nbFichiers;
    }


    public void creerNouveauFichier()    {
        try{
            this.indexPageActuelle = -1;
            this.indexFichierActuel +=1 ;
            String nomFichier = "F" + this.indexFichierActuel + ".rsdb";
            String cheminFichier = this.dbconfig.getDbPath() + "/BinData/" + nomFichier;
            this.fichierCourant = new RandomAccessFile(new File (cheminFichier), "rw");
        } catch (FileNotFoundException e){
            System.out.println(e.getMessage());
        }
    }

    public PageId AllocPage ()  {
        // Dans le cas où il y a une page libre dans la liste des free files
        if (!this.pagesLibres.isEmpty()){
            return this.pagesLibres.removeFirst();
        }
        // Si jamais il n y'a pas de pages libre ! On fait quoi ?
        // On doit vérifier si il existe bien un dossier "BinData" où sont stockés mes fichiers
        File bindata = new File (dbconfig.getDbPath()+"/BinData");
        if (! bindata.exists()) {
            // Créer le dossier si jamais il n'existe pas
            bindata.mkdirs();
        }
        // On verifie si il existe un fichier courant, sinon on en crée un directement
        if (this.fichierCourant == null)    {
            creerNouveauFichier();
        }
        // On vérifie s'il ya assez d'espace dans le fichier courant pour rajouter notre page, sinon on passe sur un autre fichier
        long tailleFichier = (long)(this.getDBConfig().getPagesize() * (this.indexPageActuelle+1));
        if (tailleFichier + dbconfig.getPagesize() > dbconfig.getDm_maxfilesize()) {
            creerNouveauFichier();
        }

        // Maintenant qu'on a notre fichier pret, on peut y allouer notre page
        this.indexPageActuelle += 1;
        PageId maPage = new PageId (this.indexFichierActuel, this.indexPageActuelle);
        return maPage;
    }

    // Cette méthode permettra de lire le contenu d'une page spécifiée par PageId et le stocker directement dans le buffer en entrée
    public void ReadPage(PageId maPage, byte [] buffer)   {
        // On recupére le nom du fichier correspondant à ma page
        String nomFichier = "F" + maPage.getFileIdx() + ".rsdb";
        String cheminFichier = this.dbconfig.getDbPath() + "/BinData/" + nomFichier;
        try{
            // On va ouvrir le fichier .rsdb correspondant à la page en argument
            RandomAccessFile fichierCorrespondant = new RandomAccessFile(new File(cheminFichier), "rw");
            // On deplace la tête de lécture jusqu'à la bonne position de lécture
            fichierCorrespondant.seek((long)(maPage.getPageIdx() * this.dbconfig.getPagesize()));
            // On lit le contenu de la page et on la place dans le buffer directement
            fichierCorrespondant.readFully(buffer);
        }   catch(IOException e)    {
            System.out.println(e.getMessage());
        }
    }

    // Cette méthode permet d'écrire le contenu du buffer (en bits) dans la page concernée directement
    public void WritePage (PageId maPage, byte [] buffer) throws IOException   {
        // On vérifie que le buffer contient assez de bits qu'il y a dans une page (pageSize)
        if (buffer.length != this.dbconfig.getPagesize())   {
            throw new IOException("Erreur : La taille du buffer (" + buffer.length + " octets) ne correspond pas à la taille de la page concernée (" + this.dbconfig.getPagesize() + "octets)");
        }
        // On recupére le nom du fichier correspondant à ma page
        String nomFichier = "F" + maPage.getFileIdx() + ".rsdb";
        String cheminFichier = this.dbconfig.getDbPath() + "/BinData/" + nomFichier;
        try {
            // On va ouvrir le fichier .rsdb correspondant à la page en argument
            RandomAccessFile fichierCorrespondant = new RandomAccessFile(new File(cheminFichier), "rw");
            // On deplace la tête d'écriture jusqu'à la bonne position pour écrire
            fichierCorrespondant.seek((long) (maPage.getPageIdx() * this.dbconfig.getPagesize()));
            // On écrit le contenu de notre buffer dans la position concernée
            fichierCorrespondant.write(buffer);
        } catch (IOException e) {
            System.out.println(e.getMessage());
        }
    }

    // Cette méthode permettra de désallouer une page déjà allouée
    public void DeallocPage (PageId maPage)    {
        this.pagesLibres.add(maPage);
    }

    // Cette méthode permettra de sauvegarder la liste des pages libres dans un fichier en utilisant la serialisation de l'objet ArrayList
    //public void saveState()     {
    //     try {
    //         String cheminFichier = this.dbconfig.getDbPath() + "/dm.ser";
    //         File fic = new File(cheminFichier);
    //         if (fic.exists()){
    //             fic.delete(); // Si le fichier n'existe pas on doit sortir de cette méthode directement car on a rien à faire dedant
    //         }
    //         // Ouvrir le fichier où stocker notre liste
    //         FileOutputStream fileOut = new FileOutputStream(cheminFichier);
    //         // créer un flux qui s'occupera de faire la serialisation
    //         ObjectOutputStream out = new ObjectOutputStream(fileOut);
    //         // Lancer La serialisation
    //         out.writeObject(this);
    //     } catch (IOException e) {
    //         System.out.println(e.getMessage());
    //     }
    // }

    public void saveState() {
        // On crée un objet JSON stockant toutes les informations qu'on va enregistrer dans cet état
        JSONObject etat = new JSONObject();
        etat.put("indexFichierCourant", this.indexFichierActuel);
        etat.put("indexPageCourante", this.indexPageActuelle);
        JSONArray jsonArrayList = new JSONArray(this.pagesLibres);
        etat.put("pagesLibres", jsonArrayList);
        try{
            String cheminFichier = this.dbconfig.getDbPath() + "/dm.json";
            File monFichier = new File (cheminFichier);
            FileWriter fw = new FileWriter(monFichier);
            fw.write(etat.toString());
            fw.flush();
        }   catch(IOException e){
            System.out.println(e.getMessage());
        }
    }


    public void loadState() {
        try{
            String cheminFichier = this.dbconfig.getDbPath() + "/dm.json";
            File monFichier = new File (cheminFichier);
            if (!monFichier.exists()){
                return;
            }
            FileReader fr = new FileReader(monFichier);
            BufferedReader br = new BufferedReader(fr);
            StringBuffer sb = new StringBuffer();
            String line = null;
            while ((line = br.readLine()) != null){
                sb.append(line);
            }
            br.close();
            JSONObject etat = new JSONObject(sb.toString());
            this.indexFichierActuel = etat.getInt("indexFichierCourant");
            this.indexPageActuelle = etat.getInt("indexPageCourante");
            this.fichierCourant = new RandomAccessFile(this.dbconfig.getDbPath() + "/BinData/F" + this.indexFichierActuel + ".rsdb" , "rw");
            JSONArray list = etat.getJSONArray("pagesLibres");
            this.pagesLibres.clear();
            for (int j = 0; j < list.length(); j++) {
                this.pagesLibres.add((PageId)list.get(j));
            }
        }   catch(IOException e){
            System.out.println(e.getMessage());
        }   catch(JSONException e){
            System.out.println(e.getMessage());
        }
    }
}