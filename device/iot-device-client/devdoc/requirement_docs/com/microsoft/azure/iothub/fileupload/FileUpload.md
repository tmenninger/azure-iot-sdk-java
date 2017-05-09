# FileUpload Requirements

## Overview

Provide means to upload file in the Azure Storage using the IoTHub.

## References

## Exposed API

```java
public final class FileUpload
{
    public FileUpload(DeviceIO client, DeviceClientConfig config);
    
    public synchronized void uploadToBlobAsync(
            String blobName, String filePath, 
            IotHubEventCallback statusCallback, Object statusCallbackContext)
            throws FileNotFoundException;
    
    public synchronized void UploadToBlobAsync(
            String blobName, InputStream fileStream, 
            IotHubEventCallback statusCallback, Object statusCallbackContext);
}
```


### FileUpload
```java
public FileUpload(DeviceIO client, DeviceClientConfig config)
```
**SRS_FILEUPLOAD_21_001: [**The constructor shall create a instance of the FileUpload class.**]**  
**SRS_FILEUPLOAD_21_002: [**If the provided `client` or `config` is null, the constructor shall throw IllegalArgumentException.**]**  
**SRS_FILEUPLOAD_21_003: [**The constructor shall create a new instance of `DeviceIO` with the same connectionString of the provided `client` and HTTPS as the protocol.**]**  
**SRS_FILEUPLOAD_21_004: [**The constructor shall store the new instance of `DeviceIO` as the file upload iothub client.**]**  
**SRS_FILEUPLOAD_21_005: [**If the constructor fail to create the new instance of the `DeviceIO`, it shall throw IOException.**]**  
**SRS_FILEUPLOAD_21_006: [**The constructor shall save the file upload message callback as `OnBlobRequestResponse`, by calling setFileUploadMessageCallback, where any further messages for file upload shall be delivered.**]**  
 
 
### UploadToBlobAsync
```java
public synchronized void uploadToBlobAsync(
        String blobName, String filePath, 
        IotHubEventCallback statusCallback, Object statusCallbackContext)
        throws FileNotFoundException
```
**SRS_FILEUPLOAD_21_007: [**The uploadToBlobAsync shall asynchronously upload the `filePath` file into the `blobName` blob, calling the uploadToBlobAsync with a InputStream.**]**  
**SRS_FILEUPLOAD_21_008: [**If the `filePath` or the `blobName` is null or empty, the uploadToBlobAsync shall throw IllegalArgumentException.**]**  
**SRS_FILEUPLOAD_21_009: [**If the file `filePath` do not exists, the uploadToBlobAsync shall throw FileNotFoundException.**]**  
**SRS_FILEUPLOAD_21_010: [**The uploadToBlobAsync shall create a InputStream for the `filePath`.**]**  
**SRS_FILEUPLOAD_21_011: [**If uploadToBlobAsync failed to create the InputStream, it shall throw IllegalArgumentException.**]**  


### UploadToBlobAsync
```java
public synchronized void uploadToBlobAsync(String blobName, InputStream fileStream, IotHubEventCallback statusCallback, Object statusCallbackContext)
```
**SRS_FILEUPLOAD_21_012: [**The uploadToBlobAsync shall asynchronously upload the `fileStream` file into the `blobName` blob.**]**  
**SRS_FILEUPLOAD_21_013: [**If the `fileStream` is null, the uploadToBlobAsync shall throw IllegalArgumentException.**]**  
**SRS_FILEUPLOAD_21_014: [**If the `blobName` is null or empty, the uploadToBlobAsync shall throw IllegalArgumentException.**]**  
**SRS_FILEUPLOAD_21_015: [**If the `statusCallback` is null, the uploadToBlobAsync shall throw IllegalArgumentException.**]**  
**SRS_FILEUPLOAD_21_016: [**The uploadToBlobAsync shall create a context to control this file upload, and store the `blobName`, `fileStream`, `statusCallback`, and the `statusCallbackContext`.**]**  
**SRS_FILEUPLOAD_21_017: [**The uploadToBlobAsync shall open the connection with the IotHub using the created client.**]**  
**SRS_FILEUPLOAD_21_018: [**If open connection fail, the uploadToBlobAsync shall throw IOException.**]**  
**SRS_FILEUPLOAD_21_019: [**The uploadToBlobAsync shall send the blob request to the iothub, using the client sendEventAsync.**]**  
Json request example:
```json
{ 
    "blobName": "[name of the file for which a SAS URI will be generated]" 
} 
```
**SRS_FILEUPLOAD_21_020: [**The uploadToBlobAsync shall set the message status callback as `OnBlobRequestStatus`.**]**  
**SRS_FILEUPLOAD_21_021: [**If send throw an exception, the uploadToBlobAsync shall throw IOException.**]**  


