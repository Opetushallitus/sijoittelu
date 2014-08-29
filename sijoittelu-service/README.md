# Sijoittelu-service

Start service with

`mvn jetty:run`

If you want to use local embedded database with data imported to fixtures, then

`mvn jetty:run -Dspring.profiles.active=it`

Jetty will be running on port 8180. Now you can try the following:

`curl -v http://ophadmin:ilonkautta@localhost:8180/resources/sijoittelu/1.2.246.562.5.2013080813081926341928/sijoitteluajo/latest/hakukohde/1.2.246.562.5.72607738902`
