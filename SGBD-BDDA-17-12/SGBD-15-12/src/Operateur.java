public enum Operateur {
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
            throw new IllegalArgumentException("opeString : L'opérateur n'a pas été trouvé " + operation + ", les seuls opérateurs possibles sont les suivants :  =  !=  <  >  <=  >=");
    }
}
}
