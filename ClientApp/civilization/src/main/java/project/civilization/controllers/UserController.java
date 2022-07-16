package project.civilization.controllers;


import java.io.*;
import java.net.Socket;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import javafx.scene.image.Image;
import org.json.JSONException;
import org.json.JSONObject;
import project.civilization.CivilizationApplication;
import project.civilization.enums.Actions;
import project.civilization.enums.MenuCategory;
import project.civilization.models.User;

public class UserController {
    public static User loggedInUser = null;
    private static HashMap<String, User> users = new HashMap<String, User>();
    private static ArrayList<String> nicknames = new ArrayList<String>();
    private static ArrayList<User> usersArray = new ArrayList<User>();
    private static DataInputStream dataInputStream;
    private static DataOutputStream dataOutputStream;

    //initialize server and stream
    public static void initializeNetwork() {
        try {
            Socket socket = new Socket("localhost", 772);
            dataInputStream = new DataInputStream(socket.getInputStream());
            dataOutputStream = new DataOutputStream(socket.getOutputStream());
        } catch (IOException x) {
            x.printStackTrace();
        }
    }

    public static void setLoggedInUser(User user) {
        loggedInUser = user;
    }

    public static ArrayList<User> getUsersArray() {
        return usersArray;
    }

    public static User getLoggedInUser() {
        return loggedInUser;
    }

    public static HashMap<String, User> getUsers() {
        return users;
    }

