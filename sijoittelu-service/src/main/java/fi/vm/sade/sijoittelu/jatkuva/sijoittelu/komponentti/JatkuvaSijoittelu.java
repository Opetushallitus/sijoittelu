package fi.vm.sade.sijoittelu.jatkuva.sijoittelu.komponentti;

import fi.vm.sade.sijoittelu.jatkuva.sijoittelu.dto.AjastettuSijoitteluInfo;

import java.util.List;

public interface JatkuvaSijoittelu {

  List<AjastettuSijoitteluInfo> haeAjossaOlevatAjastetutSijoittelut();

  void teeJatkuvaSijoittelu();
}
