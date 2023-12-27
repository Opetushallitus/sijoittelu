## Sijoittelu-service

Sijoittelu-service on sijoittelu-palvelun osa joka sisältää varsinaisen sijoittelurajapinnan.

### Ajaminen lokaalisti testiympäristön palveluita vasten

Joissakin tapauksissa on tarpeellista ajaa ympäristöä lokaalisti jonkin jaetun kehitysympäristön datalla, esim. virheiden
selvittämistä varten. Tämä onnistuu seuraavilla ohjeilla.

1. Kehitysympäristössä käytetään aina paikallista sijoittelutietokantaa (jotta skeemamuutoksia voi kehittää lokaalisti).
Luo PostgreSQL kontti seuraavilla komennoilla (tarvitsee tehdä vain kerran):

    ``` shell
    cd sijoittelu-service/postgresql/docker
    docker build --tag sijoittelu-postgres .
    docker create --name sijoittelu-postgres --env POSTGRES_PASSWORD=postgres -p 5433:5432 sijoittelu-postgres
    ```

2. Kopioi konfiguraatio-template lokaalia kehitystä varten ```'/src/test/resources/application-dev.properties.template'``` -> ```'/src/test/resources/application-dev.properties'```.
Application-dev.properties on ignoroitu Gitissä etteivät salasanat valu repoon. Tähän tiedostoon täytyy täyttää tarvittavat testi-ympäristön
salasanat.
  
  
3. Koska käytetään testiympäristön valintalaskennan postgres-kantaa, tarvitaan ssh-porttiohjaus, esim. untuva:

    `ssh -N -L 25446:valintalaskenta.db.untuvaopintopolku.fi:5432 <ssh-tunnus>@bastion.untuvaopintopolku.fi`
  
  
4. Lisää tarvittavat JVM-parametrit, mene Run -> Edit Configurations -> Valitse DevApp.java -> Modify Options -> Add VM Options
Ja lisää:

    `--add-opens=java.base/java.lang=ALL-UNNAMED --add-opens java.base/java.math=ALL-UNNAMED --add-opens java.base/java.util=ALL-UNNAMED`
  
  
5. Käynnistä Ideassa ```DevApp.java``` (right-click -> Run), ja avaa selaimessa allaoleva osoite (oid vaihtelee sen mukaan mikä sijoittelu halutaan ajaa).
Sijoittelu käynnistyy autentikoinnin jälkeen.

    `https://localhost:8443/sijoittelu-service/resources/sijoittele/1.2.246.562.29.00000000000000021303`

### Swagger endpoint

Swagger löytyy osoitteesta [https://localhost:8443/sijoittelu-service/swagger-ui/index.html](https://localhost:8443/sijoittelu-service/swagger-ui/index.html).

