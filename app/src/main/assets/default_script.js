var page = document.childNodes;
function initViews(page,parentId) {
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
initViews(page,"root");