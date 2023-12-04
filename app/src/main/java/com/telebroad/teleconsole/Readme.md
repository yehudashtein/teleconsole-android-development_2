# STRUCTURE

This app is mostly structured using the MVC design pattern. If the controller got to out of hand, I also added a view model.

## Pacakges
### chat
I have kept the chat logic separate from the rest of the app, as it could be an app as of itself. 

### controller
Here I have put the controller logic. 

### helpers
This is a bunch of utility classes

### model
The models for MVC. Also contains DB logic, as well as repositories. 

### notification
All classes that have to do with notifications

### pjsip
All SIP related stuff. Everything to do with backend of calling and the libraries

### viewmodels
Here live the viewmodels.

# Assorted Classes
In this package there are also some classes that don't really fit anywhere. Most of it is boilerplate. 

### DialPadView
A custom view for the dialpad.

### MyFileProvider
Boilerplate to allow to share files.

### NetworkChangeCallBack
Boilerplate to force the backend to deregister when the app goes offline



