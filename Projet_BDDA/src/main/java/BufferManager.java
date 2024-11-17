import java.io.IOException;
import java.nio.Buffer;
import org.json.JSONException;
import org.json.JSONObject;

public class BufferManager {
    public DBConfig dbconfig;
    DiskManager diskmanager;
    private NewBuffer[] buffers;
    private int timecount;

    public BufferManager(DBConfig dbconfig, DiskManager diskManager, int bfnum) {
        this.dbconfig = dbconfig;
        this.diskmanager = diskManager;
        this.buffers = new NewBuffer[bfnum];
        this.timecount = 0;
    }

    public int getTimeCount(){
        return timecount++;
    }

    public NewBuffer GetPage(PageId pageid) throws IOException {
        for(NewBuffer buffer : buffers){
            if(buffer != null && buffer.getPageid().equals(pageid)){
                System.out.println("Buffer trouvé : " + buffer);
                return buffer;
            }
        }
        String bm_policy = dbconfig.getBm_policy();
        System.out.println("Politique de remplacement appliquer au buffer : " + bm_policy);
        if(bm_policy.equals("LRU")){
            System.out.println("On va utiliser la politique de remplacement LRU");
            LRUAlgorithme(pageid);
        }else if(bm_policy.equals("MRU")){
            System.out.println("On va utiliser la politique de remplacement MRU");
            MRUAlgorithme(pageid);
        }
        System.out.println("Aucune page trouvée dans le buffer pour la page : " + pageid);
        return null;
    }

    public void LRUAlgorithme(PageId pageid) throws IOException {
        NewBuffer lru = null;
        boolean trouve = true;

        // Parcourir les buffers pour voir si un buffer vide est disponible
        for (NewBuffer buff : buffers) {
            if (buff == null && trouve) {
                // Si un buffer vide est trouvé, charger la page ici
                buff = new NewBuffer(pageid, timecount);
                byte[] data = new byte[(int) dbconfig.getPagesize()];
                diskmanager.ReadPage(pageid, data);
                trouve = false;
            }
        }

        // Trouver le buffer avec le plus ancien temps de libération (LRU)
        for (NewBuffer buffs : buffers) {
            if (buffs != null) {  // Vérifier si le buffer n'est pas null
                if (lru == null || buffs.tpsliberation < lru.tpsliberation) {
                    lru = buffs;
                }
            }
        }

        // Si un buffer a été trouvé, on remplace la page
        if (lru != null) {
            if (lru.dirty == 1) {
                byte[] donnee = new byte[(int) dbconfig.getPagesize()];
                diskmanager.WritePage(lru.pageid, donnee);
            }

            // Charger la nouvelle page
            byte[] donne = new byte[(int) dbconfig.getPagesize()];
            diskmanager.ReadPage(pageid, donne);
            lru.pageid = pageid;
            lru.tpsliberation = timecount;
            lru.pin_count = 0;
            lru.dirty = 0;
        }
    }

    public void MRUAlgorithme(PageId pageid) throws IOException {
        NewBuffer mru = null;
        boolean trouve = true;

        // Parcourir les buffers pour voir si un buffer vide est disponible
        for (NewBuffer buff : buffers) {
            if (buff == null && trouve) {
                // Si un buffer vide est trouvé, charger la page ici
                buff = new NewBuffer(pageid, timecount);
                byte[] data = new byte[(int) dbconfig.getPagesize()];
                diskmanager.ReadPage(pageid, data);
                trouve = false;
            }
        }

        // Trouver le buffer avec le temps de libération le plus récent (MRU)
        for (NewBuffer buffs : buffers) {
            if (buffs != null) {  // Vérifier si le buffer n'est pas null
                if (mru == null || buffs.tpsliberation > mru.tpsliberation) {
                    mru = buffs;
                }
            }
        }

        // Si un buffer MRU a été trouvé, remplacer la page
        if (mru != null) {
            if (mru.dirty == 1) {
                byte[] donnee = new byte[(int) dbconfig.getPagesize()];
                diskmanager.WritePage(mru.pageid, donnee);
            }

            // Charger la nouvelle page
            byte[] donne = new byte[(int) dbconfig.getPagesize()];
            diskmanager.ReadPage(pageid, donne);
            mru.pageid = pageid;
            mru.tpsliberation = timecount;
            mru.pin_count = 0;
            mru.dirty = 0;
        }
    }

    public void InsertBuffer(NewBuffer buffer) throws IOException {
        for (int i = 0; i < buffers.length; i++) {
            if (buffers[i] != null && buffers[i].getPageid().equals(buffer.getPageid())) {
                buffers[i] = buffer;
                return;
            }
        }

        for (int i = 0; i < buffers.length; i++) {
            if (buffers[i] == null) {
                buffers[i] = buffer;
                timecount++;
                return;
            }
        }


        int lru= -1;
        int oldestTime = Integer.MAX_VALUE;

        for (int i = 0; i < buffers.length; i++) {
            if (buffers[i] != null && getTimeCount() < oldestTime) {
                oldestTime = getTimeCount();
                lru = i;
            }
        }

        // Remplacer le buffer LRU par le nouveau buffer
        if (lru != -1) {
            buffers[lru] = buffer;
            timecount++;
        }
    }

    public void FreePage(PageId pageid, int valdirty) throws IOException {
        NewBuffer buffer = GetPage(pageid);
        if (buffer != null) {
            buffer.dirty = valdirty;
        }
    }

    public void SetCurrentRemplacementPolicy(String policy){
        dbconfig.bm_policy = policy;
    }

    public void FlushBuffers() throws IOException {
        int pagesize = (int) dbconfig.getPagesize();
        for(NewBuffer buff : buffers){
            if(buff != null && buff.dirty == 1){
                byte[] data = new byte[(int) pagesize];
                diskmanager.WritePage(buff.getPageid(), data);
            }
        }
        for(NewBuffer buffs : buffers){
            if(buffs != null){
                buffs.pin_count = 0;
                buffs.dirty = 0;
                buffs.tpsliberation = timecount;
            }
        }
    }

}