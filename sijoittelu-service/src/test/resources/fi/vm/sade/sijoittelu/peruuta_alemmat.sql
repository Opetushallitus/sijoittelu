INSERT INTO valinnanvaihe(
    id, jarjestysnumero, haku_oid, hakukohde_oid, valinnanvaihe_oid, tarjoaja_oid, nimi)
VALUES ('68401760-bd8d-4e79-bae5-67d3c341b94a', 1, 'haku1', 'hakukohde2', 'valinnanvaihe2', 'tarjoaja2', 'Varsinainen valinta');

INSERT INTO valinnanvaihe(
    id, jarjestysnumero, haku_oid, hakukohde_oid, valinnanvaihe_oid, tarjoaja_oid, nimi)
VALUES ('68401760-bd8d-4e79-bae5-67d3c341b94b', 1, 'haku1', 'hakukohde3', 'valinnanvaihe3', 'tarjoaja3', 'Varsinainen valinta');

INSERT INTO valinnanvaihe(
    id, jarjestysnumero, haku_oid, hakukohde_oid, valinnanvaihe_oid, tarjoaja_oid, nimi)
VALUES ('68401760-bd8d-4e79-bae5-67d3c341b94c', 1, 'haku1', 'hakukohde1', 'valinnanvaihe1', 'tarjoaja1', 'Varsinainen valinta');


INSERT INTO valintatapajono(
    id, valintatapajono_oid, nimi, prioriteetti, aloituspaikat, siirretaan_sijoitteluun, tasasijasaanto, ei_varasijatayttoa, valmis_sijoiteltavaksi, valinnanvaihe)
VALUES ('83c1c848-da64-435f-be41-23a29f4d8d97', 'jono2', 'Jono2', 0, 2, true, 'ARVONTA', false, true, '68401760-bd8d-4e79-bae5-67d3c341b94a');

INSERT INTO valintatapajono(
    id, valintatapajono_oid, nimi, prioriteetti, aloituspaikat, siirretaan_sijoitteluun, tasasijasaanto, ei_varasijatayttoa, valmis_sijoiteltavaksi, valinnanvaihe)
VALUES ('83c1c848-da64-435f-be41-23a29f4d8d98', 'jono3', 'Jono3', 0, 2, true, 'ARVONTA', false, true, '68401760-bd8d-4e79-bae5-67d3c341b94b');

INSERT INTO valintatapajono(
    id, valintatapajono_oid, nimi, prioriteetti, aloituspaikat, siirretaan_sijoitteluun, tasasijasaanto, ei_varasijatayttoa, valmis_sijoiteltavaksi, valinnanvaihe)
VALUES ('83c1c848-da64-435f-be41-23a29f4d8d99', 'jono1', 'Jono1', 0, 2, true, 'ARVONTA', false, true, '68401760-bd8d-4e79-bae5-67d3c341b94c');




INSERT INTO jonosija(
    hakemus_oid, hakija_oid, hakutoiveprioriteetti, harkinnanvarainen, valintatapajono, hylatty_valisijoittelussa, syotetyt_arvot, funktio_tulokset)
VALUES ('hakemus1', 'hakija1', 3, false, '83c1c848-da64-435f-be41-23a29f4d8d97', false, '{"funktioTulokset": []}', '{"syotetytArvot": []}');

INSERT INTO jarjestyskriteeritulos(arvo, nimi, prioriteetti, tila, jonosija)
SELECT 10.0, 'Testikriteeri', 0, 'HYVAKSYTTAVISSA', jonosija.id from jonosija
WHERE hakemus_oid = 'hakemus1';

INSERT INTO jonosija(
    hakemus_oid, hakija_oid, hakutoiveprioriteetti, harkinnanvarainen, valintatapajono, hylatty_valisijoittelussa, syotetyt_arvot, funktio_tulokset)
VALUES ('hakemus2', 'hakija2', 2, false, '83c1c848-da64-435f-be41-23a29f4d8d97', false, '{"funktioTulokset": []}', '{"syotetytArvot": []}');

INSERT INTO jarjestyskriteeritulos(arvo, nimi, prioriteetti, tila, jonosija)
SELECT 9.0, 'Testikriteeri', 0, 'HYVAKSYTTAVISSA', jonosija.id from jonosija
WHERE hakemus_oid = 'hakemus2';

