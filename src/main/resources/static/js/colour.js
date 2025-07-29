const sliderIds = ["hueMin", "satMin", "valMin", "hueMax", "satMax", "valMax"];

document.addEventListener("DOMContentLoaded", () => {
    initSliders();
    initSubmitButton();
    updateImages().catch(console.error);
});

function initSliders() {
    const debouncedSend = debounce(sendSliderVal, 150);
    sliderIds.forEach(id =>
        document.getElementById(id)?.addEventListener("input", () => debouncedSend(id))
    );
}

function initSubmitButton() {
    const button = document.getElementById("submitButton");
    if (!button) return;

    button.addEventListener("click", async () => {
        const input = document.querySelector("input[type='text']");
        const colorName = input?.value.trim();

        if (!colorName) {
            alert("Please enter a name.");
            return;
        }

        if (/\s/.test(colorName)) {
            alert("Name contains spaces.");
            return;
        }

        try {
            const res = await fetch("/api/submitColour", {
                method: "POST",
                headers: { "Content-type": "text/plain" },
                body: colorName
            });

            if (!res.ok) throw new Error(`Server error: ${res.status}`);
            alert("Colour has been saved successfully.");
        } catch (err) {
            console.error("Submit error:", err);
            alert("Failed to save colour.");
        }
    });
}

async function sendSliderVal(sliderName) {
    const slider = document.getElementById(sliderName);
    if (!slider) return;

    const value = slider.value;
    console.log(value);

    try {
        const response = await fetch("/api/slider", {
            method: "POST",
            headers: { "Content-type": "application/json" },
            body: JSON.stringify({ sliderName, sliderValue: value })
        });

        if (!response.ok) throw new Error(`Slider update failed: ${response.status}`);
        await updateImages();
        console.log("Images updated.");
    } catch (err) {
        console.error("Error updating slider/image:", err);
    }
}

async function updateImages() {
    const timestamp = Date.now();
    const original = document.getElementById("originalImage");
    const modified = document.getElementById("modifiedImage");

    if (original) original.src = `/api/originalImage?t=${timestamp}`;
    if (modified) modified.src = `/api/modifiedImage?t=${timestamp}`;
}

function debounce(func, wait) {
    let timeout;
    return function (...args) {
        clearTimeout(timeout);
        timeout = setTimeout(() => func.apply(this, args), wait);
    };
}
