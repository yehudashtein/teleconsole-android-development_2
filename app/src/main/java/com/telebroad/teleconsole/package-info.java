
/**
The top-level package for the app

 <h3>BUILD INSTRUCTIONS</h3>
 <b>
 <a href=https://drive.google.com/drive/u/0/folders/0B9sSOAqJNv0hVTBodWg5bC1lSU0?resourcekey=0-IqigPMo-U1x32Gbzt_UF1A>Get the keys</a>
 Ask josh/shea for access. in the folder you will see a doc with the passwords and aliases. In the build.gradle
 file I have commented out the signingConfig settings, because they need to have the .jks file on the
 build computer. You can save the jks file on your computer and update the build.config it as needed, or you can just
 use the wizard. If you choose NOT to update the signingConfig settings, Sign in with Microsoft will
 not work in debug builds.
 </b>

 <br><br>
This app is mostly structured using the MVC design pattern. If the controller got to out of hand,
 I also added a view model.
<br>
 <br>
 First let's give an overview of the app. As of now the app has 3 parts. 1. VOIP/SIP phone service
 2. Telebroad APIs for content 3. Telebroad Chat (this is still in progress and not yet finished)
 The VoIP service is a foreground service and uses the PJSIP library. The service does not usually
 run, except when there is a phone call in progress. More about this later.
 <br>
 <br>
 Telebroad APIs, these provide users with Call Logs, SMS messages, Contacts, Voicemails, and Fax
 messages. These communicate with the Telebroad webserv server via REST.
 <br><br>
 Chat uses a separate server and communicates with it via websocket.
 <br><br>
 Let's explore a bit more the VoIP service. The strategy used is the Push Notification Strategy.
 Regular SIP works as follows, a client opens a connection, and sends ReRegisters as long as it wants
 to receive messages, however this is not a good idea for mobile devices as they have limited battery
 life and keeping the client alive kills the battery. Instead what is done is that the app creates a
 SIP client when is starts up, sends the server a message that it can receive calls, then after a few
 seconds the client shuts down. After this, when a call is received, the server sends a push notification
 to the app, this notification wakes up the app, the app then restarts the client, &#38; sends a register
 through a process known as late-forking, the server sends the call to the newly created client. the
 client stays awake as long as the call is ongoing. After the call is done, the client checks for other
 calls, if there are none, the client waits a few seconds and then shuts down. {@link com.telebroad.teleconsole.pjsip.SipService SipService}
 is the client class, in it's onCreate method the SIP client gets initialized.
 <br><br>
 Regarding the Telebroad APIs, they are fetched from the server using REST, then saved in a <a
 href="https://developer.android.com/training/data-storage/room">Room Database</a> if the user has
 no internet, then the messages are retrieved from the database.

 <br><br>
 Let's go through some flows just to get an idea of how the app works, when the app starts it will
 fetch all the user data from the Telebroad API server, this includes the SIP line the user wants to
 use for the app. After fetching this, the app calls {@link com.telebroad.teleconsole.pjsip.PJSIPManager#updateUser(boolean) PJSIPManager.updateUser(boolean)}
 this method is used to register users, or to update their registration details (hence the name update user)
 Since any SIP related messages must be sent using the special PJSIP thread, all this method does is
 send a message to {@link com.telebroad.teleconsole.pjsip.SipService SipService} that the user wants to register.
 In turn, SipService calls {@link com.telebroad.teleconsole.pjsip.BackgroundService#enqueueJob(java.lang.Runnable, java.lang.String)
enqueueJob(Runnable, String)} which then calls {@link com.telebroad.teleconsole.pjsip.SipService#updateUser(boolean) SipService.updateUser(boolean)}
 on the special PJSIP thread. (all method calls to any pjsip libraries, must be called using enqueueJob)
 In turn SipService.updateUser calls {@link com.telebroad.teleconsole.pjsip.TelebroadSipAccount#register() TelebroadSipAccount.register}
 which then registers the user.

 <br><br>

 Let's go to another flow, let's say a call comes in, and the app is asleep. Through the power of
 Firebase Cloud Messaging,
 <br><br>
 Finally let's go through the classes in this package
 1-2. BuildConfig and DataBindingTriggerClass, these are generated at build time by the IDE. <br>
 3. DialPadView this class is a custom view for a dialpad this class also uses the interface DialpadView.CallAction <br>
 4-6. Boilerplate classes for various functions.

* */
package com.telebroad.teleconsole;