INSERT INTO jonosija(
    hakemus_oid, hakija_oid, hakutoiveprioriteetti, harkinnanvarainen, valintatapajono, hylatty_valisijoittelussa, syotetyt_arvot, funktio_tulokset)
VALUES ('hakemus3', 'hakija3', 2, false, '83c1c848-da64-435f-be41-23a29f4d8d97', false, '{"funktioTulokset": []}', '{"syotetytArvot": []}');

INSERT INTO jarjestyskriteeritulos(arvo, nimi, prioriteetti, tila, jonosija)
SELECT 8.0, 'Testikriteeri', 0, 'HYVAKSYTTAVISSA', jonosija.id from jonosija
WHERE hakemus_oid = 'hakemus3';

INSERT INTO jonosija(
    hakemus_oid, hakija_oid, hakutoiveprioriteetti, harkinnanvarainen, valintatapajono, hylatty_valisijoittelussa, syotetyt_arvot, funktio_tulokset)
VALUES ('hakemus4', 'hakija3', 2, false, '83c1c848-da64-435f-be41-23a29f4d8d97', false, '{"funktioTulokset": []}', '{"syotetytArvot": []}');

INSERT INTO jarjestyskriteeritulos(arvo, nimi, prioriteetti, tila, jonosija)
SELECT 7.0, 'Testikriteeri', 0, 'HYVAKSYTTAVISSA', jonosija.id from jonosija
WHERE hakemus_oid = 'hakemus4';

INSERT INTO jonosija(
    hakemus_oid, hakija_oid, hakutoiveprioriteetti, harkinnanvarainen, valintatapajono, hylatty_valisijoittelussa, syotetyt_arvot, funktio_tulokset)
VALUES ('hakemus5', 'hakija3', 2, false, '83c1c848-da64-435f-be41-23a29f4d8d97', false, '{"funktioTulokset": []}', '{"syotetytArvot": []}');

INSERT INTO jarjestyskriteeritulos(arvo, nimi, prioriteetti, tila, jonosija)
SELECT 6.0, 'Testikriteeri', 0, 'HYVAKSYTTAVISSA', jonosija.id from jonosija
WHERE hakemus_oid = 'hakemus5';

INSERT INTO jonosija(
    hakemus_oid, hakija_oid, hakutoiveprioriteetti, harkinnanvarainen, valintatapajono, hylatty_valisijoittelussa, syotetyt_arvot, funktio_tulokset)
VALUES ('hakemus6', 'hakija3', 2, false, '83c1c848-da64-435f-be41-23a29f4d8d97', false, '{"funktioTulokset": []}', '{"syotetytArvot": []}');

INSERT INTO jarjestyskriteeritulos(arvo, nimi, prioriteetti, tila, jonosija)
SELECT 5.0, 'Testikriteeri', 0, 'HYVAKSYTTAVISSA', jonosija.id from jonosija
WHERE hakemus_oid = 'hakemus6';

INSERT INTO jonosija(
    hakemus_oid, hakija_oid, hakutoiveprioriteetti, harkinnanvarainen, valintatapajono, hylatty_valisijoittelussa, syotetyt_arvot, funktio_tulokset)
VALUES ('hakemus1', 'hakija1', 2, false, '83c1c848-da64-435f-be41-23a29f4d8d98', false, '{"funktioTulokset": []}', '{"syotetytArvot": []}');

INSERT INTO jarjestyskriteeritulos(arvo, nimi, prioriteetti, tila, jonosija)
SELECT 10.0, 'Testikriteeri', 0, 'HYVAKSYTTAVISSA', jonosija.id from jonosija
WHERE hakemus_oid = 'hakemus1' AND valintatapajono = '83c1c848-da64-435f-be41-23a29f4d8d98';

INSERT INTO jonosija(
    hakemus_oid, hakija_oid, hakutoiveprioriteetti, harkinnanvarainen, valintatapajono, hylatty_valisijoittelussa, syotetyt_arvot, funktio_tulokset)
VALUES ('hakemus1', 'hakija1', 1, false, '83c1c848-da64-435f-be41-23a29f4d8d99', false, '{"funktioTulokset": []}', '{"syotetytArvot": []}');

