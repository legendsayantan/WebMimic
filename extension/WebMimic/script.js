var recording = false;
var paused = false;
var Alldata = [];
async function update() {
    if (recording) {
        recordBtn.style.display = 'none';
        uploadBtn.style.display = 'none';
        pauseBtn.style.display = 'block';
        saveBtn.style.display = 'block';
        deleteBtn.style.display = 'block';
        saveform.style.display = 'block';
    } else {
        recordBtn.style.display = 'block';
        uploadBtn.style.display = 'block';
        pauseBtn.style.display = 'none';
        saveBtn.style.display = 'none';
        deleteBtn.style.display = 'none';
        saveform.style.display = 'none';
    }
    document.getElementById('img1').src = paused ? 'images/play.svg' : 'images/pause.svg';
    addpause.style.display = paused ? 'block' : 'none';
    chrome.storage.local.get(['actions']).then((result) => {
        Alldata = JSON.parse(result.actions);
        populateList(Alldata);
    });
}
var recordBtn = document.getElementById('record');
var pauseBtn = document.getElementById('pause');
var addpause = document.getElementById('addpause');
var saveBtn = document.getElementById('save');
var deleteBtn = document.getElementById('delete');
var uploadBtn = document.getElementById('upload');
var saveform = document.getElementById('saveform');
var AutoName = document.getElementById('name');
var AutoDelay = document.getElementById('delay');
chrome.runtime.sendMessage({ message: 'data?' }, function (response) {
    recording = response.recording;
    paused = response.paused;
    update();
});
recordBtn.addEventListener('click', function () {
    recording = true;
    chrome.runtime.sendMessage({ message: 'start_recording' });
    update();
});
pauseBtn.addEventListener('click', function () {
    paused = !paused;
    chrome.runtime.sendMessage({ message: paused ? 'pause_recording' : 'resume_recording' });
    update();
});
addpause.addEventListener('click', function () {
    chrome.runtime.sendMessage({ message: 'pause' }, function (response) {
        if (response.message === 'added_pause') {
            addpause.style.display = 'none';
            showToast('Pause recorded.');
        }
    });
});
saveBtn.addEventListener('click', function () {
    if (AutoName.value && AutoDelay.value) {
        recording = false;
        AutoName.style.border = "2px solid black";
        AutoDelay.style.border = "2px solid black";
        chrome.runtime.sendMessage({ message: 'save', name: AutoName.value, delay: AutoDelay.value }, function (response) {
            if (response.message === 'save_recording') {
                Alldata.push(JSON.parse(response.json));
                chrome.storage.local.set({ 'actions': JSON.stringify(Alldata) }).then((result) => {
                    showToast('Saved!');
                    update();
                });
            }
        });
        update();
    } else {
        if (!AutoName.value) AutoName.style.border = "1px solid red";
        if (!AutoDelay.value) AutoDelay.style.border = "1px solid red";
    }
});
uploadBtn.addEventListener('click', function () {
    let json = prompt('Paste JSON here');
    if (json) {
        try {
            let data = JSON.parse(json);
            Alldata.push(data);
            chrome.storage.local.set({ 'actions': JSON.stringify(Alldata) }).then((result) => {
                showToast('Uploaded!');
                update();
            });
        } catch (e) {
            showToast('Invalid JSON');
        }
    }
});
deleteBtn.addEventListener('click', function () {
    recording = false;
    paused = false;
    chrome.runtime.sendMessage({ message: 'delete' });
    update();
});
function showToast(message) {
    let toast = document.getElementById('toast');
    toast.innerHTML = message;
    toast.style.fontSize = '200%';
    setTimeout(() => {
        toast.innerHTML = 'WebMimic is currently in beta. If you find any bugs, please <a id="issue" href="/">report them</a>.';
        toast.style.fontSize = '100%';
    }, 3000);
}
function copyAction(index) {
    let data = JSON.stringify(Alldata[index]);
    navigator.clipboard.writeText(data).then(function () {
        showToast('Copied!');
        update();
    }, function (err) {
        showToast('Could not copy text.');
        console.error('Async: Could not copy text: ', err);
    });
}
function deleteAction(index) {
    if (index > -1) {
        Alldata.splice(index, 1);
    }
    chrome.storage.local.set({ 'actions': JSON.stringify(Alldata) }).then((result) => {
        showToast('Deleted!');
        update();
    });
}
function populateList(data) {
    let list = document.getElementById('list');
    list.innerHTML = '';
    data.forEach((item) => {
        let li = document.createElement('li');
        let index = Alldata.indexOf(item);
        let html = '<div class="visibleOnHover" style="float: right" >' +
            '<img src="images/copy.svg" style="height: 18px; width:15px; margin: 0px 10px" id="copy_' + index + '"/>' +
            '<img src="images/delete.svg" style="height: 18px; width:15px;" id="delete_' + index + '"/>' +
            '</div>';
        li.innerHTML = item.name + html;
        li.clickable = true;
        li.id = 'item_' + index;
        li.style.justifyContent = 'center';
        li.style.alignItems = 'center';
        list.appendChild(li);
        li.style.cursor = 'pointer';
        document.getElementById('item_' + index).addEventListener('click', function () {
            chrome.runtime.sendMessage({ message: 'play', json: JSON.stringify(item) });
        });
        document.getElementById('copy_' + index).addEventListener('click', function (event) {
            copyAction(index);
            event.stopPropagation();
        });
        document.getElementById('delete_' + index).addEventListener('click', function (event) {
            deleteAction(index);
            event.stopPropagation();
        });
    });
}
