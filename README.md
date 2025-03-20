# Sijoittelu

## Testien ajaminen

Projektissa käytetään tällä hetkellä Javan versiota 21. Riippuvuuksina käytetään kuitenkin kirjastoja jotka eivät ole Java-21 -yhteensopivia, koska ne käsittelevät reflektiolla JDK:n luokkia. Testien
ajamiseksi (ja sovelluksen käynnistämiseksi) JVM:lle pitää antaa seuraavat parametrit:

`--add-opens=java.base/java.lang=ALL-UNNAMED --add-opens java.base/java.math=ALL-UNNAMED --add-opens java.base/java.util=ALL-UNNAMED`

Mavenin osalta kyseiset parametrit on jo lisätty, mutta kun koodia ajetaan IDE:n kautta, niin kyseiset parametrit on lisättävä esim.
IntelliJ:n Run Configuraatioihin (VM-parametreina).