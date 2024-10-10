import java.io.*;
import java.nio.Buffer;

public class NewBuffer {
    public PageId pageid;
    public int pin_count;
    public int dirty;
    public int tpsliberation;

    public NewBuffer(PageId pageid, int tpsliberation){
        this.pageid = pageid;
        this.pin_count = 0;
        this.dirty = 0;
        this.tpsliberation = tpsliberation;
    }

    public PageId getPageid(){
        return pageid;
    }

    public int getPin_count(){
        return pin_count;
    }

    public int getDirty(){
        return dirty;
    }

    public int getTpsliberation(){
        return tpsliberation;
    }
}
