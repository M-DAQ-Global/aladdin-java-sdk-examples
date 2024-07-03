# aladdin-java-sdk-examples

---

## Prerequisites

---
Please make sure that you have access to the following key pieces of information before proceeding:

- A private-public key pair of your creation. The public key should be shared with and signed by M-DAQ. This private key
  should be used with the SDK.
- The account and API key provided by M-DAQ (as part of the client onboarding process, you should be receiving these
  details).
- The Aladdin endpoint details.

## Importing the SDK

---
For Gradle, add the following to your `build.gradle.kts` file:

```kotlin
dependencies {
    implementation("com.m-daq:mdaq-aladdin-java-sdk:0.1.0")
}
```

For Maven, add the following to your 'pom.xml' file:
```xml

<dependency>
    <groupId>com.m-daq</groupId>
    <artifactId>mdaq-aladdin-java-sdk</artifactId>
    <version>0.1.0</version>
</dependency>
```

## Configuring the Aladdin Client

```kotlin
import com.mdaq.aladdin.sdk.AladdinClientBuilder

val privateKeyPath = "FILE PATH FOR YOUR PRIVATE KEY"
val apiKey = "YOUR API KEY"
val basePath = "BASE URL FOR ALADDIN"

val aladdinClient = AladdinClientBuilder()
    .withBasePath(basePath)
    .withApiKey(apiKey)
    .withPrivateKeyFileName(privateKeyPath)
    .build()
```

### Parameters

| Method                                                                   | Description                                                                |
|--------------------------------------------------------------------------|----------------------------------------------------------------------------|
| withBasePath(String basePath)                                            | Sets the base URL for the Aladdin API.                                     |
| withPrivateKey(String privateKey)                                        | Sets the private key as string for authentication.                         |
| withPrivateKeyFileName(String privateKeyFileName)                        | Sets the file path to the private key file for authentication.             |
| withApiKey(String apiKey)                                                | Sets the API key for authentication.                                       |
| withRetryOnConnectionFailure(Boolean retryOnConnectionFailure)           | Enables or disables retrying on connection failure.                        |
| withConnectTimeout(Long connectTimeout)                                  | Sets the connect timeout value in seconds.                                 |
| withReadTimeout(Long readTimeout)                                        | Sets the read timeout value in seconds.                                    |
| withWriteTimeout(Long writeTimeout)                                      | Sets the write timeout value in seconds.                                   |
| withRetryCountOnServerError(Int retryCountOnServerError)                 | Sets the maximum number of retries on server error.                        |
| withRetryDelayMsOnServerError(Long retryDelayMsOnServerError)            | Sets the delay in milliseconds before retrying on server error.            |
| withRetryBackoffFactorOnServerError(Int retryBackoffFactorOnServerError) | Sets the backoff factor for exponential backoff on server error.           |
| withApiVersion(VersionHeaderInterceptor.APIVersion apiVersion)           | Sets the version of the Aladdin API to use. Default is V4.                 |
| build()                                                                  | Builds and returns an AladdinClient instance with the configured settings. |

## Use cases

### Submit advices (Sync)

Submits a list of advice synchronously.

```kotlin
val adviceRequestSync = Advice.AdviceBuilder()
    .adviceId(uuid)
    .transactionId(uuid)
    .adviceType(AdviceType.OA)
    .ccyPair(pricingSheet.ccyPair)
    .transactionCcy(pricingSheet.ccyPair!!.split("/")[0])
    .transactionCcyType(pricingSheet.transactionCcyType)
    .requestedPricingRefId(pricingSheet.pricingReferenceId)
    .transactionType(TransactionType.SALE)
    .paymentProvider("VISA")
    .transactionTimestamp(currentTime)
    .amount(BigDecimal(10))
    .build()

val advices = listOf(adviceRequestSync)
val response = aladdinClient.submitAdvicesSync(advices)
```

#### Request

The advice can be built using `AdviceBuilder()`

