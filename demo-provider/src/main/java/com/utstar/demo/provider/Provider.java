/*
 * Copyright (c) 2018 UTStarcom, Inc. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package com.utstar.demo.provider;

import java.util.Collection;
import java.util.concurrent.Future;
import javax.net.ssl.SSLSession;
import org.apache.commons.lang3.ObjectUtils;
import org.opendaylight.controller.md.sal.binding.api.DataBroker;
import org.opendaylight.controller.md.sal.binding.api.DataTreeChangeListener;
import org.opendaylight.controller.md.sal.binding.api.DataTreeIdentifier;
import org.opendaylight.controller.md.sal.binding.api.DataTreeModification;
import org.opendaylight.controller.md.sal.binding.api.NotificationPublishService;
import org.opendaylight.controller.md.sal.common.api.data.LogicalDatastoreType;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.demo.rev150105.DataModel;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.demo.rev150105.DemoService;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.demo.rev150105.GreetInput;
import org.opendaylight.yangtools.concepts.ListenerRegistration;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;
import org.opendaylight.yangtools.yang.common.RpcResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Provider implements DemoService, DataTreeChangeListener<DataModel>, AutoCloseable{
    
    private static final Logger logger = LoggerFactory.getLogger(Provider.class);
    private static final InstanceIdentifier<DataModel> DATAMODEL_IID = InstanceIdentifier.builder(DataModel.class).build();
    
    private DataBroker dataBroker;
    private NotificationPublishService notificationPublishService;
    private ListenerRegistration<Provider> registerDataTreeChangeListener;
    
    public Provider(final DataBroker dataBroker, final NotificationPublishService notificationPublishService) {
        this.dataBroker = dataBroker;
        this.notificationPublishService = notificationPublishService;
    }
    
    //Method called when the blueprint container is created
    public void init() {
        logger.info("Provider session init");
        //register listener to data tree
        DataTreeIdentifier<DataModel> dataTreeIdentifier = new DataTreeIdentifier<>(LogicalDatastoreType.CONFIGURATION, DATAMODEL_IID);
        registerDataTreeChangeListener = dataBroker.registerDataTreeChangeListener(dataTreeIdentifier, this);
    }
    
    @Override
    public void close() throws Exception {
        logger.info("Provider session closed");
        if(registerDataTreeChangeListener != null) {
            registerDataTreeChangeListener.close();
        }
        logger.info("Provider session closed+++");
    }
    @Override
    public void onDataTreeChanged(Collection<DataTreeModification<DataModel>> arg0) {
        // TODO Auto-generated method stub
        
    }
    @Override
    public Future<RpcResult<Void>> greet(GreetInput input) {
        // TODO Auto-generated method stub
        return null;
    }



}
