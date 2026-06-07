package com.shayshankrathore.irishvisadate.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.shayshankrathore.irishvisadate.AppPreferences
import com.shayshankrathore.irishvisadate.AppPreferences.TrackedDocument
import com.shayshankrathore.irishvisadate.ui.theme.*
import kotlinx.coroutines.launch
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit
import java.util.Locale
import java.util.UUID

private val DATE_FMT_DT = DateTimeFormatter.ofPattern("d MMM yyyy", Locale.getDefault())
private fun LocalDate.fmtDt() = format(DATE_FMT_DT)

private val DOCUMENT_CATEGORIES = listOf(
    "Police Clearance" to "👮",
    "Bank Statement"   to "🏦",
    "Employment Letter" to "💼",
    "Insurance Policy" to "🛡️",
    "Invitation Letter" to "✉️",
    "Medical Certificate" to "🏥",
    "Travel Insurance" to "✈️",
    "Other"            to "📄",
)

@Suppress("DEPRECATION")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DocumentTrackerScreen(onBack: () -> Unit) {
    val context = LocalContext.current
    val scope   = rememberCoroutineScope()
    val docs by AppPreferences.trackedDocumentsFlow(context).collectAsState(initial = emptyList())
    val today   = LocalDate.now()

    var showAddDialog    by remember { mutableStateOf(false) }
    var addName          by remember { mutableStateOf("") }
    var addCategory      by remember { mutableStateOf(DOCUMENT_CATEGORIES[0].first) }
    var addExpiryDate    by remember { mutableStateOf<LocalDate?>(null) }
    var showExpiryPicker by remember { mutableStateOf(false) }
    var categoryExpanded by remember { mutableStateOf(false) }

    val expiryPickerState = rememberDatePickerState()

    if (showExpiryPicker) {
        DatePickerDialog(
            onDismissRequest = { showExpiryPicker = false },
            confirmButton = {
                TextButton(onClick = {
                    expiryPickerState.selectedDateMillis?.let {
                        addExpiryDate = Instant.ofEpochMilli(it).atZone(ZoneOffset.UTC).toLocalDate()
                    }
                    showExpiryPicker = false
                }) { Text("OK", color = IrishGreen, fontWeight = FontWeight.Bold) }
            },
            dismissButton = { TextButton(onClick = { showExpiryPicker = false }) { Text("Cancel") } },
        ) { DatePicker(state = expiryPickerState) }
    }

    if (showAddDialog) {
        AlertDialog(
            onDismissRequest = { showAddDialog = false },
            title = { Text("Add Document", fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedTextField(
                        value = addName,
                        onValueChange = { addName = it },
                        label = { Text("Document name") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = IrishGreen,
                            focusedLabelColor  = IrishGreen,
                        ),
                    )
                    ExposedDropdownMenuBox(
                        expanded = categoryExpanded,
                        onExpandedChange = { categoryExpanded = it },
                    ) {
                        OutlinedTextField(
                            value = addCategory,
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Category") },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = categoryExpanded) },
                            modifier = Modifier.fillMaxWidth().menuAnchor(),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = IrishGreen,
                                focusedLabelColor  = IrishGreen,
                            ),
                        )
                        ExposedDropdownMenu(expanded = categoryExpanded, onDismissRequest = { categoryExpanded = false }) {
                            DOCUMENT_CATEGORIES.forEach { (cat, emoji) ->
                                DropdownMenuItem(
                                    text = { Text("$emoji  $cat") },
                                    onClick = { addCategory = cat; categoryExpanded = false },
                                )
                            }
                        }
                    }
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Column(Modifier.weight(1f)) {
                            Text("Expiry date", style = MaterialTheme.typography.labelSmall, color = TextHint)
                            Text(
                                addExpiryDate?.fmtDt() ?: "Not set",
                                fontWeight = FontWeight.SemiBold,
                                color = if (addExpiryDate == null) TextHint else IrishGreenDark,
                            )
                        }
                        TextButton(onClick = { showExpiryPicker = true }) {
                            Text(if (addExpiryDate == null) "Pick date" else "Change", color = IrishGreen)
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        val expiry = addExpiryDate
                        if (addName.isNotBlank() && expiry != null) {
                            scope.launch {
                                AppPreferences.saveDocument(
                                    context,
                                    TrackedDocument(
                                        id           = UUID.randomUUID().toString(),
                                        name         = addName.trim(),
                                        category     = addCategory,
                                        expiryDate   = expiry.toString(),
                                        reminderDays = 30,
                                    )
                                )
                            }
                            showAddDialog = false
                            addName = ""; addCategory = DOCUMENT_CATEGORIES[0].first; addExpiryDate = null
                        }
                    },
                    enabled = addName.isNotBlank() && addExpiryDate != null,
                ) { Text("Save", color = IrishGreen, fontWeight = FontWeight.Bold) }
            },
            dismissButton = {
                TextButton(onClick = {
                    showAddDialog = false; addName = ""; addExpiryDate = null
                }) { Text("Cancel") }
            },
        )
    }

    Scaffold(
        containerColor = IrishGreenBg,
        topBar = {
            TopAppBar(
                title = { Text("Document Tracker", fontWeight = FontWeight.ExtraBold) },
                navigationIcon = {
                    TextButton(onClick = onBack) { Text("← Back", color = IrishGreen) }
                },
                actions = {
                    TextButton(onClick = { showAddDialog = true }) {
                        Text("+ Add", color = IrishGreen, fontWeight = FontWeight.Bold)
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
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            AccentCard(title = "📂  DOCUMENT EXPIRY TRACKER", accentColor = IrishGreen) {
                Text(
                    "Track documents that have expiry dates — police certificates (12 months), bank statements (3–6 months), employment letters, insurance policies, and more. Tap + Add to begin.",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondary,
                )
            }

            if (docs.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxWidth().padding(32.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("📂", fontSize = 48.sp)
                        Spacer(Modifier.height(12.dp))
                        Text("No documents tracked yet", fontWeight = FontWeight.SemiBold, color = IrishGreenDark)
                        Spacer(Modifier.height(6.dp))
                        Text("Tap + Add in the top right to track a document.", style = MaterialTheme.typography.bodySmall, color = TextSecondary)
                        Spacer(Modifier.height(12.dp))
                        Button(
                            onClick = { showAddDialog = true },
                            colors = ButtonDefaults.buttonColors(containerColor = IrishGreen),
                            shape = RoundedCornerShape(10.dp),
                        ) { Text("+ Add Document", color = Color.White, fontWeight = FontWeight.SemiBold) }
                    }
                }
            } else {
                val expired  = docs.filter { runCatching { LocalDate.parse(it.expiryDate).isBefore(today) }.getOrElse { false } }
                val warning  = docs.filter {
                    runCatching {
                        val exp = LocalDate.parse(it.expiryDate)
                        val days = ChronoUnit.DAYS.between(today, exp)
                        days in 0..89 && !exp.isBefore(today)
                    }.getOrElse { false }
                }
                val ok       = docs.filter { d -> expired.none { it.id == d.id } && warning.none { it.id == d.id } }

                if (expired.isNotEmpty()) {
                    SectionHeader("🔴  EXPIRED (${expired.size})")
                    expired.forEach { doc ->
                        DocumentCard(doc, today, scope, context)
                    }
                }
                if (warning.isNotEmpty()) {
                    SectionHeader("🟡  EXPIRING SOON (${warning.size})")
                    warning.sortedBy { it.expiryDate }.forEach { doc ->
                        DocumentCard(doc, today, scope, context)
                    }
                }
                if (ok.isNotEmpty()) {
                    SectionHeader("🟢  VALID (${ok.size})")
                    ok.sortedBy { it.expiryDate }.forEach { doc ->
                        DocumentCard(doc, today, scope, context)
                    }
                }
            }

            Spacer(Modifier.height(16.dp))
        }
    }
}

@Composable
private fun SectionHeader(title: String) {
    Text(title, fontWeight = FontWeight.ExtraBold, fontSize = 11.sp, color = TextSecondary, letterSpacing = 0.5.sp, modifier = Modifier.padding(vertical = 2.dp))
}

@Composable
private fun DocumentCard(
    doc: TrackedDocument,
    today: LocalDate,
    scope: kotlinx.coroutines.CoroutineScope,
    context: android.content.Context,
) {
    val expiry     = runCatching { LocalDate.parse(doc.expiryDate) }.getOrNull()
    val daysLeft   = expiry?.let { ChronoUnit.DAYS.between(today, it) }
    val isExpired  = daysLeft != null && daysLeft < 0
    val isWarning  = daysLeft != null && daysLeft in 0..89
    val statusColor = when {
        isExpired -> StatusRed
        isWarning -> StatusAmber
        else      -> IrishGreen
    }
    val emoji = DOCUMENT_CATEGORIES.find { it.first == doc.category }?.second ?: "📄"

    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceCard),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
    ) {
        Row(modifier = Modifier.heightIn(min = 60.dp)) {
            Box(
                modifier = Modifier
                    .width(5.dp)
                    .fillMaxHeight()
                    .background(statusColor, RoundedCornerShape(topStart = 12.dp, bottomStart = 12.dp))
            )
            Row(
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 12.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(emoji, fontSize = 22.sp)
                Spacer(Modifier.width(10.dp))
                Column(Modifier.weight(1f)) {
                    Text(doc.name, fontWeight = FontWeight.SemiBold, color = IrishGreenDark, fontSize = 14.sp)
                    Text(doc.category, style = MaterialTheme.typography.labelSmall, color = TextHint)
                    Spacer(Modifier.height(2.dp))
                    Text(
                        when {
                            expiry == null  -> "No date set"
                            isExpired       -> "Expired ${expiry.fmtDt()} (${-daysLeft!!}d ago)"
                            daysLeft!! == 0L -> "Expires today!"
                            else            -> "Expires ${expiry.fmtDt()} (${daysLeft}d)"
                        },
                        style = MaterialTheme.typography.bodySmall,
                        color = statusColor,
                        fontWeight = FontWeight.SemiBold,
                    )
                }
                TextButton(
                    onClick = { scope.launch { AppPreferences.deleteDocument(context, doc.id) } },
                ) { Text("🗑️", fontSize = 16.sp) }
            }
        }
    }
}
