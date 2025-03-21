const { contextBridge } = require("electron");

contextBridge.exposeInMainWorld("api", {
    getApiUrl: () => process.env.API_URL || "http://localhost:8080",

    fetchUsers: async () => {
        try {
            // Make an API request to fetch users from the backend
            const response = await fetch(`${process.env.API_URL}/api/users`);

            // Check if the response is OK (status 200-299)
            if (!response.ok) throw new Error("Network error");

            // Parse the response as JSON and return the list of users
            return await response.json();
        } catch (error) {
            // Log and handle any errors during the API request
            console.error("Error fetching users:", error);
            return [];
        }
    }
});