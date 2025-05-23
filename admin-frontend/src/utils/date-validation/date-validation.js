
export default function validateDate(date) {
    const dateObj = new Date(date);

    // Extract day, month, and year
    const day = String(dateObj.getDate()).padStart(2, '0'); // Ensure 2 digits
    const month = String(dateObj.getMonth() + 1).padStart(2, '0'); // Months are zero-based
    const year = dateObj.getFullYear();

    // Extract hours and minutes
    const hours = String(dateObj.getHours()).padStart(2, '0');
    const minutes = String(dateObj.getMinutes()).padStart(2, '0');
    const seconds = String(dateObj.getSeconds()).padStart(2, '0');

    // Construct the formatted date and time string
    const formattedDate = `${day}.${month}.${year}, ${hours}:${minutes}:${seconds}`;

    return formattedDate;
}