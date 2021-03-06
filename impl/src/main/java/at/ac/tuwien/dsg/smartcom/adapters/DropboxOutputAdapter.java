/**
 * Copyright (c) 2014 Technische Universitat Wien (TUW), Distributed Systems Group E184 (http://dsg.tuwien.ac.at)
 *
 * This work was partially supported by the EU FP7 FET SmartSociety (http://www.smart-society-project.eu/).
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */
package at.ac.tuwien.dsg.smartcom.adapters;

import at.ac.tuwien.dsg.smartcom.adapter.OutputAdapter;
import at.ac.tuwien.dsg.smartcom.adapter.annotations.Adapter;
import at.ac.tuwien.dsg.smartcom.adapter.exception.AdapterException;
import at.ac.tuwien.dsg.smartcom.adapters.dropbox.DropboxClientUtils;
import at.ac.tuwien.dsg.smartcom.model.Message;
import at.ac.tuwien.dsg.smartcom.model.PeerChannelAddress;
import com.dropbox.core.DbxException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;

/**
 * @author Philipp Zeppezauer (philipp.zeppezauer@gmail.com)
 * @version 1.0
 */
@Adapter(name = "Dropbox", stateful = true)
public class DropboxOutputAdapter implements OutputAdapter {
    private static final Logger log = LoggerFactory.getLogger(DropboxOutputAdapter.class);

    private final String path;
    private final DropboxClientUtils client;

    public DropboxOutputAdapter(PeerChannelAddress address) throws AdapterException {
        client = new DropboxClientUtils((String) address.getContactParameters().get(0));

        path = (String) address.getContactParameters().get(1);
        try {
            log.debug("Linked account: " + client.getAccount());
        } catch (DbxException e) {
            log.error("Error while accessing account information of dropbox client!", e);
            throw new AdapterException("Could not create Adapter: "+e.getLocalizedMessage());
        }
    }

    @Override
    public void push(Message message, PeerChannelAddress address) throws AdapterException {
        File taskFile = null;
        try {
            taskFile = File.createTempFile("task_"+message.getConversationId(), "task");
            FileWriter writer = new FileWriter(taskFile);

            writer.write(messageToString(message));
            writer.close();
        } catch (IOException e) {
            log.error("Could not create temporary file 'task_{}", message.getConversationId());
            throw new AdapterException("Could not create temporary file");
        }

        FileInputStream inputStream = null;
        try {
            inputStream = new FileInputStream(taskFile);
        } catch (FileNotFoundException e) {
            throw new AdapterException("Could not create temporary file");
        }
        try {
            client.uploadFile(path, "task_"+message.getConversationId()+".task", taskFile.length(), inputStream);
        } catch (IOException | DbxException e) {
            throw new AdapterException("Could not upload file!");
        } finally {
            try {
                inputStream.close();
            } catch (IOException ignored) {
            }
        }
    }

    private String messageToString(Message message) {
        return message.toString();
    }
}
