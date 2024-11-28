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

    public void setTaille(int taille)   {
        this.taille = taille;
    }

}