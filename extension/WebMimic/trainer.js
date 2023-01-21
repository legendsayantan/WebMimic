/*
@author legendsayantan
*/
var page = document.getElementsByTagName("*");
var inputs = document.getElementsByTagName("input");
var selections = document.getElementsByTagName("select");
var eventBuffer = null;
function changer(event) {
    var source = event.target || event.srcElement;
    console.log('change-->'+source.id+'-->'+source.value);
}
function clicker(){
    var event = event || window.event;
    if(event==null ||(event.type!='click' && event.type!='input'))return;
    var source = event.target || event.srcElement;
    if(source.nodeName == "SELECT")return;
    if(eventBuffer!=event)console.log('click-->'+source.id);
    eventBuffer=window.event;
}

async function initViews(node,parentId) {
    let x = 0;
    for (var i = 0; i < node.length; i++) {
        let view = node[i];
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
        let element = inputs[i];
        element.addEventListener('click',function(e){
            clicker();
        });
        element.addEventListener('keyup',function(e){
            if(e.isTrusted)changer(e);
        });
    }
}
async function logSelections(){
    for (var i = 0; i < selections.length; i++) {
        let element = selections[i];
        element.addEventListener('change', function(e){
            if(e.isTrusted)changer(e);
        });
    }
}
async function logClicks() {
    for (var i = 0; i < page.length; i++) {
        var element = page[i];
        element.addEventListener('click', function(e){
            clicker();
        });
    }
}

async function treeObserver(){
// Callback function to execute when mutations are observed
    var callback = async function() {
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
await logClicks();
await logInputs();
await logSelections();
}

main();
treeObserver();
