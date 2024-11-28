import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.List;
import java.util.ArrayList;

public class RelationBis {

    private String nomRelation;
    private int nbColonnes;
    private List<ColInfo> colonnes;
    private boolean varchar;
    private PageId headerPageId; // identifiant de Header Page de la relation
    private DiskManager diskManager;
    private BufferManagerBis bufferManager;

    public RelationBis(String nomRelation, int nbColonnes, PageId headerPageId, DiskManager diskManager, BufferManagerBis bufferManager) {
        this.nomRelation = nomRelation;
        this.nbColonnes = nbColonnes;
        this.headerPageId = headerPageId;
        this.diskManager = diskManager;
        this.bufferManager = bufferManager;
        this.varchar= possedeUnVarchar();
    }

    //J'ai créer ce constructeur pour la classe SGBD
    public RelationBis(String nomRelation) {
        this.nomRelation = nomRelation;
        this.nbColonnes = 0;
        this.colonnes = new ArrayList<>();
        this.varchar = false;
        this.headerPageId = null;
        this.diskManager = null;
        this.bufferManager = null;
    }

    public RelationBis(String nomRelation, int nbColonnes, PageId headerPageId, DiskManager diskManager, BufferManagerBis bufferManager,List<ColInfo> colonnes) {
        this.nomRelation = nomRelation;
        this.nbColonnes = nbColonnes;
        this.colonnes = colonnes;
        this.headerPageId = headerPageId;
        this.diskManager = diskManager;
        this.bufferManager = bufferManager;
        this.varchar=possedeUnVarchar();
    }

    public RelationBis(String nomRelation, int nbColonnes) {
        this.nomRelation = nomRelation; // Correction de l'attribut nomRelation (au lieu de nomCol)
        this.nbColonnes = nbColonnes;
        this.colonnes = new ArrayList<>(this.nbColonnes); // Utilisation d'une ArrayList pour respecter le type List
    }

    public RelationBis(String nomRelation, int nbColonnes, DiskManager diskManager, BufferManagerBis bufferManager) throws IOException {
        this.nomRelation = nomRelation;
        this.nbColonnes = nbColonnes;
        this.diskManager = diskManager;
        this.bufferManager = bufferManager;
        this.colonnes = new ArrayList<>(this.nbColonnes); // Utilisation d'une ArrayList pour stocker les colonnes

        // Créer et initialiser la headerPage directement dans le constructeur
        this.headerPageId = diskManager.AllocPage(); // Allocation d'une nouvelle page via DiskManager

        // Obtention de la page associée à headerPageId
        ByteBuffer headerPage = bufferManager.GetPage(headerPageId);

        // Initialisation de la headerPage avec les données de base
        // Exemple d'initialisation simplifiée : écrire le nombre de colonnes à l'offset 0
        headerPage.putInt(0, 0); // Par exemple, le nombre de colonnes à l'offset 0
        headerPage.putInt(4, 0); // Initialisation d'un compteur de pages de données à l'offset 4 (exemple)

        // Libérer la page après initialisation
        bufferManager.ReleasePage(headerPage);
    }

    public DiskManager getDiskManager() {
        return diskManager;
    }

    public void setDiskManager(DiskManager diskManager) {
        this.diskManager = diskManager;
    }

    public BufferManagerBis getBufferManager() {
        return bufferManager;
    }

    public void setBufferManager(BufferManagerBis bufferManager) {
        this.bufferManager = bufferManager;
    }

    public boolean getVarchar(){
        return varchar;
    }

    public String getNom()  {
        return this.nomRelation;
    }

    public void setNom(String nom)  {
        this.nomRelation = nom;
    }

    public PageId getHeaderPageId() {
        return headerPageId;
    }

    public void setHeaderPageId(PageId headerPageId) {
        this.headerPageId = headerPageId;
    }

    public void setNbColonnes(int nbColonnes) {
        this.nbColonnes = nbColonnes;
    }

    public List<ColInfo> getColonnes()  {
        return this.colonnes;
    }

    public void setColonnes(List<ColInfo> colonnes)   {
        this.colonnes = colonnes;
    }

    public int writeRecordToBuffer(Record record, ByteBuffer buffer, int pos) {
        //On commence par determiner le format d'écriture à utiliser et appeler directement la méthode d'écriture correspondante
        for (int i=0; i<this.nbColonnes; i+=1)  {
            if (this.colonnes.get(i).getType().equals("VARCHAR")){
                return writeRecordToBufferFormatVariable(record, buffer, pos);
            }
        }
        return writeRecordToBufferFormatFixe(record, buffer, pos);
    }

