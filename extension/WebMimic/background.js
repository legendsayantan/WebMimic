import JsAction from './JsAction.js';
import AutomationData from './AutomationData.js'
console.log('service worker registered');
var recording = false;
var paused = false;
var pageUrl;
var data;
var positionData = "";
var positionurl = "";

//runner
var runnerData;
var pausedIndex = 0;
var delay = 200;
var run;
chrome.runtime.onMessage.addListener(async function (request, sender, sendResponse) {
    try {
        let action;
        console.log('message received - ' + request.message);
        let splits = [''];
        if (request.message) {
            splits = request.message.split('-->');
        }
        if (splits[0] === 'click' || splits[0] === 'change') {
            if (recording && !paused) {
                if (positionData != "") {
                    action = new JsAction(positionurl, 'scroll', '', positionData);
                    positionData = "";
                    data.jsActions.push(action);
                }
                action = new JsAction(sender.tab.url ? sender.tab.url : "", splits[0], splits[1], splits.length > 2 ? splits[2] : "");
                data.jsActions.push(action);
                pageUrl = sender.tab.url;
            }
        } else if (splits[0] === 'scroll' && recording && !paused) {
            positionData = splits[1];
            positionurl = sender.tab.url;
        } else if (request.message === 'pause') {
            if (recording) {
                sendResponse({ message: 'added_pause' });
                action = new JsAction(pageUrl, 'pause', '', '');
                data.jsActions.push(action);
            }
        }
        else if (request.message === 'start_recording') {
            recordingStart();
            sendResponse({ message: 'recording_started' });
        } else if (request.message === 'stop_recording') {
            recording = false;
            sendResponse({ message: 'recording_stopped' });
        } else if (request.message === 'pause_recording') {
            paused = true;
        } else if (request.message === 'resume_recording') {
            recordingResume();
        } else if (request.message === 'save') {
            recording = false;
            paused = false;
            action = new JsAction(data.jsActions.length > 0 ? '' : pageUrl, 'pause', '', '');
            data.jsActions.push(action);
            console.log('saving --> ' + request.name + ' - ' + request.delay + 'ms ');
            data.name = request.name;
            data.delay = request.delay;
            let data2 = JSON.stringify(AutomationData.optimise(data));
            consoleReturnExecute();
            sendResponse({ message: 'save_recording', json: data2 });
        } else if (request.message === 'data?') {
            sendResponse({ recording: recording, paused: paused });
        } else if (request.message === 'delete') {
            recording = false;
            paused = false;
            data = null;
        } else if (request.message === 'play') {
            runnerData = JSON.parse(request.json);
            console.log(runnerData.name + ' - name');
            await chrome.tabs.create({ url: runnerData.jsActions[0].url }, async (tab) => {
                await chrome.webNavigation.onCompleted.addListener(async function onCompleted(info) {
                    if (info.tabId == tab.id) {
                        await chrome.webNavigation.onCompleted.removeListener(onCompleted);
                        tab.url = info.url;
                        console.log('tab url - ' + tab.url);
                        await executeOn(tab, 'default.js', runnerData, async function () {
                            //onPause

                        }, async function () {
                            //onResume

                        });
                    }
                });

            });
        } else if (request.message === 'pause_run') {
            pauseRun();
        } else if (request.message === 'play_run') {
            await resumeRun(sender.tab.id, async function () {
                await chrome.tabs.query({ active: true, currentWindow: true }, async function (tabs) {
                    await processExecution(tabs[0], runnerData.name, 'default.js', runnerData.jsActions, function () {
                        //onPause

                    }, function () {
                        //onResume

                    });
                });
            });

        }
    } catch (err) {
        console.log(err);
    }
});
async function continueRecording(tabId) {
    chrome.scripting.executeScript({
        target: { tabId: tabId, allFrames: true },
        func: function () {
            let x = confirm('Webmimic has paused recording. Continue recording on this tab?');
            if (x) chrome.runtime.sendMessage({ message: 'resume_recording' });
        },
    }, () => { });
}
async function tabListener(activeTab) {
    console.log('tab changed');
    if (recording && !paused) {
        paused = true;
        if (activeTab.tabId)
            chrome.tabs.get(activeTab.tabId, (tab) => {
                if (!(tab.url.startsWith('chrome') || tab.url == "")) continueRecording(tab.id);
                pageUrl = tab.url;
            });
        else if (!(activeTab.url.startsWith('chrome') || activeTab.url == "")) continueRecording(activeTab.id);
        pageUrl = activeTab.url;
    }
}
chrome.tabs.onActivated.addListener((info) => tabListener(info));
chrome.tabs.onCreated.addListener((info) => tabListener(info));
chrome.webNavigation.onCompleted.addListener(function onComplete(info) {
    if (recording && !paused) {
        recordingResume();
    }
    if (recording && paused) {
        continueRecording(info.tabId);
    }
});

