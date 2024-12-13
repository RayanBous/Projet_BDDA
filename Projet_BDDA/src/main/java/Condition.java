public class Condition {
    private int indexColonne;
    private Object valeurConstante;
    private Operateur operateur;
    private String typeColonne;

    public enum Operateur{
        EGALE,
        NON_EGALE,
        SUPERIEUR,
        INFERIEUR,
        SUPERIEUR_EGALE,
        INFERIEUR_EGALE;

        public static Operateur opeString(String operation) {
            switch (operation) {
                case "=" :
                    return EGALE;
                case "!=" :
                    return NON_EGALE;
                case ">" :
                    return SUPERIEUR;
                case "<" :
                    return INFERIEUR;
                case ">=" :
                    return SUPERIEUR_EGALE;
                case "<=" :
                    return INFERIEUR_EGALE;
                default :
                    throw new IllegalArgumentException("opeString : L'opérateur n'a pas été trouver " + operation);
            }
        }
    }

    public Condition(int indexColonne, Object valeurConstante, String operateur, String typeColonne){
        this.indexColonne = indexColonne;
        this.valeurConstante = valeurConstante;
        this.operateur = Operateur.opeString(operateur);
        this.typeColonne = typeColonne;
    }

    private boolean comparaisonEvalNum(Object valColonne, Object valConstant) {
        double numeroCol = Double.parseDouble(valColonne.toString());
        double numeroConst = Double.parseDouble(valConstant.toString());

        switch (operateur) {
            case EGALE:
                return numeroCol == numeroConst;
            case NON_EGALE:
                return numeroCol != numeroConst;
            case SUPERIEUR:
                return numeroCol > numeroConst;
            case INFERIEUR:
                return numeroCol < numeroConst;
            case SUPERIEUR_EGALE:
                return numeroCol >= numeroConst;
            case INFERIEUR_EGALE:
                return numeroCol <= numeroConst;
            default :
                throw new IllegalArgumentException("comparaisonEvalNum : L'opérateur n'est pas valide");
        }
    }

    private boolean comparaisonEvalStr(Object valColonne, Object valConstant) {
        String strColonne = valColonne.toString();
        String strConstant = valConstant.toString();

        int comp = strColonne.compareTo(strConstant);
        switch(operateur){
            case EGALE:
                return comp == 0;
            case NON_EGALE:
                return comp != 0;
            case SUPERIEUR:
                return comp > 0;
            case INFERIEUR:
                return comp < 0;
            case SUPERIEUR_EGALE:
                return comp >= 0;
            case INFERIEUR_EGALE:
                return comp <= 0;
            default:
                throw new IllegalArgumentException("ComparaisonEvalStr : L'opérateur n'est pas valide");
        }
    }

    public boolean evaluation(Object valColonne){
        if(valColonne == null){
            return false;
        }

        switch(typeColonne.toUpperCase()){
            case "INT":
            case "FLOAT":
                return comparaisonEvalNum(valColonne, valeurConstante);
            case "VARCHAR":
            case "CHAR":
                return comparaisonEvalStr(valColonne, valeurConstante);
            default:
                throw new IllegalArgumentException("evaluation : Le type de la colonne et/ou la valeur constante n'est pas correcte");
        }
    }

    @Override
    public String toString() {
        return "Condition(" + "Index colonne : " + indexColonne + "\n" + "Valeur constante : " + valeurConstante + "\n" + "Operateur : " + operateur + "\n" + "Type : " + typeColonne + "\n" + "Type de la colonne : " + typeColonne + "\n" + ")";
    }

    public Object getValeurConstante() {
        return valeurConstante;
    }

    public int getIndexColonne() {
        return indexColonne;
    }
}