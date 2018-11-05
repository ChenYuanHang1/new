/*
 * Copyright (c) 2018 UTStarcom, Inc. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package com.utstar.demo.consumer;

import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.demo.rev150105.DemoListener;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.demo.rev150105.DemoService;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.demo.rev150105.GreetMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Consumer implements DemoListener{
    
    private static final Logger logger = LoggerFactory.getLogger(Consumer.class);
    private DemoService demoService;
    
    public Consumer(final DemoService demoService) {
        this.demoService = demoService;
    }
    
    @Override
    public void onGreetMessage(GreetMessage notification) {
        logger.info("GreetMessage:" + notification.getMessage());
    }

}