async function recordingStart() {
    recording = true;
    console.log('recording started');
    data = new AutomationData();
    chrome.tabs.query({ active: true, currentWindow: true }, function (arrayOfTabs) {
        trainerExecute(arrayOfTabs[0].id);
    });
}
async function recordingResume() {
    console.log('recording resuming');
    recording = true;
    paused = false;
    chrome.tabs.query({ active: true, currentWindow: true }, function (arrayOfTabs) {
        trainerExecute(arrayOfTabs[0].id);
    });
}
async function recordingReset() {

}
function consoleOverride() {
    var _console = console.log;
    console.log = function (message) {
        chrome.runtime.sendMessage({ message: message });
    }
    window.onscroll = function () {
        chrome.runtime.sendMessage({ message: 'scroll-->' + window.scrollX + '-' + window.scrollY + '-' + window.devicePixelRatio });
    }
}
function consoleReturn() {
    if (_console) console.log = _console;
}
async function consoleReturnExecute() {
    chrome.tabs.query({ active: true }, function (arrayOfTabs) {
        for (var tab in arrayOfTabs) {
            chrome.scripting.executeScript({
                target: { tabId: tab.id },
                func: consoleReturn,
            }, () => { })
        }
    });
}
async function trainerExecute(tab) {
    await chrome.scripting.executeScript({
        target: { tabId: tab },
        func: consoleOverride,
    }, async function () {
        await chrome.scripting.executeScript(
            {
                target: { tabId: tab, allFrames: true },
                files: ['trainer.js'],
            },
            () => { }
        );
    });
}

//------------------------------------------------------------------------------
//Runner below
//------------------------------------------------------------------------------


