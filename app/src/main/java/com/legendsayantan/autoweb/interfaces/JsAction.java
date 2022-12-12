package com.legendsayantan.autoweb.interfaces;

import static com.legendsayantan.autoweb.BuildConfig.DEBUG;

/**
 * @author legendsayantan
 */
public class JsAction {
    public JsAction(String url, ActionType actionType) {
        this.url = url;
        this.actionType = actionType;
    }
    public enum ActionType {
        click,
        change,
        scroll,
        pause
    }
    public ActionType actionType;
    String url,element,value;
    public JsAction(String url,ActionType actionType, String element, String value) {
        this.url = url;
        this.actionType = actionType;
        this.element = element;
        this.value = value;
    }
    public JsAction(String url,ActionType actionType, String element) {
        this.url = url;
        this.actionType = actionType;
        this.element = element;
    }

    public JsAction() {
    }

    public static String getJs(JsAction jsAction){
        String debug = DEBUG?"console.log('"+jsAction.actionType+" "+jsAction.element+" "+jsAction.value+"');":"";
        switch (jsAction.actionType){
            case click:
                return debug+"document.getElementById('"+jsAction.element+"').click();";
            case change:
                return debug+"document.getElementById('"+jsAction.element+"').value='"+jsAction.value+"';";
            case scroll:
                return debug;
            case pause:
                return "console.log('Paused Execution');";
        }
        return null;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getElement() {
        return element;
    }

    public void setElement(String element) {
        this.element = element;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }
}
