package fi.vm.sade.sijoittelu.batch.logic.impl.itutil;

import de.flapdoodle.embed.process.io.IStreamProcessor;

/**
 * 
 * Output processor for flapdoodle that does nothing. Output on large mongo
 * files is just too much, tests take ages to complete
 * 
 * @author Kari Kammonen
 * 
 */
public class DummyOutputProcessor implements IStreamProcessor {

    @Override
    public void process(String block) {
        // do nothing

    }

    @Override
    public void onProcessed() {
        // do nothing

    }

}