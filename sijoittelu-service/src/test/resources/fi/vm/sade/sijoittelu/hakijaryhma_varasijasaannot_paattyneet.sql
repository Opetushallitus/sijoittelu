INSERT INTO valinnanvaihe(
    id, jarjestysnumero, haku_oid, hakukohde_oid, valinnanvaihe_oid, tarjoaja_oid, nimi)
VALUES ('68401760-bd8d-4e79-bae5-67d3c341b94a', 1, '1.2.246.562.29.173465377510', '1.2.246.562.20.18895322503', '1410634881770577442307879925613', '1.2.246.562.10.28342991297', 'Varsinainen valinta');

INSERT INTO valintatapajono(
    id, valintatapajono_oid, nimi, prioriteetti, aloituspaikat, siirretaan_sijoitteluun, tasasijasaanto, ei_varasijatayttoa, valmis_sijoiteltavaksi, valinnanvaihe)
VALUES ('83c1c848-da64-435f-be41-23a29f4d8d97', '1410634972926-6437870106021103090', 'valintatapajono1', 0, 1, true, 'YLITAYTTO', false, true, '68401760-bd8d-4e79-bae5-67d3c341b94a');

INSERT INTO valintatapajono(
    id, valintatapajono_oid, nimi, prioriteetti, aloituspaikat, siirretaan_sijoitteluun, tasasijasaanto, ei_varasijatayttoa, valmis_sijoiteltavaksi, valinnanvaihe)
VALUES ('83c1c848-da64-435f-be41-23a29f4d8d98', '1410634972926-6437870106021103091', 'valintatapajono2', 1, 1, true, 'YLITAYTTO', false, true, '68401760-bd8d-4e79-bae5-67d3c341b94a');

INSERT INTO hakijaryhma(
    id, hakijaryhma_oid, prioriteetti, hakukohde_oid, nimi, kuvaus, kiintio, kayta_kaikki, tarkka_kiintio, kaytetaan_ryhmaan_kuuluvia)
VALUES ('ce7664b5-86cd-48af-b02f-28cdb5265bc6', '14134961691971462906249875676829', 0, '1.2.246.562.20.18895322503', 'Ensikertalaisten kiintiö', 'Ensikertalaisten kiintiö', 2, false, false, true);


INSERT INTO jonosija(
    hakemus_oid, hakija_oid, hakutoiveprioriteetti, harkinnanvarainen, valintatapajono, hylatty_valisijoittelussa, syotetyt_arvot, funktio_tulokset, hakijaryhma, jarjestyskriteeritulokset)
VALUES ('A', '1.2.246.562.24.45661259022', 1, false, '83c1c848-da64-435f-be41-23a29f4d8d97', false, '{"funktioTulokset": []}', '{"syotetytArvot": []}', 'ce7664b5-86cd-48af-b02f-28cdb5265bc6', '{"jarjestyskriteeritulokset": [{"arvo": 17.0, "nimi": "Testikriteeri", "prioriteetti": 0, "tila": "HYVAKSYTTAVISSA" }]}');

INSERT INTO jonosija(
    hakemus_oid, hakija_oid, hakutoiveprioriteetti, harkinnanvarainen, valintatapajono, hylatty_valisijoittelussa, syotetyt_arvot, funktio_tulokset, hakijaryhma, jarjestyskriteeritulokset)
VALUES ('D', '1.2.246.562.24.34552787533', 2, false, '83c1c848-da64-435f-be41-23a29f4d8d98', false, '{"funktioTulokset": []}', '{"syotetytArvot": []}', 'ce7664b5-86cd-48af-b02f-28cdb5265bc6', '{"jarjestyskriteeritulokset": [{"nimi": "Hakijaryhmän tulokset", "prioriteetti": 0, "tila": "HYVAKSYTTAVISSA" }]}');



INSERT INTO jonosija(
    hakemus_oid, hakija_oid, hakutoiveprioriteetti, harkinnanvarainen, valintatapajono, hylatty_valisijoittelussa, syotetyt_arvot, funktio_tulokset, hakijaryhma, jarjestyskriteeritulokset)
VALUES ('B', '1.2.246.562.24.42438870129', 2, false, '83c1c848-da64-435f-be41-23a29f4d8d97', false, '{"funktioTulokset": []}', '{"syotetytArvot": []}', 'ce7664b5-86cd-48af-b02f-28cdb5265bc6', '{"jarjestyskriteeritulokset": [{"nimi": "Hakijaryhmän tulokset", "prioriteetti": 0, "tila": "HYVAKSYTTAVISSA" }]}');


INSERT INTO jonosija(
    hakemus_oid, hakija_oid, hakutoiveprioriteetti, harkinnanvarainen, valintatapajono, hylatty_valisijoittelussa, syotetyt_arvot, funktio_tulokset, hakijaryhma, jarjestyskriteeritulokset)
VALUES ('C', '1.2.246.562.24.45661259011', 1, false, '83c1c848-da64-435f-be41-23a29f4d8d98', false, '{"funktioTulokset": []}', '{"syotetytArvot": []}', 'ce7664b5-86cd-48af-b02f-28cdb5265bc6', '{"jarjestyskriteeritulokset": [{"arvo": 19.0, "nimi": "Hakijaryhmän tulokset", "prioriteetti": 0, "tila": "HYVAKSYTTAVISSA" }]}');

