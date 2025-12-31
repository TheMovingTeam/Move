package io.github.azakidev.move.utils

import android.annotation.SuppressLint
import android.util.Log
import io.github.azakidev.move.data.items.Capabilities
import io.github.azakidev.move.data.items.LineItem
import io.github.azakidev.move.data.items.LineTime
import io.github.azakidev.move.data.items.ProviderItem
import io.github.azakidev.move.data.items.StopItem
import io.github.azakidev.move.data.providers.fetchEMTMadridToken
import io.github.azakidev.move.data.providers.parseEMTMadrid
import io.github.azakidev.move.data.providers.parseEMTValencia
import io.github.azakidev.move.data.providers.parseFGVResponse
import io.github.azakidev.move.data.providers.parseMetrobusValencia
import io.github.azakidev.move.data.providers.parseSOAP
import io.github.azakidev.move.data.providers.parseTMPMurcia
import io.github.azakidev.move.data.providers.parseTranviaMurcia
import io.github.azakidev.move.data.providers.parseVectaliaTimes
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.internal.platform.Platform
import java.security.cert.X509Certificate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import javax.net.ssl.HostnameVerifier
import javax.net.ssl.SSLSocketFactory
import javax.net.ssl.TrustManager
import javax.net.ssl.X509TrustManager

private const val SOAP_REQUEST = """
            <?xml version='1.0' encoding='utf-8'?>
              <soap:Envelope xmlns:soap='http://schemas.xmlsoap.org/soap/envelope/' xmlns:xsi='http://www.w3.org/2001/XMLSchema-instance'  xmlns:xsd='http://www.w3.org/2001/XMLSchema'>
                <soap:Body>
                  <GetStopMonitoring xmlns='http://tempuri.org/'>
                    <request>
                      <ServiceRequestInfo xmlns=''>
                        <RequestTimestamp xmlns='http://www.siri.org.uk/siri'>@currentTime</RequestTimestamp>
                        <AccountId xmlns='http://www.siri.org.uk/siri'>@accId</AccountId>
                        <AccountKey xmlns='http://www.siri.org.uk/siri'>@accKey</AccountKey>
                      </ServiceRequestInfo>
                      <Request xmlns=''>
                        <RequestTimestamp xmlns='http://www.siri.org.uk/siri'>@currentTime</RequestTimestamp>
                        <MonitoringRef xmlns='http://www.siri.org.uk/siri'>@stopId</MonitoringRef>
                      </Request>
                    </request>
                  </GetStopMonitoring>
                </soap:Body>
              </soap:Envelope>
          """

private fun formSoapRequest(
    stopId: Int,
    accountId: String,
    accountKey: String
): String {
    val currentTime =
        LocalDateTime.now()
            .format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)
            .toString().take(19)
    return SOAP_REQUEST
        .trimIndent()
        .replace("@currentTime", currentTime)
        .replace("@stopId", stopId.toString())
        .replace("@accId", accountId)
        .replace("@accKey", accountKey)
}

fun fetchStopTime(provider: ProviderItem, stopItem: StopItem, lines: List<LineItem>) {
    val url = provider.timeSource.replace("@stop", stopItem.id.toString())
        .replace("@comId", stopItem.comId.toString())

    val client =
        OkHttpClient.Builder().trustSelfSignedCertsIfNeeded(provider).retryOnConnectionFailure(true)
            .build()

    try {
        val requestBuilt =
            Request.Builder().formRequest(client, provider, stopItem).url(url).build()

        val response = client.newCall(requestBuilt).execute()

        val responseText = response.body!!.string()

        try {
            val times = parseTimes(responseText, provider, stopItem, lines) ?: emptyList()

            stopItem.setTimeTable(times)

        } catch (e: Exception) {
            Log.e(
                LogTags.MoveModel.name, "Could not parse times for ${stopItem.name}: $e", e
            )
            stopItem.setTimeTable(emptyList())
            return
        }
    } catch (e: Exception) {
        Log.e(
            LogTags.Networking.name, "Could not get times for ${stopItem.name}: $e", e
        )
        return
    }
}

