import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class BufferManagerBis {
    private DBConfig config; // configuration du SGBD
    private DiskManager diskManager;
    private HashMap<Integer, List<Object>> bufferMap; // list = [pageId, dirty,pin_count]
    private ByteBuffer [] bufferPool; // Tableau de buffer
    private String policy; // politique de remplacement des buffer

    public BufferManagerBis(DBConfig config, DiskManager diskManager) { // constructeur
        this.config = config;
        this.diskManager = diskManager;
        this.bufferMap = new HashMap<>();
        bufferPool = new ByteBuffer[(int) config.getBmBufferCount()];
        this.policy= config.getBm_policy();
        initBufferPoolAndMap();
    }

    public BufferManagerBis() {
        this.config = null;
        this.diskManager = null;
        this.bufferMap = new HashMap<>();
        this.bufferPool = new ByteBuffer[0]; // Par défaut, un tableau vide
        this.policy = null; // Aucune politique de remplacement définie
    }

    public ByteBuffer GetPage(PageId pageId) throws IOException {
        // Vérifier si le pageId est null avant toute autre opération
        if (pageId == null) {
            System.out.println("ERREUR : La PageId ne peut pas être null.");
            return null;  // Retourner null ou un buffer vide si la PageId est invalide
        }

        int pinCount;
        List<Integer> frameDispo = new ArrayList<>();
        List<Integer> framePC0 = new ArrayList<>();

        // Parcours des bufferPool pour trouver si la page est déjà en mémoire
        for (int i = 0; i < bufferPool.length; i++) {
            if (bufferMap.get(i).get(0) != null) {  // Si une page est présente dans le buffer
                // Comparer les PageId dans le buffer avec celle recherchée
                if (pageId.equals(bufferMap.get(i).get(0))) {
                    System.out.println("BUFFER MANAGER : GET PAGE : La page ID : " + bufferMap.get(i).get(0) + " est déjà présente dans le buffer " + i);
                    pinCount = (Integer) bufferMap.get(i).get(2);  // Récupérer le pinCount actuel
                    bufferMap.get(i).set(2, pinCount + 1);  // Incrémenter le pinCount
                    return bufferPool[i];  // Retourner le buffer
                }
            }

            // Si aucun buffer n'est présent à cette position
            if (bufferMap.get(i).get(0) == null) {
                frameDispo.add(i);  // Ajouter l'indice de la frame disponible
            }

            // Si le pin count est égal à 0, on l'ajoute à la liste framePC0
            if (bufferMap.get(i).get(2).equals(0)) {
                framePC0.add(i);
            }
        }

        // Si aucune frame n'est libre, on vérifie s'il y a des frames avec un pinCount à 0
        if (frameDispo.isEmpty()) {
            if (!framePC0.isEmpty()) {
                // Si on a des frames avec un pinCount à 0, on applique une politique de remplacement
                int indiceBuffer = indicePolicy(framePC0);  // Choisir la page à remplacer selon la politique
                if (bufferMap.get(indiceBuffer).get(1).equals(true)) {
                    // Si la page est marquée comme "dirty" (modifiée), on l'écrit sur le disque
                    diskManager.WritePage((PageId) bufferMap.get(indiceBuffer).get(0), bufferPool[indiceBuffer].array());
                    bufferMap.get(indiceBuffer).set(1, false);  // Marquer la page comme "propre"
                }

                // Remplacer la page dans le buffer
                bufferMap.get(indiceBuffer).set(2, 1);  // Mettre à jour le pinCount
                bufferMap.get(indiceBuffer).set(0, pageId);  // Remplacer la PageId
                diskManager.ReadPage(pageId, bufferPool[indiceBuffer].array());  // Charger la page dans le buffer
                return bufferPool[indiceBuffer];  // Retourner le buffer
            } else {
                // Si aucune frame n'est disponible avec un pinCount à 0
                System.out.println("ERREUR : Aucune page disponible avec un pin count à 0.");
                return null;  // Ou retournez un buffer vide si nécessaire
            }
        } else {
            // Si des frames sont disponibles, choisir une frame à remplacer
            int indiceBuffer = indicePolicy(frameDispo);  // Choisir la page à remplacer selon la politique

            // Remplacer la page dans le buffer
            bufferMap.get(indiceBuffer).set(2, 1);  // Mettre à jour le pinCount
            bufferMap.get(indiceBuffer).set(0, pageId);  // Remplacer la PageId
            diskManager.ReadPage(pageId, bufferPool[indiceBuffer].array());  // Charger la page dans le buffer
            return bufferPool[indiceBuffer];  // Retourner le buffer
        }
    }

    public void FreePage(PageId pageId, boolean valDirty) { // valDirty => page modifié
        for(int i=0; i<bufferMap.size(); i++){
            //System.out.println("**************  "+bufferMap.get(i).get(0));
            if (bufferMap.get(i).get(0)!=null) {
                if (pageId.equals((PageId) bufferMap.get(i).get(0))) { // Trouve le buffer contenant la pageId
                    //System.out.println("BUFFER MANAGER : FREE PAGE : la pageID "+pageId+" a été trouvé pour être libérer");
                    int pinCount =  (Integer) bufferMap.get(i).get(2);
                    bufferMap.get(i).set(1,valDirty);
                    bufferMap.get(i).set(2,pinCount-1);  // Décrémentation de pin count
                    bufferPool[i].position(0); // il faut remettre au premier octet
                    bufferPool[i].limit(bufferPool[i].capacity()); // Remets la limite à jour
                }
            }

        }
    }

    public void SetCurrentReplacementPolicy (String policy){

        if ( ( !policy.equals("LRU") && ( !policy.equals("MRU") ) ) ){ // Vérification de la politique de remplacement
            System.out.println(policy+" ne fait partie des politiques acceptés : LRU / MRU");
        }else {
            // Vérification que la politique de remplacement renseigné est la même que celle actuelle
            if( policy.equals( getPolicy() ) ){
                System.out.println("On utilise deja la politique "+policy);
            }else{
                setPolicy(policy); // Changement de la variable policy
                System.out.println("La politique utilisé "+getPolicy()+" devient : "+policy);
            }
        }
    }

    public void FlushBuffers() throws IOException {
        for(int i =0; i<bufferMap.size(); i++){
            // Parcours du tableau de buffer
            if (bufferMap.get(i).get(1).equals(true)){ // Si le dirty = true, Écriture dans sa page des modifications
                diskManager.WritePage((PageId) bufferMap.get(i).get(0), bufferPool[i].array()); // Écriture du buffer dans la page
                bufferMap.get(i).set(1,false);
            }
            bufferMap.get(i).set(0,null);
            bufferMap.get(i).set(2,0);
        }
        for(int i =0;i<bufferPool.length;i++){
            bufferPool[i].clear();
        }

    }

    private void initBufferPoolAndMap() {
        // Initialisation du tableau de buffer et de la map
        for (int i = 0; i < config.getBmBufferCount(); i++) {
            List<Object> bufferInfo = new ArrayList<>();
            bufferInfo.add(null);  // pageId (initialisé à null)
            bufferInfo.add(false); // dirty (initialisé à false)
            bufferInfo.add(0);     // pin_count (initialisé à 0)
            bufferMap.put(i, bufferInfo);  // Associe la liste à l'indice dans bufferMap
        }

        // Initialisation du bufferPool avec des instances de ByteBuffer
        for (int i = 0; i < bufferPool.length; i++) {
            bufferPool[i] = ByteBuffer.allocate((int) config.getPagesize());
        }

    }

    public String getPolicy() {
        return policy;
    }

    public void setPolicy(String policy) {
        this.policy = policy;
    }

    private int indicePolicy(List<Integer> frames){
        // Pour LRU: on prends la première frame qui a un pin count =0
        if (getPolicy().equals("LRU")) {
            return frames.get(0);
        }
        else{
            // Pour MRU, on prends la dernière frame qui a un pin count =0
            return frames.get(frames.size()-1);
        }
        // plus tard si le code est bon, on rajoutera surement d'autres politiques pour prendre le bonus
    }

    public HashMap<Integer, List<Object>> getBufferMap(){
        return bufferMap;
    }

    public void ReleasePage(ByteBuffer pageId) {
        for (int i = 0; i < bufferMap.size(); i++) {
            if (bufferMap.get(i).get(0) != null) {
                if (pageId.equals((PageId) bufferMap.get(i).get(0))) {
                    // Page trouvée, on libère la page
                    int pinCount = (Integer) bufferMap.get(i).get(2);
                    if (pinCount > 0) {
                        // Décrémentation du pin count
                        bufferMap.get(i).set(2, pinCount - 1);

                        // Si pin count atteint zéro, on peut envisager de libérer ou de gérer la page selon l'état
                        if ((Integer) bufferMap.get(i).get(2) == 0) {
                            // Si la page n'est plus utilisée, on la libère complètement
                            bufferMap.get(i).set(0, null); // On enlève la page de la map
                            bufferMap.get(i).set(1, false); // On marque la page comme non modifiée
                        }
                        // Si la page a été modifiée (dirty), on peut décider de la persister si nécessaire
                        if ((Boolean) bufferMap.get(i).get(1)) {
                            try {
                                // Si dirty = true, on écrit la page dans le disque
                                diskManager.WritePage((PageId) bufferMap.get(i).get(0), bufferPool[i].array());
                                bufferMap.get(i).set(1, false); // Réinitialisation du dirty à false après écriture
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                    break;
                }
            }
        }
    }

    public int getIndiceBufferMap(PageId pageId){ // retourne l'indice du buffer contenant la page indiqué en argumant
        for(int i =0; i<bufferMap.size(); i++){
            System.out.println("i = "+i+" "+bufferMap.get(i));
            if (bufferMap.get(i).get(0)!=null) {
                if (pageId.equals((PageId) bufferMap.get(i).get(0))) {
                    return i;
                }
            }
        }
        return -1;
    }


    public boolean getDirtyPage(PageId pageId){
        int indicePage =getIndiceBufferMap(pageId);
        boolean rep =(boolean) bufferMap.get(indicePage).get(1) ;

        /*if(rep){
            System.out.println("RELATION : BUffER MANAGER : GET DIRTY PAGE : le dirty de la header page était anciennement true, donc il restera true");
        }else{
            System.out.println("RELATION : GET FREE DATA PAGE ID : GET DIRTY PAGE : le dirty de la header page était anciennement false, il est donc pas nécessaire de le mettre à true, il restera à false");

        }*/
        return rep;
    }


}