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

    /*Cette méthode n'était pas demander, mais elle permet d'ouvrire
    le fichier, avec les modes "rw" : read and write/lecture et écriture
     */
    private void openOrCreateFile() {
        try {
            String fileName = String.format("..\\DB\\file%d.db", currentFileIndex); //(CHATGPT), on choisit le bon chemin, en relatif ici
            currentFile = new RandomAccessFile(new File(fileName), "rw"); //(CHATGPT) On met en lecture et écriture
            currentPageIndex = (int) (currentFile.length() / pageSize); //ça calcule le nombre de page qu'il y'a dans le fichier, c'est ce qu'on voulait faire pour le AllocPage()
        } catch (IOException e) { //Gestion d'erreur sur les fichiers
            e.printStackTrace();
        }
    }

    /* L'idée ici pour cette méthode est d'allouer une nouvelle
    page, après que le fichier actuelle ai atteint sa taille maximum
     */
    public PageId AllocPage() throws IOException {
        if (currentFile == null) { //Si y'a pas de fichier actuelle, on en créer un nouveau
            openOrCreateFile();
        }

        if (currentFile.length() >= maxFileSize) { //1er if() : on vérifie si le fichier a atteint sa taille max
            if (currentFileIndex < maxFiles - 1) { //2ème if() : s'il a atteint sa taille max, on incrémente l'indice, et par conséquent on créer un nouveau fichier
                currentFileIndex++; //(CHATGPT)
                openOrCreateFile();
            } else {
                // Ici c'est le cas où tout les fichiers sont pleins (les 5 fichiers, 5 est définit comme nombre de fichier max dans le constructeur
                throw new IllegalStateException("Tous les fichiers sont pleins."); //(CHATGPT), juste pour l'Exception
            }
        }

        //Sinon, là on va allouer une page avec la classe PageId
        PageId pageId = new PageId(currentFileIndex, currentPageIndex);
        pageallouer.add(pageId);
        currentPageIndex++;

        // Là on veut savoir si on a atteint le nombre max de page, sur un seul fichier
        if (currentPageIndex * pageSize >= maxFileSize) {
            //Si c'est bien le cas, on passe à un autre fichier en créant un nouveau fichier avec le openOrCreateFile()
            currentPageIndex = 0;
            currentFileIndex++; //(CHATGPT)
            openOrCreateFile();
        }

        //On retourne la page
        return pageId;
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
        //Avec cette ligne, on ouvre un fichier en mode écriture
        //Si il n'a pas été créer juste avant, il se créer directement ici
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream("state.dat"))) {
            oos.writeObject(pageallouer); //On écrit directement
        }
    }

    //CHATGPT
    //Cette méthode permet de charger l'état du fichier
    public void LoadState() throws IOException, ClassNotFoundException {
        //Là en fait on ouvre le fichier en mode lecture
        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream("state.dat"))) {
            pageallouer.clear(); //On le "ferme" après la lecture
            pageallouer.addAll((ArrayList<PageId>) ois.readObject()); //Il va lire l'objet, puis ensuite va le caster en ArrayList car pageallouer est un ArrayList, et va l'ajouter à pageallouer
        }
    }
}
