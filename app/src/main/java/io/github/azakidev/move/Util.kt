package io.github.azakidev.move

import android.annotation.SuppressLint
import android.util.Log
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.util.fastJoinToString
import androidx.navigation3.runtime.NavKey
import io.github.azakidev.move.data.LineItem
import io.github.azakidev.move.data.LineTime
import io.github.azakidev.move.data.ProviderItem
import io.github.azakidev.move.data.StopItem
import io.github.azakidev.move.data.providers.fetchEMTMadridToken
import io.github.azakidev.move.data.providers.parseEMTMadrid
import io.github.azakidev.move.data.providers.parseEMTValencia
import io.github.azakidev.move.data.providers.parseFGVResponse
import io.github.azakidev.move.data.providers.parseTranviaMurcia
import io.github.azakidev.move.data.providers.parseVectaliaTimes
import kotlinx.serialization.Serializable
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.internal.platform.Platform
import java.security.cert.X509Certificate
import java.util.Locale
import javax.net.ssl.HostnameVerifier
import javax.net.ssl.SSLSocketFactory
import javax.net.ssl.TrustManager
import javax.net.ssl.X509TrustManager
import kotlin.streams.toList


// App locations
@Serializable
internal data object MainView : NavKey

@Serializable
internal data object Settings : NavKey

@Serializable
internal data object Providers : NavKey

@Serializable
internal data object QrScanner : NavKey

// Log tags
enum class LogTags {
    MoveModel, Networking, Parser,
}

fun Request.Builder.formRequest(client: OkHttpClient, provider: ProviderItem): Request.Builder {

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

    return this.get() // If none match, don't add any headers
}

fun OkHttpClient.Builder.trustSelfSignedCertsIfNeeded(provider: ProviderItem): OkHttpClient.Builder {

    val trustManager = createInsecureTrustManager()
    val sslSocketFactory = createInsecureSslSocketFactory(trustManager)

    val insecureClient = this.sslSocketFactory(sslSocketFactory, trustManager)
        .hostnameVerifier(createInsecureHostnameVerifier())

    if (provider.name.contains("Tranvía de Murcia")) {
        return insecureClient
    }

    return this // Default case
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
                parseVectaliaTimes(response, lines)
            } catch (e: Exception) {
                Log.e(
                    LogTags.Networking.name, "Couldn't parse Vectalia times in ${e.message}", e
                )
                return null
            }
            return estimations
        }

        "Vectalia Albacete" -> {
            val estimations: List<LineTime> = try {
                parseVectaliaTimes(response, lines)
            } catch (e: Exception) {
                Log.e(
                    LogTags.Networking.name, "Couldn't parse Vectalia times in ${e.message}", e
                )
                return null
            }
            return estimations
        }

        "Vectalia Cáceres" -> {
            val estimations: List<LineTime> = try {
                parseVectaliaTimes(response, lines)
            } catch (e: Exception) {
                Log.e(
                    LogTags.Networking.name, "Couldn't parse Vectalia times in ${e.message}", e
                )
                return null
            }
            return estimations
        }

        "Vectalia Alcoi" -> {
            val estimations: List<LineTime> = try {
                parseVectaliaTimes(response, lines)
            } catch (e: Exception) {
                Log.e(
                    LogTags.Networking.name, "Couldn't parse Vectalia times in ${e.message}", e
                )
                return null
            }
            return estimations
        }

        "Vectalia Mérida" -> {
            val estimations: List<LineTime> = try {
                parseVectaliaTimes(response, lines)
            } catch (e: Exception) {
                Log.e(
                    LogTags.Networking.name, "Couldn't parse Vectalia times in ${e.message}", e
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
                    LogTags.Networking.name, "Couldn't parse Tram Alacant times in ${e.message}", e
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
                    LogTags.Networking.name, "Couldn't parse Metrovalencia times in ${e.message}", e
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
                    LogTags.Networking.name, "Couldn't parse EMT Valencia times in ${e.message}", e
                )
                return null
            }
            return estimations
        }

        "EMT Madrid" -> {
            val estimations: List<LineTime> = try {
                parseEMTMadrid(response, lines)
            } catch (e: Exception) {
                Log.e(
                    LogTags.Networking.name, "Couldn't parse EMT Madrid times in ${e.message}", e
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
                    LogTags.Networking.name,
                    "Couldn't parse Tranvía de Murcia times in ${e.message}",
                    e
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

fun listShape(
    count: Int, total: Int, roundingLarge: Dp = 12.dp, roundingSmall: Dp = 4.dp
): Shape {
    if (total == 1) {
        return RoundedCornerShape(
            roundingLarge
        )
    }
    return when (count) {
        0 -> {
            RoundedCornerShape(
                topStart = roundingLarge,
                topEnd = roundingLarge,
                bottomStart = roundingSmall,
                bottomEnd = roundingSmall,
            )
        }

        total - 1 -> {
            RoundedCornerShape(
                topStart = roundingSmall,
                topEnd = roundingSmall,
                bottomStart = roundingLarge,
                bottomEnd = roundingLarge
            )
        }

        else -> {
            RoundedCornerShape(
                roundingSmall
            )
        }
    }
}

fun String.fmt(): String {
    return this.lowercase().replace("-", " - ").replace("–", " - ").replace("—", " - ")
        .replace(">", " > ").replace("(", " ( ").replace("/", " / ").replace(".", ". ")
        .replace("'", "' ").replace("\"", "").replace("_", " ").replace("avda", "av.")
        .replace("- obres", "( obres )").replace("..", ".").replace("  ", " ").replace("- >", ">")
        .split(' ').map { word ->
            if (word.uppercase().chars()
                    .allMatch { "MDCLXVI".chars().toList().contains(it) }
            ) { // Check if it's a roman numeral
                word.uppercase()
            } else {
                word.replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString() }
            }
        }.fastJoinToString(" ").replace("c/", "C/").replace("C/", "C/ ").replace("' ", "'")
        .replace("( ", "(").replace(" )", ")").replace(" / ", "/")
}

fun String.fmtSearch(): String {
    return this.lowercase().toList().filterNot { listOf('-', ' ', '(', ')', '.').contains(it) }
        .joinToString("")
}