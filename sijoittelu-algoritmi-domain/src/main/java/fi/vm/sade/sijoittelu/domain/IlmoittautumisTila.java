package fi.vm.sade.sijoittelu.domain;

public enum IlmoittautumisTila {
    EI_TEHTY,                           // Ei tehty
    LASNA_KOKO_LUKUVUOSI,               // Läsnä (koko lukuvuosi)
    POISSA_KOKO_LUKUVUOSI,              // Poissa (koko lukuvuosi)
    EI_ILMOITTAUTUNUT,                  // Ei ilmoittautunut
    LASNA_SYKSY,                        // Läsnä syksy, poissa kevät
    POISSA_SYKSY,                       // Poissa syksy, läsnä kevät
    LASNA,                              // Läsnä, keväällä alkava koulutus
    POISSA                              // Poissa, keväällä alkava koulutus
}
