import java.util.Objects;

public class PageId {
    public int FileIdx;
    public int PageIdx;

    public PageId(int FileIdx, int PageIdx) {
        this.FileIdx = FileIdx;
        this.PageIdx = PageIdx;
    }

    public int getFileIdx() {
        return FileIdx;
    }

    public int getPageIdx() {
        return PageIdx;
    }

    public void setFileIdx(int FileIdx) {
        this.FileIdx = FileIdx;
    }

    public void setPageIdx(int PageIdx) {
        this.PageIdx = PageIdx;
    }

    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        PageId that = (PageId) o;
        return FileIdx == that.FileIdx && PageIdx == that.PageIdx;
    }

    @Override
    public String toString() {
        return "PageId{" + "FileIdx=" + FileIdx + ", PageIdx=" + PageIdx + '}';
    }
    public int HashCode() {
        return Objects.hash(FileIdx, PageIdx);
    }
}

//Object.hash()