    public static String register(String username, String password, String nickname) {
        JSONObject json = new JSONObject();
        try {
            json.put("menu", MenuCategory.LOGIN.getCharacter());
            json.put("action", Actions.REGISTER.getCharacter());
            json.put("username", username);
            json.put("password", password);
            json.put("nickname", nickname);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        try {
            dataOutputStream.writeUTF(json.toString());
            dataOutputStream.flush();
            return dataInputStream.readUTF();
        } catch (IOException x) {
            x.printStackTrace();
            return "something is wrong";
        }
    }

    public static String logoutUSer() {
        JSONObject json = new JSONObject();
        try {
            json.put("menu", MenuCategory.MAIN.getCharacter());
            json.put("action", Actions.LOGOUT.getCharacter());
            json.put("UUID",User.getUuid());
        } catch (JSONException e) {
            e.printStackTrace();
        }
        try {
            dataOutputStream.writeUTF(json.toString());
            dataOutputStream.flush();
            return dataInputStream.readUTF();
        } catch (IOException x) {
            x.printStackTrace();
            return "something is wrong";
        }
    }

    public static ArrayList<String> getTopUsers() {
        JSONObject json = new JSONObject();
        try {
            json.put("menu", MenuCategory.MAIN.getCharacter());
            json.put("action", Actions.SCOREBOARD.getCharacter());
        } catch (JSONException e) {
            e.printStackTrace();
        }
        try {
            dataOutputStream.writeUTF(json.toString());
            dataOutputStream.flush();
            try {
                return new Gson().fromJson(dataInputStream.readUTF(), new TypeToken<List<String>>() {
                }.getType());

            } catch (IOException e) {
                e.printStackTrace();
            }
        } catch (IOException x) {
            x.printStackTrace();
        }
        return null;
    }

    public static String deleteAccount() {
        JSONObject json = new JSONObject();
        try {
            json.put("menu", MenuCategory.PROFILE.getCharacter());
            json.put("action", Actions.DELETEACOUNT.getCharacter());
            json.put("UUID",User.getUuid());
        } catch (JSONException e) {
            e.printStackTrace();
        }
        try {
            dataOutputStream.writeUTF(json.toString());
            dataOutputStream.flush();
            return dataInputStream.readUTF();
        } catch (IOException x) {
            x.printStackTrace();
        }
        return null;
    }

    public void setUsers(HashMap<String, User> users) {
        UserController.users = users;
    }

    public static void setNicknames(ArrayList<String> nicknames) {
        UserController.nicknames = nicknames;
    }

    public ArrayList<String> getNicknames() {
        return nicknames;
    }

    public void createUser(String username, String password, String nickname) {
        User user = new User(username, password, nickname);
        users.put(username, user);
        nicknames.add(nickname);
    }

    public static String changeNickname(String nickname) {
        JSONObject json = new JSONObject();
        try {
            json.put("menu", MenuCategory.PROFILE.getCharacter());
            json.put("action", Actions.CHAGENICKNAME.getCharacter());
            json.put("nickname",nickname);
            json.put("UUID",User.getUuid());
        } catch (JSONException e) {
            e.printStackTrace();
        }
        try {
            dataOutputStream.writeUTF(json.toString());
            dataOutputStream.flush();
            return dataInputStream.readUTF();
        } catch (IOException x) {
            x.printStackTrace();
            return "something is wrong";
        }
    }

    public static boolean isUsernameUnique(String username) {
        for (User user : usersArray) {
            if (user.getUsername().equals(username)) {
                return false;
            }
        }
        return true;
    }

    public static String login(String username, String password) {
        JSONObject json = new JSONObject();
        try {
            json.put("menu", MenuCategory.LOGIN.getCharacter());
            json.put("action", Actions.LOGIN.getCharacter());
            json.put("username", username);
            json.put("password", password);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        try {
            dataOutputStream.writeUTF(json.toString());
            dataOutputStream.flush();
            String res = dataInputStream.readUTF();
            JSONObject obj = new JSONObject(res);
            if(obj.getString("message").equals("logged in")){
                User.setUuid(obj.getString("UUID"));
            }
            return obj.getString("message");
        } catch (IOException x) {
            x.printStackTrace();
            return "something is wrong";
        }
    }

    public void Login(String username, String password) {
        loggedInUser = users.get(username);
    }

    public static String logout() {
        if (loggedInUser == null) {
            return "you have not logged in yet";
        }
        loggedInUser = null;
        return "logout successfully";
    }

    public static void saveUsers() {
        JSONObject json = new JSONObject();
        try {
            json.put("menu", MenuCategory.LOGIN.getCharacter());
            json.put("action", Actions.SAVEUSERS.getCharacter());
        } catch (JSONException e) {
            e.printStackTrace();
        }
        try {
            dataOutputStream.writeUTF(json.toString());
            dataOutputStream.flush();
            dataInputStream.readUTF();
        } catch (IOException x) {
            x.printStackTrace();
        }
    }

    public static void importSavedUsers() {
        try {
            URL address = new URL(Objects.requireNonNull(CivilizationApplication.class.getResource("files/UserInfo.txt")).toExternalForm());
            String user = new String(Files.readAllBytes(Paths.get(address.toURI())));
            if (user.equals("")) {
                return;
            }
            String[] readUser = user.split("\n");
            for (String temp : readUser) {
                String[] read = temp.split(" ");
                String Username = read[0];
                String Password = read[1];
                String Nickname = read[2];
                int score = Integer.parseInt(read[3]);
                int picNum = Integer.parseInt(read[4]);


                User addUser = new User(Username, Password, Nickname);

                addUser.setAvatarPic(new Image(CivilizationApplication.class.getResource("pictures/avatar/" + picNum + ".png").toExternalForm()), picNum);
                addUser.increaseScore(score);
                users.put(Username, addUser);
                usersArray.add(addUser);
                nicknames.add(Nickname);
            }

        } catch (IOException | URISyntaxException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    public static boolean isNicknameUsed(String newNickname) {
        for (String nickname : nicknames) {
            if (nickname.equals(newNickname))
                return true;
        }
        return false;
    }

    public static boolean isPasswordValid(String password) {
        return loggedInUser.getPassword().equals(password);
    }



    public static String changePassword( String newPass) {

        JSONObject json = new JSONObject();
        try {
            json.put("menu", MenuCategory.PROFILE.getCharacter());
            json.put("action", Actions.CHAGEPASSWORD.getCharacter());
            json.put("password",newPass);
            json.put("UUID",User.getUuid());
        } catch (JSONException e) {
            e.printStackTrace();
        }
        try {
            dataOutputStream.writeUTF(json.toString());
            dataOutputStream.flush();
            return dataInputStream.readUTF();
        } catch (IOException x) {
            x.printStackTrace();
            return "something is wrong";
        }
    }
}
