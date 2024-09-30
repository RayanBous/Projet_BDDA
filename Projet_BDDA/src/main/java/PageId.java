import java.io.Serializable;
import java.util.Objects;

public class PageId implements Serializable { //Evitez les erreurs sur les entrées/sortie de fichier
    //On met Serializable ici pour pouvoir lire correctement le contenu du fichier, surtout qu'on le réutilise dans le main
    private static final long serialVersion = 1L; // Ajout d'un identifiant de version pour la sérialisation

    //En dehors du Serializable, j'ai rien changer dans cette classe
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

    @Override
    public String toString() {
        return "PageId{" + "FileIdx=" + FileIdx + ", PageIdx=" + PageIdx + "}";
    }
}
