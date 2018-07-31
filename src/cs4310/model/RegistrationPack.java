package cs4310.model;
import java.util.Scanner;
import java.util.NoSuchElementException;
import java.io.*;

public class RegistrationPack{
      public String userName;
      public String firstName;
      public String lastName;
      public String password;
      public String oldUserName;
      public String type;
      public RegistrationPack(String json){
            Scanner scanner = new Scanner(json);
            //userName
            scanner.useDelimiter("userName");
            try {
                  scanner.next();
            }
            catch (NoSuchElementException e){
                  System.out.println("Message is corrupted");
                  return;
            }
            scanner.useDelimiter("\"");
            if (scanner.hasNext()){scanner.next();}
            if (scanner.hasNext()){scanner.next();}
            if (scanner.hasNext()){this.userName = scanner.next();}

            //firstName
            scanner.useDelimiter("firstName");
            if (scanner.hasNext()){scanner.next();}
            scanner.useDelimiter("\"");
            if (scanner.hasNext()){scanner.next();}
            if (scanner.hasNext()){scanner.next();}
            if (scanner.hasNext()){this.firstName = scanner.next();}

            //lastName
            scanner.useDelimiter("lastName");
            if (scanner.hasNext()){scanner.next();}
            scanner.useDelimiter("\"");
            if (scanner.hasNext()){scanner.next();}
            if (scanner.hasNext()){scanner.next();}
            if (scanner.hasNext()){this.lastName = scanner.next();}

            //password
            scanner.useDelimiter("password");
            if (scanner.hasNext()){scanner.next();}
            scanner.useDelimiter("\"");
            if (scanner.hasNext()){scanner.next();}
            if (scanner.hasNext()){scanner.next();}
            if (scanner.hasNext()){this.password = scanner.next();}

            //oldPassword
            scanner.useDelimiter("oldUserName");
            if (scanner.hasNext()){scanner.next();}
            scanner.useDelimiter("\"");
            if (scanner.hasNext()){scanner.next();}
            if (scanner.hasNext()){scanner.next();}
            if (scanner.hasNext()){this.oldUserName = scanner.next();}

            //Type
            scanner.useDelimiter("type");
            if (scanner.hasNext()){scanner.next();}
            scanner.useDelimiter("\"");
            if (scanner.hasNext()){scanner.next();}
            if (scanner.hasNext()){scanner.next();}
            if (scanner.hasNext()){this.type = scanner.next();}
      }
}