INSERT INTO jarjestyskriteeritulos(arvo, nimi, prioriteetti, tila, jonosija)
SELECT 10.0, 'Testikriteeri', 0, 'HYVAKSYTTAVISSA', jonosija.id from jonosija
WHERE hakemus_oid = 'hakemus1' AND valintatapajono = '83c1c848-da64-435f-be41-23a29f4d8d99';

INSERT INTO jonosija(
    hakemus_oid, hakija_oid, hakutoiveprioriteetti, harkinnanvarainen, valintatapajono, hylatty_valisijoittelussa, syotetyt_arvot, funktio_tulokset)
VALUES ('hakemus2', 'hakija2', 1, false, '83c1c848-da64-435f-be41-23a29f4d8d99', false, '{"funktioTulokset": []}', '{"syotetytArvot": []}');

INSERT INTO jarjestyskriteeritulos(arvo, nimi, prioriteetti, tila, jonosija)
SELECT 9.0, 'Testikriteeri', 0, 'HYVAKSYTTAVISSA', jonosija.id from jonosija
WHERE hakemus_oid = 'hakemus2' AND valintatapajono = '83c1c848-da64-435f-be41-23a29f4d8d99';

INSERT INTO jonosija(
    hakemus_oid, hakija_oid, hakutoiveprioriteetti, harkinnanvarainen, valintatapajono, hylatty_valisijoittelussa, syotetyt_arvot, funktio_tulokset)
VALUES ('hakemus3', 'hakija3', 1, false, '83c1c848-da64-435f-be41-23a29f4d8d99', false, '{"funktioTulokset": []}', '{"syotetytArvot": []}');

INSERT INTO jarjestyskriteeritulos(arvo, nimi, prioriteetti, tila, jonosija)
SELECT 8.0, 'Testikriteeri', 0, 'HYVAKSYTTAVISSA', jonosija.id from jonosija
WHERE hakemus_oid = 'hakemus3' AND valintatapajono = '83c1c848-da64-435f-be41-23a29f4d8d99';

INSERT INTO jonosija(
    hakemus_oid, hakija_oid, hakutoiveprioriteetti, harkinnanvarainen, valintatapajono, hylatty_valisijoittelussa, syotetyt_arvot, funktio_tulokset)
VALUES ('hakemus4', 'hakija3', 1, false, '83c1c848-da64-435f-be41-23a29f4d8d99', false, '{"funktioTulokset": []}', '{"syotetytArvot": []}');

INSERT INTO jarjestyskriteeritulos(arvo, nimi, prioriteetti, tila, jonosija)
SELECT 7.0, 'Testikriteeri', 0, 'HYVAKSYTTAVISSA', jonosija.id from jonosija
WHERE hakemus_oid = 'hakemus4' AND valintatapajono = '83c1c848-da64-435f-be41-23a29f4d8d99';

INSERT INTO jonosija(
    hakemus_oid, hakija_oid, hakutoiveprioriteetti, harkinnanvarainen, valintatapajono, hylatty_valisijoittelussa, syotetyt_arvot, funktio_tulokset)
VALUES ('hakemus5', 'hakija3', 1, false, '83c1c848-da64-435f-be41-23a29f4d8d99', false, '{"funktioTulokset": []}', '{"syotetytArvot": []}');

INSERT INTO jarjestyskriteeritulos(arvo, nimi, prioriteetti, tila, jonosija)
SELECT 6.0, 'Testikriteeri', 0, 'HYVAKSYTTAVISSA', jonosija.id from jonosija
WHERE hakemus_oid = 'hakemus5' AND valintatapajono = '83c1c848-da64-435f-be41-23a29f4d8d99';

INSERT INTO jonosija(
    hakemus_oid, hakija_oid, hakutoiveprioriteetti, harkinnanvarainen, valintatapajono, hylatty_valisijoittelussa, syotetyt_arvot, funktio_tulokset)
VALUES ('hakemus6', 'hakija3', 1, false, '83c1c848-da64-435f-be41-23a29f4d8d99', false, '{"funktioTulokset": []}', '{"syotetytArvot": []}');

INSERT INTO jarjestyskriteeritulos(arvo, nimi, prioriteetti, tila, jonosija)
SELECT 5.0, 'Testikriteeri', 0, 'HYVAKSYTTAVISSA', jonosija.id from jonosija
WHERE hakemus_oid = 'hakemus6' AND valintatapajono = '83c1c848-da64-435f-be41-23a29f4d8d99';