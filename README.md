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


<img src="https://github.com/user-attachments/assets/6d634dcc-f33b-46ca-b9fb-db858c3c81c0" alt="login-page" width="650"/>

# Mobile Client

**The mobile client** is a two-page application:
- Login and registration pages
- Home page

## Authentication Screens

<img src="https://github.com/user-attachments/assets/f1ec89b6-0d0c-42e2-a5b8-13df22e5c23e" alt="login-page" width="200"/>
<img src="https://github.com/user-attachments/assets/4fdedd02-575c-4a0c-b499-cdfc4f991a23" alt="login-page-error" width="200"/>
<img src="https://github.com/user-attachments/assets/3e252530-8e0b-440f-a07e-53ffa8bff278" alt="register-page" width="200"/>

## Home Screen

**The home page** contains a navigation map, which displays all restricted areas. As well as a button, which, when pressed, starts transmitting GPS data to the backend.

By clicking on an area you can view its short description:
- Area name
- Type: speed/location limit
- Maximum speed: 0 for location area
- Short description

<img src="https://github.com/user-attachments/assets/16f69931-36a0-4af0-9134-52ad26fcc5a8" alt="areas" width="200"/>
<img src="https://github.com/user-attachments/assets/99f7701e-1c3e-4912-a81b-fcb87821ca9f" alt="zone-information" width="200"/>
<img src="https://github.com/user-attachments/assets/4f37ac5f-dbbc-4ee3-ab94-8fc055cba3be" alt="register-page" width="200"/>

## Notifications

Also, when a user is in a zone and violates its conditions, he receives a corresponding notification. Notifications are implemented using **Firebase Cloud Messaging**.

<img src="https://github.com/user-attachments/assets/7a0b4ea2-747c-4573-a6ed-ae3d6463983d" alt="register-page" width="200"/>



