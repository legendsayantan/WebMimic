export default class JsAction {
    constructor(url,action,element,value) {
        this.url = url;
        this.element = element;
        this.value = value;
        this.actionType = action;
    }
}
