function formatTime(date) {
    if (!(date instanceof Date)) date = new Date(date);
    return date.getHours().toString().padStart(2, '0') + ':' +
        date.getMinutes().toString().padStart(2, '0');
}

function showAlert(msg, type = 'info') {
    alert(msg); // Replace with custom UI if needed
}

// Export if using modules
// export { formatTime, showAlert };
