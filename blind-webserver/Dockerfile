FROM bram2473/haystack-nvidia:0.0.1

ADD jvm.policy jvm.policy 
ADD blind-webserver-0.0.1.jar blind-webserver-0.0.1.jar
ADD encapsulate.jar encapsulate.jar

ENTRYPOINT java -jar blind-webserver-0.0.1.jar
