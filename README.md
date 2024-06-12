# Blind-webserver
A webserver which allows two parties to run private code on secret data in a trust-less setup.
### General concept
Two parties are involved. Lets call them the **code provider** and the **data provider**.
The **code provider** has closed source private code they do now wish to share. The **data provider** has private data they do not wish to share, but are willing to run against the closed source private code of the **code provider**. This webserver provides a multi step process to make this possible.
- Step 1 : The **data provider** starts a cloud based docker image provided by the **code provider**. It contains this webserver as well as the ciphered version of their jar file.
- Step 2 : The **data provider** connects to the webserver UI and inputs their data. The webserver is open source with tiny codebase so that the **data provider** can ensure their data does not leak anywhere.
- Step 3 : The **code provider** can now also connect to the UI and provide the decipher key for their binary, allowing the ciphered binary to start using the data provided by the **data provider**. The key point here is that this jar is started using the provided JVM security policy (see the **jvm.policy** file) which prevents any data-extracting operation which could be performed by the ciphered jar.
### How to build and run the docker image
- docker build blind-webserver -t <dockerhub_username>/<image_name>:<image_version>
- docker run -p 8080:8080 <dockerhub_username>/<image_name>:<image_version>
### How to build the ciphered binary
- Create a standalone jar file and cipher it (using **AES/ECB/PKCS5Padding** for instance, but any cipher would work). The cipher key will be required in the UI to start execution.
- Encapsulate the jar in another jar which will be run as **java -jar encapsulate.jar**. This second jar will read the cipher key on stdin, decipher the jar and pass the **data provider** inputs through stdin.
- Code will be provided to guide **code providers** in this process in a future release.*emphasized text*