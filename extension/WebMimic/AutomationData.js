export default class data{
    constructor(options){
        if(typeof(options)==='undefined')options={};
        this.name = options.name || "";
        this.delay = options.delay || 250;
        this.desktopMode = options.desktopMode || true;
        this.color = options.color || Math.round(Math.random()*4);
        this.landscape = options.landscape || true;
        this.jsActions = options.jsActions || [];
    }
    static optimise(data){
        let index = 0;
        while(index < data.jsActions.length-1){
            if(data.jsActions[index].actionType=='pause'){
                index++;
                continue;
            }
            if (data.jsActions[index].actionType == 'change' &&
                    data.jsActions[index + 1].actionType == 'change' &&
                    data.jsActions[index].element==data.jsActions[index + 1].element) {
                data.jsActions = data.jsActions.filter(obj => obj !== data.jsActions[index]);
                continue;
            }
            for (let i = 1+index; i < data.jsActions.length; i++) {
                if(data.jsActions[index].url==null)break;
                if(data.jsActions[i].url==null)continue;
                if(data.jsActions[i].url==data.jsActions[index].url){
                    data.jsActions[i].url = null;
                }else break;
            }
            index++;
        }
        return data;
    }
}