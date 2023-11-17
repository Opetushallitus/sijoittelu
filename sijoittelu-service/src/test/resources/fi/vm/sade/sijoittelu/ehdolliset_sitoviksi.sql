INSERT INTO valinnanvaihe(
    id, jarjestysnumero, haku_oid, hakukohde_oid, valinnanvaihe_oid, tarjoaja_oid, nimi)
VALUES ('68401760-bd8d-4e79-bae5-67d3c341b94a', 1, 'haku1', 'hakukohde1', 'vaihe1', '1.2.246.562.10.28342991297', 'Varsinainen valinta');

INSERT INTO valintatapajono(
    id, valintatapajono_oid, nimi, prioriteetti, aloituspaikat, siirretaan_sijoitteluun, tasasijasaanto, ei_varasijatayttoa, valmis_sijoiteltavaksi, valinnanvaihe, poissa_oleva_taytto)
VALUES ('83c1c848-da64-435f-be41-23a29f4d8d97', 'oid1', 'Pisteet', 0, 1, true, 'ARVONTA', false, true, '68401760-bd8d-4e79-bae5-67d3c341b94a', true);

INSERT INTO valintatapajono(
    id, valintatapajono_oid, nimi, prioriteetti, aloituspaikat, siirretaan_sijoitteluun, tasasijasaanto, ei_varasijatayttoa, valmis_sijoiteltavaksi, valinnanvaihe)
VALUES ('83c1c848-da64-435f-be41-23a29f4d8d98', 'oid2', 'Koe', 1, 1, true, 'ARVONTA', false, true, '68401760-bd8d-4e79-bae5-67d3c341b94a');




INSERT INTO jonosija(
    hakemus_oid, hakija_oid, hakutoiveprioriteetti, harkinnanvarainen, valintatapajono, hylatty_valisijoittelussa, syotetyt_arvot, funktio_tulokset)
VALUES ('hakemus1', '1.2.246.562.24.34552787533', 1, false, '83c1c848-da64-435f-be41-23a29f4d8d97', false, '{"funktioTulokset": []}', '{"syotetytArvot": []}');

INSERT INTO jarjestyskriteeritulos(arvo, nimi, prioriteetti, tila, jonosija)
SELECT 11.0, 'Testikriteeri', 0, 'HYVAKSYTTAVISSA', jonosija.id from jonosija
WHERE hakemus_oid = 'hakemus1';

INSERT INTO jonosija(
    hakemus_oid, hakija_oid, hakutoiveprioriteetti, harkinnanvarainen, valintatapajono, hylatty_valisijoittelussa, syotetyt_arvot, funktio_tulokset)
VALUES ('hakemus2', '1.2.246.562.24.42438870792', 2, false, '83c1c848-da64-435f-be41-23a29f4d8d97', false, '{"funktioTulokset": []}', '{"syotetytArvot": []}');

INSERT INTO jarjestyskriteeritulos(arvo, nimi, prioriteetti, tila, jonosija)
SELECT 19.0, 'Testikriteeri', 0, 'HYVAKSYTTAVISSA', jonosija.id from jonosija
WHERE hakemus_oid = 'hakemus2';

INSERT INTO jonosija(
    hakemus_oid, hakija_oid, hakutoiveprioriteetti, harkinnanvarainen, valintatapajono, hylatty_valisijoittelussa, syotetyt_arvot, funktio_tulokset)
VALUES ('hakemus3', '1.2.246.562.24.45661259022', 4, false, '83c1c848-da64-435f-be41-23a29f4d8d97', false, '{"funktioTulokset": []}', '{"syotetytArvot": []}');

INSERT INTO jarjestyskriteeritulos(arvo, nimi, prioriteetti, tila, jonosija)
SELECT 17.0, 'Testikriteeri', 0, 'HYVAKSYTTAVISSA', jonosija.id from jonosija
WHERE hakemus_oid = 'hakemus3';

INSERT INTO jonosija(
    hakemus_oid, hakija_oid, hakutoiveprioriteetti, harkinnanvarainen, valintatapajono, hylatty_valisijoittelussa, syotetyt_arvot, funktio_tulokset)
VALUES ('hakemus1', '1.2.246.562.24.34552787533', 1, false, '83c1c848-da64-435f-be41-23a29f4d8d98', false, '{"funktioTulokset": []}', '{"syotetytArvot": []}');

INSERT INTO jarjestyskriteeritulos(arvo, nimi, prioriteetti, tila, jonosija)
SELECT 11.0, 'Testikriteeri', 0, 'HYVAKSYTTAVISSA', jonosija.id from jonosija
WHERE hakemus_oid = 'hakemus1' AND valintatapajono = '83c1c848-da64-435f-be41-23a29f4d8d98';

INSERT INTO jonosija(
    hakemus_oid, hakija_oid, hakutoiveprioriteetti, harkinnanvarainen, valintatapajono, hylatty_valisijoittelussa, syotetyt_arvot, funktio_tulokset)
VALUES ('hakemus2', '1.2.246.562.24.42438870792', 2, false, '83c1c848-da64-435f-be41-23a29f4d8d98', false, '{"funktioTulokset": []}', '{"syotetytArvot": []}');

INSERT INTO jarjestyskriteeritulos(arvo, nimi, prioriteetti, tila, jonosija)
SELECT 19.0, 'Testikriteeri', 0, 'HYVAKSYTTAVISSA', jonosija.id from jonosija
WHERE hakemus_oid = 'hakemus2' AND valintatapajono = '83c1c848-da64-435f-be41-23a29f4d8d98';

INSERT INTO jonosija(
    hakemus_oid, hakija_oid, hakutoiveprioriteetti, harkinnanvarainen, valintatapajono, hylatty_valisijoittelussa, syotetyt_arvot, funktio_tulokset)
VALUES ('hakemus3', '1.2.246.562.24.45661259022', 4, false, '83c1c848-da64-435f-be41-23a29f4d8d98', false, '{"funktioTulokset": []}', '{"syotetytArvot": []}');

INSERT INTO jarjestyskriteeritulos(arvo, nimi, prioriteetti, tila, jonosija)
SELECT 17.0, 'Testikriteeri', 0, 'HYVAKSYTTAVISSA', jonosija.id from jonosija
WHERE hakemus_oid = 'hakemus3' AND valintatapajono = '83c1c848-da64-435f-be41-23a29f4d8d98';