| Field                 | Description                                                                                                                                                                                                |
|-----------------------|------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| adviceType            | Defines whether is it an order advice (OA) or execution advice (EA). <br/>Enum: `OA` `EA`                                                                                                                  |
| adviceId              | Unique ID to identify a record in the order advice or the execution advice.                                                                                                                                |
| transactionId         | Client creates this Id. It is shared by Order Advice and Execution Advices(Sale/Refund/…) that belong to the same transaction.                                                                             |
| relatedAdviceId       | This field can be used for tracing through the related transactions.                                                                                                                                       |
| ccyPair               | CcyPair as stated in the pricing sheet.                                                                                                                                                                    |
| amount                | The fixed amount of the transaction.                                                                                                                                                                       |
| transactionCcy        | The currency of the fixed amount.                                                                                                                                                                          |
| consumerCcy           | The currency to be sold, if different from transactionCcy.                                                                                                                                                 |
| transactionCcyType    | Identify the type of transaction currency. <br/>Enum: `DELIV` `NDF` `DTSB`                                                                                                                                 |
| transactionType       | The underlying transaction type of this transaction. Identifies whether to buy or sell the transaction currency from the client perspective.  <br/>Enum: `SALE` `REFUND` `CHARGEBACK` `CHARGEBACK_REVERSE` |
| scenario              | The nature of the transaction. <br/>Enum: `ONLINE_PAYMENT` `OFFLINE_PAYMENT` `CONVERSION` `REMITTANCE`                                                                                                     |
| settlementAmount      | For NDF/DTSB scenario Only. The total amount of the transaction from the Payment Provider.                                                                                                                 |
| settlementCcy         | The currency of the settlement amount for NDF/DTSB scenario.                                                                                                                                               |
| paymentProvider       | The payment provider for the transaction                                                                                                                                                                   |
| transactionTimestamp  | The time which the transaction (e.g. sale, refund, chargeback, chargeback reverse etc) took placed                                                                                                         |
| requestedPricingRefId | The identifier to the pricing sheet that client wants to use for this transaction                                                                                                                          |
| beneficiary           | This field is to identify client’s beneficiary, e.g. end merchant to pay to                                                                                                                                |
| clientRef             | Pass through information from client                                                                                                                                                                       |

#### Response

`BatchAdviceSubmitResponse`

| Field   | Description                                                                    |
|---------|--------------------------------------------------------------------------------|
| message | Indicates whether the request is successful.                                   |
| batchId | Unique Id to identify a batch.                                                 | 
| advices | Contains all the responses to each advice requested. Return a list of `Advice` |

`Advice`

| Field                 | Description                                                                                                       |
|-----------------------|-------------------------------------------------------------------------------------------------------------------|
| adviceId              | Unique Id to identify an advice.                                                                                  |
| batchId               | Unique Id to identify a batch.                                                                                    |
| adviceType            | Defines whether is it an order advice (OA) or execution advice (EA).<br/>Enums: `OA` `EA`                         |    
| transactionId         | Same transaction Id as provided in the advice message.                                                            |
| accountName           | Unique account name to allow Aladdin identifies the client. M-DAQ provides this identifier.                       |
| ccyPair               | Currency pair with slash as stated in the pricing sheet.                                                          |
| transactionCcy        | The currency of the transaction.                                                                                  |
| consumerCcy           | The consumer ccy.                                                                                                 |
| transactionCcyType    | Identifies the type of Transaction currency.<br/>Enums: `DELIV` `NDF` `DTSB`                                      |
| amount                | The total amount of the transaction.                                                                              |
| transactionType       | The underlying transaction type of this transaction.<br/>Enums: `SALE` `REFUND` `CHARGEBACK` `CHARGEBACK_REVERSE` |
| scenario              | The nature of the transaction. <br/>Enums: `ONLINE_PAYMENT` `OFFLINE_PAYMENT` `CONVERSION` `REMITTANCE`           |
| settlementAmount      | For NDF/DTSB scenario. The total amount of the transaction from the Payment Provider.                             |
| settlementCcy         | The currency of the settlement amount.                                                                            |
| paymentProvider       | The payment provider for the transaction.                                                                         |
| transactionTimestamp  | The time which the transaction (e.g. sale, refund, chargeback, chargeback reverse etc) took placed.               |
| requestedPricingRefId | The identifier to the pricing sheet.                                                                              |
| beneficiary           | This field is to identify client’s beneficiary, e.g. end merchant to pay to.                                      |
| clientRef             | Pass through information from client in JSON format.                                                              |
| status                | Indicate the status of the advice.<br/>Enums: `VALID` `INVALID` `IN_PROGRESS`                                     |

### Submit advices (Async)

Submits a list of advice asynchronously.

