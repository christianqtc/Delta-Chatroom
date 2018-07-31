package cs4310.model;
import java.util.Scanner;
import java.util.NoSuchElementException;
import java.io.*;
import cs4310.model.Message;

public class MessagePack{
      public String author;
      public String message;
      public String posted;
      public String edited;
      public String type;
      public MessagePack(String json){
            Scanner scanner = new Scanner(json);
            //Author
            scanner.useDelimiter("author");
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
            if (scanner.hasNext()){this.author = scanner.next();}

            //Message
            scanner.useDelimiter("message");
            if (scanner.hasNext()){scanner.next();}
            scanner.useDelimiter("\"");
            if (scanner.hasNext()){scanner.next();}
            if (scanner.hasNext()){scanner.next();}
            if (scanner.hasNext()){this.message = scanner.next();}

            //Posted
            scanner.useDelimiter("posted");
            if (scanner.hasNext()){scanner.next();}
            scanner.useDelimiter("\"");
            if (scanner.hasNext()){scanner.next();}
            if (scanner.hasNext()){scanner.next();}
            if (scanner.hasNext()){this.posted = scanner.next();}

            //Edited
            scanner.useDelimiter("edited");
            if (scanner.hasNext()){scanner.next();}
            scanner.useDelimiter("\"");
            if (scanner.hasNext()){scanner.next();}
            if (scanner.hasNext()){scanner.next();}
            if (scanner.hasNext()){this.edited = scanner.next();}

            //Type
            scanner.useDelimiter("type");
            if (scanner.hasNext()){scanner.next();}
            scanner.useDelimiter("\"");
            if (scanner.hasNext()){scanner.next();}
            if (scanner.hasNext()){scanner.next();}
            if (scanner.hasNext()){this.type = scanner.next();}
      }
      public MessagePack(Message message){
            this.author=message.author;
            this.message=message.message;
            this.posted=message.posted;
            this.edited=message.edited;
            this.type="message";
      }
      public String toJson(){
            String json =
                 "{\n"+
                    "\"author\": \""+author+"\",\n"+
                    "\"message\": \""+message+"\",\n"+
                    "\"posted\": \""+posted+"\",\n"+
                    "\"edited\": \""+edited+"\",\n"+
                    "\"type\": \""+type+"\"\n"+
                 "}\n";
          return json;
      }
}
