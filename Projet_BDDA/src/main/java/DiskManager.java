import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

public class DiskManager {

    private DBConfig dbconfig;  // Référence à l'instance DBConfig
    private ArrayList<PageId> pagelibre;  // Liste des pages désallouées
    private List<RandomAccessFile> openFiles;  // Liste des fichiers ouverts

    // Constructeur
    public DiskManager(DBConfig dbconfig) {
        this.dbconfig = dbconfig;
        this.pagelibre = new ArrayList<>();
        this.openFiles = new ArrayList<>();
    }

    // Méthode pour récupérer ou créer un fichier en fonction de l'index
    private RandomAccessFile getFileIdx(int fileIdx) throws IOException {
        if (fileIdx < openFiles.size()) {
            return openFiles.get(fileIdx);
        } else {
            String filePath = dbconfig.getDbPath() + "/F" + fileIdx + ".rsdb";
            RandomAccessFile file = new RandomAccessFile(filePath, "rw");
            openFiles.add(file);
            return file;
        }
    }
/*
    public PageId AllocPage() throws IOException {

        if (!pagelibre.isEmpty()) {
            return pagelibre.remove(0);
            RandomAccessFile file = getFileIdx(pagelibre.get(pagelibre.size()-1).getFileIdx());
            double fileSize = file.length();
            double pageIdx = fileSize / dbconfig.getPagesize();
            double maxFileSize = dbconfig.getDm_maxfilesize();
            if(fileSize + dbconfig.getPagesize() <= maxFileSize){
                file.setLength((long) (fileSize + dbconfig.getPagesize()));
                return new PageId((pagelibre.size()-1).getFileIdx()), (int) pageIdx));
            }
        }
    }
*/
    public void ReadPage(PageId pageId, byte[] bytes) throws IOException {
        RandomAccessFile file = getFileIdx(pageId.getFileIdx());
        long fileSize = file.length();
        double maxFileSize = dbconfig.getDm_maxfilesize();
        if (fileSize + dbconfig.getPagesize() <= maxFileSize) {
            file.setLength((long) (fileSize + dbconfig.getPagesize()));
        }
    }

    public void WritePage(PageId pageId, byte[] bytes) throws IOException {
    }

    public void DeallocPage(PageId pageId) throws IOException {
    }

    public void SaveState(){
    }

    public void LoadState(){
    }
}
