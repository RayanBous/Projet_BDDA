import java.io.IOException;

import com.sun.source.tree.Tree;

public class BufferManagerTest {

    public static void main (String [] args)    {

        try {
            // Creer une configuration de base de données
            DBConfig dbconfig = DBConfig.loadDBConfig("src/main/java/config.json");
            if (dbconfig == null)     {
                System.err.println("Erreur lors du chargement de la configuration de la BDD.");
                return ;
            }

            // Creer une instance de DiskManager
            DiskManager diskManager = new DiskManager(dbconfig);

            // Creer une instance de BufferManager
            BufferManager bufferManager = new BufferManager(dbconfig, diskManager);

            bufferManager.SetCurrentReplacementPolicy("MRU");

            BufferManagerTest.TestSimplePageAllocation(bufferManager, diskManager);

            BufferManagerTest.TestLRUReplacement(bufferManager, diskManager);

            System.out.println("Etat actuel du buffer manager : ");
            System.out.print(bufferManager.toString());

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void TestSimplePageAllocation(BufferManager bufferManager, DiskManager diskManager) throws IOException{
        System.out.println("Test de l'allocation d'une page simple.");

        // Allouer une nouvelle page dans le DiskManager
        PageId pageId = diskManager.AllocPage();

        // Lire cette page via le BufferManager (elle devrait être chargée en mémoire)
        MyBuffer buffer = bufferManager.getPage(pageId);
        if (buffer != null) {
            System.out.println("Test réussi : la page a été correctement chargée en mémoire, et un buffer lui a été attribué.");
            System.out.println("Buffer en question : " + buffer);
            bufferManager.freePage(pageId, true);
        } else {
            System.out.println("Test échoué : Aucun buffer n'a été attribué à cette page.");
        }
    }

    public static void TestLRUReplacement(BufferManager bufferManager, DiskManager diskManager) throws IOException{

        //On va allouer 3 nouvelles pages (sachant que le buffer manager gère jusqu'à 3 buffers en même temps)
        PageId page1 = diskManager.AllocPage();
        PageId page2 = diskManager.AllocPage();
        PageId page3 = diskManager.AllocPage();
        //lire les pages dans le bufferManager
        MyBuffer buffer1 = bufferManager.getPage(page1);
        bufferManager.freePage(page1, false);

        MyBuffer buffer2 = bufferManager.getPage(page2);
        buffer2.put((byte) 'a');
        buffer2.put((byte) 'b');
        buffer2.put((byte) 'c');
        bufferManager.freePage(page2, true);

        MyBuffer buffer3 = bufferManager.getPage(page3);
        //bufferManager.freePage(page3, false);

        // bufferManager.getPage(page1);
        // bufferManager.freePage(page1,false);

        //PageId page4 = diskManager.AllocPage();
        //bufferManager.getPage(page4);

    }
}