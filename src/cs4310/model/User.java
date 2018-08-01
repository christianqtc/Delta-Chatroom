package cs4310.model;
import cs4310.Main;
import cs4310.model.RegistrationPack;
import java.util.Scanner;
import java.util.NoSuchElementException;
import java.util.regex.Pattern;
import java.util.regex.Matcher;
import java.io.*;
import java.io.File;

public class User{
      public String userName;
      public String firstName;
      public String lastName;
      public String password;
      //Default
      public User(){
            this.userName=null;
            this.firstName=null;
            this.lastName=null;
            this.password=null;
            return;
      }
      //Base constructor
      public User(String firstName,String lastName,String userName,String password){
            this.userName=userName;
            this.firstName=firstName;
            this.lastName=lastName;
            this.password=password;
            return;
      }
      //Regpacket to User construcor
      public User(RegistrationPack packet){
            this.userName=packet.userName;
            this.firstName=packet.firstName;
            this.lastName=packet.lastName;
            this.password=packet.password;
            return;
      }
      //Constructor based on userName
      public User(String userName){
            File userDB;
            if ( Main.isUsingSrcFolderAsCWD() )
                userDB = new File("Database/Users.dat");
            else
                userDB = new File("src/Database/Users.dat");
            
            Scanner scanner = null;
            try {
                  scanner = new Scanner(userDB);
            } catch (FileNotFoundException e) {
                  e.printStackTrace();
            }
            String uitter = null;
            //loop to find username
            while(!userName.equals(uitter)){
                  scanner.useDelimiter("userName");
                  if (scanner.hasNext()){scanner.next();}
                  scanner.useDelimiter("\"");
                  if (scanner.hasNext()){scanner.next();}
                  if (scanner.hasNext()){scanner.next();}
                  try {
                        uitter = scanner.next();
                  }
                  catch (NoSuchElementException e){
                        System.out.println("No User: "+userName);
                        this.userName=null;
                        this.firstName=null;
                        this.lastName=null;
                        this.password=null;
                        return;
                  }
                  this.userName = uitter;
            }
            //Gets firstName
            scanner.useDelimiter("firstName");
            if (scanner.hasNext()){scanner.next();}
            scanner.useDelimiter("\"");
            if (scanner.hasNext()){scanner.next();}
            if (scanner.hasNext()){scanner.next();}
            uitter = scanner.next();
            this.firstName = uitter;

            //Gets lastName
            scanner.useDelimiter("lastName");
            if (scanner.hasNext()){scanner.next();}
            scanner.useDelimiter("\"");
            if (scanner.hasNext()){scanner.next();}
            if (scanner.hasNext()){scanner.next();}
            uitter = scanner.next();
            this.lastName = uitter;

            //Gets password
            scanner.useDelimiter("password");
            if (scanner.hasNext()){scanner.next();}
            scanner.useDelimiter("\"");
            if (scanner.hasNext()){scanner.next();}
            if (scanner.hasNext()){scanner.next();}
            uitter = scanner.next();
            this.password = uitter;
      }

      public static void removeFromDB(String targ){
            File userDB;
            if ( Main.isUsingSrcFolderAsCWD() )
                userDB = new File("Database/Users.dat");
            else
                userDB = new File("src/Database/Users.dat");
            
            Scanner scanner = null;
            try {
                  scanner = new Scanner(userDB);
            } catch (FileNotFoundException e) {
                  e.printStackTrace();
            }
            //create db string
            String db ="";
            while (scanner.hasNext()) {
                  db +=scanner.next();
            }
            //search for json objects
            Pattern pat = Pattern.compile("\\{[^\\}]*\\},");
            Matcher mat = pat.matcher(db);
            while (mat.find()) {
                  String match = mat.group(0);
                  String userName = null;
                  Scanner miniscan = new Scanner(match);
                  miniscan.useDelimiter("userName");
                  if (miniscan.hasNext()){miniscan.next();}
                  miniscan.useDelimiter("\"");
                  if (miniscan.hasNext()){miniscan.next();}
                  if (miniscan.hasNext()){miniscan.next();}
                  if (miniscan.hasNext()){userName=miniscan.next();}
                  if(userName.equals(targ)){
                        db=db.replace(match,"");
                        break;
                  }
            }
            FileWriter newUserDB = null;
            try {
                  newUserDB = new FileWriter(userDB);
                  newUserDB.write(db);
                  newUserDB.close();
            } catch (IOException e) {
                  e.printStackTrace();
            }
            return;

      }
      public String toJson(){
            String json =
            "{"+
            "\"userName\": \""+userName+"\","+
            "\"firstName\": \""+firstName+"\","+
            "\"lastName\": \""+lastName+"\","+
            "\"password\": \""+password+"\""+
            "}";
            return json;
      }

      public void addToDB(){
            //Users.dat must have at least one user before using this function or else json will be corrupted.
            //Create file object
            File userDB;
            if ( Main.isUsingSrcFolderAsCWD() )
                userDB = new File("Database/Users.dat");
            else
                userDB = new File("src/Database/Users.dat");
            
            //Create Scanner to grab everything before the closing bracket
            Scanner scanner = null;
            try {
                  scanner = new Scanner(userDB).useDelimiter("\\{");
            } catch (FileNotFoundException e) {
                  e.printStackTrace();
            }
            //Create string with appeneded new user
            String appended = scanner.next()+toJson()+",";
            //Append last part of json file
            while(scanner.hasNextLine()){
                  appended+=scanner.nextLine();
                  appended+="\n";
            }
            if(scanner!=null){scanner.close();}
            //Write completed json to file
            FileWriter newUserDB = null;
            try {
                  newUserDB = new FileWriter(userDB);
                  newUserDB.write(appended);
                  newUserDB.close();
            } catch (IOException e) {
                  e.printStackTrace();
            }
            return;
      }
}
