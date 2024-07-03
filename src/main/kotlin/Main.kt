package org.example

import com.mdaq.aladdin.sdk.AladdinClient
import com.mdaq.aladdin.sdk.AladdinClientBuilder
import com.mdaq.aladdin.sdk.model.Advice
import com.mdaq.aladdin.sdk.model.enum.AdviceType
import com.mdaq.aladdin.sdk.model.enum.TransactionType
import java.math.BigDecimal
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneOffset
import java.util.*

//TIP What are needed: <br/>
// 1. Private key file <br/>
// 2. API key <br/>
// 3. Base url for our service <br/>
fun main() {
    //TIP API client instantiation
    val builder = AladdinClientBuilder()

    val privateKeyPath = "<REPLACE WITH YOUR PRIVATE KEY PATH>"
    val apiKey = "<REPLACE WITH YOUR API KEY>"
    val basePath = "<REPLACE WITH SERVICE BASE URL>"

    val client = builder
        .withPrivateKeyFileName(privateKeyPath)
        .withApiKey(apiKey)
        .withBasePath(basePath)
        .build()

    //TIP different flows here
    //    syncFlow(client)
    //    asyncFlow(client)
    //    getPricingSheetActiveForACertainTime(client)
    //    requestWithInsufficientInfo(client)
    //    howToUseResponse(client)
}

//TIP Sync flow: Getting pricing sheet -> Send sync request
fun syncFlow(client: AladdinClient){
    println("Running sync flow...")
    //TIP Get latest pricing sheet
    println("Getting latest pricing sheet...")
    val latestPricingSheetResponse = client.getLatestPricingSheet()
    println(latestPricingSheetResponse)

    val pricingSheet = latestPricingSheetResponse.responseObject?.get(0) ?: return
    val uuid = UUID.randomUUID().toString()
    val currentTime = LocalDateTime.ofInstant(Instant.now(), ZoneOffset.UTC)

    //TIP Submit advice sync
    println("Submitting advice sync...")
    val adviceRequestSync = Advice.AdviceBuilder()
        .adviceId(uuid)
        .transactionId(uuid)
        .adviceType(AdviceType.OA)
        .ccyPair(pricingSheet.ccyPair)
        .transactionCcy(pricingSheet.ccyPair!!.split("/")[0])
        .transactionCcyType(pricingSheet.transactionCcyType)
        .requestedPricingRefId(pricingSheet.pricingReferenceId)
        .transactionType(TransactionType.SALE)
        .paymentProvider("VISA") //TIP this field should be given by M-DAQ, it is related to value/settlement date
        .transactionTimestamp(currentTime)
        .amount(BigDecimal(10))
        .build()

    val adviceAsyncResponse = client.submitAdvicesSync(listOf(adviceRequestSync))
    println(adviceAsyncResponse)
    println()
}

//TIP Async flow: Getting pricing sheet -> Send async request -> Poll for status
fun asyncFlow(client: AladdinClient) {
    println("Running async flow...")
    //TIP Get latest pricing sheet
    println("Getting latest pricing sheet...")
    val latestPricingSheetResponse = client.getLatestPricingSheet()
    println(latestPricingSheetResponse)

    val pricingSheet = latestPricingSheetResponse.responseObject?.get(0) ?: return
    val uuid = UUID.randomUUID().toString()
    val currentTime = LocalDateTime.ofInstant(Instant.now(), ZoneOffset.UTC)

    //TIP Submit advice async
    println("Submitting advice async...")
    val adviceRequestAsync = Advice.AdviceBuilder()
        .adviceId(uuid)
        .transactionId(uuid)
        .adviceType(AdviceType.OA)
        .ccyPair(pricingSheet.ccyPair)
        .transactionCcy(pricingSheet.ccyPair!!.split("/")[0])
        .transactionCcyType(pricingSheet.transactionCcyType)
        .requestedPricingRefId(pricingSheet.pricingReferenceId)
        .transactionType(TransactionType.SALE)
        .paymentProvider("VISA") //TIP this field should be given by M-DAQ, it is related to value/settlement date
        .transactionTimestamp(currentTime)
        .amount(BigDecimal(10))
        .build()

    val adviceAsyncResponse = client.submitAdvicesAsync(listOf(adviceRequestAsync))
    println(adviceAsyncResponse)

    // wait for aladdin to process the transaction
    Thread.sleep(3000)

    //TIP Query advice status
    println("Querying advice status...")
    val batchId = adviceAsyncResponse.responseObject?.batchId ?: return
    val statusResponse = client.queryAdviceStatus(batchId)
    println(statusResponse)
    println()
}

//TIP Get the pricing sheet that is active for a certain period
fun getPricingSheetActiveForACertainTime(client: AladdinClient){
    println("Getting pricing sheet active for a certain time...")
    val pricingSheetByTimeResponse = client.getActivePricingSheetByTs(LocalDateTime.of(2024,4,26,7,30))
    println(pricingSheetByTimeResponse)
    println()
}

//TIP User provide request with insufficient details
fun requestWithInsufficientInfo(client: AladdinClient){
    println("Submitting advice with missing field...")
    //TIP construct advice request with missing field
    val latestPricingSheetResponse = client.getLatestPricingSheet()
    println(latestPricingSheetResponse)

    val pricingSheet = latestPricingSheetResponse.responseObject?.get(0) ?: return
    val uuid = UUID.randomUUID().toString()
    val currentTime = LocalDateTime.ofInstant(Instant.now(), ZoneOffset.UTC)

    val adviceRequestWithMissingField = Advice.AdviceBuilder()
        .adviceId(uuid)
        .transactionId(uuid)
        .adviceType(AdviceType.OA)
        .ccyPair(pricingSheet.ccyPair)
        .transactionType(TransactionType.SALE)
        .paymentProvider("VISA") //TIP this field should be given by M-DAQ, it is related to value/settlement date
        .transactionTimestamp(currentTime)
        .amount(BigDecimal(10))
        .build()

    val adviceWithMissingFieldResponse = client.submitAdvicesSync(listOf(adviceRequestWithMissingField))
    println(adviceWithMissingFieldResponse)
    println()
}

//TIP Example on how to extract data from response object
fun howToUseResponse(client: AladdinClient){
    println("Demo on how to use response object...")
    println("Getting latest pricing sheet...")
    val latestPricingSheetResponse = client.getLatestPricingSheet()
    val httpCode = latestPricingSheetResponse.httpCode
    println("HTTP status code = ${httpCode}")
    val pricingSheets = latestPricingSheetResponse.responseObject
    if (httpCode/100 == 2){
        if (pricingSheets == null){
            throw Exception("pricingSheets are null while http code is 2xx")
        }
        println("There are ${pricingSheets.size} pricing sheets")
        println("1st pricing sheet = ${pricingSheets[0]}")
    }
    println("Error response = ${latestPricingSheetResponse.errorObject}")

    println()
}