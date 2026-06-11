package com.example.reservation_eeg_android_app.ui.reservation

import android.content.Intent
import android.net.Uri
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Security
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.reservation_eeg_android_app.model.Reservation
import com.example.reservation_eeg_android_app.ui.reservation.viewmodel.ReservationViewModel
import com.example.reservation_eeg_android_app.data.SupabaseConfig
import java.time.OffsetDateTime
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PaymentScreen(
    reservationId: Int,
    viewModel: ReservationViewModel,
    onPaymentSuccess: () -> Unit,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    var reservation by remember { mutableStateOf<Reservation?>(null) }
    var isPageLoading by remember { mutableStateOf(true) }
    var showWebView by remember { mutableStateOf(false) }

    val isLoadingState by viewModel.isLoading.collectAsState()

    LaunchedEffect(reservationId) {
        reservation = viewModel.fetchReservationById(reservationId)
        isPageLoading = false
    }

    val gradientBackground = Brush.verticalGradient(
        colors = listOf(
            MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.2f),
            MaterialTheme.colorScheme.surface
        )
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("결제하기", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(gradientBackground)
                .padding(innerPadding)
        ) {
            when {
                isPageLoading -> {
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                }
                reservation == null -> {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text("예약 정보를 찾을 수 없습니다.", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.error)
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(onClick = onBack) {
                            Text("돌아가기")
                        }
                    }
                }
                else -> {
                    val currentReservation = reservation!!
                    val eegType = currentReservation.eegType
                    val displayTime = remember(currentReservation.reservedAt) {
                        try {
                            val dt = OffsetDateTime.parse(currentReservation.reservedAt)
                            val kstDt = dt.atZoneSameInstant(SupabaseConfig.KST_ZONE_ID)
                            kstDt.format(DateTimeFormatter.ofPattern("yyyy년 MM월 dd일 HH:mm"))
                        } catch (_: Exception) {
                            currentReservation.reservedAt
                        }
                    }

                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .verticalScroll(rememberScrollState())
                            .padding(horizontal = 20.dp)
                    ) {
                        Spacer(modifier = Modifier.height(16.dp))

                        // Step Indicator
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("예약정보 입력", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.outline)
                            Text(">", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.outline)
                            Text("날짜선택", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.outline)
                            Text(">", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.outline)
                            Text("결제진행", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
                        }

                        Spacer(modifier = Modifier.height(24.dp))

                        // Reservation Summary Card
                        Text(
                            text = "예약 확인",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(bottom = 12.dp)
                        )

                        ElevatedCard(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(20.dp),
                            colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surface)
                        ) {
                            Column(modifier = Modifier.padding(20.dp)) {
                                Text(
                                    text = eegType.displayName,
                                    style = MaterialTheme.typography.titleLarge,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                                Spacer(modifier = Modifier.height(12.dp))
                                HorizontalDivider(thickness = 0.5.dp, color = MaterialTheme.colorScheme.outlineVariant)
                                Spacer(modifier = Modifier.height(12.dp))

                                Row(modifier = Modifier.fillMaxWidth(), justify = Arrangement.SpaceBetween) {
                                    Text("피검자", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.outline)
                                    Text(currentReservation.patientName, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
                                }
                                Spacer(modifier = Modifier.height(8.dp))
                                Row(modifier = Modifier.fillMaxWidth(), justify = Arrangement.SpaceBetween) {
                                    Text("예약 일시", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.outline)
                                    Text(displayTime, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
                                }
                                Spacer(modifier = Modifier.height(8.dp))
                                Row(modifier = Modifier.fillMaxWidth(), justify = Arrangement.SpaceBetween) {
                                    Text("소요 시간", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.outline)
                                    Text("${eegType.baseDurationMin}분", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(24.dp))

                        // Pricing Breakdown
                        Text(
                            text = "결제 금액",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(bottom = 12.dp)
                        )

                        ElevatedCard(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(20.dp),
                            colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surface)
                        ) {
                            Column(modifier = Modifier.padding(20.dp)) {
                                Row(modifier = Modifier.fillMaxWidth(), justify = Arrangement.SpaceBetween) {
                                    Text("EEG 검사 요금", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    Text(eegType.formattedPrice, style = MaterialTheme.typography.bodyMedium)
                                }
                                Spacer(modifier = Modifier.height(8.dp))
                                Row(modifier = Modifier.fillMaxWidth(), justify = Arrangement.SpaceBetween) {
                                    Text("할인/감면 금액", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    Text("0원", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.outline)
                                }
                                Spacer(modifier = Modifier.height(16.dp))
                                HorizontalDivider(thickness = 0.5.dp, color = MaterialTheme.colorScheme.outlineVariant)
                                Spacer(modifier = Modifier.height(16.dp))
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    justify = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text("최종 결제 금액", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                                    Text(
                                        text = eegType.formattedPrice,
                                        style = MaterialTheme.typography.headlineMedium,
                                        fontWeight = FontWeight.Black,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(24.dp))

                        // Payment Method Selection
                        Text(
                            text = "결제 수단",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(bottom = 12.dp)
                        )

                        OutlinedCard(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(20.dp),
                            border = BorderStroke(2.dp, MaterialTheme.colorScheme.primary),
                            colors = CardDefaults.outlinedCardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.05f))
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(20.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Surface(
                                    modifier = Modifier.size(44.dp),
                                    shape = RoundedCornerShape(10.dp),
                                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.08f)
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Icon(
                                            imageVector = Icons.Default.CreditCard,
                                            contentDescription = null,
                                            tint = MaterialTheme.colorScheme.primary
                                        )
                                    }
                                }
                                Spacer(modifier = Modifier.width(16.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text("신용카드 결제", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                                    Text("키움페이 PG 안전결제", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.outline)
                                }
                                Icon(
                                    imageVector = Icons.Default.CheckCircle,
                                    contentDescription = "Selected",
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Security,
                                contentDescription = "Security",
                                tint = MaterialTheme.colorScheme.outline,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                "본 결제는 다우데이타 키움페이 보안결제 엔진을 사용합니다.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.outline
                            )
                        }

                        Spacer(modifier = Modifier.height(32.dp))

                        Button(
                            onClick = { showWebView = true },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(58.dp),
                            shape = RoundedCornerShape(18.dp),
                            elevation = ButtonDefaults.buttonElevation(defaultElevation = 4.dp)
                        ) {
                            Text("결제하기", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        }

                        Spacer(modifier = Modifier.height(32.dp))
                    }
                }
            }

            if (isLoadingState) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            }
        }
    }

    // Kiwoom Pay WebView Dialog
    if (showWebView && reservation != null) {
        val currentReservation = reservation!!
        Dialog(
            onDismissRequest = { showWebView = false },
            properties = DialogProperties(
                usePlatformDefaultWidth = false,
                dismissOnBackPress = false,
                dismissOnClickOutside = false
            )
        ) {
            Surface(
                modifier = Modifier.fillMaxSize(),
                color = MaterialTheme.colorScheme.background
            ) {
                AndroidView(
                    factory = { ctx ->
                        WebView(ctx).apply {
                            settings.javaScriptEnabled = true
                            settings.domStorageEnabled = true
                            settings.allowFileAccess = true
                            settings.allowContentAccess = true
                            
                            addJavascriptInterface(
                                PaymentJavaScriptInterface(
                                    onPaymentSuccess = { cardIssuer, txnId ->
                                        showWebView = false
                                        viewModel.confirmPayment(
                                            id = reservationId,
                                            amount = currentReservation.eegType.price,
                                            cardIssuer = cardIssuer,
                                            transactionId = txnId
                                        ) {
                                            onPaymentSuccess()
                                        }
                                    },
                                    onPaymentCancel = {
                                        showWebView = false
                                    }
                                ),
                                "AndroidInterface"
                            )

                            webViewClient = object : WebViewClient() {
                                override fun shouldOverrideUrlLoading(view: WebView?, request: WebResourceRequest?): Boolean {
                                    val url = request?.url?.toString() ?: return false
                                    // Handle mobile app card scheme redirects (e.g. intent://)
                                    if (!url.startsWith("http://") && !url.startsWith("https://") && !url.startsWith("file://")) {
                                        try {
                                            val intent = Intent.parseUri(url, Intent.URI_INTENT_SCHEME)
                                            if (intent.resolveActivity(ctx.packageManager) != null) {
                                                ctx.startActivity(intent)
                                                return true
                                            } else {
                                                val packageName = intent.`package`
                                                if (packageName != null) {
                                                    val marketIntent = Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=$packageName"))
                                                    ctx.startActivity(marketIntent)
                                                    return true
                                                }
                                            }
                                        } catch (e: Exception) {
                                            e.printStackTrace()
                                        }
                                        return true
                                    }
                                    return false
                                }
                            }

                            val queryUrl = "file:///android_asset/kiwoompay/checkout.html" +
                                    "?amount=${currentReservation.eegType.price}" +
                                    "&item=${Uri.encode(currentReservation.eegType.displayName)}" +
                                    "&name=${Uri.encode(currentReservation.patientName)}"
                            loadUrl(queryUrl)
                        }
                    },
                    modifier = Modifier.fillMaxSize()
                )
            }
        }
    }
}

// Row helper extension to simplify layout code
@Composable
private fun Row(
    modifier: Modifier = Modifier,
    justify: Arrangement.Horizontal = Arrangement.Start,
    verticalAlignment: Alignment.Vertical = Alignment.Top,
    content: @Composable RowScope.() -> Unit
) {
    androidx.compose.foundation.layout.Row(
        modifier = modifier,
        horizontalArrangement = justify,
        verticalAlignment = verticalAlignment,
        content = content
    )
}

class PaymentJavaScriptInterface(
    private val onPaymentSuccess: (String, String) -> Unit,
    private val onPaymentCancel: () -> Unit
) {
    @android.webkit.JavascriptInterface
    fun onPaymentSuccess(cardIssuer: String, transactionId: String) {
        onPaymentSuccess(cardIssuer, transactionId)
    }

    @android.webkit.JavascriptInterface
    fun onPaymentCancel() {
        onPaymentCancel()
    }
}