async function executeOn(tab, runnerCodeFile, automationData, onPause, onResume) {
    pausedIndex = 0;
    delay = automationData.delay;
    var actions = automationData.jsActions;
    await onResume();
    await setupPopup(tab.id, automationData.name, () => { });
    await resumeRun(tab.id, () => { });
    if (true) {
        await chrome.scripting.executeScript({
            target: { tabId: tab.id, allFrames: true },
            files: [runnerCodeFile],
        }, async function () {
            await processExecution(tab, automationData.name, runnerCodeFile, actions, onPause, onResume);
        });
    }
}
//Warning: spaghetti code ahead
async function processExecution(tab, name, runnerCodeFile, actions, onPause, onResume) {
    while (run) {
        console.log('pausedIndex --> ' + pausedIndex);
        if (pausedIndex >= actions.length - 1) {
            await resetExecution();
            await onPause();
            await setupPopup(tab.id, name, async function () {
                await chrome.scripting.executeScript({
                    target: { tabId: tab.id, allFrames: true },
                    func: function () {
                        document.getElementById('webmimic_state').innerText = 'completed.';
                        pausedWebMimic();
                    },
                }, () => { });
            });
            chrome.webNavigation.onCompleted.addListener(async function onComplete(info) {
                await setupPopup(tab.id, name, async function () {
                    await chrome.scripting.executeScript({
                        target: { tabId: tab.id, allFrames: true },
                        func: function () {
                            document.getElementById('webmimic_state').innerText = 'completed.';
                            pausedWebMimic();
                        },
                    }, () => {
                        chrome.webNavigation.onCompleted.removeListener(onComplete);
                     });
                });
            });
            break;
        }
        var action = actions[pausedIndex];
        if (action.url && action.url != "" && tab.url.trim() != action.url.trim()) {
            await pauseRun();
            await chrome.webNavigation.onCompleted.addListener(async function onComplete(info) {
                if (info.tabId == tab.id) {
                    await chrome.webNavigation.onCompleted.removeListener(onComplete);
                    tab.url = info.url;
                    if (pausedIndex < actions.length - 1) {
                        await setupPopup(tab.id, name, async function () {
                            await onResume();
                            await resumeRun(tab.id, async () => {
                                await chrome.scripting.executeScript({
                                    target: { tabId: tab.id, allFrames: true },
                                    files: [runnerCodeFile],
                                }, async function () {
                                    await processExecution(tab, name, runnerCodeFile, actions, onPause, onResume);
                                });
                            });
                        });
                    }
                }

            });
            console.log('changing url to --> ' + action.url + ' taburl: ' + tab.url + ' pausedIndex: ' + pausedIndex + ' actionurl: ' + action.url);
            await chrome.tabs.update(tab.id, { url: action.url });
        } else {
            if (action.actionType == 'scroll') {
                let splits = action.value.split('-');
                await chrome.tabs.setZoom(tab.id, parseFloat(splits[2]), async function () {
                    await chrome.scripting.executeScript({
                        target: { tabId: tab.id, allFrames: true },
                        args: [splits],
                        func: function (args) {
                            window.scrollTo(args.splits[0], args.splits[1]);
                        },
                    }, () => { });
                });
            } else if (action.actionType == 'pause') {
                await pauseRun();
                await onPause();
                await chrome.scripting.executeScript({
                    target: { tabId: tab.id, allFrames: true },
                    func: function () {
                        document.getElementById('webmimic_state').innerText = 'paused execution.';
                        pausedWebMimic();
                    },
                }, () => { });
            } else await chrome.scripting.executeScript({
                target: { tabId: tab.id, allFrames: true },
                args: [{ action: action }],
                func: function (args) {
                    console.log(args.action.actionType + ' ' + args.action.element + ' ' + args.action.value);
                    switch (args.action.actionType) {
                        case 'click':
                            document.getElementById(args.action.element).click();
                            break;
                        case 'change':
                            document.getElementById(args.action.element).value = args.action.value;
                            break;
                        case 'pause':
                            console.log('Paused Execution');
                            break;
                    }
                },
            }, () => { });
            pausedIndex++;
        }
        await new Promise(r => setTimeout(r, delay));
    }
}
async function resetExecution() {
    pausedIndex = 0;
    pauseRun();
}

async function pauseRun() {
    console.log('paused execution');
    run = false;
}
async function resumeRun(tabId, after) {
    console.log('resumed execution');
    run = true;
    chrome.scripting.executeScript({
        target: { tabId: tabId, allFrames: true },
        func: function () {
            document.getElementById('webmimic_state').innerText = 'running on this tab';
        },
    }, after);
}
async function setupPopup(tabId, name, after) {
    console.log('setting up popup - ' + tabId + ' - ' + name);
    await fetch('popup.html').then(async (response) => response.text())
        .then(async (text) => {
            await chrome.scripting.executeScript({
                target: { tabId: tabId },
                args: [{ html: text, name: name }],
                func: function (args) {
                    if (!document.getElementById('webmimic_view')) {
                        let element = document.createElement('div');
                        element.innerHTML = args.html;
                        element.id = 'webmimic_view';
                        document.body.prepend(element);
                    }
                    if (args.name != undefined) document.getElementById('webmimic_name').innerText = args.name;
                },
            }, async () => {
                if (true) {
                    await chrome.scripting.executeScript({
                        target: { tabId: tabId },
                        files: ['popup.js'],
                    }, after)
                }
            });
        });

}