package fi.vm.sade.sijoittelu.domain;

import java.util.ArrayList;
import java.util.List;

import com.google.code.morphia.annotations.Embedded;

/**
 * 
 * @author Kari Kammonen
 * 
 */
@Embedded
public class Valintatapajono {

	private Tasasijasaanto tasasijasaanto;

	private ValintatapajonoTila tila;

	private String oid;

	private Integer prioriteetti;

	private Integer aloituspaikat;

	//@Embedded
	//private List<Saanto> saannot = new ArrayList<Saanto>();

	@Embedded
	private ArrayList<Hakemus> hakemukset = new ArrayList<Hakemus>();

	public ValintatapajonoTila getTila() {
		return tila;
	}

	public void setTila(ValintatapajonoTila tila) {
		this.tila = tila;
	}

	public String getOid() {
		return oid;
	}

	public void setOid(String oid) {
		this.oid = oid;
	}

	public int getPrioriteetti() {
		return prioriteetti;
	}

	public void setPrioriteetti(int prioriteetti) {
		this.prioriteetti = prioriteetti;
	}

	public int getAloituspaikat() {
		return aloituspaikat;
	}

	public void setAloituspaikat(int aloituspaikat) {
		this.aloituspaikat = aloituspaikat;
	}

	public ArrayList<Hakemus> getHakemukset() {
		return hakemukset;
	}

	//public List<Saanto> getSaannot() {
//		return saannot;
	//}

	public Tasasijasaanto getTasasijasaanto() {
		return tasasijasaanto;
	}

	public void setTasasijasaanto(Tasasijasaanto tasasijasaanto) {
		this.tasasijasaanto = tasasijasaanto;
	}
}
