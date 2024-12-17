public class Condition {
    private String valeurConstante;
    private int indexColonne;
    private Operateur operateur;
    private String typeColonne;
    private String attributRelation1;

    public void setAttributRelation1(String attributRelation1) {
        this.attributRelation1 = attributRelation1;
    }

    private String attributRelation2;

    public Condition(String valeurConstante, int indexColonne, String operateur, String typeColonne){
        this.valeurConstante = valeurConstante;
        this.indexColonne = indexColonne;
        this.operateur = Operateur.opeString(operateur);
        this.typeColonne = typeColonne;
    }

    public Condition(String valeurConstante, int indexColonne, Operateur operateur, String typeColonne){
        this.valeurConstante = valeurConstante;
        this.indexColonne = indexColonne;
        this.operateur = operateur;
        this.typeColonne = typeColonne;
    }

    public Condition(String attributRelation1,String attributRelation2, String operateur, String typeColonne){
        this.valeurConstante = valeurConstante;
        this.indexColonne = indexColonne;
        this.operateur = Operateur.opeString(operateur);
        this.typeColonne = typeColonne;
        this.attributRelation1 = attributRelation1;
        this.attributRelation2 = attributRelation2;
    }
    

    private boolean comparaisonEvalNum(String valeurConstante1, String valeurConstante2) {
        double valeur1 = Double.parseDouble(valeurConstante1.toString());
        double valeur2 = Double.parseDouble(valeurConstante2.toString());

        switch (operateur) {
            case Operateur.EGALE:
                return valeur1 == valeur2;
            case Operateur.NON_EGALE:
                return valeur1 != valeur2;
            case Operateur.SUPERIEUR:
                return valeur1 > valeur2;
            case Operateur.INFERIEUR:
                return valeur1 < valeur2;
            case Operateur.SUPERIEUR_EGALE:
                return valeur1 >= valeur2;
            case Operateur.INFERIEUR_EGALE:
                return valeur1 <= valeur2;
            default :
                throw new IllegalArgumentException("comparaisonEvalNum : L'opérateur n'est pas valide");
        }
    }

    private boolean comparaisonEvalStr(String valeurConstante1, String valeurConstante2) {
        int comp = valeurConstante1.compareTo(valeurConstante2);
        switch(operateur){
            case Operateur.EGALE:
                return comp == 0;
            case Operateur.NON_EGALE:
                return comp != 0;
            case Operateur.SUPERIEUR:
                return comp > 0;
            case Operateur.INFERIEUR:
                return comp < 0;
            case Operateur.SUPERIEUR_EGALE:
                return comp >= 0;
            case Operateur.INFERIEUR_EGALE:
                return comp <= 0;
            default:
                throw new IllegalArgumentException("ComparaisonEvalStr : L'opérateur n'est pas valide");
        }
    }

    public boolean evaluate(String valeurColonne)  {
        switch(this.typeColonne.toUpperCase()){
            case "INT":
            case "REAL":
                return comparaisonEvalNum(valeurColonne,this.valeurConstante);
            case "VARCHAR":
            case "CHAR":
                return comparaisonEvalStr(valeurColonne,this.valeurConstante);
            default:
                throw new IllegalArgumentException("evaluation : Le type de la colonne et/ou la valeur constante n'est pas correcte");
        }
    }

    public int getIndexColonne()    {
        return this.indexColonne;
    }

    public String getAttributRelation1() {
        return attributRelation1;
    }

    public String getAttributRelation2() {
        return attributRelation2;
    }

    public String getTypeColonne() {
        return typeColonne;
    }

    public Operateur getOperateur() {
        return operateur;
    }


}