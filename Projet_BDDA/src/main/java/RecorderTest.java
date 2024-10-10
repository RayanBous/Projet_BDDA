import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;

public class RecorderTest {
    public static void main(String[] args) {
        ColInfo[] colinfo = {
                new ColInfo("col1", "INT"),
                new ColInfo("col2", "REAL"),
                new ColInfo("col3", "CHAR"),
                new ColInfo("col4", "VARCHAR")
        };

        Relation relation = new Relation("TestRelation", 4, colinfo);
        PageId pageId = new PageId(1, 100);

        ArrayList<Object> values = new ArrayList<>(Arrays.asList(123, 45.67f, 'A', "Hello"));
        Record record = new Record(values, pageId);

        ByteBuffer buffer = ByteBuffer.allocate(1024);
        System.out.println("Écriture du record dans le buffer...");

        int pos = relation.writeRecordToBuffer(record, buffer, 0);
        buffer.flip();

        Record newRecord = new Record();
        System.out.println("Lecture du record depuis le buffer...");

        int bytesRead = relation.readFromBuffer(newRecord, buffer, 0);

        System.out.println("Record original : " + record.getRecord());
        System.out.println("PageId original : " + record.getRecordid());

        System.out.println("Record lu : " + newRecord.getRecord());
        System.out.println("Nombre d'octets lus : " + bytesRead);

        if (record.getRecord().equals(newRecord.getRecord())) {
            System.out.println("Test réussi : Les données sont identiques !");
        } else {
            System.out.println("Test échoué : Les données ne correspondent pas.");
        }
    }
}