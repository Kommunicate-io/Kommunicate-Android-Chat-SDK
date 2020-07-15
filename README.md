<img align="center" src="https://raw.githubusercontent.com/Kommunicate-io/Kommunicate-Android-Chat-SDK/master/img/Header.png" width="900" />

## [Kommunicate](https://www.kommunicate.io/?utm_source=github&utm_medium=readme&utm_campaign=android) Android Chat SDK for Customer Support

An Open Source Android Live Chat SDK for Customer Support

## Overview

Kommunicate provides open source live chat SDK in android. The Kommunicate SDK is flexible, lightweight and easily integrable. It lets you easily add real-time live chat and in-app messaging in your mobile applications and websites for customer support. The SDK is equipped with advance messaging options such as sending attachments, sharing location and rich messaging. 

<img align="center" src="https://raw.githubusercontent.com/Kommunicate-io/Kommunicate-Android-Chat-SDK/master/img/Support.gif" height="520" />

Kommunicate SDK lets you integrate custom chatbots in your mobile apps for automating tasks. It comes with multiple features to make it a full-fledged customer support SDK. 

<img align="center" src="https://raw.githubusercontent.com/Kommunicate-io/Kommunicate-Android-Chat-SDK/master/img/Bot.gif" height="520" />

## Get Started

To get started with Kommunicate Android SDK, head over to the Kommunicate website and [Signup](https://dashboard.kommunicate.io/signup?utm_source=github&utm_medium=readme&utm_campaign=android) to get your Application ID.

This is a sample that implements the Kommunicate android chat SDK. To use this sample you need to provide your application ID in app level build.gradle file's defaultConfig [Refer here](https://github.com/Kommunicate-io/Kommunicate-Android-Chat-SDK/blob/f78948edd81124847d1b6ee2179eadd968ec57b1/app/build.gradle#L13). Replace <Your-APP_ID> with the application ID obtained from Kommunicate dashboard.

## Build a BOT on Kommunicate and integrate it in your Android app

### Kompose

[Kompose](https://dashboard.kommunicate.io/bots/bot-builder) is a Kommunicate’s bot builder that help you in building your own bot, a techie, non-techie, or a person who doesn’t have any idea about chatbots can also build the bot. Anyone can create a chatbot with the Kompose without any assistance.

![Kompose-build](https://user-images.githubusercontent.com/38066371/87525821-a519f000-c6a7-11ea-90d6-97e8fab4d1b3.gif)



### Following is the UI to create the BOT

![Kompose](https://user-images.githubusercontent.com/38066371/87527229-71d86080-c6a9-11ea-8a2c-467c99badac9.jpeg)



## Dialogflow chatbot integration in your Android app

Dialogflow is a Google-owned NLP platform to facilitate human-computer interactions such as chatbots, voice bots, etc. 

Kommunicate's Dialogflow integration provides a more versatile, customizable and better chatting experience. Kommunicate Android Live Chat SDK supports all of Dialogflow's features such as Google Assistant, Rich Messaging, etc. On top of that, it is equipped with advanced features such as bot-human handoff, conversation managing dashboard, reporting, and others. 

You can connect your Dialogflow chatbot with Kommunicate in the following 4 simple steps. [Here](https://www.kommunicate.io/blog/build-chatbot-with-dialogflow-android-sdk/) is a step by step blog to add Kommunicate SDK in your Android app. 

### Step 1: Get your API credentials from Dialogflow
- Login to Dialogflow console and select your agent from the dropdown in the left panel.
- Click on the settings button. It will open a setting page for the agent.
- Inside the general tab search for GOOGLE PROJECTS and click on your service account.
- After getting redirected to your SERVICE ACCOUNT, create a key in JSON format for your project from the actions section and it will get automatically downloaded.

### Step 2: Create a free Kommunicate account
Create a free account on [Kommunicate](https://dashboard.kommunicate.io/signup) and navigate to the [Bots section](https://dashboard.kommunicate.io/bots/bot-integrations).

### Step 3: Integrate your Dialogflow chatbot with Kommunicate
- In the Bot integrations section, choose Dialogflow. A popup window will open.
- Upload your Key file here and proceed.
- Give a name and image to your chatbot. It will be visible to the users chatting with your chatbot.
- Enable/Disable chatbot to human handoff. If enabled, it will automatically assign conversations to humans in case the chatbot is not able to answer.

### Step 4: Install the Kommunicate Android SDK to your app
You can add the Kommunicate SDK in your Android app easily. More information on how to integrate with your Andriod app [here](https://docs.kommunicate.io/docs/android-installation.html). 

> Note: Here's a [sample chatbot](https://docs.kommunicate.io/docs/bot-samples) for you to get started with Dialogflow. 


## Other Features

**Live chat widget:**  Make it easier for your visitors and users to reach you with an instant website and in-app support through chat. The widget is highly customizable. 

**Chatbots:** Automate and speed up your customer service by integrating AI-powered chatbots. Build your chatbots and deploy them using Kommunicate and seamlessly add them in the live chat.

**Conversations:** Manage all your customer queries coming from the live chat plugin. Easily manage and assign agents to cater to user conversations.

**Dashboard:** A powerful dashboard to see, analyze and act upon your customer conversation data. Helps you analyze the performance of support agents as well.

**Helpcenter:** Create your knowledge base and deploy on a dedicated page to cater to generic and recurring customer queries. Your customers will also be able to directly access FAQs in chat.

**Mailbox:** A simple and powerful team inbox for ticketing, managing, receiving and replying to all your customer support emails. 

**Integrations:** Easily move data between Kommunicate and your other favorite apps. Integrate your favorite CRM, knowledge base software and other apps.

**Conversation Routing:** Select routing rules for incoming conversations for both your agents and bots. Choose between automatic assignments or to notify all.

**Smart Rich Messaging:** Leverage rich messages using buttons, cards, carousels, forms or lists to provide an exquisite support chat experience to your customers.

**Quick Replies:** Quickly respond to generic user queries using Quick Replies. Easily create and manage templated messages from your dashboard.


## Technical Documentation

Detailed instructions for installing, configuring and customizing the Kommunicate Android SDK are [availble here](https://docs.kommunicate.io/docs/android-installation.html).

