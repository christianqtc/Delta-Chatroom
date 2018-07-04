package model;
import java.util.Scanner;
import java.io.*;

public class User{
      public int id;
      public String firstName;
      public String lastName;
      public String userName;
      public String password;

      public User(int id,String firstName,String lastName,String userName,String password){
            this.id=id;
            this.firstName=firstName;
            this.lastName=lastName;
            this.userName=userName;
            this.password=password;
      }
      public String toJson(){
            String json =
                 "{\n"+
                    "\"id\": \""+id+"\",\n"+
                    "\"firstName\": \""+firstName+"\",\n"+
                    "\"lastName\": \""+lastName+"\",\n"+
                    "\"userName\": \""+userName+"\",\n"+
                    "\"password\": \""+password+"\"\n"+
                 "}\n";
          return json;
      }
      public void addToDB(){
            //Users.dat must have at least one user before using this function or else json will be corrupted.
            //Create file object
            File userDB = new File("../Database/Users.dat");
            //Create Scanner to grab everything before the closing bracket
            Scanner scanner = null;
            try {
			scanner = new Scanner(userDB).useDelimiter("]");
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
            //Create string with appeneded new user
            String appended = scanner.next()+","+toJson();
            //Append last part of json file
            while(scanner.hasNextLine()){appended+=scanner.nextLine();}
            //Write completed json to file
            FileWriter newUserDB = null;
		try {
			newUserDB = new FileWriter(userDB);
                  newUserDB.write(appended);
                  newUserDB.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
      }
}