fun Request.Builder.formRequest(
    client: OkHttpClient, provider: ProviderItem, stopItem: StopItem
): Request.Builder {

    if (provider.name.contains("Vectalia")) { // Vectalia headers
        return this.header("Accept", "*/*").header("responseType", "ResponseType.json")
            .header("followRedirects", "true").get()
    }

    if (provider.name.contains("EMT Valencia")) { // EMT Valencia headers
        return this.header(
            "X-WSSE",
            "UsernameToken Username=\"7gH8m45w7A\", " + "PasswordDigest=\"NjA4ZTY3N2U3MzRiYTYyMmJhNjRlMDI0Y2Y5N2Q4NDJlZDM2ZTg1Nw==\", " + "Nonce=\"NDFlMjdjMjMzODgxOGRiNDBkMGNiYjk0MGRhMWI4MTE=\", " + "Created=\"1760182100\""
        ).get()
    }

    if (provider.name.contains("EMT Madrid")) {
        val token = fetchEMTMadridToken(client)
        val content =
            "{\n" + "\"statistics\":\"\",\n" + "\"cultureInfo\":\"\",\n" + "\"Text_StopRequired_YN\":\"N\",\n" + "\"Text_EstimationsRequired_YN\":\"Y\",\n" + "\"Text_IncidencesRequired_YN\":\"N\",\n" + "\"DateTime_Referenced_Incidencies_YYYYMMDD\":\"20180823\"\n" + "}"
        return this.header(
            "accessToken", token
        ).post(
            content.toRequestBody(
                "application/json".toMediaTypeOrNull()
            )
        )
    }

    if (provider.name.contains("Transporte de Murcia")) {
        return this.post(
            formSoapRequest(stopItem.id, "wshuesca", "WS.huesca")
                .toRequestBody(
                    "text/xml".toMediaTypeOrNull()
                )
        )
    }

    if (provider.name.contains("Llorente Bus")) {
        return this
            .post(
                formSoapRequest(stopItem.id, "benidorm", "benidormSiri")
                    .toRequestBody(
                        "text/xml".toMediaTypeOrNull()
                    )
            )
    }

    return this.get() // If none match, don't add any headers
}

fun OkHttpClient.Builder.trustSelfSignedCertsIfNeeded(provider: ProviderItem): OkHttpClient.Builder {

    if (provider.name != "Tranvía de Murcia" || !provider.capabilities.contains(Capabilities.Unsafe)) {
        return this
    } else {
        val trustManager = createInsecureTrustManager()
        val sslSocketFactory = createInsecureSslSocketFactory(trustManager)

        val insecureClient = this.sslSocketFactory(sslSocketFactory, trustManager)
            .hostnameVerifier(createInsecureHostnameVerifier())
        return insecureClient
    }
}

@SuppressLint("CustomX509TrustManager", "TrustAllX509TrustManager")
private fun createInsecureTrustManager(): X509TrustManager = object : X509TrustManager {
    override fun checkClientTrusted(
        chain: Array<X509Certificate>,
        authType: String,
    ) {
    }

    override fun checkServerTrusted(
        chain: Array<X509Certificate>,
        authType: String,
    ) {
    }

    override fun getAcceptedIssuers(): Array<X509Certificate> = arrayOf()
}

private fun createInsecureSslSocketFactory(trustManager: TrustManager): SSLSocketFactory =
    Platform.get().newSSLContext().apply {
        init(null, arrayOf(trustManager), null)
    }.socketFactory

private fun createInsecureHostnameVerifier(): HostnameVerifier = HostnameVerifier { _, _ -> true }


