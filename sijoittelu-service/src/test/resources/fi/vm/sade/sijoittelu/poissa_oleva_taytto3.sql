INSERT INTO valinnanvaihe(
    id, jarjestysnumero, haku_oid, hakukohde_oid, valinnanvaihe_oid, tarjoaja_oid, nimi)
VALUES ('68401760-bd8d-4e79-bae5-67d3c341b94a', 1, 'haku1', 'hakukohde1', 'vaihe1', '1.2.246.562.10.28342991297', 'Varsinainen valinta');

INSERT INTO valinnanvaihe(
    id, jarjestysnumero, haku_oid, hakukohde_oid, valinnanvaihe_oid, tarjoaja_oid, nimi)
VALUES ('68401760-bd8d-4e79-bae5-67d3c341b94b', 1, 'haku1', 'hakukohde2', 'vaihe2', '1.2.246.562.10.28342991297', 'Varsinainen valinta');


INSERT INTO valintatapajono(
    id, valintatapajono_oid, nimi, prioriteetti, aloituspaikat, siirretaan_sijoitteluun, tasasijasaanto, ei_varasijatayttoa, valmis_sijoiteltavaksi, valinnanvaihe, poissa_oleva_taytto)
VALUES ('83c1c848-da64-435f-be41-23a29f4d8d97', 'jono1', 'Pisteet', 0, 4, true, 'ARVONTA', false, true, '68401760-bd8d-4e79-bae5-67d3c341b94a', true);

INSERT INTO valintatapajono(
    id, valintatapajono_oid, nimi, prioriteetti, aloituspaikat, siirretaan_sijoitteluun, tasasijasaanto, ei_varasijatayttoa, valmis_sijoiteltavaksi, valinnanvaihe, poissa_oleva_taytto)
VALUES ('83c1c848-da64-435f-be41-23a29f4d8d98', 'jono2', 'Pisteet', 0, 5, true, 'ARVONTA', false, true, '68401760-bd8d-4e79-bae5-67d3c341b94a', true);




INSERT INTO jonosija(
    hakemus_oid, hakija_oid, hakutoiveprioriteetti, harkinnanvarainen, valintatapajono, hylatty_valisijoittelussa, syotetyt_arvot, funktio_tulokset, jarjestyskriteeritulokset)
VALUES ('oid1', '1.2.246.562.24.34552787533', 1, false, '83c1c848-da64-435f-be41-23a29f4d8d97', false, '{"funktioTulokset": []}', '{"syotetytArvot": []}', '{"jarjestyskriteeritulokset": [{"arvo": 50.0, "nimi": "Testikriteeri", "prioriteetti": 0, "tila": "HYVAKSYTTAVISSA" }]}');


INSERT INTO jonosija(
    hakemus_oid, hakija_oid, hakutoiveprioriteetti, harkinnanvarainen, valintatapajono, hylatty_valisijoittelussa, syotetyt_arvot, funktio_tulokset, jarjestyskriteeritulokset)
VALUES ('oid2', '1.2.246.562.24.34552787533', 1, false, '83c1c848-da64-435f-be41-23a29f4d8d97', false, '{"funktioTulokset": []}', '{"syotetytArvot": []}', '{"jarjestyskriteeritulokset": [{"arvo": 40.0, "nimi": "Testikriteeri", "prioriteetti": 0, "tila": "HYVAKSYTTAVISSA" }]}');



INSERT INTO jonosija(
    hakemus_oid, hakija_oid, hakutoiveprioriteetti, harkinnanvarainen, valintatapajono, hylatty_valisijoittelussa, syotetyt_arvot, funktio_tulokset, jarjestyskriteeritulokset)
VALUES ('oid3', '1.2.246.562.24.34552787533', 1, false, '83c1c848-da64-435f-be41-23a29f4d8d97', false, '{"funktioTulokset": []}', '{"syotetytArvot": []}', '{"jarjestyskriteeritulokset": [{"arvo": 30.0, "nimi": "Testikriteeri", "prioriteetti": 0, "tila": "HYVAKSYTTAVISSA" }]}');



