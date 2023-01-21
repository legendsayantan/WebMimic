var pause = document.getElementById("pauseWebMimic");
var play = document.getElementById("playWebMimic");
play.style.display = "none";
pause.addEventListener("click", function () {
    chrome.runtime.sendMessage({ message: "pause_run" });
    pausedWebMimic();
});
play.addEventListener("click", function () {
    chrome.runtime.sendMessage({ message: "play_run" });
    playedWebMimic();
});
var stop = document.getElementById("stopWebMimic");
stop.addEventListener("click", function () {
    document.getElementsByTagName('body')[0].removeChild(document.getElementById("webmimic_view"));
    chrome.runtime.sendMessage({ message: "stop_run" });
});
function pausedWebMimic() {
    pause.style.display = "none";
    play.style.display = "block";
}
function playedWebMimic() {
    pause.style.display = "block";
    play.style.display = "none";
}