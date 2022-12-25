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
    boolean desktopMode = false;
    boolean landscape = false;

    public boolean isLandscape() {
        return landscape;
    }

    public void setLandscape(boolean landscape) {
        this.landscape = landscape;
    }

    public boolean isDesktopMode() {
        return desktopMode;
    }

    public void setDesktopMode(boolean desktopMode) {
        this.desktopMode = desktopMode;
    }

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
        if(delay>=250){
            this.delay = delay;
        }else {
            this.delay = 250;
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
    public static AutomationData optimise(AutomationData automationData){
        int index = 0;
        while(index < automationData.jsActions.size()-1){
            if(automationData.jsActions.get(index).actionType.equals(JsAction.ActionType.pause)){
                index++;
                continue;
            }
            if (automationData.jsActions.get(index).actionType == JsAction.ActionType.change &&
                    automationData.jsActions.get(index + 1).actionType == JsAction.ActionType.change &&
                    automationData.jsActions.get(index).getElement().equals(automationData.jsActions.get(index + 1).getElement())) {
                automationData.jsActions.remove(index);
                continue;
            }
//            if (automationData.jsActions.get(index).type == JsAction.Type.click &&
//                    automationData.jsActions.get(index + 1).type == JsAction.Type.click &&
//                    automationData.jsActions.get(index).getElement().equals(automationData.jsActions.get(index + 1).getElement()) &&
//                    Long.parseLong(automationData.jsActions.get(index+1).getValue()) - Long.parseLong(automationData.jsActions.get(index).getValue()) < 50) {
//                automationData.jsActions.remove(index);
//                continue;
//            }
            for (int i = 1+index; i < automationData.jsActions.size(); i++) {
                if(automationData.jsActions.get(index).getUrl()==null)break;
                if(automationData.jsActions.get(i).getUrl()==null)continue;
                if(automationData.jsActions.get(i).getUrl().equals(automationData.jsActions.get(index).getUrl())){
                    automationData.jsActions.get(i).setUrl(null);
                }else break;
            }
            index++;
        }
        return automationData;
    }
}