INSERT INTO jonosija(
    hakemus_oid, hakija_oid, hakutoiveprioriteetti, harkinnanvarainen, valintatapajono, hylatty_valisijoittelussa, syotetyt_arvot, funktio_tulokset, jarjestyskriteeritulokset)
VALUES ('oid4', '1.2.246.562.24.34552787533', 1, false, '83c1c848-da64-435f-be41-23a29f4d8d97', false, '{"funktioTulokset": []}', '{"syotetytArvot": []}', '{"jarjestyskriteeritulokset": [{"arvo": 20.0, "nimi": "Testikriteeri", "prioriteetti": 0, "tila": "HYVAKSYTTAVISSA" }]}');



INSERT INTO jonosija(
    hakemus_oid, hakija_oid, hakutoiveprioriteetti, harkinnanvarainen, valintatapajono, hylatty_valisijoittelussa, syotetyt_arvot, funktio_tulokset, jarjestyskriteeritulokset)
VALUES ('oid6', '1.2.246.562.24.34552787533', 2, false, '83c1c848-da64-435f-be41-23a29f4d8d97', false, '{"funktioTulokset": []}', '{"syotetytArvot": []}', '{"jarjestyskriteeritulokset": [{"arvo": 9.0, "nimi": "Testikriteeri", "prioriteetti": 0, "tila": "HYVAKSYTTAVISSA" }]}');



INSERT INTO jonosija(
    hakemus_oid, hakija_oid, hakutoiveprioriteetti, harkinnanvarainen, valintatapajono, hylatty_valisijoittelussa, syotetyt_arvot, funktio_tulokset, jarjestyskriteeritulokset)
VALUES ('oid7', '1.2.246.562.24.34552787533', 2, false, '83c1c848-da64-435f-be41-23a29f4d8d97', false, '{"funktioTulokset": []}', '{"syotetytArvot": []}', '{"jarjestyskriteeritulokset": [{"arvo": 9.0, "nimi": "Testikriteeri", "prioriteetti": 0, "tila": "HYVAKSYTTAVISSA" }]}');



INSERT INTO jonosija(
    hakemus_oid, hakija_oid, hakutoiveprioriteetti, harkinnanvarainen, valintatapajono, hylatty_valisijoittelussa, syotetyt_arvot, funktio_tulokset, jarjestyskriteeritulokset)
VALUES ('oid8', '1.2.246.562.24.34552787533', 2, false, '83c1c848-da64-435f-be41-23a29f4d8d97', false, '{"funktioTulokset": []}', '{"syotetytArvot": []}', '{"jarjestyskriteeritulokset": [{"arvo": 9.0, "nimi": "Testikriteeri", "prioriteetti": 0, "tila": "HYVAKSYTTAVISSA" }]}');


INSERT INTO jonosija(
    hakemus_oid, hakija_oid, hakutoiveprioriteetti, harkinnanvarainen, valintatapajono, hylatty_valisijoittelussa, syotetyt_arvot, funktio_tulokset, jarjestyskriteeritulokset)
VALUES ('oid9', '1.2.246.562.24.34552787533', 1, false, '83c1c848-da64-435f-be41-23a29f4d8d97', false, '{"funktioTulokset": []}', '{"syotetytArvot": []}', '{"jarjestyskriteeritulokset": [{"arvo": 9.0, "nimi": "Testikriteeri", "prioriteetti": 0, "tila": "HYVAKSYTTAVISSA" }]}');




INSERT INTO jonosija(
    hakemus_oid, hakija_oid, hakutoiveprioriteetti, harkinnanvarainen, valintatapajono, hylatty_valisijoittelussa, syotetyt_arvot, funktio_tulokset, jarjestyskriteeritulokset)