```kotlin
val adviceRequestAsync = Advice.AdviceBuilder()
    .adviceId(uuid)
    .transactionId(uuid)
    .adviceType(AdviceType.OA)
    .ccyPair(pricingSheet.ccyPair)
    .transactionCcy(pricingSheet.ccyPair!!.split("/")[0])
    .transactionCcyType(pricingSheet.transactionCcyType)
    .requestedPricingRefId(pricingSheet.pricingReferenceId)
    .transactionType(TransactionType.SALE)
    .paymentProvider("VISA")
    .transactionTimestamp(currentTime)
    .amount(BigDecimal(10))
    .build()

val advices = listOf(adviceRequestAsync)
val response = aladdinClient.submitAdvicesAsync(advices)
```

#### Request

The advice can be built using `AdviceBuilder()`

| Field                 | Description                                                                                                                                                                                                |
|-----------------------|------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| adviceType            | Defines whether is it an order advice (OA) or execution advice (EA). <br/>Enum: `OA` `EA`                                                                                                                  |
| adviceId              | Unique ID to identify a record in the order advice or the execution advice.                                                                                                                                |
| transactionId         | Client creates this Id. It is shared by Order Advice and Execution Advices(Sale/Refund/…) that belong to the same transaction.                                                                             |
| relatedAdviceId       | This field can be used for tracing through the related transactions.                                                                                                                                       |
| ccyPair               | CcyPair as stated in the pricing sheet.                                                                                                                                                                    |
| amount                | The fixed amount of the transaction.                                                                                                                                                                       |
| transactionCcy        | The currency of the fixed amount.                                                                                                                                                                          |
| consumerCcy           | The currency to be sold, if different from transactionCcy.                                                                                                                                                 |
| transactionCcyType    | Identify the type of transaction currency. <br/>Enum: `DELIV` `NDF` `DTSB`                                                                                                                                 |
| transactionType       | The underlying transaction type of this transaction. Identifies whether to buy or sell the transaction currency from the client perspective.  <br/>Enum: `SALE` `REFUND` `CHARGEBACK` `CHARGEBACK_REVERSE` |
| scenario              | The nature of the transaction. <br/>Enum: `ONLINE_PAYMENT` `OFFLINE_PAYMENT` `CONVERSION` `REMITTANCE`                                                                                                     |
| settlementAmount      | For NDF/DTSB scenario Only. The total amount of the transaction from the Payment Provider.                                                                                                                 |
| settlementCcy         | The currency of the settlement amount for NDF/DTSB scenario.                                                                                                                                               |
| paymentProvider       | The payment provider for the transaction.                                                                                                                                                                  |
| transactionTimestamp  | The time which the transaction (e.g. sale, refund, chargeback, chargeback reverse etc) took placed.                                                                                                        |
| requestedPricingRefId | The identifier to the pricing sheet that client wants to use for this transaction.                                                                                                                         |
| beneficiary           | This field is to identify client’s beneficiary, e.g. end merchant to pay to.                                                                                                                               |
| clientRef             | Pass through information from client.                                                                                                                                                                      |

#### Response

`BatchAdviceSubmitResponse`

| Field   | Description                                                                    |
|---------|--------------------------------------------------------------------------------|
| message | Indicates whether the request is successful.                                   |
| batchId | Unique Id to identify a batch.                                                 | 
| advices | Contains all the responses to each advice requested. Return a list of `Advice` |

`Advice`

| Field                 | Description                                                                                                       |
|-----------------------|-------------------------------------------------------------------------------------------------------------------|
| adviceId              | Unique Id to identify an advice.                                                                                  |
| batchId               | Unique Id to identify a batch.                                                                                    |
| adviceType            | Defines whether is it an order advice (OA) or execution advice (EA).<br/>Enums: `OA` `EA`                         |    
| transactionId         | Same transaction Id as provided in the advice message.                                                            |
| accountName           | Unique account name to allow Aladdin identifies the client. M-DAQ provides this identifier.                       |
| ccyPair               | Currency pair with slash as stated in the pricing sheet.                                                          |
| transactionCcy        | The currency of the transaction.                                                                                  |
| consumerCcy           | The consumer ccy.                                                                                                 |
| transactionCcyType    | Identifies the type of Transaction currency.<br/>Enums: `DELIV` `NDF` `DTSB`                                      |
| amount                | The total amount of the transaction.                                                                              |
| transactionType       | The underlying transaction type of this transaction.<br/>Enums: `SALE` `REFUND` `CHARGEBACK` `CHARGEBACK_REVERSE` |
| scenario              | The nature of the transaction. <br/>Enums: `ONLINE_PAYMENT` `OFFLINE_PAYMENT` `CONVERSION` `REMITTANCE`           |
| settlementAmount      | For NDF/DTSB scenario. The total amount of the transaction from the Payment Provider.                             |
| settlementCcy         | The currency of the settlement amount.                                                                            |
| paymentProvider       | The payment provider for the transaction.                                                                         |
| transactionTimestamp  | The time which the transaction (e.g. sale, refund, chargeback, chargeback reverse etc) took placed.               |
| requestedPricingRefId | The identifier to the pricing sheet.                                                                              |
| beneficiary           | This field is to identify client’s beneficiary, e.g. end merchant to pay to.                                      |
| clientRef             | Pass through information from client in JSON format.                                                              |
| status                | Indicate the status of the advice.<br/>Enums: `VALID` `INVALID` `IN_PROGRESS`                                     |

