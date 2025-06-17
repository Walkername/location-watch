# Table of contents

1. [What is this about](#what-is-this-about)
2. [Structure](#structure)
3. [Mobile Client](#mobile-client)  
   3.1. [Authentication](#authentication-screens)  
   3.2. [Home Screen](#home-screen)  
   3.3. [Notifications](#notifications)  
4. [Admin Interface](#admin-interface)  
   4.1. [Login Page](#login-page)  
   4.2. [Zones](#zones)  
   4.3. [Zone Creating](#zone-creating)  
   4.4. [Violators List](#violators-list)  

# What is this about

This development provides a "template" for creating a vehicle rental system with user control via their mobile devices to provide additional flexibility in management and security.
<p align="center">
  <img src="https://github.com/user-attachments/assets/778a91b0-e2b2-4bbc-8325-ea2cc327eba7" alt="logo_slogan" width="550" />
</p>

# Structure

**Client-server architecture:**

- **A mobile client (_Kotlin_)** for Android collects GPS data from the phone's sensors and sends it via an MQTT broker to the backend.
- **The backend (_Java Spring Boot_)** is a monolithic solution that provides data processing from the user's mobile client.
The main idea is to check GPS data to see if the user is in any prohibited areas.
Depending on this, it sends a corresponding notification to the user and a message to the administrator interface.
- **The frontend (_JavaScript, React_)** provides an interface for administrators where violators are displayed in real time.

<p align="center">
  <img src="https://github.com/user-attachments/assets/046f4146-df6b-4711-b303-4fad0c776911" alt="architecture" width="650"/>
</p>

# Backend

The backend is a monolithic **Spring Boot** application that processes data from mobile clients, checks whether users are in specified zones, and sends appropriate notifications.

- Authentication and authorization (JWT + Refresh Token)
- Receiving GPS data via MQTT (Yandex IoT Core)
- Zone checking (PostgreSQL + geodata)
- Real-time Notifications (WebSocket for admin panel, FCM for mobile clients)

<p align="center">
  <img src="https://github.com/user-attachments/assets/6e3d1b65-42e4-45bb-a469-42c2d648a600" alt="architecture" width="1000"/>
</p>

# Mobile Client

**The mobile client** is a two-page application:
- Login and registration pages
- Home page

## Authentication Screens
<p align="center">
  <img src="https://github.com/user-attachments/assets/f1ec89b6-0d0c-42e2-a5b8-13df22e5c23e" alt="login-page" width="200"/>
  <img src="https://github.com/user-attachments/assets/4fdedd02-575c-4a0c-b499-cdfc4f991a23" alt="login-page-error" width="200"/>
  <img src="https://github.com/user-attachments/assets/3e252530-8e0b-440f-a07e-53ffa8bff278" alt="register-page" width="200"/>
</p>

## Home Screen

**The home page** contains a navigation map, which displays all restricted areas. As well as a button, which, when pressed, starts transmitting GPS data to the backend.

By clicking on an area you can view its short description:
- **Area name**
- **Type**: speed/location limit
- **Maximum speed**: 0 for location area
- **Short description**

<p align="center">
  <img src="https://github.com/user-attachments/assets/16f69931-36a0-4af0-9134-52ad26fcc5a8" alt="areas" width="200"/>
  <img src="https://github.com/user-attachments/assets/99f7701e-1c3e-4912-a81b-fcb87821ca9f" alt="zone-information-1" width="200"/>
  <img src="https://github.com/user-attachments/assets/4f37ac5f-dbbc-4ee3-ab94-8fc055cba3be" alt="zone-information-2" width="200"/>
</p>

## Notifications

Also, when a user is in a zone and violates its conditions, he receives a corresponding notification. Notifications are implemented using **Firebase Cloud Messaging**.

<p align="center">
  <img src="https://github.com/user-attachments/assets/7a0b4ea2-747c-4573-a6ed-ae3d6463983d" alt="notification" width="200"/>
</p>

# Admin Interface

**The admin interface** is developed using _JavaScript and React_.
It provides the following basic functionality:
- Displaying all existing zones on an interactive map
- Displaying all information about a zone
- Creating a zone
- Displaying a list of all zones
- Displaying a list of all offending users
- Displaying all offenders on an interactive map

## Login Page

Using this page, you can log in as an administrator, oddly enough, and get into the corresponding interface.

<p align="center">
  <img src="https://github.com/user-attachments/assets/adcd5832-b9af-455c-8034-5f6480c618ec" alt="login-page" width="700"/>
</p>

## Zones

Here you can see that the interface provides the ability to view all zones on an interactive map. When you click on a zone, a pop-up window opens with more detailed information about the zone. Also, all zones are displayed in the list of zones on the right.

![popup-zones](https://github.com/user-attachments/assets/1961e91c-7d91-4e9a-b8a0-c89d244bbbcf)

## Zone Creating

You can create **your own zone** by selecting specific points on the map that connect to each other to create an area.

The **zone** can be set to:
- **Name**.
- **Type**: speed limit or location limit.
- **Maximum speed**: 0 by default for location zones; for speed zones, you can set from 0 to 20. 
- **Points** that store latitude and longitude.

**The Clear button** deletes all markers (points) that were added during the creation of the zone. This is useful in cases where you need to cancel the current selections.

You can also use the **Ctrl+Z** keyboard shortcut to delete the last point you set.

![creating-zone](https://github.com/user-attachments/assets/05ba7e16-78b7-4567-9e7d-fdda2adcc38a)

## Violators List

You can also see the list of violators at the very bottom right. It displays current violators in real time.

And also displays their location on an interactive map.

![popups-users](https://github.com/user-attachments/assets/c9d6f18c-6acd-4d01-a29f-39f49e8a525f)