    // Cette méthode écrit dans un buffer en utilisant le format fixe
    public int writeRecordToBufferFormatFixe(Record record, ByteBuffer buffer, int pos)   {
        buffer.position(pos);
        for (int i=0; i<this.nbColonnes; i+=1)  {
            ColInfo colonne = this.colonnes.get(i);
            String value = record.getAttributs()[i];
            switch (colonne.getType()) {
                case "INT":
                    buffer.putInt(Integer.parseInt(value));
                    break;
                case "REAL":
                    buffer.putFloat(Float.parseFloat(value));
                    break;
                case "CHAR":
                    for (int j=0; j<value.length(); j+=1)    {
                        buffer.putChar(value.charAt(j));
                    }
                    break;
                default:
                    break;
            }
        }
        return buffer.position()-pos;
    }

    // Cette méthode écrit dans un buffer en utilisant le format variable
    public int writeRecordToBufferFormatVariable(Record record, ByteBuffer buffer, int pos)   {
        int positionIemeElement = pos + ((this.nbColonnes+1)*Integer.BYTES);
        buffer.position(pos);
        // Premierement on complete notre buffer avec le tableau de l'offset directory
        for (int i=0; i<this.nbColonnes; i+=1)  {
            buffer.putInt(positionIemeElement);
            switch (this.colonnes.get(i).getType()) {
                case "INT":
                    positionIemeElement += Integer.BYTES;
                    break;
                case "REAL":
                    positionIemeElement += Float.BYTES;
                    break;
                case "CHAR":
                    positionIemeElement += (Character.BYTES * this.colonnes.get(i).getTaille());
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
        for (int i=0; i<this.nbColonnes; i+=1)  {
            ColInfo colonne = this.colonnes.get(i);
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
                    for (int j=0; j<value.length(); j+=1)    {
                        buffer.putChar(value.charAt(j));
                    }
                    break;
                default:
                    break;
            }
        }
        return buffer.position()-pos;
    }

    public int readFromBuffer(Record record, ByteBuffer buffer, int pos)    {
        //On commence par determiner le format de lécture à utiliser et appeler directement la méthode lecture correspondante
        for (int i=0; i<this.nbColonnes; i+=1)  {
            if (this.colonnes.get(i).getType().equals("VARCHAR")){
                return readFromBufferFormatVariable(record, buffer, pos);
            }
        }
        return readFromBufferFormatFixe(record, buffer, pos);
    }

    public int readFromBufferFormatFixe(Record record, ByteBuffer buffer, int pos)  {
        buffer.position(pos);
        for (int i=0; i<this.nbColonnes; i+=1)  {
            ColInfo colonne = this.colonnes.get(i);
            switch(colonne.getType()){
                case "INT":
                    String attributInt = buffer.getInt()+"";
                    record.setAttribut(i, attributInt);
                    break;
                case "REAL":
                    String attributFloat = buffer.getFloat()+"";
                    record.setAttribut(i, attributFloat);
                    break;
                case "CHAR":
                    StringBuilder attributString = new StringBuilder();
                    for (int j=0; j<colonne.getTaille(); j+=1){
                        attributString.append(buffer.getChar());
                    }
                    record.setAttribut(i, attributString.toString());
                    break;
                default:
                    break;
            }
        }
        return buffer.position()-pos;
    }

    public int readFromBufferFormatVariable(Record record, ByteBuffer buffer, int pos)  {
        buffer.position(pos);
        int positionIemeElement;
        for (int i=0; i<this.nbColonnes; i+=1)  {
            ColInfo colonne = this.colonnes.get(i);
            positionIemeElement = buffer.getInt();
            switch(colonne.getType())  {
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
                    int positionIemePlusUnElement = buffer.getInt(pos+((i+1)*Integer.BYTES));
                    StringBuilder attributString = new StringBuilder();
                    for (int j=positionIemeElement; j<positionIemePlusUnElement; j+=Character.BYTES)  {
                        attributString.append(buffer.getChar(j));
                    }
                    record.setAttribut(i,  attributString.toString());
                    break;
                default:
                    break;
            }
        }
        return buffer.getInt()-pos;
    }

    public void addDataPage() throws IOException {
        System.out.println("\n************** Début Ajout d'une page de données *********************");
        int taillePage = (int) diskManager.getDBConfig().getPagesize();
        ByteBuffer buffHeader = bufferManager.GetPage(headerPageId);
        // Récupération du nombre de pages de données déjà enregistrées
        int nbDataPage = buffHeader.getInt(nbColonnes);
        int indice_dernierOctetLecture = nbDataPage * 12 + 4;

        System.out.println("RELATION : Nombre actuel de pages de données : " + nbDataPage);
        System.out.println("RELATION : Indice dernier octet lecture : " + indice_dernierOctetLecture);

        // Vérification de la place disponible
        if ((indice_dernierOctetLecture + 12) > taillePage) {
            System.out.println("ERREUR : Pas assez de place dans la headerPage pour ajouter une nouvelle page de données.");
            boolean dirtyPage = bufferManager.getDirtyPage(headerPageId);
            bufferManager.FreePage(headerPageId, dirtyPage);
        } else {
            // Allocation d'une nouvelle page de données
            PageId nouvellePageDonnee = diskManager.AllocPage();
            System.out.println("TEST addDataPage: Nouvelle page allouée : " + nouvellePageDonnee);

            // Mise à jour de la headerPage
            buffHeader.position(indice_dernierOctetLecture);
            buffHeader.putInt(nouvellePageDonnee.getFileIdx());
            buffHeader.putInt(nouvellePageDonnee.getPageIdx());

            int octetDisponibles = (int) diskManager.getDBConfig().getPagesize() - 4 * 2;
            buffHeader.putInt(octetDisponibles);

            // Mise à jour du compteur de pages de données
            nbDataPage++;
            buffHeader.putInt(0, nbDataPage);

            System.out.println("RELATION : Mise à jour de la headerPage réussie.");
            System.out.println("RELATION : Nombre total de pages de données : " + nbDataPage);

            // Libération de la page (modifiée)
            bufferManager.FreePage(headerPageId, true);
        }

        System.out.println("************** Fin Ajout d'une page de données *********************");
    }

    public PageId getFreeDataPageId(int sizeRecord) throws IOException {
        System.out.println("\n**************  Début getFreeDataPageId   *********************");

        PageId pageDisponible = null;
        boolean pageTrouve = false;
        int currentPosition = 12; // Position initiale dans le buffer (en supposant que la page commence après le PageId et la position du slot directory)
        int octetNecessaireInsertion = sizeRecord + 8; // Taille nécessaire pour insérer le record (taille + 8 octets pour les métadonnées)

        // Lire la page d'en-tête
        ByteBuffer buffHeader = bufferManager.GetPage(headerPageId);
        System.out.println("RELATION : getFreeDataPageId : buff info : " + buffHeader);

        // Vérification du nombre de pages dans l'en-tête
        int n = buffHeader.getInt(0); // Nombre de pages dans l'en-tête
        if (n <= 0) {
            System.out.println("Nombre de pages invalide : " + n);
            return null; // Retourner null si le nombre de pages est invalide
        }

        // Parcours des pages pour trouver une page avec suffisamment d'espace libre
        int i = 0;
        buffHeader.position(currentPosition); // Positionnement du buffer à la première page de données
        while (i < n && !pageTrouve) {
            // Vérifier qu'il y a assez d'octets restants dans le buffer pour lire une page
            if (buffHeader.remaining() >= 8) {
                int positionEspaceLibre = buffHeader.getInt(); // Position du premier espace libre
                int nbCases = buffHeader.getInt(); // Nombre de cases dans le slot directory
                System.out.println("Position espace libre : " + positionEspaceLibre + ", Nombre de cases : " + nbCases);

                // Vérifier chaque case dans le slot directory
                for (int j = 0; j < nbCases; j++) {
                    if (buffHeader.remaining() >= 8) {
                        int positionRecord = buffHeader.getInt(); // Position du i-ème record
                        int tailleRecord = buffHeader.getInt(); // Taille du i-ème record
                        System.out.println("Case " + j + ": position = " + positionRecord + ", taille = " + tailleRecord);

                        // Vérifier si l'espace est suffisant pour insérer le nouveau record
                        if (tailleRecord == 0 || tailleRecord >= octetNecessaireInsertion) {
                            // Lire les informations nécessaires pour instancier le PageId
                            if (buffHeader.remaining() >= 8) {
                                int numeroFichier = buffHeader.getInt(); // Numéro de fichier
                                int numeroPage = buffHeader.getInt(); // Numéro de page
                                pageDisponible = new PageId(numeroFichier, numeroPage);
                                pageTrouve = true; // Page trouvée
                                System.out.println("Page disponible trouvée : " + pageDisponible);
                                break; // Sortir de la boucle si une page a été trouvée
                            } else {
                                System.out.println("Pas assez d'octets pour lire le numéro de fichier et de page !");
                                break; // Sortir si on ne peut pas lire ces informations
                            }
                        }
                    } else {
                        System.out.println("Pas assez d'octets pour lire la case du slot directory !");
                        break; // Sortir si on ne peut pas lire les cases
                    }
                }
            } else {
                System.out.println("Pas assez d'octets pour lire le slot directory !");
                break; // Sortir si on ne peut pas lire les données nécessaires
            }

            // Mise à jour de la position dans le buffer (avancer de 12 octets par page)
            i++;
            currentPosition += 12; // Décalage de la position pour la prochaine page
            System.out.println("TEST getFreeDataPageId : positionPlaceDispo  " + currentPosition);

            // Vérifier si on dépasse la limite du buffer
            if (currentPosition < buffHeader.limit()) {
                buffHeader.position(currentPosition); // Mettre à jour la position dans le buffer
            } else {
                System.out.println("Dépassement de la capacité du buffer à la position " + currentPosition);
                break; // Sortir si on dépasse la capacité du buffer
            }
        }

        if (pageDisponible != null) {
            boolean dirtyHeaderPage = bufferManager.getDirtyPage(headerPageId);
            bufferManager.FreePage(headerPageId, dirtyHeaderPage);
        }

        System.out.println("RELATION : getFreeDataPageId  : Page trouvée : " + pageDisponible);
        System.out.println("\n**************  Fin getFreeDataPageId   *********************");

        return pageDisponible; // Retourner la PageId de la page trouvée
    }

    public RecordId writeRecordToDataPage(Record record, PageId pageId) throws IOException {
        System.out.println("\n**************  Ecriture d'un record dans la page de donnée   *********************");

        if (pageId == null) {
            System.out.println("ERREUR : La PageId ne peut pas être null.");
            return null;  // Retourner null si la pageId est invalide
        }

        ByteBuffer buffData = bufferManager.GetPage(pageId);
        if (buffData == null) {
            System.out.println("ERREUR : Impossible d'obtenir le buffer pour la page " + pageId);
            return null;  // Retourner null si le buffer est invalide
        }

        int offsetNombreSlot = (int) diskManager.getDBConfig().getPagesize() - 8;
        buffData.position(offsetNombreSlot);  // Positionnement à l'endroit du nombre de slots

        // Récupération du nombre actuel de slots et de la position pour l'écriture du prochain record
        int nombreSlot = buffData.getInt();
        int positionEcrireRecord = buffData.getInt();
        buffData.flip();  // Remet le buffer en mode lecture/écriture

        // Appel à une méthode qui écrit le record dans le buffer et renvoie la taille du record
        int tailleRecord = writeRecordToBuffer(record, buffData, positionEcrireRecord);

        buffData.limit(buffData.capacity());

        int positionEcrireSlot = (int) diskManager.getDBConfig().getPagesize() - 8 - nombreSlot * 8 - 8;

        buffData.position(positionEcrireSlot);
        buffData.putInt(positionEcrireRecord);  // Position d'écriture du record
        buffData.putInt(tailleRecord);  // Taille du record

        // Mise à jour du nombre de slots et de la position pour les futurs records
        buffData.position(offsetNombreSlot);
        buffData.putInt(nombreSlot + 1);  // Incrémentation du nombre de slots
        buffData.putInt(positionEcrireRecord + tailleRecord);  // Mise à jour de la position après l'écriture du record
        buffData.flip();

        // Libération de la page de données après l'écriture
        bufferManager.FreePage(pageId, true);

        // Mise à jour de l'espace disponible dans la header page
        ByteBuffer buffHeader = bufferManager.GetPage(headerPageId);

        // Vérification si le buffer de la page d'en-tête est valide
        if (buffHeader == null) {
            System.out.println("ERREUR : Impossible d'obtenir le buffer pour la page d'en-tête");
            return null;
        }

        // Recherche de la page de données correspondante dans la page d'en-tête
        boolean pageTrouve = false;
        int i = 0, n = buffHeader.getInt(0);  // Récupère le nombre d'entrées dans la page d'en-tête
        buffHeader.position(4);  // Positionnement au début des informations de fichier/page

        // Parcours des entrées pour trouver la page correspondante
        while (i < n && !pageTrouve) {
            int numeroFichier = buffHeader.getInt();  // Numéro de fichier
            int numeroPage = buffHeader.getInt();     // Numéro de page
            if (pageId.getFileIdx() == numeroFichier && pageId.getPageIdx() == numeroPage) {
                pageTrouve = true;
                int octetRestantDispo = buffHeader.getInt();
                buffHeader.position(buffHeader.position() - 4);  // Retourne 4 octets pour réécrire la valeur
                buffHeader.putInt(octetRestantDispo - tailleRecord - 8);  // Mise à jour des octets restants
                System.out.println("Octet Restant désormais: " + (octetRestantDispo - tailleRecord - 8));
            }
            i++;
            if (buffHeader.position() + 4 < buffHeader.capacity()) {
                buffHeader.position(buffHeader.position() + 4);  // Avance à la prochaine page
            }
        }

        // Libération de la page d'en-tête après la mise à jour
        bufferManager.FreePage(headerPageId, true);
        System.out.println("\n**************  FIN Ecriture d'un record dans la page de donnée   *********************");

        // Retourne le RecordId qui inclut l'ID de la page et le numéro du slot
        return new RecordId(pageId, nombreSlot + 1);
    }

    public ArrayList<Record> getRecordsInDataPage(PageId pageId) throws IOException {
        int nombreSlot; // Le nombre de slots dans la page
        Record record;
        ArrayList<Record> listeRecord = new ArrayList<>();

        // Obtention du buffer de la page de données
        ByteBuffer buffData = bufferManager.GetPage(pageId);

        // Vérification si le buffer est null
        if (buffData == null) {
            System.out.println("Erreur : Impossible d'obtenir le buffer pour la page " + pageId);
            return listeRecord;
        }

        // Positionnement à l'endroit où se trouve le nombre de slots
        int offsetNombreSlot = (int) diskManager.getDBConfig().getPagesize() - 8;
        buffData.position(offsetNombreSlot);  // Positionne le buffer à l'endroit du nombre de slots

        // Lecture du nombre de slots et du nombre total de slots
        nombreSlot = buffData.getInt();  // Le nombre de slots
        int positionSlot = buffData.getInt();  // La position du prochain record à écrire
        buffData.flip();  // Remet le buffer en mode lecture/écriture pour la lecture des records

        // Affichage des informations pour le débogage
        System.out.println("Nombre de slots : " + nombreSlot);
        System.out.println("Position du prochain record : " + positionSlot);

        // Parcours des slots pour récupérer les records
        int currentPosition = offsetNombreSlot - 8; // La position de la première case indiquant la position du premier record
        int i = 0;
        while (i < nombreSlot) {
            // Position dans le slot actuel
            buffData.position(currentPosition);

            // Lecture de la position du record dans le slot
            int positionRecord = buffData.getInt();

            // Affichage pour débogage
            System.out.println("Slot " + i + " : Position du record = " + positionRecord);

            // Si la position est valide (différente de -1), on lit le record
            if (positionRecord != -1) {
                record = new Record();
                // Appel de readFromBuffer pour lire le record à la position spécifiée
                readFromBuffer(record, buffData, positionRecord);
                listeRecord.add(record);  // Ajout du record à la liste
            }

            // Décrémentation de la position pour lire le prochain slot
            currentPosition -= 8;
            i++;
        }

        // Vérification si la page est sale et doit être enregistrée
        boolean dirtyDataPage = bufferManager.getDirtyPage(pageId);

        // Libération de la page
        bufferManager.FreePage(pageId, dirtyDataPage);

        // Retourner la liste des records extraits
        return listeRecord;
    }


    public List<PageId> getDataPages() throws IOException {
        List<PageId> dataPages = new ArrayList<>();
        ByteBuffer buffHeader = bufferManager.GetPage(headerPageId);

        if (buffHeader == null || buffHeader.remaining() < 4) {
            throw new IOException("Buffer is null or insufficient to read nbDataPage.");
        }

        // Afficher la position initiale et le contenu du buffer pour débogage
        System.out.println("Initial buffer position: " + buffHeader.position());
        System.out.println("Buffer content (headerPage): " + buffHeader.toString());

        // Lire le nombre de pages de données
        buffHeader.position(0); // S'assurer que la position est correcte avant de lire nbDataPage
        int nbDataPage = buffHeader.getInt();
        System.out.println("nbDataPage read from header: " + nbDataPage);

        // Vérification de la validité du nombre de pages
        int maxNbDataPage = (buffHeader.capacity() - 4) / 8;  // 4 octets pour nbDataPage, 8 octets par PageId
        if (nbDataPage <= 0 || nbDataPage > maxNbDataPage) {
            throw new IOException("Invalid nbDataPage value: " + nbDataPage + ". Expected range: 1-" + maxNbDataPage);
        }

        buffHeader.position(4);

        for (int i = 0; i < nbDataPage; i++) {
            if (buffHeader.remaining() < 8) {
                throw new IOException("Insufficient data to read PageId for data page " + i);
            }

            int fid = buffHeader.getInt();
            int pid = buffHeader.getInt();
            PageId pageId = new PageId(fid, pid);
            dataPages.add(pageId);

            System.out.println("PageId found: " + pageId);
        }

        // Libérer la page après usage
        boolean dirtyPage = bufferManager.getDirtyPage(headerPageId);
        bufferManager.FreePage(headerPageId, dirtyPage);

        return dataPages;
    }


    /*public RecordId InsertRecord(Record record) throws IOException {
        RecordId rid=null;  // initialisation du rid
        int octetCumulerRecord=0;
        for(ColInfo c : colonnes){ // on obtient la somme en terme d'octet que la colonne fait
            octetCumulerRecord +=c.getTaille();
        }
        if(varchar){ // Si il y a un varchar dans les colonnes, alors il faut ajouter les octets que vont prendre les n+1 int pour délimiter les cases
            octetCumulerRecord+= 4* (getNbColonnes()+1);
        }
        PageId pageDispo =getFreeDataPageId(octetCumulerRecord); // On cherche une page disponible
        System.out.println("Page Dispo : "+pageDispo);
        if (pageDispo!=null){ // Si une page est disponible, on "cris le contenu du record dans la page et n sauvegarde le rid
            rid =writeRecordToDataPage(record,pageDispo);
            System.out.println("Insertion du record réussi !!  "+rid);
        }else{
            System.out.println(" !!!! Erreur lors de l'insertion d'un record : Aucune page ne semble disponible (Insert Record) !!!!");
        }
        return rid; // retour du rid
    }*/

    public RecordId InsertRecord(Record record) throws IOException {
        RecordId rid=null;  // initialisation du rid
        int octetCumulerRecord=0;
        for( ColInfo c : colonnes){ // on obtient la somme en terme d'octet que la colonne fait
            octetCumulerRecord +=c.getTaille();
        }
        if(varchar){ // Si il y a un varchar dans les colonnes, alors il faut ajouter les octets que vont prendre les n+1 int pour délimiter les cases
            octetCumulerRecord+= 4* (getNbColonnes()+1);
        }
        PageId pageDispo =getFreeDataPageId(octetCumulerRecord);
        System.out.println("Page Dispo : "+pageDispo);
        if (pageDispo!=null){
            rid = writeRecordToDataPage(record,pageDispo);
            System.out.println("Insertion du record réussi !!  "+rid);
        }else{
            System.out.println(" !!!! Erreur lors de l'insertion d'un record : Aucune page ne semble disponible (Insert Record) !!!!");
        }
        return rid;
    }

    public List<Record> GetAllRecords() throws IOException {
        System.out.println("\n**************  DEBUT Get All Records   *********************");

        List<Record> records = new ArrayList<>();

        List<PageId> listePageDonnees = getDataPages();
        System.out.println("RELATION : Get All Records : liste des pages disponibles : " + listePageDonnees);

        if (listePageDonnees == null || listePageDonnees.isEmpty()) {
            System.out.println("Aucune page de données disponible dans la relation.");
            return records;
        }

        for (PageId pageDonnee : listePageDonnees) {
            List<Record> recordsInPage = getRecordsInDataPage(pageDonnee);
            if (recordsInPage != null) {
                records.addAll(recordsInPage);
            } else {
                System.out.println("Aucun record trouvé dans la page : " + pageDonnee);
            }
        }

        System.out.println("\n**************  Fin Get All Records   *********************");
        return records;
    }

    public int getNbColonnes() {
        return nbColonnes;
    }

    private boolean possedeUnVarchar(){
        boolean var=false;
        for( ColInfo Col :colonnes ){
            if( (Col.getType().equals("VARCHAR")) || (Col.getType().equals("varchar")) ) {
                var = true;
                break;
            }
        }
        return var;
    }
}



