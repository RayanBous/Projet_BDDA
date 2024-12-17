public class RecordId   {
    private PageId pageId;
    private int slotIdx;

    public PageId getPageId()   {
        return this.pageId;
    }
    public void setPageId(PageId pageId) {
        this.pageId = pageId;
    }

    public int getSlotIdx() {
        return this.slotIdx;
    }
    public void setSlotIdx(int slotIdx) {
        this.slotIdx = slotIdx;
    }

    public RecordId(PageId pageId, int slotIdx) {
        this.pageId = pageId;
        this.slotIdx = slotIdx;
    }
    @Override
    public String toString() {
        return "RecordId [pageId=" + pageId + ", slotIdx=" + slotIdx + "]";
    }

}