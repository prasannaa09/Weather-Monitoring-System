document.getElementById("getWeatherBtn").addEventListener("click", function() {
    getWeather();
});

function getWeather() {
    const city = document.getElementById("city").value.trim();
    
    if (!city) {
        alert("Please enter a city name");
        return;
    }

    // Show loading message while fetching weather data
    document.getElementById("weatherResult").innerHTML = "<p class='loading'>Fetching weather data...</p>";

    // Make fetch request to backend
    fetch(`http://localhost:8080/weather/${encodeURIComponent(city)}`)
        .then(response => {
            if (!response.ok) {
                // If response is not OK, throw an error with message from backend
                return response.json().then(err => { 
                    throw new Error(err.message || "Network response was not ok"); 
                });
            }
            return response.json();
        })
        .then(data => {
            // Assuming the backend returns { city, temperature, humidity, windSpeed }
            document.getElementById("weatherResult").innerHTML = `
                <h2>Weather in ${data.city}</h2>
                <p>Temperature: <strong>${data.temperature}°C</strong></p>
                <p>Humidity: <strong>${data.humidity}%</strong></p>
                <p>Wind Speed: <strong>${data.windSpeed} m/s</strong></p>
            `;
        })
        .catch(error => {
            console.error("Error fetching weather data:", error);
            document.getElementById("weatherResult").innerHTML = `<p class='alert'>${error.message}</p>`;
        });
}
