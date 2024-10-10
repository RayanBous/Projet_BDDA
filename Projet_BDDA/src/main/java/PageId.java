import java.io.Serializable;

public class PageId implements Serializable{

    private static final long serialVersion = 1L;

    private int fileIdx;
    private int pageIdx;

    public PageId (int fileIdx, int pageIdx)    {
        this.fileIdx = fileIdx;
        this.pageIdx = pageIdx;
    }

    public int getFileIdx() {
        return fileIdx;
    }

    public void setFileIdx(int fileIdx) {
        this.fileIdx = fileIdx;
    }

    public int getPageIdx() {
        return pageIdx;
    }

    public void setPageIdx(int pageIdx) {
        this.pageIdx = pageIdx;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("PageId{");
        sb.append("fileIdx=").append(fileIdx);
        sb.append(", pageIdx=").append(pageIdx);
        sb.append('}');
        return sb.toString();
    }


}