### Query advice status

Queries the status of advice by batch ID or advice ID.

```kotlin
val batchResponse = aladdinClient.queryAdviceStatus(batchId = "batch-id")
val adviceResponse = aladdinClient.queryAdviceStatus(adviceId = "advice-id")
```

#### Request

| Field    | Description                      |
|----------|----------------------------------|
| batchId  | Unique Id to identify a batch.   |
| adviceId | Unique Id to identify an advice. |

#### Response

`BatchAdviceStatusResponse`

| Field   | Description                                                                    |
|---------|--------------------------------------------------------------------------------|
| message | Indicates whether the request is successful.                                   |
| advices | Contains all the responses to each advice requested. Return a list of `Advice` |

`Advice`

| Field                 | Description                                                                                                       |
|-----------------------|-------------------------------------------------------------------------------------------------------------------|
| adviceId              | Unique Id to identify an advice.                                                                                  |
| batchId               | Unique Id to identify a batch.                                                                                    |
| adviceType            | Defines whether is it an order advice (OA) or execution advice (EA).<br/>Enums: `OA` `EA`                         |    
| transactionId         | Same transaction Id as provided in the advice message.                                                            |
| accountName           | Unique account name to allow Aladdin identifies the client. M-DAQ provides this identifier.                       |
| ccyPair               | Currency pair with slash as stated in the pricing sheet.                                                          |
| transactionCcy        | The currency of the transaction.                                                                                  |
| consumerCcy           | The consumer ccy.                                                                                                 |
| transactionCcyType    | Identifies the type of Transaction currency.<br/>Enums: `DELIV` `NDF` `DTSB`                                      |
| amount                | The total amount of the transaction.                                                                              |
| transactionType       | The underlying transaction type of this transaction.<br/>Enums: `SALE` `REFUND` `CHARGEBACK` `CHARGEBACK_REVERSE` |
| scenario              | The nature of the transaction. <br/>Enums: `ONLINE_PAYMENT` `OFFLINE_PAYMENT` `CONVERSION` `REMITTANCE`           |
| settlementAmount      | For NDF/DTSB scenario. The total amount of the transaction from the Payment Provider.                             |
| settlementCcy         | The currency of the settlement amount.                                                                            |
| paymentProvider       | The payment provider for the transaction.                                                                         |
| transactionTimestamp  | The time which the transaction (e.g. sale, refund, chargeback, chargeback reverse etc) took placed.               |
| requestedPricingRefId | The identifier to the pricing sheet.                                                                              |
| beneficiary           | This field is to identify client’s beneficiary, e.g. end merchant to pay to.                                      |
| clientRef             | Pass through information from client in JSON format.                                                              |
| status                | Indicate the status of the advice.<br/>Enums: `VALID` `INVALID` `IN_PROGRESS`                                     |

### Get latest pricing sheet

Retrieves the latest pricing sheet.

```kotlin
val response = aladdinClient.getLatestPricingSheet()
```

