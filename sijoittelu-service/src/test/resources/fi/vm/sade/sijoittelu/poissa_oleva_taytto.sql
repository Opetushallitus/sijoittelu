INSERT INTO valinnanvaihe(
    id, jarjestysnumero, haku_oid, hakukohde_oid, valinnanvaihe_oid, tarjoaja_oid, nimi)
VALUES ('68401760-bd8d-4e79-bae5-67d3c341b94a', 1, 'haku1', 'hakukohde1', 'vaihe1', '1.2.246.562.10.28342991297', 'Varsinainen valinta');

INSERT INTO valintatapajono(
    id, valintatapajono_oid, nimi, prioriteetti, aloituspaikat, siirretaan_sijoitteluun, tasasijasaanto, ei_varasijatayttoa, valmis_sijoiteltavaksi, valinnanvaihe, poissa_oleva_taytto)
VALUES ('83c1c848-da64-435f-be41-23a29f4d8d97', 'jono1', 'Pisteet', 0, 5, true, 'ARVONTA', false, true, '68401760-bd8d-4e79-bae5-67d3c341b94a', true);

INSERT INTO jonosija(
    hakemus_oid, hakija_oid, hakutoiveprioriteetti, harkinnanvarainen, valintatapajono, hylatty_valisijoittelussa, syotetyt_arvot, funktio_tulokset)
VALUES ('oid1', '1.2.246.562.24.34552787533', 1, false, '83c1c848-da64-435f-be41-23a29f4d8d97', false, '{"funktioTulokset": []}', '{"syotetytArvot": []}');

INSERT INTO jarjestyskriteeritulos(arvo, nimi, prioriteetti, tila, jonosija)
SELECT 50.0, 'Testikriteeri', 0, 'HYVAKSYTTAVISSA', jonosija.id from jonosija
WHERE hakemus_oid = 'oid1';

INSERT INTO jonosija(
    hakemus_oid, hakija_oid, hakutoiveprioriteetti, harkinnanvarainen, valintatapajono, hylatty_valisijoittelussa, syotetyt_arvot, funktio_tulokset)
VALUES ('oid2', '1.2.246.562.24.34552787533', 1, false, '83c1c848-da64-435f-be41-23a29f4d8d97', false, '{"funktioTulokset": []}', '{"syotetytArvot": []}');

INSERT INTO jarjestyskriteeritulos(arvo, nimi, prioriteetti, tila, jonosija)
SELECT 40.0, 'Testikriteeri', 0, 'HYVAKSYTTAVISSA', jonosija.id from jonosija
WHERE hakemus_oid = 'oid2';

INSERT INTO jonosija(
    hakemus_oid, hakija_oid, hakutoiveprioriteetti, harkinnanvarainen, valintatapajono, hylatty_valisijoittelussa, syotetyt_arvot, funktio_tulokset)
VALUES ('oid3', '1.2.246.562.24.34552787533', 1, false, '83c1c848-da64-435f-be41-23a29f4d8d97', false, '{"funktioTulokset": []}', '{"syotetytArvot": []}');

INSERT INTO jarjestyskriteeritulos(arvo, nimi, prioriteetti, tila, jonosija)
SELECT 30.0, 'Testikriteeri', 0, 'HYVAKSYTTAVISSA', jonosija.id from jonosija
WHERE hakemus_oid = 'oid3';

INSERT INTO jonosija(
    hakemus_oid, hakija_oid, hakutoiveprioriteetti, harkinnanvarainen, valintatapajono, hylatty_valisijoittelussa, syotetyt_arvot, funktio_tulokset)
VALUES ('oid4', '1.2.246.562.24.34552787533', 1, false, '83c1c848-da64-435f-be41-23a29f4d8d97', false, '{"funktioTulokset": []}', '{"syotetytArvot": []}');

INSERT INTO jarjestyskriteeritulos(arvo, nimi, prioriteetti, tila, jonosija)
SELECT 20.0, 'Testikriteeri', 0, 'HYVAKSYTTAVISSA', jonosija.id from jonosija
WHERE hakemus_oid = 'oid4';

INSERT INTO jonosija(
    hakemus_oid, hakija_oid, hakutoiveprioriteetti, harkinnanvarainen, valintatapajono, hylatty_valisijoittelussa, syotetyt_arvot, funktio_tulokset)
