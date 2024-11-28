/*import java.io.IOException;
import java.nio.ByteBuffer;

public class Relation {

    private String nom;
    private int nbColonnes;
    private ColInfo[] colonnes;
    private PageId headerPageId;
    private DiskManager diskManager;
    private BufferManager bufferManager;


    public Relation(String nom, int nbColonnes, ColInfo[] colonnes, PageId headerPageId, DiskManager diskManager, BufferManager bufferManager) {
        this.nom = nom;
        this.nbColonnes = nbColonnes;
        this.colonnes = colonnes;
        this.headerPageId = headerPageId;
        this.diskManager = diskManager;
        this.bufferManager = bufferManager;
    }

    public Relation(String nom, int nbColonnes, ColInfo[] colonnes, PageId headerPageId, DBConfig dbConfig) {
        this.nom = nom;
        this.nbColonnes = nbColonnes;
        this.colonnes = colonnes;
        this.headerPageId = headerPageId;
        this.diskManager = new DiskManager(dbConfig);
        this.bufferManager = new BufferManager(dbConfig, diskManager);
    }

    public Relation(String nom, int nbColonnes, PageId headerPageId, DiskManager diskManager, BufferManager bufferManager) {
        this.nom = nom;
        this.nbColonnes = nbColonnes;
        this.colonnes = new ColInfo[this.nbColonnes];
        this.headerPageId = headerPageId;
        this.diskManager = diskManager;
        this.bufferManager = bufferManager;
    }

    public String getNom() {
        return this.nom;
    }

    public void setNom(String nom) {
        this.nom = nom;
    }

    public int getNbColonnes() {
        return this.nbColonnes;
    }

    public void setNbColonnes(int nbColonnes) {
        this.nbColonnes = nbColonnes;
    }

    public ColInfo[] getColonnes() {
        return this.colonnes;
    }

    public void setColonnes(ColInfo[] colonnes) {
        this.colonnes = colonnes;
    }

    public PageId getHeaderPageId() {
        return this.headerPageId;
    }

    public void setHeaderPageId(PageId headerPageId) {
        this.headerPageId = headerPageId;
    }

    public DiskManager getDiskManager() {
        return this.diskManager;
    }

    public void setDiskManager(DiskManager diskManager) {
        this.diskManager = diskManager;
    }

    public BufferManager getBufferManager() {
        return this.bufferManager;
    }

    public void setBufferManager(BufferManager bufferManager) {
        this.bufferManager = bufferManager;
    }

    public int writeRecordToBuffer(Record record, ByteBuffer buffer, int pos) {
        //On commence par determiner le format d'écriture à utiliser et appeler directement la méthode d'écriture correspondante
        for (int i = 0; i < this.nbColonnes; i += 1) {
            if (this.colonnes[i].getType().equals("VARCHAR")) {
                return writeRecordToBufferFormatVariable(record, buffer, pos);
            }
        }
        return writeRecordToBufferFormatFixe(record, buffer, pos);
    }

    // Cette méthode écrit dans un buffer en utilisant le format fixe
    public int writeRecordToBufferFormatFixe(Record record, ByteBuffer buffer, int pos) {
        buffer.position(pos);
        for (int i = 0; i < this.nbColonnes; i += 1) {
            ColInfo colonne = this.colonnes[i];
            String value = record.getAttributs()[i];
            switch (colonne.getType()) {
                case "INT":
                    buffer.putInt(Integer.parseInt(value));
                    break;
                case "REAL":
                    buffer.putFloat(Float.parseFloat(value));
                    break;
                case "CHAR":
                    for (int j = 0; j < value.length(); j += 1) {
                        buffer.putChar(value.charAt(j));
                    }
                    break;
                default:
                    break;
            }
        }
        return buffer.position() - pos;
    }

    // Cette méthode écrit dans un buffer en utilisant le format variable
    public int writeRecordToBufferFormatVariable(Record record, ByteBuffer buffer, int pos) {
        int positionIemeElement = pos + ((this.nbColonnes + 1) * Integer.BYTES);
        buffer.position(pos);
        // Premierement on complete notre buffer avec le tableau de l'offset directory
        for (int i = 0; i < this.nbColonnes; i += 1) {
            buffer.putInt(positionIemeElement);
            switch (this.colonnes[i].getType()) {
                case "INT":
                    positionIemeElement += Integer.BYTES;
                    break;
                case "REAL":
                    positionIemeElement += Float.BYTES;
                    break;
                case "CHAR":
                    positionIemeElement += (Character.BYTES * this.colonnes[i].getTaille());
                    break;
                case "VARCHAR":
                    positionIemeElement += (record.getAttributs()[i].length() * Character.BYTES);
                    break;
                default:
                    break;
            }
        }
        buffer.putInt(positionIemeElement);
        // Maintenant on doit enregistrer nos attributs dans notre buffer
        for (int i = 0; i < this.nbColonnes; i += 1) {
            ColInfo colonne = this.colonnes[i];
            String value = record.getAttributs()[i];
            switch (colonne.getType()) {
                case "INT":
                    buffer.putInt(Integer.parseInt(value));
                    break;
                case "REAL":
                    buffer.putFloat(Float.parseFloat(value));
                    break;
                case "CHAR":
                case "VARCHAR":
                    for (int j = 0; j < value.length(); j += 1) {
                        buffer.putChar(value.charAt(j));
                    }
                    break;
                default:
                    break;
            }
        }
        return buffer.position() - pos;
    }

    public int readFromBuffer(Record record, ByteBuffer buffer, int pos) {
        //On commence par determiner le format de lécture à utiliser et appeler directement la méthode lecture correspondante
        for (int i = 0; i < this.nbColonnes; i += 1) {
            if (this.colonnes[i].getType().equals("VARCHAR")) {
                return readFromBufferFormatVariable(record, buffer, pos);
            }
        }
        return readFromBufferFormatFixe(record, buffer, pos);
    }

    public int readFromBufferFormatFixe(Record record, ByteBuffer buffer, int pos) {
        buffer.position(pos);
        for (int i = 0; i < this.nbColonnes; i += 1) {
            ColInfo colonne = this.colonnes[i];
            switch (colonne.getType()) {
                case "INT":
                    String attributInt = buffer.getInt() + "";
                    record.setAttribut(i, attributInt);
                    break;
                case "REAL":
                    String attributFloat = buffer.getFloat() + "";
                    record.setAttribut(i, attributFloat);
                    break;
                case "CHAR":
                    StringBuilder attributString = new StringBuilder();
                    for (int j = 0; j < colonne.getTaille(); j += 1) {
                        attributString.append(buffer.getChar());
                    }
                    record.setAttribut(i, attributString.toString());
                    break;
                default:
                    break;
            }
        }
        return buffer.position() - pos;
    }

    public int readFromBufferFormatVariable(Record record, ByteBuffer buffer, int pos) {
        buffer.position(pos);
        int positionIemeElement;
        for (int i = 0; i < this.nbColonnes; i += 1) {
            ColInfo colonne = this.colonnes[i];
            positionIemeElement = buffer.getInt();
            switch (colonne.getType()) {
                case "INT":
                    String attributInt = buffer.getInt(positionIemeElement) + "";
                    record.setAttribut(i, attributInt);
                    break;
                case "REAL":
                    String attributFloat = buffer.getFloat(positionIemeElement) + "";
                    record.setAttribut(i, attributFloat);
                    break;
                case "CHAR":
                case "VARCHAR":
                    int positionIemePlusUnElement = buffer.getInt(pos + ((i + 1) * Integer.BYTES));
                    StringBuilder attributString = new StringBuilder();
                    for (int j = positionIemeElement; j < positionIemePlusUnElement; j += Character.BYTES) {
                        attributString.append(buffer.getChar(j));
                    }
                    record.setAttribut(i, attributString.toString());
                    break;
                default:
                    break;
            }
        }
        return buffer.getInt() - pos;
    }

    public void addDataPage() {
        // On doit commencer par instancier une nouvelle page de data en mémoire
        PageId newPage = this.diskManager.AllocPage();
        // On doit commencer par effectuer des modifications dans la header page
        // Pour ce faire on va charger un buffer sur cette page
        MyBuffer bufferHeaderPage = bufferManager.getPage(this.headerPageId);
        // Lire le nombre actuel de data pages avant l'ajout de notre nouvelle page
        int nbDataPages = bufferHeaderPage.getInt(0);
        // On doit faire un test pour voir si on peut encore rajouter une nouvelle page dans la HeaderPage...
        int sizeOfHeaderPage = 4 + (12 * nbDataPages);
        int maxSizeOfPage = (int) (this.diskManager.getDBConfig().getPageSize());
        if (sizeOfHeaderPage + 12 > maxSizeOfPage) {
            throw new IndexOutOfBoundsException("Erreur : Impossible d'ajouter une nouvelle page au header... le nombre de pages maximal a été atteint !!");
        }
        // Dans ce cas, on doit ajouter la nouvelle page, et donc on commence par ajuster le nb de pages
        bufferHeaderPage.putInt(0, nbDataPages + 1);
        // On doit enregistrer notre nouvelle page dans le header page
        bufferHeaderPage.position(sizeOfHeaderPage);
        bufferHeaderPage.putInt(newPage.getFileIdx());
        bufferHeaderPage.putInt(newPage.getPageIdx());
        bufferHeaderPage.putInt(maxSizeOfPage - 8); // On met -8 car notre nouvelle page contient au début deux entiers (2*4octets)
        // On libère le buffer de la HeaderPage en mentionnant qu'elle a été modifiée
        bufferHeaderPage.freePage(this.headerPageId, true);
        // On doit écrire nos deux entiers dans la nouvelle data page, on les insere en fin de page!
        MyBuffer bufferNewPage = bufferManager.getPage(newPage);
        bufferNewPage.position(maxSizeOfPage - 8);
        bufferNewPage.putInt(0); // 0 correspond à la position à partir de laquelle commence l’espace libre sur la page
        bufferNewPage.putInt(0); // 0 correspond au nombre de records dans le directory
        bufferNewPage.freePage(newPage, true);
    }

    public PageId getFreeDataPageId(int sizeRecord) throws IOException {
        MyBuffer bufferHeaderPage = this.bufferManager.getPage(this.headerPageId);
        // Dans ce cas où bufferHeaderPage==null, tous les buffers du bufferManager sont occupés, c'est à dire que leurs pincount>0
        // On crée un buffer vite fait histoire de l'utiliser pour lire le contenu de la headerPage
        if (bufferHeaderPage == null) {
            bufferHeaderPage = new MyBuffer(this.headerPageId, this.diskManager.getDBConfig().getPagesize(), this.bufferManager.getTimeCount());
        }
        PageId freeDataPage = null;
        int nbDataPages = bufferHeaderPage.getInt(0);
        for (int i = 0; i < nbDataPages; i += 1) {
            int pageOffset = 4 + (i * 12);
            int fileIdx = bufferHeaderPage.getInt(pageOffset);
            int pageIdx = bufferHeaderPage.getInt(pageOffset + 4);
            int availableSpace = bufferHeaderPage.getInt(pageOffset + 8);
            if (availableSpace >= sizeRecord + 8) {
                freeDataPage = new PageId(fileIdx, pageIdx);
                break;
            }
        }
        bufferHeaderPage.freePage(this.headerPageId, false);
        return freeDataPage;
    }

    public RecordId writeRecordToDataPage(Record record, PageId pageId) throws IOException {
        MyBuffer bufferHeaderPage = this.bufferManager.getPage(pageId);

    }

    public int getRecordSize(Record record) {
        int recordSize = 0;
        int index = 0;
        for (ColInfo c : this.colonnes) {
            switch (c.getType()) {
                case "INT":
                    recordSize += Integer.BYTES;
                    break;
                case "REAL":
                    recordSize += Integer.BYTES;
                    break;
                case "CHAR":
                    recordSize += (Character.BYTES * this.colonnes[i].getTaille());
                    break;
                case "VARCHAR":
                    recordSize += (Character.BYTES * record.getAttributs()[i].length());
                    break;
            }
            index += 1;
        }
    }
}*/