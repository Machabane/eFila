package org.celllife.idart.start;


import org.apache.log4j.xml.DOMConfigurator;
import org.celllife.idart.commonobjects.iDartProperties;

import java.io.BufferedReader;

import java.io.File;
import java.io.FileReader;

import java.sql.SQLException;
import java.sql.DriverManager;
import java.sql.Connection;
import java.sql.Statement;

public class UpdateDatabase {

    private static final String DRIVER_NAME = "org.postgresql.Driver";

    static {
        try {
            Class.forName(DRIVER_NAME).newInstance();
            System.out.println("*** Driver loaded");
        } catch (Exception e) {
            System.out.println("*** Error : " + e.toString());
            System.out.println("*** ");
            System.out.println("*** Error : ");
            e.printStackTrace();
        }

    }

    private static final String URL = "jdbc:postgresql://localhost/pharm";
    private static final String USER = "postgres";
    private static final String PASSWORD = "postgres";
    private static String INSTRUCTIONS = new String();

    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(URL, USER, PASSWORD);
    }

    public static void resetDatabase() throws SQLException {
        String s = new String();
        StringBuffer sb = new StringBuffer();

        try {
            FileReader fr = new FileReader(new File("AlteracoesIDARTSql.sql"));
            // be sure to not have line starting with "--" or "/*" or any other non aplhabetical character

            BufferedReader br = new BufferedReader(fr);

            while ((s = br.readLine()) != null) {
                sb.append(s);
            }
            br.close();

            // here is our splitter ! We use ";" as a delimiter for each request
            // then we are sure to have well formed statements
            String[] inst = sb.toString().split(";");

            Connection c = UpdateDatabase.getConnection();
            Statement st = c.createStatement();

            for (int i = 0; i < inst.length; i++) {
                // we ensure that there is no spaces before or after the request string
                // in order to not execute empty statements
                try {
                    if (!inst[i].trim().equals("")) {
                        st.executeUpdate(inst[i]);
                        System.out.println(">>" + inst[i]);
                    }
                    break;
                }catch (SQLException e){
                    System.out.println("### - SQL Error "+e.getMessage());
                }finally {

                   continue;
                }

            }

        } catch (Exception e) {
            System.out.println("*** Error : " + e.toString());
            System.out.println("*** ");
            System.out.println("*** Error : ");
            e.printStackTrace();
            System.out.println("################################################");
            System.out.println(sb.toString());
        }

    }


    public static void main(String[] args) throws SQLException {


        resetDatabase();

    }
}