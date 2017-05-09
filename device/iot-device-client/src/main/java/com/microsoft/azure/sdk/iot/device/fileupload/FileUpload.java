// Copyright (c) Microsoft. All rights reserved.
// Licensed under the MIT license. See LICENSE file in the project root for full license information.

package com.microsoft.azure.sdk.iot.device.fileupload;

import com.microsoft.azure.sdk.iot.device.DeviceClient;
import com.microsoft.azure.sdk.iot.device.DeviceClientConfig;
import com.microsoft.azure.sdk.iot.device.IotHubEventCallback;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.concurrent.Future;

/**
 * Provide means to upload file in the Azure Storage using the IoTHub.
 *
 *   +--------------+      +---------------+    +---------------+    +---------------+
 *   |    Device    |      |    Iot Hub    |    |    Storage    |    |    Service    |
 *   +--------------+      +---------------+    +---------------+    +---------------+
 *           |                     |                    |                    |
 *           |                     |                    |                    |
 *       REQUEST_BLOB              |                    |                    |
 *           +--- request blob --->|                    |                    |
 *           |<-- blob SAS token --+                    |                    |
 *           |                     |                    |                    |
 *       UPLOAD_FILE               |                    |                    |
 *           +---- upload file to the provided blob --->|                    |
 *           +<------ end of upload with `status` ------+                    |
 *           |                     |                    |                    |
 *       NOTIFY_IOTHUB             |                    |                    |
 *           +--- notify status -->|                    |                    |
 *           |                     +------ notify new file available ------->|
 *           |                     |                    |                    |
 *
 */
public final class FileUpload
{
    private DeviceClient client;
    private DeviceClient fileUploadClient;
    private DeviceClientConfig config;

    private enum State
    {
        REQUEST_BLOB,
        UPLOAD_FILE,
        NOTIFY_IOTHUB
    }

    public FileUpload(DeviceClient client, DeviceClientConfig config)
    {
        if(client == null)
        {
            throw new IllegalArgumentException("client is null");
        }
        if(config == null)
        {
            throw new IllegalArgumentException("config is null");
        }

        this.client = client;
        this.config = config;
    }

    public synchronized void uploadToBlobAsync(
            String blobName, String filePath,
            IotHubEventCallback statusCallback, Object statusCallbackContext)
            throws FileNotFoundException
    {
        if((blobName == null) || blobName.isEmpty())
        {
            throw new IllegalArgumentException("blobName is null or empty");
        }

        if((filePath == null) || filePath.isEmpty())
        {
            throw new IllegalArgumentException("filePath is null or empty");
        }

        uploadToBlobAsync(blobName, new FileInputStream(filePath), statusCallback, statusCallbackContext);
    }

    public synchronized void uploadToBlobAsync(String blobName, InputStream fileStream, IotHubEventCallback statusCallback, Object statusCallbackContext)
    {
        if((blobName == null) || blobName.isEmpty())
        {
            throw new IllegalArgumentException("blobName is null or empty");
        }

        if(fileStream == null)
        {
            throw new IllegalArgumentException("fileStream is null or empty");
        }
    }
}
