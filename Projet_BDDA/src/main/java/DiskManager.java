import java.io.*;
import java.util.ArrayList;

public class DiskManager {
    private RandomAccessFile currentFile;
    private int pageSize;
    private int maxFileSize;
    private int currentFileIndex;
    private int currentPageIndex;
    private ArrayList<PageId> pageallouer;
    private DBConfig dbconfig;

    public DiskManager(DBConfig config) {
        this.pageSize = (int) config.getPagesize();
        this.maxFileSize = (int) config.getDm_maxfilesize();
        this.currentFileIndex = 0;
        this.currentPageIndex = 0;
        this.pageallouer = new ArrayList<>();
        this.dbconfig = config;
        openOrCreateFile();  // Appel pour créer/ouvrir le fichier initial
    }

    public int getPageSize() {
        return pageSize;
    }

    private void openOrCreateFile() {
        try {
            // Création du nom de fichier avec le format "F<numéro>.rsdb"
            String fileName = String.format("F%d.rsdb", currentFileIndex);

            // Chemin complet vers le fichier dans le répertoire BinData
            String filePath = this.dbconfig.getDbPath() + "/BinData/" + fileName;

            // Vérification et création du répertoire BinData si nécessaire
            File binDataDir = new File(this.dbconfig.getDbPath() + "/BinData");
            if (!binDataDir.exists()) {
                binDataDir.mkdirs();
                System.out.println("Répertoire 'BinData' créé avec succès.");
            }

            // Création ou ouverture du fichier en mode lecture/écriture
            currentFile = new RandomAccessFile(new File(filePath), "rw");

            // Calcul de l'index actuel de la page en fonction de la taille du fichier
            currentPageIndex = (int) (currentFile.length() / pageSize);

            System.out.println("Fichier ouvert/créé : " + fileName);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public PageId AllocPage() {
        try {
            long fileSize = this.currentFile.length();

            // Si le fichier atteint la taille max, ouvrir un nouveau fichier
            if (fileSize + pageSize > maxFileSize) {
                this.currentFileIndex++;
                openOrCreateFile();
            }

            // Calculer l'index de la page
            int pageIdx = (int) (this.currentFile.length() / pageSize);
            PageId pageId = new PageId(this.currentFileIndex, pageIdx);

            // Ajouter une page vide pour augmenter la taille du fichier
            byte[] emptyPage = new byte[pageSize];
            WritePage(pageId, emptyPage);  // Écrire une page vide pour allouer la taille

            return pageId;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    public void WritePage(PageId pageId, byte[] data) throws IOException {
        if (data.length != pageSize) {
            throw new IOException("La taille des données à écrire doit être égale à la taille d'une page.");
        }

        // Positionner correctement le curseur dans le fichier à l'index de la page
        String filePath = this.dbconfig.getDbPath() + "/BinData/" + String.format("F%d.rsdb", pageId.getFileIdx());
        try (RandomAccessFile file = new RandomAccessFile(new File(filePath), "rw")) {
            long pagePosition = pageId.getPageIdx() * pageSize;

            // Vérifier si la taille actuelle du fichier est suffisante pour accueillir la page
            long currentFileSize = file.length();
            if (pagePosition > currentFileSize) {
                // Forcer l'agrandissement du fichier avec des zéros si nécessaire
                file.setLength(pagePosition + pageSize);  // Agrandir de la taille de la page
            }

            file.seek(pagePosition);  // Aller à la position de la page
            file.write(data);  // Écrire les données sur le fichier
            file.getFD().sync();  // Forcer la synchronisation des données avec le disque
        }
    }

    public byte[] ReadPage(PageId pageId) throws IOException {
        byte[] data = new byte[pageSize];

        // Chemin complet vers le fichier dans le répertoire BinData
        String filePath = this.dbconfig.getDbPath() + "/BinData/" + String.format("F%d.rsdb", pageId.getFileIdx());

        // Calculer la position de la page
        try (RandomAccessFile file = new RandomAccessFile(new File(filePath), "r")) {
            long pagePosition = pageId.getPageIdx() * pageSize;

            // Vérifier si le fichier est assez grand pour contenir la page demandée
            long fileSize = file.length();

            // Condition modifiée pour permettre la lecture d'une page en limite de fichier
            if (pagePosition >= fileSize) {
                throw new IOException("La page demandée dépasse la taille actuelle du fichier.");
            }

            // Si le fichier est assez grand pour contenir au moins une partie de la page
            file.seek(pagePosition);

            // Lecture partielle si la page est partiellement écrite à la fin du fichier
            int bytesToRead = (int) Math.min(pageSize, fileSize - pagePosition);
            file.readFully(data, 0, bytesToRead);

            // Remplir le reste de la page avec des zéros si les données sont incomplètes
            if (bytesToRead < pageSize) {
                for (int i = bytesToRead; i < pageSize; i++) {
                    data[i] = 0;  // Remplissage avec des zéros
                }
            }
        }
        return data;
    }

    public void DeallocPage(PageId pageId) {
        pageallouer.remove(pageId);
    }

    public void SaveState() throws IOException {
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream("state.dat"))) {
            oos.writeInt(pageallouer.size());
            for (PageId pageId : pageallouer) {
                oos.writeObject(pageId);
            }
        }
    }

    public void LoadState() throws IOException, ClassNotFoundException {
        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream("state.dat"))) {
            int size = ois.readInt();
            pageallouer.clear();
            for (int i = 0; i < size; i++) {
                pageallouer.add((PageId) ois.readObject());
            }
        }
    }
}
