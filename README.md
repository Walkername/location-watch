# What is this about

This development provides a "template" for creating a vehicle rental system with user control via their mobile devices to provide additional flexibility in management and security.

![logo_slogan](https://github.com/user-attachments/assets/778a91b0-e2b2-4bbc-8325-ea2cc327eba7)

# Structure

**Client-server architecture:**

- **A mobile client** for Android written in Kotlin collects GPS data from the phone's sensors and sends it via an MQTT broker to the backend.
- **The backend** is a monolithic solution that provides data processing from the user's mobile client.
The main idea is to check GPS data to see if the user is in any prohibited areas.
Depending on this, it sends a corresponding notification to the user and a message to the administrator interface.
- **The frontend** provides an interface for administrators. There, violators are displayed in real time.
