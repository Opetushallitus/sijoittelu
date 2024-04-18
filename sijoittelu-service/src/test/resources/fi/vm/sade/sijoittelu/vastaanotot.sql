INSERT INTO valinnanvaihe(
    id, jarjestysnumero, haku_oid, hakukohde_oid, valinnanvaihe_oid, tarjoaja_oid, nimi)
VALUES ('68401760-bd8d-4e79-bae5-67d3c341b94a', 1, '1.2.246.562.29.173465377510', 'hakukohde1', 'vaihe1', '1.2.246.562.10.28342991297', 'Varsinainen valinta');

INSERT INTO valinnanvaihe(
    id, jarjestysnumero, haku_oid, hakukohde_oid, valinnanvaihe_oid, tarjoaja_oid, nimi)
VALUES ('68401760-bd8d-4e79-bae5-67d3c341b94b', 1, '1.2.246.562.29.173465377510', 'hakukohde2', 'vaihe2', '1.2.246.562.10.28342991297', 'Varsinainen valinta');

INSERT INTO valinnanvaihe(
    id, jarjestysnumero, haku_oid, hakukohde_oid, valinnanvaihe_oid, tarjoaja_oid, nimi)
VALUES ('68401760-bd8d-4e79-bae5-67d3c341b94c', 1, '1.2.246.562.29.173465377510', 'hakukohde3', 'vaihe3', '1.2.246.562.10.28342991297', 'Varsinainen valinta');


INSERT INTO valintatapajono(
    id, valintatapajono_oid, nimi, prioriteetti, aloituspaikat, siirretaan_sijoitteluun, tasasijasaanto, ei_varasijatayttoa, valmis_sijoiteltavaksi, valinnanvaihe)
VALUES ('83c1c848-da64-435f-be41-23a29f4d8d97', 'jono1', 'Pisteet', 0, 3, true, 'YLITAYTTO', false, true, '68401760-bd8d-4e79-bae5-67d3c341b94a');

INSERT INTO valintatapajono(
    id, valintatapajono_oid, nimi, prioriteetti, aloituspaikat, siirretaan_sijoitteluun, tasasijasaanto, ei_varasijatayttoa, valmis_sijoiteltavaksi, valinnanvaihe)
VALUES ('83c1c848-da64-435f-be41-23a29f4d8d98', 'jono2', 'Pisteet', 0, 3, true, 'YLITAYTTO', false, true, '68401760-bd8d-4e79-bae5-67d3c341b94b');

INSERT INTO valintatapajono(
    id, valintatapajono_oid, nimi, prioriteetti, aloituspaikat, siirretaan_sijoitteluun, tasasijasaanto, ei_varasijatayttoa, valmis_sijoiteltavaksi, valinnanvaihe)
VALUES ('83c1c848-da64-435f-be41-23a29f4d8d99', 'jono3', 'Pisteet', 0, 3, true, 'YLITAYTTO', false, true, '68401760-bd8d-4e79-bae5-67d3c341b94c');



INSERT INTO jonosija(
    hakemus_oid, hakija_oid, hakutoiveprioriteetti, harkinnanvarainen, valintatapajono, hylatty_valisijoittelussa, syotetyt_arvot, funktio_tulokset, jarjestyskriteeritulokset)
VALUES ('oid1', '1.2.246.562.24.34552787533', 1, false, '83c1c848-da64-435f-be41-23a29f4d8d97', false, '{"funktioTulokset": []}', '{"syotetytArvot": []}', '{"jarjestyskriteeritulokset": [{"arvo": 11.0, "nimi": "Testikriteeri", "prioriteetti": 0, "tila": "HYVAKSYTTAVISSA" }]}');


INSERT INTO jonosija(
    hakemus_oid, hakija_oid, hakutoiveprioriteetti, harkinnanvarainen, valintatapajono, hylatty_valisijoittelussa, syotetyt_arvot, funktio_tulokset, jarjestyskriteeritulokset)
VALUES ('oid2', '1.2.246.562.24.34552787533', 1, false, '83c1c848-da64-435f-be41-23a29f4d8d97', false, '{"funktioTulokset": []}', '{"syotetytArvot": []}', '{"jarjestyskriteeritulokset": [{"arvo": 11.0, "nimi": "Testikriteeri", "prioriteetti": 0, "tila": "HYVAKSYTTAVISSA" }]}');


INSERT INTO jonosija(
    hakemus_oid, hakija_oid, hakutoiveprioriteetti, harkinnanvarainen, valintatapajono, hylatty_valisijoittelussa, syotetyt_arvot, funktio_tulokset, jarjestyskriteeritulokset)
VALUES ('oid3', '1.2.246.562.24.34552787533', 1, false, '83c1c848-da64-435f-be41-23a29f4d8d97', false, '{"funktioTulokset": []}', '{"syotetytArvot": []}', '{"jarjestyskriteeritulokset": [{"arvo": 11.0, "nimi": "Testikriteeri", "prioriteetti": 0, "tila": "HYVAKSYTTAVISSA" }]}');


INSERT INTO jonosija(
    hakemus_oid, hakija_oid, hakutoiveprioriteetti, harkinnanvarainen, valintatapajono, hylatty_valisijoittelussa, syotetyt_arvot, funktio_tulokset, jarjestyskriteeritulokset)
VALUES ('oid4', '1.2.246.562.24.34552787533', 1, false, '83c1c848-da64-435f-be41-23a29f4d8d97', false, '{"funktioTulokset": []}', '{"syotetytArvot": []}', '{"jarjestyskriteeritulokset": [{"arvo": 11.0, "nimi": "Testikriteeri", "prioriteetti": 0, "tila": "HYVAKSYTTAVISSA" }]}');







