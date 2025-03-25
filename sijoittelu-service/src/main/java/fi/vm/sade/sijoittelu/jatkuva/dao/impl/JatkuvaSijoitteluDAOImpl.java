package fi.vm.sade.sijoittelu.jatkuva.dao.impl;

import fi.vm.sade.sijoittelu.jatkuva.dao.JatkuvaSijoitteluDAO;

import java.time.Instant;
import java.util.*;

import fi.vm.sade.sijoittelu.jatkuva.dto.JatkuvaSijoittelu;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Service;

@Service
public class JatkuvaSijoitteluDAOImpl implements JatkuvaSijoitteluDAO {

  private final JdbcTemplate jdbcTemplate;

  private final RowMapper<JatkuvaSijoittelu> sijoitteluDtoRowMapper = (rs, rowNum) ->
      new JatkuvaSijoittelu(
          rs.getString("haku_oid"),
          rs.getBoolean("jatkuva_paalla"),
          rs.getTimestamp("viimeksi_ajettu"),
          null,
          rs.getTimestamp("aloitus"),
          rs.getInt("ajotiheys")
      );

  @Autowired
  public JatkuvaSijoitteluDAOImpl(@Qualifier("sijoitteluJdbcTemplate") JdbcTemplate jdbcTemplate) {
    this.jdbcTemplate = jdbcTemplate;
  }

  @Override
  public JatkuvaSijoittelu hae(String hakuOid) {
    try {
      return jdbcTemplate.queryForObject("SELECT haku_oid, jatkuva_paalla, viimeksi_ajettu, aloitus, ajotiheys FROM jatkuvat WHERE haku_oid=?",
          sijoitteluDtoRowMapper, hakuOid);
    } catch (EmptyResultDataAccessException e) {
      return null;
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  @Override
  public Collection<JatkuvaSijoittelu> hae() {
    try {
      return jdbcTemplate.query("SELECT haku_oid, jatkuva_paalla, viimeksi_ajettu, aloitus, ajotiheys FROM jatkuvat", sijoitteluDtoRowMapper);
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  @Override
  public JatkuvaSijoittelu merkkaaSijoittelunAjossaTila(String hakuOid, boolean tila) {
    try {
      this.jdbcTemplate.update(
          "INSERT INTO jatkuvat " +
              "(haku_oid, jatkuva_paalla, viimeksi_ajettu, aloitus, ajotiheys) " +
              "VALUES (?, ?, null, null, null) " +
              "ON CONFLICT (haku_oid) DO UPDATE SET jatkuva_paalla=?",
          hakuOid, tila, tila);
      return this.hae(hakuOid);
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  @Override
  public JatkuvaSijoittelu merkkaaSijoittelunAjetuksi(String hakuOid) {
    try {
      String now = new Date().toString();
      this.jdbcTemplate.update("INSERT INTO jatkuvat " +
              "(haku_oid, jatkuva_paalla, viimeksi_ajettu, aloitus, ajotiheys) " +
              "VALUES (?, false, ?::timestamptz, null, null) " +
              "ON CONFLICT (haku_oid) DO UPDATE SET viimeksi_ajettu=?::timestamptz",
          hakuOid, now, now);
      return this.hae(hakuOid);
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  @Override
  public void poistaSijoittelu(String hakuOid) {
    try {
      this.jdbcTemplate.update("DELETE FROM jatkuvat WHERE haku_oid=?", hakuOid);
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }



  @Override
  public void paivitaSijoittelunAloitusajankohta(
      String hakuOid, long aloitusajankohta, int ajotiheys) {
    try {
      String aloitus = Date.from(Instant.ofEpochMilli(aloitusajankohta)).toString();
      this.jdbcTemplate.update("INSERT INTO jatkuvat " +
              "(haku_oid, jatkuva_paalla, viimeksi_ajettu, aloitus, ajotiheys) " +
              "VALUES (?, false, null, ?::timestamptz, ?) " +
              "ON CONFLICT (haku_oid) DO UPDATE SET aloitus=?::timestamptz, ajotiheys=?",
          hakuOid, aloitus, ajotiheys, aloitus, ajotiheys);
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  @Override
  public void luoJatkuvaSijoittelu(
          String hakuOid, long aloitusajankohta, int ajotiheys) {
    try {
      String aloitus = Date.from(Instant.ofEpochMilli(aloitusajankohta)).toString();
      this.jdbcTemplate.update("INSERT INTO jatkuvat " +
                      "(haku_oid, jatkuva_paalla, viimeksi_ajettu, aloitus, ajotiheys) " +
                      "VALUES (?, true, null, ?::timestamptz, ?) " +
                      "ON CONFLICT (haku_oid) DO UPDATE SET aloitus=?::timestamptz, ajotiheys=?",
              hakuOid, aloitus, ajotiheys, aloitus, ajotiheys);
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }
}