fun parseTimes(
    response: String, provider: ProviderItem, stopItem: StopItem, lines: List<LineItem>
): List<LineTime>? {
    Log.d(LogTags.Networking.name, "Fetching for stop: ${stopItem.name}")
    when (provider.name) {
        "DummyProvider" -> {
            val times = Regex("\\w+").findAll(response).toList().map { it.value.toInt() }
            if (times.isNotEmpty()) {
                var count = 0
                val timeList = mutableListOf<LineTime>()
                stopItem.lines.forEach { i ->
                    timeList.add(LineTime(i, times[count]))
                    count++
                }
                return timeList
            } else {
                return null
            }
        }

        "Vectalia Alicante" -> {
            val estimations: List<LineTime> = try {
                parseVectaliaTimes(response, lines.filter { it.provider == provider.id })
            } catch (e: Exception) {
                Log.e(
                    LogTags.Parser.name, "Couldn't parse Vectalia times in ${e.message}", e
                )
                return null
            }
            return estimations
        }

        "Vectalia Albacete" -> {
            val estimations: List<LineTime> = try {
                parseVectaliaTimes(response, lines.filter { it.provider == provider.id })
            } catch (e: Exception) {
                Log.e(
                    LogTags.Parser.name, "Couldn't parse Vectalia times in ${e.message}", e
                )
                return null
            }
            return estimations
        }

        "Vectalia Cáceres" -> {
            val estimations: List<LineTime> = try {
                parseVectaliaTimes(response, lines.filter { it.provider == provider.id })
            } catch (e: Exception) {
                Log.e(
                    LogTags.Parser.name, "Couldn't parse Vectalia times in ${e.message}", e
                )
                return null
            }
            return estimations
        }

        "Vectalia Alcoi" -> {
            val estimations: List<LineTime> = try {
                parseVectaliaTimes(response, lines.filter { it.provider == provider.id })
            } catch (e: Exception) {
                Log.e(
                    LogTags.Parser.name, "Couldn't parse Vectalia times in ${e.message}", e
                )
                return null
            }
            return estimations
        }

        "Vectalia Mérida" -> {
            val estimations: List<LineTime> = try {
                parseVectaliaTimes(response, lines.filter { it.provider == provider.id })
            } catch (e: Exception) {
                Log.e(
                    LogTags.Parser.name, "Couldn't parse Vectalia times in ${e.message}", e
                )
                return null
            }
            return estimations
        }

        "Tram Alacant" -> {
            val estimations: List<LineTime> = try {
                parseFGVResponse(response)
            } catch (e: Exception) {
                Log.e(
                    LogTags.Parser.name, "Couldn't parse Tram Alacant times in ${e.message}", e
                )
                return null
            }
            return estimations
        }

        "Metrovalencia" -> {
            val estimations: List<LineTime> = try {
                parseFGVResponse(response)
            } catch (e: Exception) {
                Log.e(
                    LogTags.Parser.name, "Couldn't parse Metrovalencia times in ${e.message}", e
                )
                return null
            }
            return estimations
        }

        "EMT Valencia" -> {
            val estimations: List<LineTime> = try {
                parseEMTValencia(response)
            } catch (e: Exception) {
                Log.e(
                    LogTags.Parser.name, "Couldn't parse EMT Valencia times in ${e.message}", e
                )
                return null
            }
            return estimations
        }

        "EMT Madrid" -> {
            val estimations: List<LineTime> = try {
                parseEMTMadrid(response, lines.filter { it.provider == provider.id })
            } catch (e: Exception) {
                Log.e(
                    LogTags.Parser.name, "Couldn't parse EMT Madrid times in ${e.message}", e
                )
                return null
            }
            return estimations
        }

        "Tranvía de Murcia" -> {
            val estimations: List<LineTime> = try {
                parseTranviaMurcia(response)
            } catch (e: Exception) {
                Log.e(
                    LogTags.Parser.name, "Couldn't parse Tranvía de Murcia times in ${e.message}", e
                )
                return null
            }
            return estimations
        }

        "TMP Murcia" -> {
            val estimations: List<LineTime> = try {
                parseTMPMurcia(response, lines.filter { it.provider == 12 })
            } catch (e: Exception) {
                Log.e(
                    LogTags.Parser.name, "Couldn't parse TMP Murcia times in ${e.message}", e
                )
                return null
            }
            return estimations
        }

        "Metrobus Valencia" -> {
            val estimations: List<LineTime> = try {
                parseMetrobusValencia(response, lines.filter { it.provider == 13 })
            } catch (e: Exception) {
                Log.e(
                    LogTags.Parser.name, "Couldn't parse Metrobus Valencia times in ${e.message}", e
                )
                return null
            }
            return estimations
        }

        "Transporte de Murcia" -> {
            val estimations: List<LineTime> = try {
                parseSOAP(response)
            } catch (e: Exception) {
                Log.e(
                    LogTags.Parser.name, "Couldn't parse TMurcia times in ${e.message}", e
                )
                return null
            }
            return estimations
        }

        "Llorente Bus" -> {
            val estimations: List<LineTime> = try {
                parseSOAP(response)
            } catch (e: Exception) {
                Log.e(
                    LogTags.Parser.name, "Couldn't parse Llorente Bus times in ${e.message}", e
                )
                return null
            }
            return estimations
        }

        else -> {
            return null
        }
    }
}