INSERT INTO jonosija(
    hakemus_oid, hakija_oid, hakutoiveprioriteetti, harkinnanvarainen, valintatapajono, hylatty_valisijoittelussa, syotetyt_arvot, funktio_tulokset, jarjestyskriteeritulokset)
VALUES ('oid1', '1.2.246.562.24.34552787533', 2, false, '83c1c848-da64-435f-be41-23a29f4d8d98', false, '{"funktioTulokset": []}', '{"syotetytArvot": []}', '{"jarjestyskriteeritulokset": [{"arvo": 11.0, "nimi": "Testikriteeri", "prioriteetti": 0, "tila": "HYVAKSYTTAVISSA" }]}');


INSERT INTO jonosija(
    hakemus_oid, hakija_oid, hakutoiveprioriteetti, harkinnanvarainen, valintatapajono, hylatty_valisijoittelussa, syotetyt_arvot, funktio_tulokset, jarjestyskriteeritulokset)
VALUES ('oid2', '1.2.246.562.24.34552787533', 2, false, '83c1c848-da64-435f-be41-23a29f4d8d98', false, '{"funktioTulokset": []}', '{"syotetytArvot": []}', '{"jarjestyskriteeritulokset": [{"arvo": 11.0, "nimi": "Testikriteeri", "prioriteetti": 0, "tila": "HYVAKSYTTAVISSA" }]}');


INSERT INTO jonosija(
    hakemus_oid, hakija_oid, hakutoiveprioriteetti, harkinnanvarainen, valintatapajono, hylatty_valisijoittelussa, syotetyt_arvot, funktio_tulokset, jarjestyskriteeritulokset)
VALUES ('oid3', '1.2.246.562.24.34552787533', 2, false, '83c1c848-da64-435f-be41-23a29f4d8d98', false, '{"funktioTulokset": []}', '{"syotetytArvot": []}', '{"jarjestyskriteeritulokset": [{"arvo": 11.0, "nimi": "Testikriteeri", "prioriteetti": 0, "tila": "HYVAKSYTTAVISSA" }]}');


INSERT INTO jonosija(
    hakemus_oid, hakija_oid, hakutoiveprioriteetti, harkinnanvarainen, valintatapajono, hylatty_valisijoittelussa, syotetyt_arvot, funktio_tulokset, jarjestyskriteeritulokset)
VALUES ('oid4', '1.2.246.562.24.34552787533', 2, false, '83c1c848-da64-435f-be41-23a29f4d8d98', false, '{"funktioTulokset": []}', '{"syotetytArvot": []}', '{"jarjestyskriteeritulokset": [{"arvo": 11.0, "nimi": "Testikriteeri", "prioriteetti": 0, "tila": "HYVAKSYTTAVISSA" }]}');







INSERT INTO jonosija(
    hakemus_oid, hakija_oid, hakutoiveprioriteetti, harkinnanvarainen, valintatapajono, hylatty_valisijoittelussa, syotetyt_arvot, funktio_tulokset, jarjestyskriteeritulokset)
VALUES ('oid1', '1.2.246.562.24.34552787533', 3, false, '83c1c848-da64-435f-be41-23a29f4d8d99', false, '{"funktioTulokset": []}', '{"syotetytArvot": []}', '{"jarjestyskriteeritulokset": [{"arvo": 11.0, "nimi": "Testikriteeri", "prioriteetti": 0, "tila": "HYVAKSYTTAVISSA" }]}');


INSERT INTO jonosija(
    hakemus_oid, hakija_oid, hakutoiveprioriteetti, harkinnanvarainen, valintatapajono, hylatty_valisijoittelussa, syotetyt_arvot, funktio_tulokset, jarjestyskriteeritulokset)
VALUES ('oid2', '1.2.246.562.24.34552787533', 3, false, '83c1c848-da64-435f-be41-23a29f4d8d99', false, '{"funktioTulokset": []}', '{"syotetytArvot": []}', '{"jarjestyskriteeritulokset": [{"arvo": 11.0, "nimi": "Testikriteeri", "prioriteetti": 0, "tila": "HYVAKSYTTAVISSA" }]}');


INSERT INTO jonosija(
    hakemus_oid, hakija_oid, hakutoiveprioriteetti, harkinnanvarainen, valintatapajono, hylatty_valisijoittelussa, syotetyt_arvot, funktio_tulokset, jarjestyskriteeritulokset)
VALUES ('oid3', '1.2.246.562.24.34552787533', 3, false, '83c1c848-da64-435f-be41-23a29f4d8d99', false, '{"funktioTulokset": []}', '{"syotetytArvot": []}', '{"jarjestyskriteeritulokset": [{"arvo": 11.0, "nimi": "Testikriteeri", "prioriteetti": 0, "tila": "HYVAKSYTTAVISSA" }]}');


INSERT INTO jonosija(
    hakemus_oid, hakija_oid, hakutoiveprioriteetti, harkinnanvarainen, valintatapajono, hylatty_valisijoittelussa, syotetyt_arvot, funktio_tulokset, jarjestyskriteeritulokset)
VALUES ('oid4', '1.2.246.562.24.34552787533', 3, false, '83c1c848-da64-435f-be41-23a29f4d8d99', false, '{"funktioTulokset": []}', '{"syotetytArvot": []}', '{"jarjestyskriteeritulokset": [{"arvo": 11.0, "nimi": "Testikriteeri", "prioriteetti": 0, "tila": "HYVAKSYTTAVISSA" }]}');
