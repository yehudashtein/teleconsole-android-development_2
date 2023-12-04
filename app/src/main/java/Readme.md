#Packages

Here I will explain what th purpose of the packages are

##com.telebroad.teleconsole.*

This is app code. See the Javadocs for this.

##org.pjsip.pjsua2.* 

This is the Sip Library. When you update the library you will need to replace the files in this package.

PJCallID.java is a special class, which allows us to change the callID for the register. Future builds of the PJSIP library can probably get rid of this class, as pjsip added the functionality to its main class. Just make sure to use the new functionality.

##crl.android.pdfwriter

Is a library that allows us to write PDF documents easily. This is used for fax. If you want you can change the library and remove the code. 