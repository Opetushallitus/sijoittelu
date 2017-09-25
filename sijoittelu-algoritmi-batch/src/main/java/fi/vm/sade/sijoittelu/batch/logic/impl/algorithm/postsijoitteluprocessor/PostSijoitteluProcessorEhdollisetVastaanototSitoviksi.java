package fi.vm.sade.sijoittelu.batch.logic.impl.algorithm.postsijoitteluprocessor;

import fi.vm.sade.sijoittelu.batch.logic.impl.algorithm.wrappers.HakemusWrapper;
import fi.vm.sade.sijoittelu.batch.logic.impl.algorithm.wrappers.SijoitteluajoWrapper;
import fi.vm.sade.sijoittelu.batch.logic.impl.algorithm.wrappers.ValintatapajonoWrapper;
import fi.vm.sade.sijoittelu.domain.ValintatuloksenTila;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Stream;

public class PostSijoitteluProcessorEhdollisetVastaanototSitoviksi implements PostSijoitteluProcessor {
    private static final Logger LOG = LoggerFactory.getLogger(PostSijoitteluProcessorEhdollisetVastaanototSitoviksi.class);

    @Override
    public void process(SijoitteluajoWrapper sijoitteluAjo) {
        Function<HakemusWrapper, Boolean> onkoYlempiaHakutoiveitaVaralla = (hakemus) ->
            hakemus.getYlemmatTaiSamanarvoisetMuttaKorkeammallaJonoPrioriteetillaOlevatHakutoiveet()
                .filter(HakemusWrapper::isVaralla)
                .filter(h0 -> !sijoitteluAjo.onkoVarasijaSaannotVoimassaJaVarasijaTayttoPaattynyt(h0.getValintatapajono()))
                .findAny().isPresent();

        Consumer<ValintatapajonoWrapper> muutaEhdollisetVastaanototSitoviksi = (valintatapajono) ->
            valintatapajono.ehdollisestiVastaanottaneetJonossa()
                .filter(h -> !onkoYlempiaHakutoiveitaVaralla.apply(h))
                .forEach(h -> h.getValintatulos().ifPresent(v -> {
                    v.setTila(ValintatuloksenTila.VASTAANOTTANUT_SITOVASTI, "Ehdollinen vastaanotto ylimmässä mahdollisessa hakutoiveessa sitovaksi");
                    sijoitteluAjo.addMuuttuneetValintatulokset(v);
                }));

        if (sijoitteluAjo.isKKHaku()) {
            sijoitteluAjo.getHakukohteet().forEach(h -> h.getValintatapajonot().forEach(j -> muutaEhdollisetVastaanototSitoviksi.accept(j)));
        }
    }
}
