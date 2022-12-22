/*
@author legendsayantan
*/
var page = document.getElementsByTagName("*");
var inputs = document.getElementsByTagName("input");
var selections = document.getElementsByTagName("select");
var eventBuffer;
var clicker = "event = event || window.event;"
+"var source = event.target || event.srcElement;"
+"if(eventBuffer!=event)console.log('click-->'+source.id);"
+"eventBuffer=window.event;";
var changer = "console.log('change-->'+this.id+'-->'+this.value)";
var submitter = "console.log('submit-->'+this.id)";
async function initViews(node,parentId) {
    var x = 0;
    for (var i = 0; i < node.length; i++) {
        var view = node[i];
        if (view.id == null || view.id == "") {
            view.id = parentId + "_" + x;
            x++;
        }
        if (view.hasChildNodes()) {
            initViews(view.childNodes,view.id);
        }
    }
}
async function logInputs() {
    for (var i = 0; i < inputs.length; i++) {
        var element = inputs[i];
        if(element.hasAttribute("onclick") && element.getAttribute('onclick')!=clicker) {
            element.setAttribute("onclick", element.getAttribute('onclick')+";"+clicker);
        }else{
            element.setAttribute("onclick",clicker);
        }
        if(element.hasAttribute("onkeyup") && element.getAttribute('onkeyup')!=changer) {
            element.setAttribute("onkeyup", element.getAttribute('onkeyup')+";"+changer);
        }else{
            element.setAttribute("onkeyup",changer);
        }

    }
}
async function logSelections(){
    for (var i = 0; i < selections.length; i++) {
        var element = selections[i];
        if(element.hasAttribute("onchange") && element.getAttribute('onchange')!=changer) {
            element.setAttribute("onchange", element.getAttribute('onchange')+";"+changer);
        }else{
            element.setAttribute("onchange",changer);
        }
        //remove onclick attribute
        if(element.hasAttribute("onclick")) {
            if(element.getAttribute('onclick')==clicker)element.removeAttribute("onclick");
            else element.setAttribute("onclick",element.getAttribute('onclick').replace(clicker,""));
        }
    }
}
async function logClicks() {
    for (var i = 0; i < page.length; i++) {
        var element = page[i];
        if(element.hasAttribute("onclick") && element.getAttribute('onclick')!=clicker) {
            element.setAttribute("onclick", element.getAttribute('onclick')+";"+clicker);
        }else{
            element.setAttribute("onclick",clicker);
        }
    }
}
async function treeObserver(){
// Callback function to execute when mutations are observed
    var callback = async function(mutationsList) {
        await main();
    };
    var targetNode = document.getElementsByTagName('html')[0];
    if(!targetNode) targetNode = document.getElementsByTagName('body')[0];
    var config = {
        attributes: false,
        childList: true,
        subtree: true
    };
    var observer = new MutationObserver(callback);
    observer.observe(targetNode, config);
}
async function main(){
await initViews(document.childNodes,"root");

await logInputs();
await logClicks();
await logSelections();
}

main();
treeObserver();
