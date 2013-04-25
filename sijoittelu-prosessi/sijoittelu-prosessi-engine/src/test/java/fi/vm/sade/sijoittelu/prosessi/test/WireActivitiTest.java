/*
 * Copyright
 * *
 *  Copyright (c) 2012 The Finnish Board of Education - Opetushallitus
 *
 *  This program is free software:  Licensed under the EUPL, Version 1.1 or - as
 *  soon as they will be approved by the European Commission - subsequent versions
 *  of the EUPL (the "Licence");
 *
 *  You may not use this work except in compliance with the Licence.
 *  You may obtain a copy of the Licence at: http://www.osor.eu/eupl/
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  European Union Public Licence for more details.
 *
 */

package fi.vm.sade.sijoittelu.prosessi.test;

import org.activiti.engine.test.ActivitiRule;
import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import static org.junit.Assert.assertNotNull;

/**
 * @author Eetu Blomqvist
 */
@Ignore
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration("classpath*:test-context.xml")
public class WireActivitiTest {

    @Autowired
    @Rule
    public ActivitiRule activitiRule;

    @Test
    public void testInit(){
        assertNotNull(activitiRule.getProcessEngine());
    }
}
