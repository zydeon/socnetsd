SocNet
=============

A social network application with integration with Facebook __REST__ API and updates in real-time for users social activity, by making use of __WebSockets__.

## Features

* Register user
* Login user
* Create a new post (with or without Images)
* Edit a previous post
* Delete a previous post
* Reply to any post
* Send Direct Messages to other users
* See online users (automatically updated)
* Join chatrooms
* When a post is created, it's created on Facebook.
* When a post is deleted, it's deleted on Facebook.
* When a reply is made, it's added on Facebook.

## Overall architecture

The application uses 2 servers and 3 types of client: TCP, RMI (Remote Method Invocation, and HTTP. See the diagram below.

![](/screenshots/architecture.png)

## Pre-requisites

* JDBC driver
* Apache Tomcat
* WebBrowser with support to websockets
