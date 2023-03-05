package com.github.DerekMcCauley2002;

import com.google.gson.*;
import org.javacord.api.DiscordApi;

import org.javacord.api.entity.message.MessageSet;
import org.javacord.api.event.message.MessageCreateEvent;
import org.javacord.api.listener.message.MessageCreateListener;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.*;


public class Commands extends Main {

    private DiscordApi api;
    private String msg = "empty";
    private final String RIOTAPI = "RGAPI-838186e9-3ba6-4085-9f3a-60638a7e00b5";
    private Gson gson = new Gson();


    public Commands() {
        this.api = null;
    }

    public Commands(DiscordApi api) {
        this.api = api;
    }

    public boolean testForAliases(String[] aList, String msg) {
        boolean isAnAlias = false;
        ArrayList<String> aliases = new ArrayList<>(aList.length);
        aliases.addAll(Arrays.asList(aList));

        if (msg.contains(" ")) {
            String substring = msg.substring(0, (msg.indexOf(' ')));
            for (int i = 0; i < aList.length; i++) {
                if (substring.equalsIgnoreCase(aliases.get(i))) {
                    isAnAlias = true;
                    break;
                }
            }
        } else {
            for (int i = 0; i < aList.length; i++) {
                if (msg.equalsIgnoreCase(aliases.get(i))) {
                    isAnAlias = true;
                    break;
                }
            }
        }


        return isAnAlias;
    }

    public String nameToID(String name) {

        byte[] bytes = name.getBytes(StandardCharsets.UTF_8);

        String utf8Name = new String(bytes, StandardCharsets.UTF_8);
        utf8Name = utf8Name.stripLeading();

        String convertedName = utf8Name.replaceAll(" ", "_"); //fixes issue with spaces in name
        //System.out.println(convertedName);
        String idUrl = "https://na1.api.riotgames.com/lol/summoner/v4/summoners/by-name/" + utf8Name + "?api_key=" + RIOTAPI;
        String basicSummonerID = "none";
        try(java.io.InputStream is = new java.net.URL(idUrl).openStream()) {
            String contents = new String(is.readAllBytes());
            com.google.gson.JsonElement element = com.google.gson.JsonParser.parseString(contents); //from 'com.google.code.gson:gson:2.8.6'
            JsonObject response = element.getAsJsonObject();
            JsonElement idResponse = response.get("id");
            basicSummonerID = idResponse.getAsString();
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return basicSummonerID;
    }

    public void setupCommands() {

        //ERROR IS IN PARSING SPECIAL CHARACTERS
        //Command for RANK
        api.addMessageCreateListener(event -> {
           msg = event.getMessageContent();
           String[] aliasList = {"?leaguerank", "?rank", "?rstats"};

           if (testForAliases(aliasList, msg)) {

               System.out.println(event.getMessage());

               byte[] bytes = event.getMessageContent().getBytes(StandardCharsets.UTF_8);

               String utf8Input = new String(bytes, StandardCharsets.UTF_8);
               //utf8Input = utf8Input.replaceAll(" ", "%");
               System.out.println(utf8Input);

               String summNameInput = msg.substring(msg.indexOf(' ') + 3);
               System.out.println(summNameInput);

               String summonerID = nameToID(summNameInput);
               String url = "https://na1.api.riotgames.com/lol/league/v4/entries/by-summoner/" + summonerID + "?api_key=" + RIOTAPI;
               try(java.io.InputStream is = new java.net.URL(url).openStream()) {
                   String contents = new String(is.readAllBytes());
                   com.google.gson.JsonElement element = com.google.gson.JsonParser.parseString(contents); //from 'com.google.code.gson:gson:2.8.6'
                   JsonArray jsonArray = element.getAsJsonArray();
                   if (msg.split(" ")[1].equalsIgnoreCase("s")) {
                       System.out.println("passed");
                   }
                   JsonObject solo5v5 = (JsonObject) jsonArray.get(0);
                   event.getChannel().sendMessage(utf8Input + " is: " + solo5v5.get("tier").getAsString() + " " + solo5v5.get("rank").getAsString());

               } catch (MalformedURLException e) {
                   e.printStackTrace();
               } catch (IOException e) {
                   e.printStackTrace();
                   event.getChannel().sendMessage("Player not found!");
               }
           }
        });

        //Command for SUMMONER LEVEL
        api.addMessageCreateListener(event -> {
            msg = event.getMessageContent();
            String[] aliasList = {"?summlevel", "?slvl", "?userlvl"};
            if (testForAliases(aliasList, msg)) {
                String summNameInput = msg.substring(msg.indexOf(' ') + 1);
                String url = "https://na1.api.riotgames.com/lol/summoner/v4/summoners/by-name/" + summNameInput + "?api_key=" + RIOTAPI;

                try(java.io.InputStream is = new java.net.URL(url).openStream()) {
                    String contents = new String(is.readAllBytes());
                    com.google.gson.JsonElement element = com.google.gson.JsonParser.parseString(contents); //from 'com.google.code.gson:gson:2.8.6'
                    JsonObject response = element.getAsJsonObject();
                    JsonElement summLevel = response.get("summonerLevel");
                    int summLevelAsInt = summLevel.getAsInt();
                    event.getChannel().sendMessage(summNameInput + " is level: " + summLevelAsInt);

                } catch (MalformedURLException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });

        //Command for CLEAR
        api.addMessageCreateListener(event -> {
            if (event.getMessageContent().contains("?clear")) {
                if (event.getMessageContent().indexOf(' ') <= 0) {
                    event.getChannel().sendMessage("Please provide a number of messages to delete.");
                } else {
                    String[] inputSplit = event.getMessageContent().split(" ");
                    int num = Integer.parseInt(inputSplit[1]);

                    if (inputSplit[0].equals("?clear")) {
                        CompletableFuture<MessageSet> msg = event.getChannel().getMessages(num);
                        try {
                            event.getChannel().deleteMessages(msg.get(5, TimeUnit.SECONDS));
                        } catch (InterruptedException e) {
                            event.getChannel().sendMessage("Took too long, wait and try again!");
                        } catch (ExecutionException e) {
                            event.getChannel().sendMessage("Took too long, wait and try again!");
                        } catch (TimeoutException e) {
                            event.getChannel().sendMessage("Took too long, wait and try again!");
                        }
                    }
                }
            }
        });
    }


}
