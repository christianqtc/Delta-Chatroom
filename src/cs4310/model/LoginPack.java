package cs4310.model;
import java.util.Scanner;
import java.util.NoSuchElementException;
import java.io.*;

public class LoginPack{
      public String userName;
      public String password;
      public String type;
      public LoginPack(String json){
            Scanner scanner = new Scanner(json);
            //UserName
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

            //Password
            scanner.useDelimiter("password");
            if (scanner.hasNext()){scanner.next();}
            scanner.useDelimiter("\"");
            if (scanner.hasNext()){scanner.next();}
            if (scanner.hasNext()){scanner.next();}
            if (scanner.hasNext()){this.password = scanner.next();}

            //Type
            scanner.useDelimiter("type");
            if (scanner.hasNext()){scanner.next();}
            scanner.useDelimiter("\"");
            if (scanner.hasNext()){scanner.next();}
            if (scanner.hasNext()){scanner.next();}
            if (scanner.hasNext()){this.type = scanner.next();}
      }
}