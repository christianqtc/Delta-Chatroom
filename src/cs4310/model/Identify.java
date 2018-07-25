package cs4310.model;
import java.util.Scanner;
import java.util.NoSuchElementException;
import java.io.*;

public class Identify{
      public static String type(String json){
            Scanner scanner = new Scanner(json);
            scanner.useDelimiter("type");
            try {
                  scanner.next();
            }
            catch (NoSuchElementException e){
                  System.out.println("Message is corrupted");
                  return null;
            }
            scanner.useDelimiter("\"");
            if (scanner.hasNext()){scanner.next();}
            if (scanner.hasNext()){scanner.next();}
            if (scanner.hasNext()){return scanner.next();}
            else{return null;}
      }

}