package com.shayshankrathore.irishvisadate.ui

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.browser.customtabs.CustomTabsIntent
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.shayshankrathore.irishvisadate.ALL_EMBASSIES
import com.shayshankrathore.irishvisadate.AppPreferences
import com.shayshankrathore.irishvisadate.VisaStatusApi
import com.shayshankrathore.irishvisadate.VisaCheckResult
import com.shayshankrathore.irishvisadate.ui.theme.*
import kotlinx.coroutines.launch

sealed class CheckUiState {
    object Idle : CheckUiState()
    object Loading : CheckUiState()
    data class Error(val message: String) : CheckUiState()
    data class Success(val result: VisaCheckResult) : CheckUiState()
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ApplicationStatusScreen(onBack: () -> Unit) {
    val context = LocalContext.current
    val savedState by AppPreferences.flow(context).collectAsState(initial = null)

    var selectedEmbassy by remember { mutableStateOf(ALL_EMBASSIES[0]) }
    var stateRestored by remember { mutableStateOf(false) }

    LaunchedEffect(savedState) {
        if (!stateRestored && savedState != null) {
            savedState!!.embassyId?.let { id ->
                ALL_EMBASSIES.find { it.id == id }?.also { selectedEmbassy = it }
            }
            stateRestored = true
        }
    }

    var applicationNumber by remember { mutableStateOf("") }
    var checkState by remember { mutableStateOf<CheckUiState>(CheckUiState.Idle) }
    val scope = rememberCoroutineScope()

    fun normalizeAndValidate(input: String): Pair<Boolean, String> {
        val digits = input.trim()
            .replaceFirst(Regex("^[Ii][Rr][Ll]"), "")
            .replace(Regex("\\D"), "")
        if (digits.length != 8) {
            return Pair(false, "Must be 8 digits (e.g. 63690452)")
        }
        return Pair(true, digits)
    }

    fun onCheckStatus() {
        val (isValid, normalized) = normalizeAndValidate(applicationNumber)
        if (!isValid) {
            checkState = CheckUiState.Error(normalized)
            return
        }

        if (selectedEmbassy.apiKey == null) {
            checkState = CheckUiState.Error("Status check not available for ${selectedEmbassy.label}")
            return
        }

        checkState = CheckUiState.Loading
        scope.launch {
            val result = VisaStatusApi.checkApplication(normalized, selectedEmbassy.apiKey!!)
            checkState = if (result.isSuccess) {
                CheckUiState.Success(result.getOrThrow())
            } else {
                val exception = result.exceptionOrNull()
                CheckUiState.Error(exception?.message ?: "Unknown error")
            }
        }
    }

    Scaffold(
        containerColor = IrishGreenBg,
        topBar = {
            TopAppBar(
                title = { Text("Check Application Status", fontWeight = FontWeight.ExtraBold) },
                navigationIcon = {
                    TextButton(onClick = onBack) {
                        Text("← Back", color = IrishGreen, fontWeight = FontWeight.SemiBold)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(0xFF063320),
                    titleContentColor = Color.White,
                ),
            )
        },
    ) { inner ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(inner)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            // Embassy selector
            AccentCard(title = "🌍  SELECT EMBASSY", accentColor = IrishGreen) {
                Spacer(Modifier.height(8.dp))
                Text(
                    text = selectedEmbassy.label,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 14.sp,
                    color = TextPrimary,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(Modifier.height(12.dp))
            }

            if (selectedEmbassy.apiKey == null) {
                // Not supported embassy
                AccentCard(title = "ℹ️  NOT AVAILABLE", accentColor = IrishOrange) {
                    Text(
                        text = "Status check isn't available yet for ${selectedEmbassy.label}.\n\nWe currently support: New Delhi, Beijing, Abuja, Abu Dhabi, and Ankara.",
                        fontSize = 13.sp,
                        color = TextPrimary,
                        lineHeight = 18.sp,
                    )
                }
            } else {
                // Input section
                AccentCard(title = "🔎  ENTER APPLICATION NUMBER", accentColor = IrishGreen) {
                    Spacer(Modifier.height(8.dp))
                    OutlinedTextField(
                        value = applicationNumber,
                        onValueChange = {
                            applicationNumber = it
                            if (checkState is CheckUiState.Error) checkState = CheckUiState.Idle
                        },
                        label = { Text("Application Number", fontSize = 12.sp) },
                        placeholder = { Text("e.g. 63690452", fontSize = 12.sp) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp),
                        leadingIcon = { Icon(Icons.Default.Check, contentDescription = null) },
                        singleLine = true,
                    )
                    Spacer(Modifier.height(12.dp))
                    Button(
                        onClick = { onCheckStatus() },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(44.dp),
                        enabled = checkState !is CheckUiState.Loading,
                        colors = ButtonDefaults.buttonColors(containerColor = IrishGreen),
                    ) {
                        if (checkState is CheckUiState.Loading) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(20.dp),
                                color = Color.White,
                                strokeWidth = 2.dp
                            )
                        } else {
                            Text("Check Status", fontWeight = FontWeight.Bold)
                        }
                    }
                    Spacer(Modifier.height(8.dp))
                }

                // Result section
                when (val state = checkState) {
                    is CheckUiState.Error -> {
                        AccentCard(title = "⚠️  ERROR", accentColor = StatusRed) {
                            Text(
                                text = state.message,
                                fontSize = 13.sp,
                                color = StatusRed,
                                lineHeight = 18.sp,
                            )
                        }
                    }

                    is CheckUiState.Success -> {
                        val result = state.result
                        if (result.found) {
                            // Found result
                            AccentCard(
                                title = if (result.isApproved) "✅  APPROVED" else "❌  REFUSED",
                                accentColor = if (result.isApproved) IrishGreen else StatusRed
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = if (result.isApproved) Icons.Default.Check else Icons.Default.Close,
                                        contentDescription = null,
                                        modifier = Modifier.size(32.dp),
                                        tint = if (result.isApproved) IrishGreen else StatusRed
                                    )
                                    Column(modifier = Modifier.fillMaxWidth()) {
                                        Text(
                                            text = if (result.isApproved) "Your visa was APPROVED" else "Your visa was REFUSED",
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 15.sp,
                                            color = if (result.isApproved) IrishGreen else StatusRed
                                        )
                                        Spacer(Modifier.height(4.dp))
                                        Text(
                                            text = "Application: ${result.applicationNumber}",
                                            fontSize = 12.sp,
                                            color = TextSecondary
                                        )
                                        if (result.source != null) {
                                            Spacer(Modifier.height(2.dp))
                                            Text(
                                                text = "Source: ${result.source}",
                                                fontSize = 11.sp,
                                                color = TextHint
                                            )
                                        }
                                    }
                                }
                            }
                        } else {
                            // Not found
                            AccentCard(title = "⏳  NOT YET PUBLISHED", accentColor = IrishOrange) {
                                Text(
                                    text = "Application ${result.applicationNumber} is not in the current published records for ${selectedEmbassy.label}.",
                                    fontSize = 13.sp,
                                    color = TextPrimary,
                                    lineHeight = 18.sp,
                                )
                                Spacer(Modifier.height(12.dp))

                                if (result.before != null || result.after != null) {
                                    Text(
                                        text = "Nearest records:",
                                        fontWeight = FontWeight.SemiBold,
                                        fontSize = 12.sp,
                                        color = TextSecondary
                                    )
                                    Spacer(Modifier.height(8.dp))

                                    result.before?.let {
                                        Column(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .background(
                                                    Color(0xFFF2FBF5),
                                                    RoundedCornerShape(8.dp)
                                                )
                                                .padding(14.dp),
                                            verticalArrangement = Arrangement.spacedBy(10.dp)
                                        ) {
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.SpaceBetween
                                            ) {
                                                Column(modifier = Modifier.weight(1f)) {
                                                    Text(
                                                        text = "Below ↓",
                                                        fontSize = 10.sp,
                                                        color = TextHint,
                                                        fontWeight = FontWeight.SemiBold
                                                    )
                                                    Spacer(Modifier.height(4.dp))
                                                    Text(
                                                        text = "App #:",
                                                        fontSize = 9.sp,
                                                        color = TextHint
                                                    )
                                                    Text(
                                                        text = it.number,
                                                        fontWeight = FontWeight.Bold,
                                                        fontSize = 14.sp,
                                                        color = TextPrimary
                                                    )
                                                }
                                                Column(horizontalAlignment = Alignment.End, modifier = Modifier.weight(1f)) {
                                                    Text(
                                                        text = "Decision",
                                                        fontSize = 9.sp,
                                                        color = TextHint
                                                    )
                                                    Text(
                                                        text = it.decision,
                                                        fontWeight = FontWeight.Bold,
                                                        fontSize = 12.sp,
                                                        color = if (it.isApproved) IrishGreen else StatusRed
                                                    )
                                                }
                                            }
                                            if (it.difference != null) {
                                                Row(
                                                    modifier = Modifier
                                                        .fillMaxWidth()
                                                        .background(
                                                            Color.White.copy(alpha = 0.6f),
                                                            RoundedCornerShape(6.dp)
                                                        )
                                                        .padding(10.dp),
                                                    horizontalArrangement = Arrangement.SpaceBetween,
                                                    verticalAlignment = Alignment.CenterVertically
                                                ) {
                                                    Text(
                                                        text = "Difference",
                                                        fontSize = 10.sp,
                                                        color = TextHint
                                                    )
                                                    Text(
                                                        text = it.difference.toString(),
                                                        fontWeight = FontWeight.ExtraBold,
                                                        fontSize = 13.sp,
                                                        color = IrishGreen
                                                    )
                                                }
                                            }
                                        }
                                        Spacer(Modifier.height(10.dp))
                                    }

                                    result.after?.let {
                                        Column(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .background(
                                                    Color(0xFFF2FBF5),
                                                    RoundedCornerShape(8.dp)
                                                )
                                                .padding(14.dp),
                                            verticalArrangement = Arrangement.spacedBy(10.dp)
                                        ) {
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.SpaceBetween
                                            ) {
                                                Column(modifier = Modifier.weight(1f)) {
                                                    Text(
                                                        text = "Above ↑",
                                                        fontSize = 10.sp,
                                                        color = TextHint,
                                                        fontWeight = FontWeight.SemiBold
                                                    )
                                                    Spacer(Modifier.height(4.dp))
                                                    Text(
                                                        text = "App #:",
                                                        fontSize = 9.sp,
                                                        color = TextHint
                                                    )
                                                    Text(
                                                        text = it.number,
                                                        fontWeight = FontWeight.Bold,
                                                        fontSize = 14.sp,
                                                        color = TextPrimary
                                                    )
                                                }
                                                Column(horizontalAlignment = Alignment.End, modifier = Modifier.weight(1f)) {
                                                    Text(
                                                        text = "Decision",
                                                        fontSize = 9.sp,
                                                        color = TextHint
                                                    )
                                                    Text(
                                                        text = it.decision,
                                                        fontWeight = FontWeight.Bold,
                                                        fontSize = 12.sp,
                                                        color = if (it.isApproved) IrishGreen else StatusRed
                                                    )
                                                }
                                            }
                                            if (it.difference != null) {
                                                Row(
                                                    modifier = Modifier
                                                        .fillMaxWidth()
                                                        .background(
                                                            Color.White.copy(alpha = 0.6f),
                                                            RoundedCornerShape(6.dp)
                                                        )
                                                        .padding(10.dp),
                                                    horizontalArrangement = Arrangement.SpaceBetween,
                                                    verticalAlignment = Alignment.CenterVertically
                                                ) {
                                                    Text(
                                                        text = "Difference",
                                                        fontSize = 10.sp,
                                                        color = TextHint
                                                    )
                                                    Text(
                                                        text = it.difference.toString(),
                                                        fontWeight = FontWeight.ExtraBold,
                                                        fontSize = 13.sp,
                                                        color = IrishGreen
                                                    )
                                                }
                                            }
                                        }
                                    }

                                    Spacer(Modifier.height(12.dp))
                                }

                                Button(
                                    onClick = { openDecisionsPage(context, selectedEmbassy.decisionsUrl) },
                                    modifier = Modifier.fillMaxWidth(),
                                    colors = ButtonDefaults.buttonColors(containerColor = IrishGreen),
                                ) {
                                    Text("View Full Decisions List →", fontWeight = FontWeight.SemiBold, color = Color.White)
                                }
                            }
                        }
                    }

                    else -> {}
                }
            }

            Spacer(Modifier.height(16.dp))
        }
    }
}

private fun openDecisionsPage(context: Context, url: String) {
    try {
        val customTabsIntent = CustomTabsIntent.Builder()
            .build()
        customTabsIntent.launchUrl(context, Uri.parse(url))
    } catch (e: Exception) {
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
        context.startActivity(intent)
    }
}
