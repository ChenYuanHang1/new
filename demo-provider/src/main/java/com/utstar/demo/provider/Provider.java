/*
 * Copyright (c) 2018 UTStarcom, Inc. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package com.utstar.demo.provider;

import com.google.common.base.Preconditions;
import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import java.util.Collection;
import java.util.concurrent.Future;
import org.opendaylight.controller.md.sal.binding.api.DataBroker;
import org.opendaylight.controller.md.sal.binding.api.DataObjectModification;
import org.opendaylight.controller.md.sal.binding.api.DataTreeChangeListener;
import org.opendaylight.controller.md.sal.binding.api.DataTreeIdentifier;
import org.opendaylight.controller.md.sal.binding.api.DataTreeModification;
import org.opendaylight.controller.md.sal.binding.api.NotificationPublishService;
import org.opendaylight.controller.md.sal.binding.api.ReadWriteTransaction;
import org.opendaylight.controller.md.sal.binding.api.WriteTransaction;
import org.opendaylight.controller.md.sal.common.api.data.LogicalDatastoreType;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.demo.rev181107.DataModel;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.demo.rev181107.DataModelBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.demo.rev181107.DemoService;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.demo.rev181107.GreetInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.demo.rev181107.GreetMessageBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.demo.rev181107.GreetOutput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.demo.rev181107.GreetOutputBuilder;
import org.opendaylight.yangtools.concepts.ListenerRegistration;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;
import org.opendaylight.yangtools.yang.common.RpcResult;
import org.opendaylight.yangtools.yang.common.RpcResultBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Provider implements DemoService, DataTreeChangeListener<DataModel>, AutoCloseable {

    private static final Logger logger = LoggerFactory.getLogger(Provider.class);
    private static final InstanceIdentifier<DataModel> DATAMODEL_IID =
            InstanceIdentifier.builder(DataModel.class).build();

    private final DataBroker dataBroker;
    private NotificationPublishService notificationPublishService;
    private ListenerRegistration<Provider> registerDataTreeChangeListener;
    // private final ExecutorService executor;

    public Provider(final DataBroker dataBroker, final NotificationPublishService notificationPublishService) {
        this.dataBroker = dataBroker;
        this.notificationPublishService = notificationPublishService;
        // this.executor = Executors.newFixedThreadPool(1);
    }

    // Method called when the blueprint container is created
    public void init() {
        logger.info("Provider session initializing...");
        Preconditions.checkNotNull(dataBroker, "dataBroker must be set");
        logger.info("check...");
        // register listener to data tree
        DataTreeIdentifier<DataModel> dataTreeIdentifier =
                new DataTreeIdentifier<>(LogicalDatastoreType.CONFIGURATION, DATAMODEL_IID);
        registerDataTreeChangeListener = dataBroker.registerDataTreeChangeListener(dataTreeIdentifier, this);
    }

    @Override
    public void close() throws Exception {
        logger.info("Provider session closed");
        if (registerDataTreeChangeListener != null) {
            registerDataTreeChangeListener.close();
        }
        logger.info("Provider session closed+++");

        if (dataBroker != null) {
            WriteTransaction writeTransaction = dataBroker.newWriteOnlyTransaction();
            writeTransaction.delete(LogicalDatastoreType.OPERATIONAL, DATAMODEL_IID);
            Futures.addCallback(writeTransaction.submit(), new FutureCallback<Void>() {

                @Override
                public void onSuccess(Void result) {
                    logger.debug("Successfully deleted the operational data");
                }

                @Override
                public void onFailure(Throwable throwable) {
                    logger.error("Delete of the operational data failed");
                }
            });
        }
    }

    @Override
    public void onDataTreeChanged(Collection<DataTreeModification<DataModel>> changes) {
        for (DataTreeModification<DataModel> change : changes) {
            DataObjectModification<DataModel> rootNode = change.getRootNode();
            GreetMessageBuilder messageBuilder =
                    new GreetMessageBuilder().setMessage(rootNode.getDataAfter().getAttribute1());
            switch (rootNode.getModificationType()) {
                case WRITE:
                    logger.info(
                            "WRITE: onDataTreeChanged - Demo config with path {} was added or replaced: "
                                    + "old Demo: {}, new Demo: {}",
                            change.getRootPath().getRootIdentifier(), rootNode.getDataBefore(),
                            rootNode.getDataAfter());
                    publishDataChange(rootNode, messageBuilder);
                    break;

                case SUBTREE_MODIFIED:
                    logger.info(
                            "SUBTREE_MODIFIED: onDataTreeChanged - Demo config with path {} was added or replaced: "
                                    + "old Demo: {}, new Demo: {}",
                            change.getRootPath().getRootIdentifier(), rootNode.getDataBefore(),
                            rootNode.getDataAfter());
                    publishDataChange(rootNode, messageBuilder);
                    break;

                case DELETE:
                    logger.info(
                            "DELETE: onDataTreeChanged - Demo config with path {} was added or replaced: "
                                    + "old Demo: {}, new Demo: {}",
                            change.getRootPath().getRootIdentifier(), rootNode.getDataBefore(),
                            rootNode.getDataAfter());
                    // convert data changes to nitification puplish
                    publishDataChange(rootNode, messageBuilder);
                    break;
            }
        }
    }

    @Override
    public Future<RpcResult<GreetOutput>> greet(GreetInput input) {
        GreetOutputBuilder outputBuilder = new GreetOutputBuilder();
        outputBuilder.setOutputdata(input.getInputdata());

        // Put the data to dataStore
        logger.info("Put the DataModel operational data into the MD-SAL data store");
        DataModelBuilder dataModelBuilder = new DataModelBuilder().setAttribute1(input.getInputdata());
        ReadWriteTransaction readWriteTransaction = dataBroker.newReadWriteTransaction();
        readWriteTransaction.put(LogicalDatastoreType.CONFIGURATION, DATAMODEL_IID, dataModelBuilder.build());
        Futures.addCallback(readWriteTransaction.submit(), new FutureCallback<Void>() {
            @Override
            public void onSuccess(Void result) {
                logger.info("Put the DataModel operational data into the MD-SAL data store success============");
            }

            @Override
            public void onFailure(Throwable throwable) {
                logger.error("Put the DataModel operational data into the MD-SAL data store faile", e);
            }

        });

        return RpcResultBuilder.success(outputBuilder.build()).buildFuture();
    }

    private void publishDataChange(DataObjectModification<DataModel> rootNode, GreetMessageBuilder messageBuilder) {
        try {
            notificationPublishService.putNotification(messageBuilder.build());
        } catch (InterruptedException e) {
            logger.error("WRITE:Convert data changes to notification publish error; {}", 
                    rootNode.getDataAfter(),  e);
        }
    }

}
