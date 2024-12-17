public class PageId{
    
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

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 19;
        result = prime * result + fileIdx;
        result = prime * result + pageIdx;
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final PageId other = (PageId) obj;
        return (this.fileIdx==other.getFileIdx() && this.pageIdx==other.getPageIdx());
    }

    

}
