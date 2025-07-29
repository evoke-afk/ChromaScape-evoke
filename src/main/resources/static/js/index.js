let selectedWindowMode = null;
let selectedScriptName = null;
let previouslySelectedElement = null;
let isStarted = false;

const INTERVAL_LOGS = 600;
const INTERVAL_PROGRESS = 5000;
const INTERVAL_STATUS = 500;

document.addEventListener("DOMContentLoaded", () => {
    (async () => {
        try {
            await initializeUI();
        } catch (err) {
            console.error("UI initialization failed:", err);
        }
    })();
});

async function initializeUI() {
    try {
        await fetchAndRenderScripts();
    } catch (error) {
        console.error("Failed to initialize scripts:", error);
    }

    setInterval(fetchLogs, INTERVAL_LOGS);
    setInterval(fetchProgress, INTERVAL_PROGRESS);
    setInterval(syncRunningStatus, INTERVAL_STATUS);

    setupWindowModeDropdown();
    setupStartStopButton();
}


// ----------------- SCRIPT FETCH + UI -----------------

async function fetchAndRenderScripts() {
    try {
        const response = await fetch("/api/scripts");
        if (!response.ok) throw new Error(`HTTP ${response.status}`);
        const scripts = await response.json();
        renderScriptList(scripts);
    } catch (err) {
        console.error("Error fetching scripts:", err);
    }
}

function renderScriptList(scripts) {
    const listGroup = document.getElementById("script-list");
    if (!listGroup) return;

    scripts.forEach(script => {
        if (script === "package-info.java") return;

        const listItem = document.createElement("li");
        listItem.className = "list-group-item d-flex justify-content-between align-items-start list-group-item-action bg-med";
        listItem.style.cursor = "pointer";

        const title = document.createElement("div");
        title.className = "fw-bold p-2 text-white";
        title.textContent = script;

        listItem.appendChild(title);
        listGroup.appendChild(listItem);

        listItem.addEventListener("click", () => {
            if (previouslySelectedElement) {
                previouslySelectedElement.classList.remove("bg-light");
                previouslySelectedElement.classList.add("bg-med");
            }

            listItem.classList.remove("bg-med");
            listItem.classList.add("bg-light");

            selectedScriptName = script;
            previouslySelectedElement = listItem;
        });
    });
}

// ----------------- WINDOW MODE -----------------

function setupWindowModeDropdown() {
    document.querySelectorAll(".dropdown-item").forEach(item => {
        item.addEventListener("click", e => {
            e.preventDefault();
            const dropdown = document.getElementById("windowModeDropdown");
            dropdown.textContent = item.textContent;
            selectedWindowMode = item.getAttribute("data-value");
        });
    });
}

// ----------------- LOGS -----------------

async function fetchLogs() {
    try {
        const response = await fetch("/api/logs");
        if (!response.ok) throw new Error(`HTTP ${response.status}`);
        const logs = await response.json();

        const terminal = document.getElementById("consoleOutput");
        if (!terminal) return;

        const scrollBefore = terminal.scrollTop;
        const scrollHeightBefore = terminal.scrollHeight;

        terminal.innerHTML = "";
        logs.forEach(line => {
            const logEl = document.createElement("a");
            logEl.textContent = line;
            terminal.appendChild(logEl);
            terminal.appendChild(document.createElement("br"));
        });

        const nearBottom = (scrollHeightBefore - terminal.clientHeight - scrollBefore) <= 5;
        terminal.scrollTop = nearBottom ? terminal.scrollHeight - terminal.clientHeight : scrollBefore;

    } catch (err) {
        console.error("Error fetching logs:", err);
    }
}

// ----------------- PROGRESS -----------------

async function fetchProgress() {
    try {
        const response = await fetch("/api/progress");
        if (!response.ok) throw new Error(`HTTP ${response.status}`);
        const progress = await response.json();

        const bar = document.getElementById("progressBar");
        if (bar) {
            bar.textContent = `${progress}%`;
            bar.style.width = `${progress}%`;
        }
    } catch (err) {
        console.error("Error fetching progress:", err);
    }
}

// ----------------- START/STOP -----------------

function setupStartStopButton() {
    const startBtn = document.getElementById("startButton");
    if (!startBtn) return;

    startBtn.addEventListener("click", async () => {
        const runConfig = getRunConfig();
        if (!runConfig) return;

        try {
            console.log("Button clicked. isStarted before toggle:", isStarted);
            if (!isStarted) {
                await startScript(runConfig);
                console.log("Script started. isStarted now:", isStarted);
            } else {
                await stopScript();
                console.log("Script stopped. isStarted now:", isStarted);
            }
            updateStartButtonUI();
        } catch (err) {
            console.error("Error toggling script:", err);
        }
    });
}

function getRunConfig() {
    const mode = document.getElementById("windowModeDropdown")?.textContent;
    const duration = parseInt(document.getElementById("durationInput")?.value);

    if (!selectedScriptName || isNaN(duration)) {
        alert("Please select a script and a valid duration.");
        return null;
    }

    let fixed;
    if (mode === "Fixed") {
        fixed = true;
    } else if (mode === "Resizable") {
        fixed = false;
    } else {
        alert("Please choose a window mode.");
        return null;
    }

    return {
        script: selectedScriptName,
        duration,
        fixed
    };
}

async function startScript(config) {
    const res = await fetch("/api/runConfig", {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify(config)
    });
    if (!res.ok) throw new Error("Failed to start script");
    isStarted = true;

    setTimeout(() => {
        syncRunningStatus();
    }, 2000);
}

async function stopScript() {
    const res = await fetch("/api/stop", {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: "{}"
    });
    if (!res.ok) throw new Error("Failed to stop script");
    isStarted = false;
}

function updateStartButtonUI() {
    const btn = document.getElementById("startButton");
    if (!btn) {
        console.log("Start button not found!");
        return;
    }

    console.log("Updating button UI. isStarted =", isStarted);
    if (isStarted) {
        btn.className = "btn btn-danger m-2";
        btn.textContent = "Stop";
    } else {
        btn.className = "btn btn-success m-2";
        btn.textContent = "Start";
    }
}


// ----------------- BACKEND STATE SYNC -----------------

async function syncRunningStatus() {
    try {
        const response = await fetch("/api/isRunning");
        if (!response.ok) throw new Error(`HTTP ${response.status}`);
        const isRunning = await response.json();

        const wasStarted = isStarted;
        isStarted = isRunning;

        if (isStarted !== wasStarted) {
            updateStartButtonUI();
        }
    } catch (err) {
        console.error("Error syncing script state:", err);
    }
}

