import java.util.List;

public class Condition {

    // Types d'opérateurs possibles pour la condition sous forme de chaînes
    public static final String EQUALS = "=";
    public static final String NOT_EQUALS = "!=";
    public static final String GREATER_THAN = ">";
    public static final String LESS_THAN = "<";
    public static final String GREATER_EQUALS = ">=";
    public static final String LESS_EQUALS = "<=";
    public static final String LIKE = "LIKE";
    public static final String IN = "IN";

    private String columnName;  // Nom de la colonne à tester
    private Object value;       // Valeur à comparer
    private String operator;    // Opérateur logique (ex : "=", ">", "LIKE", etc.)
    private Condition leftCondition; // Pour gérer les conditions combinées (AND/OR)
    private Condition rightCondition; // Pour gérer les conditions combinées (AND/OR)
    private String logicalOperator; // "AND" ou "OR" si on combine deux conditions

    // Constructeur pour les conditions simples (ex : colonne = valeur)
    public Condition(String columnName, String operator, Object value) {
        this.columnName = columnName;
        this.operator = operator;
        this.value = value;
    }

    // Constructeur pour les conditions combinées (ex : condition1 AND condition2)
    public Condition(Condition leftCondition, String logicalOperator, Condition rightCondition) {
        this.leftCondition = leftCondition;
        this.logicalOperator = logicalOperator;
        this.rightCondition = rightCondition;
    }

    public String getColumnName() {
        return columnName;
    }

    public void setColumnName(String columnName) {
        this.columnName = columnName;
    }

    public Object getValue() {
        return value;
    }

    public void setValue(Object value) {
        this.value = value;
    }

    public String getOperator() {
        return operator;
    }

    public void setOperator(String operator) {
        this.operator = operator;
    }

    public Condition getLeftCondition() {
        return leftCondition;
    }

    public void setLeftCondition(Condition leftCondition) {
        this.leftCondition = leftCondition;
    }

    public Condition getRightCondition() {
        return rightCondition;
    }

    public void setRightCondition(Condition rightCondition) {
        this.rightCondition = rightCondition;
    }

    public String getLogicalOperator() {
        return logicalOperator;
    }

    public void setLogicalOperator(String logicalOperator) {
        this.logicalOperator = logicalOperator;
    }

    // Fonction pour tester si une condition est remplie pour un enregistrement
    public boolean test(Record record) {
        // Si la condition est simple
        if (leftCondition == null && rightCondition == null) {
            String recordValue = record.getValue(columnName);  // Récupère la valeur de la colonne du record
            switch (operator) {
                case EQUALS:
                    return recordValue.equals(value.toString());
                case NOT_EQUALS:
                    return !recordValue.equals(value.toString());
                case GREATER_THAN:
                    return compare(recordValue, value.toString()) > 0;
                case LESS_THAN:
                    return compare(recordValue, value.toString()) < 0;
                case GREATER_EQUALS:
                    return compare(recordValue, value.toString()) >= 0;
                case LESS_EQUALS:
                    return compare(recordValue, value.toString()) <= 0;
                case LIKE:
                    return recordValue.contains(value.toString());
                case IN:
                    if (value instanceof List) {
                        return ((List<?>) value).contains(recordValue);
                    }
                    return false;
                default:
                    return false;
            }
        }
        // Si c'est une condition combinée
        else {
            boolean leftResult = leftCondition.test(record);
            boolean rightResult = rightCondition.test(record);

            // Combinaison des conditions avec l'opérateur logique (AND/OR)
            if ("AND".equals(logicalOperator)) {
                return leftResult && rightResult;
            } else if ("OR".equals(logicalOperator)) {
                return leftResult || rightResult;
            } else {
                return false;
            }
        }
    }

    // Fonction de comparaison (pour les opérateurs >, <, >=, <=)
    private int compare(String recordValue, String conditionValue) {
        try {
            double recordDouble = Double.parseDouble(recordValue);
            double conditionDouble = Double.parseDouble(conditionValue);
            return Double.compare(recordDouble, conditionDouble);
        } catch (NumberFormatException e) {
            return recordValue.compareTo(conditionValue);
        }
    }
}
