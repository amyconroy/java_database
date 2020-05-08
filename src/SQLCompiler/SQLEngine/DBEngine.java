package SQLCompiler.SQLEngine;
import SQLCompiler.DBQuery;
import SQLCompiler.SQLCondition.SQLCondition;
import SQLCompiler.SQLExceptions.InvalidQueryException;
import java.io.*;
import java.util.ArrayList;
import java.util.List;

// class to do the main engine work of the database
public class DBEngine {

    public void createDatabase(String DBName, DBQuery query) throws InvalidQueryException {
        File newDB = new File(DBName);
        if(newDB.exists()) throw new InvalidQueryException("ERROR: Database already exists.");
        if(!newDB.mkdir()) throw new InvalidQueryException("ERROR: Unable to create database.");
        query.setOutput("OK");
    }

    public void useDatabase(String DBName, DBQuery query) throws InvalidQueryException {
        File database = new File(DBName);
        checkStructureExists(database);
        query.setDatabase(DBName);
        query.setOutput("OK");
    }

    private void checkStructureExists(File checkStructure) throws InvalidQueryException {
        if(!checkStructure.exists()) throw new InvalidQueryException("ERROR: Structure does not exist.");
    }

    public void createTable(String TBLName, DBQuery query, ArrayList<String> columnValues) throws IOException, InvalidQueryException {
        Table newTable = new Table();
        newTable.addSingleColumn("id");
        if(columnValues != null) newTable.addColumns(columnValues);
        serializeTableToFile(TBLName, newTable, query);
        query.setOutput("OK");
    }

    private Boolean checkTableExists(File[] listOfTables, String TBLName){
        for(File file : listOfTables){
            String currTableName = file.getName();
            if(currTableName.equals(TBLName)){
                return true;
            }
        }
        return false;
    }

    // to serialize a table to the appropriate file
    private void serializeTableToFile(String TBLFileName, Table newTable, DBQuery query) throws IOException, InvalidQueryException {
        FileOutputStream fileOut = null;
        ObjectOutputStream objOut = null;
        String DBName = query.getDatabase();
        String TableFile = DBName + File.separator + TBLFileName;
        try {
            fileOut = new FileOutputStream(TableFile);
            objOut = new ObjectOutputStream(fileOut);
            objOut.writeObject(newTable);
            objOut.close();
        } catch (FileNotFoundException e) {
            throw new InvalidQueryException("ERROR : Unable to Create Table.");
        }
    }

    public void insertTableData(String TBLName, ArrayList<String> tableValues, DBQuery query) throws InvalidQueryException, IOException {
        String DBName = query.getDatabase();
        File database = new File(DBName);
        File[] tableList = database.listFiles();
        if(!checkTableExists(tableList, TBLName)){
            throw new InvalidQueryException("ERROR : Table does not exist.");
        }
        String TBLFileName = DBName + File.separator + TBLName;
        Table table;
        table = deserializeTableFromFile(TBLFileName);
        table.addRow(tableValues);
        serializeTableToFile(TBLName, table, query);
        query.setOutput("OK");
    }

    // to de-serialize a table from the appropriate file
    private Table deserializeTableFromFile(String TBLFileName) throws IOException, InvalidQueryException {
        FileInputStream fileIn;
        ObjectInputStream objIn;
        Table table;
        try{
            fileIn = new FileInputStream(TBLFileName);
            objIn = new ObjectInputStream(fileIn);
            table = (Table) objIn.readObject();
            objIn.close();
        } catch (FileNotFoundException | ClassNotFoundException e) {
            throw new InvalidQueryException("ERROR : Table not found.");
        }
        return table;
    }

    public void dropStructure(String dropType, String dropName, DBQuery query) throws InvalidQueryException {
        File structureFile;
        if(dropType.equals("TABLE")) structureFile = getTableFile(dropName, query);
        else structureFile = new File(dropName);
        if(structureFile.delete()) query.setOutput("OK");
        else throw new InvalidQueryException("ERROR: Unable to DROP database.");
    }

    private File getTableFile(String tableName, DBQuery query) throws InvalidQueryException {
        String database = query.getDatabase();
        String tableFileName = database + File.separator + tableName;
        File table = new File(tableFileName);
        checkStructureExists(table);
        return table;
    }

    public void selectAllFromTable(String tableName, DBQuery query, SQLCondition condition) throws IOException, InvalidQueryException {
        Table table = getTable(tableName, query);
        String columns = table.getAllColumns();
        String rows;
        if(condition == null) rows = table.getAllRows();
        else rows = table.checkCondition(condition);
        String result = columns + rows;
        query.setOutput(result);
        serializeTableToFile(tableName, table, query);
    }

    private Table getTable(String tableName, DBQuery query) throws IOException, InvalidQueryException {
        String dbName = query.getDatabase();
        String tableFileName = dbName + File.separator + tableName;
        return deserializeTableFromFile(tableFileName);
    }

    public void selectRowsCondition(String tableName, DBQuery query, SQLCondition condition, List<String> attributeList) throws IOException, InvalidQueryException {
        if(attributeList == null){
            selectAllFromTable(tableName, query, condition);
        }
        else{
            Table table = getTable(tableName, query);
            String output = table.getSpecificRows(condition, attributeList);
            query.setOutput(output);
            serializeTableToFile(tableName, table, query);
        }
    }

    public void updateRow(String tableName, String columnName, String newValue, SQLCondition condition, DBQuery query) throws IOException, InvalidQueryException {
        Table table = getTable(tableName, query);
        table.updateRowCondition(condition, columnName, newValue);
        query.setOutput("OK");
        serializeTableToFile(tableName, table, query);
    }

    public void deleteRow(String tableName, SQLCondition condition, DBQuery query) throws IOException, InvalidQueryException {
        Table table = getTable(tableName, query);
        table.removeEntireRow(condition);
        query.setOutput("OK");
        serializeTableToFile(tableName, table, query);
    }

    public void alterTable(DBQuery query, String column, String tableName, String alterationType) throws IOException, InvalidQueryException {
        Table table = getTable(tableName, query);
        if(alterationType.equals("ADD")){
            table.addSingleColumn(column);
        }
        else{
            table.removeSingleColumn(column);
        }
        query.setOutput("OK");
        serializeTableToFile(tableName, table, query);
    }
}
