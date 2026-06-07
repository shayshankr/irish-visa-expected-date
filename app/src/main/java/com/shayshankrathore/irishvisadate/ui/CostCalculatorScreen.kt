package com.shayshankrathore.irishvisadate.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.shayshankrathore.irishvisadate.*
import com.shayshankrathore.irishvisadate.ui.theme.*

// Approximate VFS Global / BLS service charges per embassy (€)
private val VAC_SERVICE_FEE: Map<String, Int> = mapOf(
    "india"       to 35,
    "moscow"      to 40,
    "london"      to 35,
    "beijing"     to 38,
    "ankara"      to 36,
    "abudhabi"    to 38,
    "islamabad"   to 42,
    "germany"     to 35,
    "france"      to 35,
    "nigeria"     to 45,
    "southafrica" to 40,
    "philippines" to 38,
    "bangladesh"  to 38,
)

@Suppress("DEPRECATION")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CostCalculatorScreen(onBack: () -> Unit) {
    var selectedEmbassy  by remember { mutableStateOf(ALL_EMBASSIES[0]) }
    var selectedVisa     by remember { mutableStateOf(VisaType.SHORT_STAY) }
    var embassyExpanded  by remember { mutableStateOf(false) }

    // editable fee fields (in €, as strings for input)
    var vacFee           by remember { mutableStateOf("") }
    var biometricEnabled by remember { mutableStateOf(false) }
    var biometricFee     by remember { mutableStateOf("12") }
    var courierFee       by remember { mutableStateOf("20") }
    var documentsFee     by remember { mutableStateOf("0") }
    var travelInsurance  by remember { mutableStateOf("0") }
    var otherFees        by remember { mutableStateOf("0") }

    // when embassy changes, reset VAC fee to suggested value
    LaunchedEffect(selectedEmbassy) {
        vacFee = (VAC_SERVICE_FEE[selectedEmbassy.id] ?: 35).toString()
    }

    val visaFeeEur   = selectedVisa.feeEur
    val vacFeeEur    = vacFee.toIntOrNull() ?: 0
    val bioFeeEur    = if (biometricEnabled) biometricFee.toIntOrNull() ?: 0 else 0
    val courierEur   = courierFee.toIntOrNull() ?: 0
    val docsEur      = documentsFee.toIntOrNull() ?: 0
    val insuranceEur = travelInsurance.toIntOrNull() ?: 0
    val otherEur     = otherFees.toIntOrNull() ?: 0
    val total        = visaFeeEur + vacFeeEur + bioFeeEur + courierEur + docsEur + insuranceEur + otherEur

    Scaffold(
        containerColor = IrishGreenBg,
        topBar = {
            TopAppBar(
                title = { Text("Cost Calculator", fontWeight = FontWeight.ExtraBold) },
                navigationIcon = {
                    TextButton(onClick = onBack) { Text("← Back", color = IrishGreen) }
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
            AccentCard(title = "🌍  SELECT APPLICATION", accentColor = IrishGreen) {
                ExposedDropdownMenuBox(
                    expanded = embassyExpanded,
                    onExpandedChange = { embassyExpanded = it },
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    OutlinedTextField(
                        value = "${selectedEmbassy.flag}  ${selectedEmbassy.label}",
                        onValueChange = {}, readOnly = true,
                        modifier = Modifier.fillMaxWidth().menuAnchor(),
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = embassyExpanded) },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = IrishGreen,
                            unfocusedBorderColor = IrishGreen.copy(alpha = 0.4f),
                        ),
                        label = { Text("Embassy") },
                        shape = RoundedCornerShape(10.dp),
                    )
                    ExposedDropdownMenu(expanded = embassyExpanded, onDismissRequest = { embassyExpanded = false }) {
                        ALL_EMBASSIES.forEach { e ->
                            DropdownMenuItem(
                                text = { Text("${e.flag}  ${e.label}") },
                                onClick = { selectedEmbassy = e; embassyExpanded = false },
                            )
                        }
                    }
                }
                Spacer(Modifier.height(10.dp))
                Text("VISA TYPE", fontWeight = FontWeight.ExtraBold, fontSize = 10.sp, color = TextHint, letterSpacing = 0.8.sp)
                Spacer(Modifier.height(6.dp))
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    VisaType.entries.forEach { vt ->
                        val sel = vt == selectedVisa
                        Surface(
                            onClick = { selectedVisa = vt },
                            shape = RoundedCornerShape(8.dp),
                            border = androidx.compose.foundation.BorderStroke(1.5.dp, if (sel) IrishGreen else DividerGreen),
                            color  = if (sel) IrishGreen.copy(alpha = 0.08f) else SurfaceCard,
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                RadioButton(selected = sel, onClick = { selectedVisa = vt },
                                    colors = RadioButtonDefaults.colors(selectedColor = IrishGreen))
                                Spacer(Modifier.width(6.dp))
                                Text(vt.label, fontSize = 13.sp, color = if (sel) IrishGreenDark else TextSecondary, modifier = Modifier.weight(1f))
                                Text("€${vt.feeEur}", style = MaterialTheme.typography.labelSmall, color = if (sel) IrishGreen else TextHint, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }

            AccentCard(title = "💶  COST BREAKDOWN", accentColor = IrishGreen) {
                CostRow(
                    label = "Visa application fee",
                    note  = "Fixed Irish government fee",
                    value = "€$visaFeeEur",
                    fixed = true,
                )
                HorizontalDivider(color = DividerGreen, modifier = Modifier.padding(vertical = 8.dp))
                EditableCostRow(
                    label   = "VAC service charge",
                    note    = "VFS Global / BLS fee (suggested: €${VAC_SERVICE_FEE[selectedEmbassy.id] ?: 35})",
                    value   = vacFee,
                    onValue = { vacFee = it },
                )
                HorizontalDivider(color = DividerGreen, modifier = Modifier.padding(vertical = 8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Column(Modifier.weight(1f)) {
                        Text("Biometric enrollment", fontWeight = FontWeight.SemiBold, color = IrishGreenDark, fontSize = 13.sp)
                        Text("Fingerprints / photo (if required at your VAC)", style = MaterialTheme.typography.labelSmall, color = TextSecondary)
                    }
                    Switch(
                        checked = biometricEnabled, onCheckedChange = { biometricEnabled = it },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = IrishGreen,
                            checkedTrackColor = IrishGreen.copy(alpha = 0.4f),
                        ),
                    )
                }
                if (biometricEnabled) {
                    Spacer(Modifier.height(4.dp))
                    EditableCostRow(
                        label   = "Biometric fee",
                        note    = "Typically €10–15",
                        value   = biometricFee,
                        onValue = { biometricFee = it },
                    )
                }
                HorizontalDivider(color = DividerGreen, modifier = Modifier.padding(vertical = 8.dp))
                EditableCostRow(
                    label   = "Return courier / delivery",
                    note    = "Passport return to your address",
                    value   = courierFee,
                    onValue = { courierFee = it },
                )
                HorizontalDivider(color = DividerGreen, modifier = Modifier.padding(vertical = 8.dp))
                EditableCostRow(
                    label   = "Document preparation",
                    note    = "Translations, attestations, photos, printing",
                    value   = documentsFee,
                    onValue = { documentsFee = it },
                )
                HorizontalDivider(color = DividerGreen, modifier = Modifier.padding(vertical = 8.dp))
                EditableCostRow(
                    label   = "Travel insurance",
                    note    = "If required for your visa type",
                    value   = travelInsurance,
                    onValue = { travelInsurance = it },
                )
                HorizontalDivider(color = DividerGreen, modifier = Modifier.padding(vertical = 8.dp))
                EditableCostRow(
                    label   = "Other costs",
                    note    = "Transport to VAC, parking, misc.",
                    value   = otherFees,
                    onValue = { otherFees = it },
                )
            }

            Card(
                shape     = RoundedCornerShape(16.dp),
                colors    = CardDefaults.cardColors(containerColor = IrishGreenDark),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Column {
                        Text("TOTAL ESTIMATED COST", fontWeight = FontWeight.ExtraBold, fontSize = 11.sp, color = Color.White.copy(alpha = 0.7f), letterSpacing = 0.8.sp)
                        Text("€$total", fontWeight = FontWeight.ExtraBold, fontSize = 34.sp, color = Color.White)
                    }
                    Text("€${selectedVisa.feeEur}\nvisa fee", style = MaterialTheme.typography.labelSmall, color = Color.White.copy(alpha = 0.6f))
                }
            }

            AccentCard(title = "ℹ️  DISCLAIMER", accentColor = IrishGreen) {
                Text(
                    "These are estimates only. VAC service charges, biometric fees, and courier costs vary by country, VAC operator, and may change. Verify exact fees on the VFS Global or BLS International website for your country before applying.",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondary,
                )
            }

            Spacer(Modifier.height(16.dp))
        }
    }
}

@Composable
private fun CostRow(label: String, note: String, value: String, fixed: Boolean) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(Modifier.weight(1f)) {
            Text(label, fontWeight = FontWeight.SemiBold, color = IrishGreenDark, fontSize = 13.sp)
            Text(note, style = MaterialTheme.typography.labelSmall, color = TextSecondary)
        }
        Text(value, fontWeight = FontWeight.ExtraBold, color = IrishGreen, fontSize = 16.sp)
    }
}

@Composable
private fun EditableCostRow(label: String, note: String, value: String, onValue: (String) -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(Modifier.weight(1f)) {
            Text(label, fontWeight = FontWeight.SemiBold, color = IrishGreenDark, fontSize = 13.sp)
            Text(note, style = MaterialTheme.typography.labelSmall, color = TextSecondary)
        }
        Spacer(Modifier.width(8.dp))
        OutlinedTextField(
            value = value,
            onValueChange = { onValue(it.filter { c -> c.isDigit() }) },
            modifier = Modifier.width(80.dp),
            singleLine = true,
            prefix = { Text("€", color = IrishGreen) },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = IrishGreen,
                unfocusedBorderColor = DividerGreen,
            ),
            shape = RoundedCornerShape(8.dp),
        )
    }
}
