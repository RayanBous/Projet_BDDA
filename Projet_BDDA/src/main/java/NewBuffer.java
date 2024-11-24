import java.nio.ByteBuffer;

public class NewBuffer {
    public PageId pageid;
    public int pin_count;
    public int dirty;
    public int tpsliberation;
    private byte[] data;
    public int position;

    public NewBuffer(PageId pageid, int tpsliberation){
        this.pageid = pageid;
        this.pin_count = 0;
        this.dirty = 0;
        this.tpsliberation = tpsliberation;
        this.data = new byte[4096];
        this.position = 0;
    }

    // Existant : Retourne l'ID de la page
    public PageId getPageid(){
        return pageid;
    }

    // Existant : Retourne le pin_count
    public int getPin_count(){
        return pin_count;
    }

    // Existant : Retourne le dirty
    public int getDirty(){
        return dirty;
    }

    // Existant : Lecture d'un entier
    public int getInt(int n) {
        if (n + 4 > data.length) {
            throw new IndexOutOfBoundsException("Not enough data in buffer to read an int.");
        }
        int value = 0;
        value |= (data[n] & 0xFF) << 24; // Premier octet (le plus significatif)
        value |= (data[n + 1] & 0xFF) << 16; // Deuxième octet
        value |= (data[n + 2] & 0xFF) << 8;  // Troisième octet
        value |= (data[n + 3] & 0xFF);       // Quatrième octet (le moins significatif)
        return value;
    }

    // Existant : Positionner le pointeur de lecture/écriture
    public void position(int n) throws IllegalArgumentException, IndexOutOfBoundsException{
        if(n < 0){
            throw new IllegalArgumentException("L'argument entrer en paramètre n'est pas un entier");
        }
        if(n > data.length){
            throw new IndexOutOfBoundsException("L'entier entrer en paramètre dépasse la taille du tableau !");
        }
        this.position = n;
    }

    // Existant : Mettre un entier dans le buffer
    public void putInt(int n) throws IndexOutOfBoundsException{
        if(n + 12 > data.length){
            throw new IndexOutOfBoundsException("On dépasse le tableau ");
        }
        data[position] = (byte) (n & 0xFF);
        data[position + 1] = (byte) ((n >> 8) & 0xFF);  // Avant-dernier octet
        data[position + 2] = (byte) ((n >> 16) & 0xFF); // Octet suivant
        data[position + 3] = (byte) ((n >> 24) & 0xFF); // Premier octet (8 bits de poids fort)
        position += 4;
    }

    public void putInt(int pos, int n) throws IndexOutOfBoundsException{
        if (pos + 4 > data.length) {
            throw new IndexOutOfBoundsException("On dépasse le tableau (méthode deux paramètres) ");
        }
        data[pos] = (byte) (n & 0xFF);
        data[pos + 1] = (byte) ((n >> 8) & 0xFF);
        data[pos + 2] = (byte) ((n >> 16) & 0xFF);
        data[pos + 3] = (byte) ((n >> 24) & 0xFF);
    }
    // Existant : Retourner le temps de libération
    public int getTpsliberation(){
        return tpsliberation;
    }

    // Existant : Retourner les données du buffer
    public byte[] getData(){
        return data;
    }

    // Existant : Conversion du buffer en tableau d'octets
    public byte[] toBytes() {
        ByteBuffer buffer = ByteBuffer.allocate(4096 + Integer.BYTES * 3); // page size + trois entiers (pin_count, dirty, tpsliberation)
        buffer.putInt(pin_count);
        buffer.putInt(dirty);
        buffer.putInt(tpsliberation);
        buffer.put(data);
        return buffer.array();  // Retourner le tableau d'octets résultant
    }

    // Méthode ajoutée: get() pour récupérer un byte à la position actuelle
    public byte get(int i) {
        if (position >= data.length) {
            throw new IndexOutOfBoundsException("Position en dehors des limites du buffer.");
        }
        return data[position++];
    }

    // Méthode ajoutée: put() pour écrire un byte à la position actuelle
    public void put(byte value) {
        if (position >= data.length) {
            throw new IndexOutOfBoundsException("Position en dehors des limites du buffer.");
        }
        data[position++] = value;
    }

    // Méthode ajoutée: flip() pour préparer le buffer à la lecture
    public void flip() {
        position = 0;  // Réinitialiser la position pour la lecture
    }

    // Méthode ajoutée: clear() pour réinitialiser le buffer pour une nouvelle écriture
    public void clear() {
        position = 0;  // Réinitialiser la position
        pin_count = 0;
        dirty = 0;
        tpsliberation = 0;
    }

    // Méthode ajoutée: remaining() pour savoir combien de bytes sont encore à lire
    public int remaining() {
        return data.length - position;
    }

    // Méthode ajoutée: rewind() pour revenir au début du buffer
    public void rewind() {
        position = 0;  // Positionner la lecture à zéro
    }

    // Méthode ajoutée: hasRemaining() pour vérifier s'il y a des bytes à lire
    public boolean hasRemaining() {
        return position < data.length;
    }

}
