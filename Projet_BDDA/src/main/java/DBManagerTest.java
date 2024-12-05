public class DBManagerTest {
    public static void main(String[] args) {
        DBConfig dbconfig = new DBConfig("DataBase", 4096, 12287, 19, "LRU");

        DBManager db = new DBManager(dbconfig);

        db.CreateDatabase("d1");
        db.CreateDatabase("d2");

        System.out.println("**************Méthode ListDatabase()****************");
        db.ListDatabases();

        System.out.println("*******************Activation de la base de données*******************");
        db.setCurrentDatabase("d1");

        //Relation r1 = new Relation("Table1", 9);
        //Relation r2 = new Relation("Table2", 10);
        //db.addTableToCurrentDatabase(r1);
        //db.addTableToCurrentDatabase(r2);

        /*Après avoir corriger le code de Relation*/
        //db.ListDatabases();

        System.out.println("****************Suppresion de la base de données*******************");
        db.RemoveDatabase();


    }
}
