INSERT INTO valinnanvaihe(
    id, jarjestysnumero, haku_oid, hakukohde_oid, valinnanvaihe_oid, tarjoaja_oid, nimi)
VALUES ('68401760-bd8d-4e79-bae5-67d3c341b94a', 1, 'haku1', 'hakukohde1', 'vaihe1', 'tarjoaja1', 'Varsinainen valinta');

INSERT INTO valintatapajono(
    id, valintatapajono_oid, nimi, prioriteetti, aloituspaikat, siirretaan_sijoitteluun, tasasijasaanto, ei_varasijatayttoa, valmis_sijoiteltavaksi, valinnanvaihe)
VALUES ('83c1c848-da64-435f-be41-23a29f4d8d97', 'jono1', 'Pisteet', 0, 2, true, 'YLITAYTTO', false, true, '68401760-bd8d-4e79-bae5-67d3c341b94a');



INSERT INTO jonosija(
    hakemus_oid, hakija_oid, hakutoiveprioriteetti, harkinnanvarainen, valintatapajono, hylatty_valisijoittelussa, syotetyt_arvot, funktio_tulokset, jarjestyskriteeritulokset)
VALUES ('hakemus1', 'hakija1', 1, false, '83c1c848-da64-435f-be41-23a29f4d8d97', true, '{"funktioTulokset": []}', '{"syotetytArvot": []}', '{"jarjestyskriteeritulokset": [{"arvo": 11.0, "nimi": "Testikriteeri", "prioriteetti": 0, "tila": "HYLATTY" }]}');


INSERT INTO jonosija(
    hakemus_oid, hakija_oid, hakutoiveprioriteetti, harkinnanvarainen, valintatapajono, hylatty_valisijoittelussa, syotetyt_arvot, funktio_tulokset, jarjestyskriteeritulokset)
VALUES ('hakemus2', 'hakija2', 2, false, '83c1c848-da64-435f-be41-23a29f4d8d97', false, '{"funktioTulokset": []}', '{"syotetytArvot": []}', '{"jarjestyskriteeritulokset": [{"arvo": 19.0, "nimi": "Testikriteeri", "prioriteetti": 0, "tila": "HYLATTY" }]}');



INSERT INTO jonosija(
    hakemus_oid, hakija_oid, hakutoiveprioriteetti, harkinnanvarainen, valintatapajono, hylatty_valisijoittelussa, syotetyt_arvot, funktio_tulokset, jarjestyskriteeritulokset)
VALUES ('hakemus3', 'hakija3', 4, false, '83c1c848-da64-435f-be41-23a29f4d8d97', false, '{"funktioTulokset": []}', '{"syotetytArvot": []}', '{"jarjestyskriteeritulokset": [{"arvo": 17.0, "nimi": "Testikriteeri", "prioriteetti": 0, "tila": "HYLATTY" }]}');


