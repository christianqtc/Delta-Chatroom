package cs4310.model;
import cs4310.Main;
import java.util.Scanner;
import java.util.NoSuchElementException;
import java.io.*;
import cs4310.model.MessagePack;

public class Message{
      public String author;
      public String message;
      public String posted;
      public String edited;

      //Default Constructor
      public Message(){
            this.author = null;
            this.message = null;
            this.posted = null;
            this.edited = null;
            return;
      }
      public Message(MessagePack packets){
            this.author = packets.author;
            this.message = packets.message;
            this.posted = packets.posted;
            this.edited = packets.edited;
            return;
      }

      //Base Constructor
      public Message(String author,String message,String posted,String edited){
            this.author = author;
            this.message = message;
            this.posted = posted;
            this.edited = edited;
            return;
      }


      public String toJson(){
            String json =
                 "{\n"+
                    "\"author\": \""+author+"\",\n"+
                    "\"message\": \""+message+"\",\n"+
                    "\"posted\": \""+posted+"\",\n"+
                    "\"edited\": \""+edited+"\"\n"+
                 "}\n";
          return json;
      }

      public void addToDB(){
            //Messages.dat must have at least one record before using this function or else json will be corrupted due to a leading comma.
            //Create file object
            File messageDB;
            if ( Main.isUsingSrcFolderAsCWD() )
                messageDB = new File("Database/Messages.dat");
            else
                messageDB = new File("src/Database/Messages.dat");
            
            //Create Scanner to grab everything before the closing bracket
            Scanner scanner = null;
            try {
			scanner = new Scanner(messageDB).useDelimiter("\\{");
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
            //Create string with appeneded new message
            String appended = scanner.next()+toJson()+",";
            //Append last part of json file
            while(scanner.hasNextLine()){
                  appended+=scanner.nextLine();
                  appended+="\n";
            }
            if(scanner!=null){scanner.close();}
            //Write completed json to file
            FileWriter newMessageDB = null;
		try {
			newMessageDB = new FileWriter(messageDB);
                  newMessageDB.write(appended);
                  newMessageDB.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
            return;
      }

      //Returns array of messages from x to y inclusive, indexed at 0
      public static Message[] history(int from,int to){
            int tot = to-from;
            tot+=1;
            Message[] arr = new Message [tot];
            
            File messageDB;
            if ( Main.isUsingSrcFolderAsCWD() )
                messageDB = new File("Database/Messages.dat");
            else
                messageDB = new File("src/Database/Messages.dat");
            
            Scanner scanner = null;
            try {
			scanner = new Scanner(messageDB);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
            String itter = null;
            int count = 0;
            //Loop through to from x to y
            for(int i=from;i<=to;i+=1){
                  Message x = new Message();
                  //Onto next message
                  scanner.useDelimiter("author");
                  try {
                        scanner.next();
                  }
                  catch (NoSuchElementException e){
                        break;
                  }
                  scanner.useDelimiter("\"");
                  if (scanner.hasNext()){scanner.next();}
                  if (scanner.hasNext()){scanner.next();}
                  if (scanner.hasNext()){itter = scanner.next();}
                  x.author = itter;

                  //Message
                  scanner.useDelimiter("message");
                  if (scanner.hasNext()){scanner.next();}
                  scanner.useDelimiter("\"");
                  if (scanner.hasNext()){scanner.next();}
                  if (scanner.hasNext()){scanner.next();}
                  if (scanner.hasNext()){itter = scanner.next();}
                  x.message = itter;

                  //Posted
                  scanner.useDelimiter("posted");
                  if (scanner.hasNext()){scanner.next();}
                  scanner.useDelimiter("\"");
                  if (scanner.hasNext()){scanner.next();}
                  if (scanner.hasNext()){scanner.next();}
                  if (scanner.hasNext()){itter = scanner.next();}
                  x.posted = itter;

                  //Edited
                  scanner.useDelimiter("edited");
                  if (scanner.hasNext()){scanner.next();}
                  scanner.useDelimiter("\"");
                  if (scanner.hasNext()){scanner.next();}
                  if (scanner.hasNext()){scanner.next();}
                  if (scanner.hasNext()){itter = scanner.next();}
                  x.edited = itter;
                  arr[count] = x;
                  count+=1;
            }
            while(count<(tot)){
                  arr[count]=new Message();
                  count +=1;
            }
            return arr;
      }

      //search for message by keyword P3
}