#### Response
| Field              | Description                                                                                                                                                  |
|--------------------|--------------------------------------------------------------------------------------------------------------------------------------------------------------|
| pricingReferenceId | Unique identifier for the pricing sheet.                                                                                                                     |
| pricingTimestamp   | The time when the pricing sheet is generated.                                                                                                                |
| startTimestamp     | The time when the client can start using the rates for booking transaction.                                                                                  |
| expiryTimestamp    | Cut-off time for booking transaction using the rates in the pricing sheet.                                                                                   |
| thresholdTimestamp | Threshold period cut-off time. Threshold period is an extended period that allows client to book the transaction using expired pricing sheet.                |
| validTimestamp     | Cut-off time for submitting confirmed transaction booked using the rates in the pricing sheet.                                                               |
| ccyPair            | Currency pair with a slash. The currency before the slash is defined as BaseCcy. The currency after the slash is defined as QuoteCcy.                        |
| transactionCcyType | Identify the type of transaction currency.<br/>Enums: `DELIV` `NDF` `DTSB`                                                                                   |
| bid                | All-in bid rate for client’s customers.                                                                                                                      |
| offer              | All-in offer rate for client’s customers.                                                                                                                    |
| mbid               | M-DAQ’s bid for client. Includes M-DAQ premium and risk premium.                                                                                             |
| moffer             | M-DAQ’s offer for client. Includes M-DAQ premium and risk premium.                                                                                           |
| mid                | Reference rate for converting the premium from bps to pips.                                                                                                  |
| pricingTierId      | Unique identifier for a pricing markup configuration used in this pricing sheet.                                                                             |
| projectedValueDate | The estimated value date for the EA if the EA is using this pricing sheet and sent before expiryTimestamp.                                                   |
| transactionFlowId  | In the scenario where there are multiple transaction flows for the same currency pair, this field is needed to differentiate one pricing sheet from another. |

### Get active pricing sheet

Retrieves the active pricing sheet for a given timestamp.

```kotlin
val activeFor = LocalDateTime.now()
val response = aladdinClient.getActivePricingSheetByTs(activeFor)
```

#### Response
`PricingSheet`

| Field              | Description                                                                                                                                                  |
|--------------------|--------------------------------------------------------------------------------------------------------------------------------------------------------------|
| pricingReferenceId | Unique identifier for the pricing sheet.                                                                                                                     |
| pricingTimestamp   | The time when the pricing sheet is generated.                                                                                                                |
| startTimestamp     | The time when the client can start using the rates for booking transaction.                                                                                  |
| expiryTimestamp    | Cut-off time for booking transaction using the rates in the pricing sheet.                                                                                   |
| thresholdTimestamp | Threshold period cut-off time. Threshold period is an extended period that allows client to book the transaction using expired pricing sheet.                |
| validTimestamp     | Cut-off time for submitting confirmed transaction booked using the rates in the pricing sheet.                                                               |
| ccyPair            | Currency pair with a slash. The currency before the slash is defined as BaseCcy. The currency after the slash is defined as QuoteCcy.                        |
| transactionCcyType | Identify the type of transaction currency.<br/>Enums: `DELIV` `NDF` `DTSB`                                                                                   |
| bid                | All-in bid rate for client’s customers.                                                                                                                      |
| offer              | All-in offer rate for client’s customers.                                                                                                                    |
| mbid               | M-DAQ’s bid for client. Includes M-DAQ premium and risk premium.                                                                                             |
| moffer             | M-DAQ’s offer for client. Includes M-DAQ premium and risk premium.                                                                                           |
| mid                | Reference rate for converting the premium from bps to pips.                                                                                                  |
| pricingTierId      | Unique identifier for a pricing markup configuration used in this pricing sheet.                                                                             |
| projectedValueDate | The estimated value date for the EA if the EA is using this pricing sheet and sent before expiryTimestamp.                                                   |
| transactionFlowId  | In the scenario where there are multiple transaction flows for the same currency pair, this field is needed to differentiate one pricing sheet from another. |

## Response object

All service methods in `AladdinClient` will return `AladdinResponse<T>` object, which contains the following fields

| Field            | Type                    | Description                                                                                                                                                                                    |
|------------------|-------------------------|------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| `httpCode`       | `Int`                   | The HTTP status code of the request                                                                                                                                                            |
| `responseObject` | `T?`                    | The actual response data from the Aladdin API if the `httpCode` is 2xx. The type `T` depends on the API endpoint being called and the expected response data type. Null if an error occurred.  |
| `errorObject`    | `AladdinErrorResponse?` | A nullable property that holds an `AladdinErrorResponse` object, which contains information about any errors that occurred during the API call. If no error occurred, this field will be null. |

## Instruction to use the examples

1. Clone this repo
2. Open the project in your favorite IDE
3. Replace the `private_key_path`, `apiKey` and `basePath` in `Main.kt`
4. Run the example by calling the corresponding method in `Main.kt`