package cs4310.model;
import java.io.*;

public class LoginResultPack{
      public boolean result;
      public String type;
      public LoginPack(boolean res){
            this.result = res;
            this.result = "loginresult"
      }

      public String toJson(){
            String json =
                 "{\n"+
                    "\"result\": \""+result+"\",\n"+
                    "\"type\": \""+type+"\",\n"+
                 "}\n";
          return json;
      }
}