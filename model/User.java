package model;

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
}
