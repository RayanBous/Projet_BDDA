import java.awt.RenderingHints;
import java.io.IOException;

public class BufferManager {

    private DiskManager diskManager;
    private DBConfig dbConfig;
    private MyBuffer[] buffers;
    private int timeCount;

    public BufferManager(DBConfig dbConfig, DiskManager diskManager)  {
        this.diskManager = diskManager;
        this.dbConfig = dbConfig;
        this.buffers = new MyBuffer[(int) dbConfig.getBmBufferCount()];
        this.timeCount = 0;
    }

    public DiskManager getDiskManager() {
        return diskManager;
    }
    public void setDiskManager(DiskManager diskManager) {
        this.diskManager = diskManager;
    }

    public DBConfig getDbConfig() {
        return dbConfig;
    }
    public void setDbConfig(DBConfig dbConfig) {
        this.dbConfig = dbConfig;
    }

    public MyBuffer[] getBuffers() {
        return buffers;
    }
    public void setBuffers(MyBuffer[] buffers) {
        this.buffers = buffers;
    }

    public int getTimeCount()   {
        return this.timeCount;
    }
    public void setTimeCount(int timeCount) {
        this.timeCount = timeCount;
    }

    public MyBuffer getPage(PageId pageId)  throws IOException  {
        // On vérifie s'il n y a pas déjà un buffer occupé par la même page
        for (int i=0; i<buffers.length; i+=1) {
            if (buffers[i] != null && buffers[i].getPageId().equals(pageId))    {
                buffers[i].incrementerPinCount();
                buffers[i].setTimer(this.timeCount);
                this.timeCount+=1;
                return buffers[i];
            }
        }
        // On vérifie s'il n y a pas un buffer libre (non occupé)
        for (int i=0; i<buffers.length; i+=1) {
            if (buffers[i] == null) {
                buffers[i] = new MyBuffer(pageId, this.dbConfig.getPagesize(), this.timeCount);
                buffers[i].incrementerPinCount();
                byte[] newContent = new byte[(int) dbConfig.getPagesize()];
                diskManager.ReadPage(pageId, newContent);
                buffers[i].setData(newContent);
                this.timeCount+=1;
                return buffers[i];
            }
        }
        // Si on arrive jusqu'ici, c'est que tous les buffers sont occupés, donc On applique une politique de remplacement pour en libérer un
        String policy = this.dbConfig.getBmPolicy().toUpperCase();
        if (policy.equals("LRU"))  {
            return LRU (pageId);
        }
        if (policy.equals("MRU"))  {
            return MRU (pageId);
        }
        // Si on arrive jusqu'ici c'est que la politique de remplacement n'est pas reconnu par notre gestionnaire de buffer
        System.err.println("Erreur : Votre politique de remplacement n'est pas reconnue par notre gestionnaire de buffer.");
        return null;
    }

    public MyBuffer LRU(PageId pageId)   {
        MyBuffer bufferToReplace = null;
        int lru=0;
        for (MyBuffer buffer : this.buffers)    {
            if (buffer.getPinCount()==0 && (bufferToReplace==null || buffer.getTimer()<lru))    {
                bufferToReplace = buffer;
                lru = buffer.getTimer();
            }
        }
        if (bufferToReplace==null)  {
            System.err.println("Erreur : Le gestionnaire de buffers est actuellement saturé, tous les buffers sont en cours d'utilisation.");
        }   else {
            if (bufferToReplace.getFlagDirty()) {
                try{
                    diskManager.WritePage(bufferToReplace.getPageId(), bufferToReplace.getData());
                } catch(IOException e){
                    e.printStackTrace();
                }
                bufferToReplace.setFlagDirty(false);
            }
            bufferToReplace.setPageId(pageId);
            bufferToReplace.setPageSize(this.dbConfig.getPagesize());
            bufferToReplace.setPosition(0);
            bufferToReplace.setTimer(this.timeCount);
            byte[] newContent = new byte[(int) dbConfig.getPagesize()];
            diskManager.ReadPage(pageId, newContent);
            bufferToReplace.setData(newContent);
            bufferToReplace.incrementerPinCount();
            this.timeCount+=1;
        }
        return bufferToReplace;
    }

    public MyBuffer MRU(PageId pageId)   {
        MyBuffer bufferToReplace = null;
        int mru=0;
        for (MyBuffer buffer : this.buffers)    {
            if (buffer.getPinCount()==0 && (bufferToReplace==null || buffer.getTimer()>mru))    {
                bufferToReplace = buffer;
                mru = buffer.getTimer();
            }
        }
        if (bufferToReplace==null)  {
            System.err.println("Erreur : Le gestionnaire de buffers est actuellement saturé, tous les buffers sont en cours d'utilisation.");
        }   else {
            if (bufferToReplace.getFlagDirty()) {
                try{
                    diskManager.WritePage(bufferToReplace.getPageId(), bufferToReplace.getData());
                } catch(IOException e){
                    e.printStackTrace();
                }
                bufferToReplace.setFlagDirty(false);
            }
            bufferToReplace.setPageId(pageId);
            bufferToReplace.setPageSize(this.dbConfig.getPagesize());
            bufferToReplace.setPosition(0);
            bufferToReplace.setTimer(this.timeCount);
            byte[] newContent = new byte[(int) dbConfig.getPagesize()];
            diskManager.ReadPage(pageId, newContent);
            bufferToReplace.setData(newContent);
            bufferToReplace.incrementerPinCount();
            this.timeCount+=1;
        }
        return bufferToReplace;
    }

    public void freePage(PageId pageId, boolean  flagDirty)  {
        MyBuffer bufferCorrespondantPage = null;
        for (MyBuffer buffer : this.buffers)  {
            if (buffer!=null && buffer.getPageId().equals(pageId))  {
                bufferCorrespondantPage = buffer;
            }
        }
        if (bufferCorrespondantPage != null)    {
            bufferCorrespondantPage.setFlagDirty(flagDirty);
            bufferCorrespondantPage.decrementerPinCount();
            //this.dbConfig = dbConfig.loadDBConfig("src/config.json");
        }
        // if (flagDirty){
        //     this.flushBuffers();
        // }
    }

    public void SetCurrentReplacementPolicy (String policy) throws IOException    {
        if (policy.toUpperCase().equals("LRU") || policy.toUpperCase().equals("MRU"))   {
            dbConfig.setBm_policy(policy);
        }   else    {
            throw new IOException("Erreur : Politique de remplacement non reconnue par notre gestionnaire de buffers.");
        }
    }

    public void flushBuffers()  {
        for (MyBuffer buffer : this.buffers)    {
            if (buffer!=null && buffer.getFlagDirty())  {
                try{
                    diskManager.WritePage(buffer.getPageId(), buffer.getData());
                } catch(IOException e){
                    e.printStackTrace();
                }
                buffer.setFlagDirty(false);
            }
        }
        for (MyBuffer buffer : this.buffers)    {
            if (buffer!=null)   {
                buffer.setPinCount(0);
                buffer.setPosition(0);
                buffer.setTimer(this.timeCount);
            }
        }
        this.timeCount+=1;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("===============\n");
        sb.append("BufferManager\n");
        for (MyBuffer buffer : this.buffers) {
            sb.append(buffer).append("\n");
        }
        sb.append("===============");
        return sb.toString();
    }

}