VALUES ('oid10', '1.2.246.562.24.34552787533', 1, false, '83c1c848-da64-435f-be41-23a29f4d8d97', false, '{"funktioTulokset": []}', '{"syotetytArvot": []}', '{"jarjestyskriteeritulokset": [{"arvo": 9.0, "nimi": "Testikriteeri", "prioriteetti": 0, "tila": "HYVAKSYTTAVISSA" }]}');



INSERT INTO jonosija(
    hakemus_oid, hakija_oid, hakutoiveprioriteetti, harkinnanvarainen, valintatapajono, hylatty_valisijoittelussa, syotetyt_arvot, funktio_tulokset, jarjestyskriteeritulokset)
VALUES ('oid11', '1.2.246.562.24.34552787533', 1, false, '83c1c848-da64-435f-be41-23a29f4d8d97', false, '{"funktioTulokset": []}', '{"syotetytArvot": []}', '{"jarjestyskriteeritulokset": [{"arvo": 19.0, "nimi": "Testikriteeri", "prioriteetti": 0, "tila": "HYVAKSYTTAVISSA" }]}');






INSERT INTO jonosija(
    hakemus_oid, hakija_oid, hakutoiveprioriteetti, harkinnanvarainen, valintatapajono, hylatty_valisijoittelussa, syotetyt_arvot, funktio_tulokset, jarjestyskriteeritulokset)
VALUES ('oid1', '1.2.246.562.24.34552787533', 1, false, '83c1c848-da64-435f-be41-23a29f4d8d98', false, '{"funktioTulokset": []}', '{"syotetytArvot": []}', '{"jarjestyskriteeritulokset": [{"arvo": 50.0, "nimi": "Testikriteeri", "prioriteetti": 0, "tila": "HYVAKSYTTAVISSA" }]}');


INSERT INTO jonosija(
    hakemus_oid, hakija_oid, hakutoiveprioriteetti, harkinnanvarainen, valintatapajono, hylatty_valisijoittelussa, syotetyt_arvot, funktio_tulokset, jarjestyskriteeritulokset)
VALUES ('oid2', '1.2.246.562.24.34552787533', 1, false, '83c1c848-da64-435f-be41-23a29f4d8d98', false, '{"funktioTulokset": []}', '{"syotetytArvot": []}', '{"jarjestyskriteeritulokset": [{"arvo": 40.0, "nimi": "Testikriteeri", "prioriteetti": 0, "tila": "HYVAKSYTTAVISSA" }]}');


INSERT INTO jonosija(
    hakemus_oid, hakija_oid, hakutoiveprioriteetti, harkinnanvarainen, valintatapajono, hylatty_valisijoittelussa, syotetyt_arvot, funktio_tulokset, jarjestyskriteeritulokset)
VALUES ('oid3', '1.2.246.562.24.34552787533', 1, false, '83c1c848-da64-435f-be41-23a29f4d8d98', false, '{"funktioTulokset": []}', '{"syotetytArvot": []}', '{"jarjestyskriteeritulokset": [{"arvo": 30.0, "nimi": "Testikriteeri", "prioriteetti": 0, "tila": "HYVAKSYTTAVISSA" }]}');


INSERT INTO jonosija(
    hakemus_oid, hakija_oid, hakutoiveprioriteetti, harkinnanvarainen, valintatapajono, hylatty_valisijoittelussa, syotetyt_arvot, funktio_tulokset, jarjestyskriteeritulokset)
VALUES ('oid4', '1.2.246.562.24.34552787533', 1, false, '83c1c848-da64-435f-be41-23a29f4d8d98', false, '{"funktioTulokset": []}', '{"syotetytArvot": []}', '{"jarjestyskriteeritulokset": [{"arvo": 20.0, "nimi": "Testikriteeri", "prioriteetti": 0, "tila": "HYVAKSYTTAVISSA" }]}');



INSERT INTO jonosija(
    hakemus_oid, hakija_oid, hakutoiveprioriteetti, harkinnanvarainen, valintatapajono, hylatty_valisijoittelussa, syotetyt_arvot, funktio_tulokset, jarjestyskriteeritulokset)
