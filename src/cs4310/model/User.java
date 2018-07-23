package cs4310.model;
import java.util.Scanner;
import java.util.NoSuchElementException;
import java.io.*;
import java.io.File;

public class User{
      public String firstName;
      public String lastName;
      public String userName;
      public String password;
      //Constructor
      public User(String firstName,String lastName,String userName,String password){
            this.userName=userName;
            this.firstName=firstName;
            this.lastName=lastName;
            this.password=password;
            return;
      }
      //Constructor based on userName
      public User(String userName){
            File userDB = new File("Database/Users.dat");
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
      public User(){
            this.userName=null;
            this.firstName=null;
            this.lastName=null;
            this.password=null;
            return;
      }
      public String toJson(){
            String json =
                 "{\n"+
                    "\"userName\": \""+userName+"\",\n"+
                    "\"firstName\": \""+firstName+"\",\n"+
                    "\"lastName\": \""+lastName+"\",\n"+
                    "\"password\": \""+password+"\"\n"+
                 "}\n";
          return json;
      }

      public void addToDB(){
            //Users.dat must have at least one user before using this function or else json will be corrupted.
            //Create file object
            File userDB = new File("Database/Users.dat");
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
