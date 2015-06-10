package fi.vm.sade.sijoittelu.laskenta.external.resource.dto;

public class ParametriArvoDTO {
    private Long date;
    private Long dateStart;
    private Long dateEnd;

    public ParametriArvoDTO() {}

    public Long getDate() {
        return date;
    }

    public void setDate(Long date) {
        this.date = date;
    }

    public Long getDateStart() {
        return dateStart;
    }

    public void setDateStart(Long dateStart) {
        this.dateStart = dateStart;
    }

    public Long getDateEnd() {
        return dateEnd;
    }

    public void setDateEnd(Long dateEnd) {
        this.dateEnd = dateEnd;
    }
}