VALUES ('oid5', '1.2.246.562.24.34552787533', 1, false, '83c1c848-da64-435f-be41-23a29f4d8d97', false, '{"funktioTulokset": []}', '{"syotetytArvot": []}');

INSERT INTO jarjestyskriteeritulos(arvo, nimi, prioriteetti, tila, jonosija)
SELECT 10.0, 'Testikriteeri', 0, 'HYVAKSYTTAVISSA', jonosija.id from jonosija
WHERE hakemus_oid = 'oid5';

INSERT INTO jonosija(
    hakemus_oid, hakija_oid, hakutoiveprioriteetti, harkinnanvarainen, valintatapajono, hylatty_valisijoittelussa, syotetyt_arvot, funktio_tulokset)
VALUES ('oid6', '1.2.246.562.24.34552787533', 1, false, '83c1c848-da64-435f-be41-23a29f4d8d97', false, '{"funktioTulokset": []}', '{"syotetytArvot": []}');

INSERT INTO jarjestyskriteeritulos(arvo, nimi, prioriteetti, tila, jonosija)
SELECT 9.0, 'Testikriteeri', 0, 'HYVAKSYTTAVISSA', jonosija.id from jonosija
WHERE hakemus_oid = 'oid6';

INSERT INTO jonosija(
    hakemus_oid, hakija_oid, hakutoiveprioriteetti, harkinnanvarainen, valintatapajono, hylatty_valisijoittelussa, syotetyt_arvot, funktio_tulokset)
VALUES ('oid7', '1.2.246.562.24.34552787533', 1, false, '83c1c848-da64-435f-be41-23a29f4d8d97', false, '{"funktioTulokset": []}', '{"syotetytArvot": []}');

INSERT INTO jarjestyskriteeritulos(arvo, nimi, prioriteetti, tila, jonosija)
SELECT 9.0, 'Testikriteeri', 0, 'HYVAKSYTTAVISSA', jonosija.id from jonosija
WHERE hakemus_oid = 'oid7';

INSERT INTO jonosija(
    hakemus_oid, hakija_oid, hakutoiveprioriteetti, harkinnanvarainen, valintatapajono, hylatty_valisijoittelussa, syotetyt_arvot, funktio_tulokset)
VALUES ('oid8', '1.2.246.562.24.34552787533', 1, false, '83c1c848-da64-435f-be41-23a29f4d8d97', false, '{"funktioTulokset": []}', '{"syotetytArvot": []}');

INSERT INTO jarjestyskriteeritulos(arvo, nimi, prioriteetti, tila, jonosija)
SELECT 9.0, 'Testikriteeri', 0, 'HYVAKSYTTAVISSA', jonosija.id from jonosija
WHERE hakemus_oid = 'oid8';

INSERT INTO jonosija(
    hakemus_oid, hakija_oid, hakutoiveprioriteetti, harkinnanvarainen, valintatapajono, hylatty_valisijoittelussa, syotetyt_arvot, funktio_tulokset)
VALUES ('oid9', '1.2.246.562.24.34552787533', 1, false, '83c1c848-da64-435f-be41-23a29f4d8d97', false, '{"funktioTulokset": []}', '{"syotetytArvot": []}');

INSERT INTO jarjestyskriteeritulos(arvo, nimi, prioriteetti, tila, jonosija)
SELECT 8.0, 'Testikriteeri', 0, 'HYVAKSYTTAVISSA', jonosija.id from jonosija
WHERE hakemus_oid = 'oid9';


INSERT INTO jonosija(
    hakemus_oid, hakija_oid, hakutoiveprioriteetti, harkinnanvarainen, valintatapajono, hylatty_valisijoittelussa, syotetyt_arvot, funktio_tulokset)
VALUES ('oid10', '1.2.246.562.24.34552787533', 1, false, '83c1c848-da64-435f-be41-23a29f4d8d97', false, '{"funktioTulokset": []}', '{"syotetytArvot": []}');

INSERT INTO jarjestyskriteeritulos(arvo, nimi, prioriteetti, tila, jonosija)
SELECT 7.0, 'Testikriteeri', 0, 'HYVAKSYTTAVISSA', jonosija.id from jonosija
WHERE hakemus_oid = 'oid10';
