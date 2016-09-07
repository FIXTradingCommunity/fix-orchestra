package org.fixtrading.orchestra.repository.messages;

import org.fixtrading.orchestra.repository.RepositoryTool;
import org.junit.Test;

/**
 * Copyright(C) 2010 MILLENNIUM IT SOFTWARE (PRIVATE) LIMITED
 * All rights reserved.
 * <p>
 * THIS IS UNPUBLISHED PROPRIETARY SOURCE CODE OF
 * MILLENNIUM IT SOFTWARE (PRIVATE) LIMITED.
 * <p>
 * This copy of the Source Code is intended for MILLENNIUM IT SOFTWARE (PRIVATE) LIMITED 's internal use only and is
 * intended for view by persons duly authorized by the management of MILLENNIUM IT SOFTWARE (PRIVATE) LIMITED. No
 * part of this file may be reproduced or distributed in any form or by any
 * means without the written approval of the Management of MILLENNIUM IT SOFTWARE (PRIVATE) LIMITED.
 * <p>
 * Created by udithaw on 9/2/16.
 */
public class RepositoryToolTest {


    @Test
    public void test01() {
        String[] strings = {"/home/udithaw/MIT/new_development/fix-orchestra/messages/src/test/resources/" +
                "FixRepository.xml", "output_file.rdf"};
        RepositoryTool.main(strings);
    }
}
