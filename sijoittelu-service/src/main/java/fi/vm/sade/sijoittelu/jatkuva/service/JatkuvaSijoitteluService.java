package fi.vm.sade.sijoittelu.jatkuva.service;

import fi.vm.sade.sijoittelu.jatkuva.dto.AjastettuSijoitteluInfo;

import java.util.List;

public interface JatkuvaSijoitteluService {

  List<AjastettuSijoitteluInfo> haeAjossaOlevatAjastetutSijoittelut();

  void teeJatkuvaSijoittelu();
}