### OnBlobRequestStatus
```java
private final class OnBlobRequestStatus implements IotHubEventCallback
{
    @Override
    void execute(IotHubStatusCode responseStatus, Object callbackContext);
};
```
**SRS_FILEUPLOAD_21_022: [**If there is no file upload context for the `callbackContext`, the OnBlobRequestStatus shall throw IOException.**]**  
**SRS_FILEUPLOAD_21_023: [**If the iothub do not accept the request, the OnBlobRequestStatus shall call the `statusCallback` forwarding the status in the response, close the iothub connection, and delete the file upload context.**]**  


### OnBlobRequestResponse
```java
private final class OnBlobRequestResponse implements MessageCallback
{
    @Override
    public IotHubMessageResult execute(Message message, Object callbackContext);
};
```
**SRS_FILEUPLOAD_21_024: [**If there is no file upload context for the `callbackContext`, the OnBlobRequestResponse shall throw IOException.**]**  
**SRS_FILEUPLOAD_21_025: [**If the iothub accepts the request, it shall provide a blob information with a correlationId.**]**  
Json response example:
```json
{ 
    "correlationId": "somecorrelationid", 
    "hostname": "contoso.azure-devices.net", 
    "containerName": "testcontainer", 
    "blobName": "test-device1/image.jpg", 
    "sasToken": "1234asdfSAStoken" 
} 
```
**SRS_FILEUPLOAD_21_026: [**The OnBlobRequestResponse shall parse and store the content of the response in the file upload context.**]**  
**SRS_FILEUPLOAD_21_027: [**The OnBlobRequestResponse shall create a thread `UploadToCloudTask` to upload the stream to the blob using `CloudBlockBlob`.**]**  


### UploadToCloudTask
```java
public final class UploadToCloudTask implements Runnable
{
    public UploadToCloudTask(BlobContext context);
    public void run();
}
```
**SRS_FILEUPLOAD_21_028: [**The UploadToCloudTask shall create a `CloudBlockBlob` using the blob information in the response.**]**  
**SRS_FILEUPLOAD_21_029: [**If the upload to blob throw an exception, the UploadToCloudTask shall call the `statusCallback` reporting an error status, close the iothub connection, and delete the file upload context.**]**  
**SRS_FILEUPLOAD_21_030: [**The UploadToCloudTask shall send the notification to the iothub reporting upload succeed or failed, using the client sendEventAsync.**]**  
Json notification example:
```json
{ 
    "correlationId": "[correlation ID returned by the initial request]", 
    "isSuccess": True, 
    "statusCode": 1234, 
    "statusDescription": "Description of the status" 
} 
```
**SRS_FILEUPLOAD_21_031: [**If send throw an exception, the UploadToCloudTask shall call the `statusCallback` reporting an error status, close the iothub connection, and delete the file upload context.**]**  


### OnBlobNotificationStatus
```java
private final class OnBlobNotificationStatus implements IotHubEventCallback
{
    @Override
    void execute(IotHubStatusCode responseStatus, Object callbackContext);
}
```
**SRS_FILEUPLOAD_21_032: [**If there is no file upload context for the `callbackContext`, the OnBlobNotificationStatus shall throw IOException.**]**  
**SRS_FILEUPLOAD_21_033: [**The OnBlobNotificationStatus shall call the `statusCallback` forwarding the status in the response.**]**  
**SRS_FILEUPLOAD_21_034: [**The OnBlobNotificationStatus shall close the connection with the IotHub.**]**  
**SRS_FILEUPLOAD_21_035: [**The OnBlobNotificationStatus shall destroy the context to control this file upload.**]**  

