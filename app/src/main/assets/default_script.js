/*
@author legendsayantan
*/

var page = document.childNodes;
async function initViews(page,parentId) {
    var x = 0;
    for (var i = 0; i < page.length; i++) {
        var view = page[i];
        if (view.id == null || view.id == "") {
            view.id = parentId + "_" + x;
            x++;
        }
        if (view.hasChildNodes()) {
            initViews(view.childNodes,view.id);
        }
    }
}
async function treeObserver(){
// Callback function to execute when mutations are observed
    var callback = async function(mutationsList) {
        await initViews(document.childNodes,"root");
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
initViews(page,"root");
treeObserver();