VALUES ('oid6', '1.2.246.562.24.34552787533', 2, false, '83c1c848-da64-435f-be41-23a29f4d8d98', false, '{"funktioTulokset": []}', '{"syotetytArvot": []}', '{"jarjestyskriteeritulokset": [{"arvo": 9.0, "nimi": "Testikriteeri", "prioriteetti": 0, "tila": "HYVAKSYTTAVISSA" }]}');



INSERT INTO jonosija(
    hakemus_oid, hakija_oid, hakutoiveprioriteetti, harkinnanvarainen, valintatapajono, hylatty_valisijoittelussa, syotetyt_arvot, funktio_tulokset, jarjestyskriteeritulokset)
VALUES ('oid7', '1.2.246.562.24.34552787533', 2, false, '83c1c848-da64-435f-be41-23a29f4d8d98', false, '{"funktioTulokset": []}', '{"syotetytArvot": []}', '{"jarjestyskriteeritulokset": [{"arvo": 9.0, "nimi": "Testikriteeri", "prioriteetti": 0, "tila": "HYVAKSYTTAVISSA" }]}');



INSERT INTO jonosija(
    hakemus_oid, hakija_oid, hakutoiveprioriteetti, harkinnanvarainen, valintatapajono, hylatty_valisijoittelussa, syotetyt_arvot, funktio_tulokset, jarjestyskriteeritulokset)
VALUES ('oid8', '1.2.246.562.24.34552787533', 2, false, '83c1c848-da64-435f-be41-23a29f4d8d98', false, '{"funktioTulokset": []}', '{"syotetytArvot": []}', '{"jarjestyskriteeritulokset": [{"arvo": 9.0, "nimi": "Testikriteeri", "prioriteetti": 0, "tila": "HYVAKSYTTAVISSA" }]}');



INSERT INTO jonosija(
    hakemus_oid, hakija_oid, hakutoiveprioriteetti, harkinnanvarainen, valintatapajono, hylatty_valisijoittelussa, syotetyt_arvot, funktio_tulokset, jarjestyskriteeritulokset)
VALUES ('oid9', '1.2.246.562.24.34552787533', 1, false, '83c1c848-da64-435f-be41-23a29f4d8d98', false, '{"funktioTulokset": []}', '{"syotetytArvot": []}', '{"jarjestyskriteeritulokset": [{"arvo": 9.0, "nimi": "Testikriteeri", "prioriteetti": 0, "tila": "HYVAKSYTTAVISSA" }]}');



INSERT INTO jonosija(
    hakemus_oid, hakija_oid, hakutoiveprioriteetti, harkinnanvarainen, valintatapajono, hylatty_valisijoittelussa, syotetyt_arvot, funktio_tulokset, jarjestyskriteeritulokset)
VALUES ('oid10', '1.2.246.562.24.34552787533', 1, false, '83c1c848-da64-435f-be41-23a29f4d8d98', false, '{"funktioTulokset": []}', '{"syotetytArvot": []}', '{"jarjestyskriteeritulokset": [{"arvo": 9.0, "nimi": "Testikriteeri", "prioriteetti": 0, "tila": "HYVAKSYTTAVISSA" }]}');




INSERT INTO jonosija(
    hakemus_oid, hakija_oid, hakutoiveprioriteetti, harkinnanvarainen, valintatapajono, hylatty_valisijoittelussa, syotetyt_arvot, funktio_tulokset, jarjestyskriteeritulokset)
VALUES ('oid11', '1.2.246.562.24.34552787533', 1, false, '83c1c848-da64-435f-be41-23a29f4d8d98', false, '{"funktioTulokset": []}', '{"syotetytArvot": []}', '{"jarjestyskriteeritulokset": [{"arvo": 9.0, "nimi": "Testikriteeri", "prioriteetti": 0, "tila": "HYVAKSYTTAVISSA" }]}');

