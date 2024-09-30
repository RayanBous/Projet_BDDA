import java.io.*;
import java.util.ArrayList;

//Ici y'a pas mal de ChatGPT, donc faudra vraiment remixer, je vais mettre des commentaires sur les lignes de code à comprendre

/*Déjà pour avoir le contexte, cette classe sert à
gérer l'évolution d'un fichier, de sa lecture à son écriture*/

public class DiskManager {
    private RandomAccessFile currentFile; //C'est le fichier actuelle, sur lequel on va lire et écrire
    private int pageSize; //C'est la taille de la page
    private int maxFileSize;//Taille max de la page
    private int currentFileIndex; //C'est l'index du fichier actuel
    private int currentPageIndex; //C'est l'index de la page actuel
    private int maxFiles; //(CHATGPT) C'est le nombre max de fichier pouvant être utiliser, voir le constructeur
    private ArrayList<PageId> pageallouer; //Un vecteur de page à allouer (liste)

    public DiskManager(DBConfig config) {
        this.pageSize = (int) config.getPagesize();
        this.maxFileSize = (int) config.getDm_maxfilesize();
        this.maxFiles = 5;
        this.currentFileIndex = 0;
        this.currentPageIndex = 0;
        this.pageallouer = new ArrayList<>();
        openOrCreateFile();
    }

    public int getPageSize(){
        return pageSize;
    }

    /*Cette méthode n'était pas demander, mais elle permet d'ouvrire
    le fichier, avec les modes "rw" : read and write/lecture et écriture
     */
    private void openOrCreateFile() {
        try {
            // Utilisation du répertoire BinData
            String fileName = String.format("DataBase/BinData/file%d.db", currentFileIndex);
            currentFile = new RandomAccessFile(new File(fileName), "rw");
            currentPageIndex = (int) (currentFile.length() / pageSize);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    /* L'idée ici pour cette méthode est d'allouer une nouvelle
    page, après que le fichier actuelle ai atteint sa taille maximum
     */
    public PageId AllocPage() {
        // Dans le cas où il y a une page libre dans la liste des pages allouées
        if (!this.pageallouer.isEmpty()) {
            return this.pageallouer.remove(0);
        }

        // Chemin relatif du répertoire DB
        File dbDirectory = new File("../DB");
        if (!dbDirectory.exists()) {
            // Créer le répertoire DB si nécessaire
            if (!dbDirectory.mkdirs()) {
                System.err.println("Erreur : Impossible de créer le répertoire DB.");
                return null;
            }
        }

        // On vérifie s'il existe un fichier courant, sinon on en crée un
        if (this.currentFile == null) {
            openOrCreateFile(); // Ouvrir ou créer le fichier
        }

        // On vérifie s'il y a assez d'espace dans le fichier actuel, sinon on passe à un nouveau fichier
        try {
            long fileSize = this.currentFile.length();
            if (fileSize + pageSize > maxFileSize) {
                // Passer à un autre fichier si la taille maximale est atteinte
                this.currentFileIndex++;
                openOrCreateFile();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        // Allocation d'une nouvelle page
        try {
            int pageIdx = (int) (this.currentFile.length() / pageSize);
            PageId pageId = new PageId(this.currentFileIndex, pageIdx);
            this.pageallouer.add(pageId);
            return pageId;
        } catch (IOException e) {
            e.printStackTrace();
        }

        return null; // Retourner null en cas d'échec
    }



    //POUR LES AUTRES METHODES, C'EST QUASIMENT QUE DU CHATGPT

    //Cette méthode permet d'écrire sur la page directement
    public void WritePage(PageId pageId, byte[] donnee) throws IOException {
        //donnee : donnée de la page spécifique
        if (donnee.length != pageSize) { //On vérifie si la taille des données correspond à la taille de la page
            throw new IOException("Erreur : La taille du buffer (" + donnee.length + " octets) ne correspond pas à la taille de page (" + pageSize + " octets).");
        }

        currentFile.seek(pageId.getPageIdx() * pageSize); //On vérifie par rapport à la taille de la page
        currentFile.write(donnee); //On écrit sur la page
    }

    //Cette méthode permet de lire sur la page, directement avec l'identifiant de la page
    public byte[] ReadPage(PageId pageId) throws IOException {
        byte[] donnee = new byte[pageSize]; //Tableau de donnée de la page, le type byte est plus approprié lorsqu'il s'agit de la gestion de données sur un fichier
        currentFile.seek(pageId.getPageIdx() * pageSize); //On vérifie par rapport à la taille de la page
        currentFile.readFully(donnee); //readFully, c'est une méthode de la classe DataInput dans la libraire java.io en Java, qui permet de lire un fichier dans son entierté
        return donnee;
    }

    //CETTE METHODE C'EST PAS DU CHATGPT
    //Permet de désallouer la page
    public void DeallocPage(PageId pageId) {
        pageallouer.remove(pageId); //On utilise "remove()", car "pageallouer" est un ArrayList<>()
    }

    //CHATGPT
    //Cette méthode permet de sauvegarder l'état du fichier, c'est les données qu'il y'a à l'intérieur qu'on appelle "l'état"
    public void SaveState() throws IOException {
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream("state.dat"))) {
            oos.writeInt(pageallouer.size()); // Écrire le nombre de pages allouées
            for (PageId pageId : pageallouer) {
                oos.writeObject(pageId); // Écrire chaque PageId
            }
        }
    }

    public void LoadState() throws IOException, ClassNotFoundException {
        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream("state.dat"))) {
            int size = ois.readInt(); // Lire le nombre de pages allouées
            pageallouer.clear();
            for (int i = 0; i < size; i++) {
                pageallouer.add((PageId) ois.readObject()); // Lire chaque PageId
            }
        }
    }

}
