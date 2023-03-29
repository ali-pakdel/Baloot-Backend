package org.Baloot.Entities;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.ObjectMapper;

import com.fasterxml.jackson.databind.node.ObjectNode;
import java.util.*;

public class Comment {
    HashMap<String, Integer> votes;
    private static int count = 0;
    private int id;
    private String userEmail;
    private String username;
    private int commodityId;
    private String text;
    private String date;

    public int getId() {
        return id;
    }

    public String getUserEmail() {
        return userEmail;
    }
    public int getCommodityId() {
        return commodityId;
    }
    public String getText() {
        return text;
    }
    public String getDate() {
        return date;
    }
    public void voteComment(String username, int vote) {
        votes.put(username, vote);
    }
    public int getLikes(){
        int likesCounter = 0;
        for (int vote : votes.values()){
            if(vote == 1){
                likesCounter++;
            }
        }
        return likesCounter;
    }
    public int getDislikes(){
        int dislikesCounter = 0;
        for (int vote : votes.values()){
            if(vote == -1){
                dislikesCounter++;
            }
        }
        return dislikesCounter;
    }
    public Comment(@JsonProperty ("userEmail") String _userEmail, @JsonProperty ("commodityId") int _commodityId,
                     @JsonProperty ("text") String _text, @JsonProperty ("date") String _date) {
        userEmail = _userEmail;
        commodityId = _commodityId;
        text = _text;
        date = _date;
        votes = new HashMap<>();
        count++;
        id = count;
    }

    public void print() {
        System.out.println(this.userEmail + " " + this.commodityId + " " + this.text + " " + this.date);
    }

    public ObjectNode toJson() {
        ObjectMapper mapper = new ObjectMapper();

        ObjectNode node = mapper.createObjectNode();
        node.put("userEmail", this.userEmail);
        node.put("commodityId", this.commodityId);
        node.put("text", this.text);
        node.put("date", this.date);
        return node;
    }
}
