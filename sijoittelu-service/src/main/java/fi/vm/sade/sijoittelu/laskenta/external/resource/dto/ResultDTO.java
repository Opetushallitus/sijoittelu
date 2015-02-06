package fi.vm.sade.sijoittelu.laskenta.external.resource.dto;

/**
 * Created by kjsaila on 06/02/15.
 */
public class ResultDTO<T> {

    private T result;

    public T getResult() {
        return result;
    }

    public void setResult(T result) {
        this.result = result;
    }
}
