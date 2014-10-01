package at.ac.tuwien.dsg.smartcom.manager.am.adapter;

import at.ac.tuwien.dsg.smartcom.adapter.OutputAdapter;
import at.ac.tuwien.dsg.smartcom.broker.MessageBroker;
import at.ac.tuwien.dsg.smartcom.broker.MessageListener;
import at.ac.tuwien.dsg.smartcom.manager.am.AddressResolver;
import at.ac.tuwien.dsg.smartcom.model.Identifier;
import at.ac.tuwien.dsg.smartcom.model.Message;
import at.ac.tuwien.dsg.smartcom.model.PeerChannelAddress;
import at.ac.tuwien.dsg.smartcom.statistic.StatisticBean;
import at.ac.tuwien.dsg.smartcom.utils.PredefinedMessageHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Execution environment for an output adapter instance. It handles automatically tasks and tells the adapter to send
 * messages to peers.
 *
 * @author Philipp Zeppezauer (philipp.zeppezauer@gmail.com)
 * @version 1.0
 */
public class OutputAdapterExecution implements MessageListener {
    private static final Logger log = LoggerFactory.getLogger(OutputAdapterExecution.class);

    protected final OutputAdapter adapter; //that is handled by this adapter execution
    protected final AddressResolver address; //used to resolve addresses of peers
    protected final Identifier id; //of the adapter
    protected final MessageBroker broker; //used to receive tasks
    protected final StatisticBean statistic;

    /**
     * Creates a new output adapter execution for an adapter and its id. The address resolver and the broker are used
     * to support the execution of the adapter.
     * @param adapter that is executed by this class
     * @param address used to resolve peer addresses for sending messages
     * @param id of the adapter
     * @param broker used to receive tasks
     * @param statistic
     */
    public OutputAdapterExecution(OutputAdapter adapter, AddressResolver address, Identifier id, MessageBroker broker, StatisticBean statistic) {
        this.adapter = adapter;
        this.address = address;
        this.id = id;
        this.broker = broker;
        this.statistic = statistic;
    }

    public OutputAdapter getAdapter() {
        return adapter;
    }

    @Override
    public void onMessage(Message message) {
        statistic.outputReceived();
        log.debug("Adapter {}: Received task {}", id, message);
        PeerChannelAddress peerChannelAddress = address.getPeerAddress(message.getReceiverId(), id);

        log.debug("Adapter {}: Sending message {} to peer {}", id, message, peerChannelAddress);
        try {
            adapter.push(message, peerChannelAddress);

            //TODO should we send an acknowledgement here?
            broker.publishControl(PredefinedMessageHelper.createAcknowledgeMessage(message));
        } catch (Exception e) {
            broker.publishControl(PredefinedMessageHelper.createCommunicationErrorMessage(message, e.getMessage()));
        }
    }
}
