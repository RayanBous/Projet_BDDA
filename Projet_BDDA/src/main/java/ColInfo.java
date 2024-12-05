public class ColInfo{

    private String nom;
    private String type;
    private int taille;

    public ColInfo(String nom, String type, int taille) {
        this.nom = nom;
        this.type = type;
        this.taille = taille;
    }

    public String getNom()  {
        return this.nom;
    }
    public void setNom(String nom)  {
        this.nom = nom;
    }

    public String getType() {
        return this.type;
    }
    public void setType(String type)    {
        this.type = type;
    }

    public int getTaille()  {
        return this.taille;
    }

    public String get(int i){
        switch(i){
            case 0 :
                return "Nom : " + this.nom;
            case 1 :
                return "Type : " + this.type;
            case 2 :
                return "Taille : " + this.taille;
            default:
                System.out.println("AUCUNE INFO");
        }
        return null;
    }

    public void setTaille(int taille)   {
        this.taille = taille;
    }

}