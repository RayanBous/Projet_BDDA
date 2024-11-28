import java.lang.StringBuilder;

public class MyBuffer {
    private PageId pageId;
    private int pinCount;
    private boolean flagDirty;
    private byte[] data;
    private long pageSize;
    private int position;
    private int timer;

    // Constructor
    public MyBuffer(PageId pageId, long pageSize, int timer) {
        this.pageId = pageId;
        this.pinCount = 0;
        this.flagDirty = false;
        this.pageSize = pageSize;
        this.data = new byte[(int) pageSize];
        this.position = 0;
        this.timer = timer;
    }


    // Getters & Setters
    public PageId getPageId() {
        return pageId;
    }
    public void setPageId(PageId pageId) {
        this.pageId = pageId;
    }

    public int getPinCount() {
        return pinCount;
    }
    public void setPinCount(int pinCount) {
        this.pinCount = pinCount;
    }

    public boolean getFlagDirty() {
        return flagDirty;
    }
    public void setFlagDirty(boolean flagDirty) {
        this.flagDirty = flagDirty;
    }

    public byte[] getData() {
        return data;
    }
    public void setData(byte[] data) {
        this.data = data;
    }

    public long getPageSize() {
        return pageSize;
    }
    public void setPageSize(long pageSize) {
        this.pageSize = pageSize;
    }

    public int getPosition() {
        return position;
    }
    public void setPosition(int position) {
        this.position = position;
    }

    public int getTimer()   {
        return this.timer;
    }
    public void setTimer(int timer) {
        this.timer = timer;
    }

    public void incrementerPinCount()    {
        this.pinCount +=1;
    }

    public void decrementerPinCount()   {
        this.pinCount -=1;
    }

    public void position (int n) throws IllegalArgumentException, IndexOutOfBoundsException  {
        if (n<0)    {
            throw new IllegalArgumentException("Erreur : L'index en entrée doit être un entier positif.");
        }   else if (n>=this.data.length)   {
            throw new IndexOutOfBoundsException("Erreur : L'index en entrée dépasse la taille du buffer.");
        }
        this.position = n;
    }

    public byte get() throws IndexOutOfBoundsException {
        if (this.position >= this.data.length) {
            throw new IndexOutOfBoundsException("Erreur : Commencez par ajuster la position du buffer, elle dépasse ses limites !");
        }
        return this.data[this.position++];
    }

    public void put(byte value)   {
        if (this.position >= this.data.length) {
            throw new IndexOutOfBoundsException("Erreur : Commencez par ajuster la position du buffer, elle dépasse ses limites !");
        }
        this.data[this.position++] = value;
    }

    public void flip()  {
        this.position = 0;
    }

    // Méthode ajouté pour repositionner la tête de lécture du buffer à 0 pour une nouvelle lecture...
    public void rewind() {
        this.position = 0;
    }

    // Cette méthode renvoie le nombre d'octets
    public int remaining()  {
        return this.data.length - this.position;
    }

    // Cette méthode vérifie s'il reste encore des octets à lire dans le buffer ou pas
    public boolean isRemaining()    {
        return (this.position < this.data.length);
    }

    // Cette méthode réinitialise un buffer à zero
    public void clear ()    {
        this.pinCount = 0;
        this.flagDirty = false;
        this.position = 0;
        this.pageId = null;
    }

    // Cette méthode permet d'affecter une nouvelle page à notre buffer
    public void replacePage(PageId pageId)   {
        this.clear();
        this.pageId = pageId;
    }

    // cette methode retourne le contenu d'un buffer (data) sous forme d'une chaine de caractères
    public String content() {
        StringBuilder sb = new StringBuilder();
        for (byte bit : this.data)  {
            sb.append(bit);
        }
        return sb.toString();
    }

    // Cette méthode permet d'afficher un buffer
    @Override
    public String toString()    {
        StringBuilder sb = new StringBuilder();
        sb.append("Buffer : pageId=").append((this.pageId==null)? "null" : this.pageId.toString()).append(", ");
        sb.append("Pin_Count=").append(this.pinCount).append(", ");
        sb.append("Flag_Dirty=").append(this.flagDirty).append(".");
        //sb.append(", data=").append(this.content()).append(".");
        return sb.toString();
    }

    public int getInt(int pos) {
        if (pos<0)  {
            throw new IllegalArgumentException("Erreur : l'index en entrée est invalide, il doit être un entier positif.");
        }   else if (pos + 4 > this.data.length) {
            throw new IndexOutOfBoundsException("Erreur : Il n y a pas assez de données à partir de cette position pour lire un entier.");
        }
        if (pos==this.position){
            this.position+=4;
        }
        return ((this.data[pos] & 0xFF) << 24) |
                ((this.data[pos+1] & 0xFF) << 16) |
                ((this.data[pos+2] & 0xFF) << 8) |
                (this.data[pos+3] & 0xFF);
    }

    public void putInt(int pos, int myInt)  {
        if (pos<0)  {
            throw new IllegalArgumentException("Erreur : l'index en entrée est invalide, il doit être un entier positif.");
        }   else if (pos + 4 > this.data.length) {
            throw new IndexOutOfBoundsException("Erreur : Il n y a pas assez de données à partir de cette position pour lire un entier.");
        }
        this.data[pos] = (byte) ((myInt >> 24) & 0xFF);
        this.data[pos+1] = (byte) ((myInt >> 16) & 0xFF);
        this.data[pos+2] = (byte) ((myInt >> 8) & 0xFF);
        this.data[pos+3] = (byte) (myInt & 0xFF);
        if (pos==this.position){
            this.position+=4;
        }
    }

    public int getInt() {
        return this.getInt(this.position);
    }

    public void putInt(int myInt)   {
        this.putInt(this.position, myInt);
    }
}