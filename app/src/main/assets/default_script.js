/*
@author legendsayantan
*/
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
initViews(document.childNodes,"root");
treeObserver();