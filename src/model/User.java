package src.model;
import java.util.Scanner;
import java.util.NoSuchElementException;
import java.io.*;

public class User{
      public Integer id;
      public String firstName;
      public String lastName;
      public String userName;
      public String password;
      //Constructors
      public User(Integer id,String firstName,String lastName,String userName,String password){
            this.id=id;
            this.firstName=firstName;
            this.lastName=lastName;
            this.userName=userName;
            this.password=password;
            return;
      }
      //Constructor based on userName
      //Semi Functional only populates user.userName 
      public User(String userName){
            File userDB = new File("../Database/Users.dat");
            Scanner scanner = null;
            try {
			scanner = new Scanner(userDB);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
            String uitter = null;
            Integer iitter = 0;
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
                        this.id=null;
                        this.firstName=null;
                        this.lastName=null;
                        this.userName=null;
                        this.password=null;
                        return;
                  }
                  this.userName = uitter;

            }
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
