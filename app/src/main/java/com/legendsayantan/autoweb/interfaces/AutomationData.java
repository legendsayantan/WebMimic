package com.legendsayantan.autoweb.interfaces;


import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.ArrayList;
import java.util.Random;

/**
 * @author legendsayantan
 */

public class AutomationData {
    private String name;
    public ArrayList<JsAction> jsActions = new ArrayList<>();
    public int color;
    long delay;

    public AutomationData(String name, ArrayList<JsAction> jsActions, long delay) {
        this.name = name;
        this.jsActions = jsActions;
        this.delay = delay;
        this.color = new Random().nextInt(4);
    }
    public AutomationData(String name, ArrayList<JsAction> jsActions) {
        this.name = name;
        this.jsActions = jsActions;
        this.color = new Random().nextInt(4);
    }

    public AutomationData() {
        this.color = new Random().nextInt(4);
    }
    public AutomationData(long delay) {
        this.color = new Random().nextInt(4);
        this.delay = delay;
    }


    public AutomationData(String s, int i) {
        this.name = s;
        this.color = i;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public long getDelay() {
        return delay;
    }

    public void setDelay(long delay) {
        if(delay>=100){
            this.delay = delay;
        }else {
            this.delay = 100;
        }
    }
    public static String toJson(AutomationData automationData) throws JsonProcessingException {
        final ObjectMapper objectMapper = new ObjectMapper();
        return objectMapper.writeValueAsString(automationData);
    }
    public static AutomationData fromJson(String json) throws JsonProcessingException {
        final ObjectMapper objectMapper = new ObjectMapper();
        return objectMapper.readValue(json, new TypeReference<AutomationData>(){});
    }
}
