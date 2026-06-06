package com.shayshankrathore.irishvisadate.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.shayshankrathore.irishvisadate.ui.theme.IrishGreen
import com.shayshankrathore.irishvisadate.ui.theme.IrishGreenDark
import com.shayshankrathore.irishvisadate.ui.theme.IrishGreenBg
import com.shayshankrathore.irishvisadate.ui.theme.TextSecondary

private data class ChecklistItem(val text: String, var checked: Boolean = false)

private fun checklistFor(visaType: VisaType): List<Pair<String, List<String>>> {
    val common = listOf(
        "Valid passport (6+ months validity beyond end of trip)",
        "Completed online visa application form",
        "Two recent passport-size photographs",
        "Proof of travel insurance (min €25,000 coverage)",
        "Bank statements for the last 6 months",
        "Proof of accommodation in Ireland",
        "Evidence of ties to home country (employment letter, property, family)",
        "Proof of sufficient funds for trip",
        "Visa application fee payment receipt",
    )
    val extra = when (visaType) {
        VisaType.SHORT_STAY -> listOf(
            "Return or onward travel ticket",
            "Detailed itinerary / letter of invitation (if visiting someone)",
        )
        VisaType.STUDY -> listOf(
            "Letter of acceptance from Irish educational institution",
            "Proof of English language proficiency (IELTS, TOEFL, etc.)",
            "Evidence of tuition fee payment",
            "Proof of accommodation for duration of studies",
            "Evidence of how you will support yourself financially during studies",
        )
        VisaType.JOIN_FAMILY -> listOf(
            "Birth or marriage certificate proving relationship to sponsor",
            "Sponsor's Irish Residence Permit (IRP) card copy",
            "Sponsor's recent payslips (last 3 months)",
            "Sponsor's employment letter confirming salary",
            "Sponsor's P60 / tax assessment documents",
            "Evidence sponsor can support you financially",
        )
        VisaType.WORK_PERMIT -> listOf(
            "Employment contract or signed job offer",
            "Critical Skills Employment Permit (CSEP) issued by DETE",
            "Proof of relevant qualifications / degree certificate",
            "Evidence of employer's registration in Ireland",
        )
        VisaType.WORKING_HOLIDAY -> listOf(
            "Return or onward travel ticket",
            "Proof of sufficient funds (minimum €2,000 recommended)",
            "Evidence you meet the age requirement (18–35)",
        )
        VisaType.TRANSIT -> listOf(
            "Confirmed onward travel ticket through Ireland",
            "Visa for destination country (if applicable)",
            "Proof of transit accommodation (if overnight)",
        )
    }
    return listOf(
        "Required for all visa types" to common,
        "Additional requirements for ${visaType.label}" to extra,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VisaChecklistScreen(visaType: VisaType, onBack: () -> Unit) {
    val sections = remember(visaType) {
        checklistFor(visaType).map { (title, items) ->
            title to items.map { mutableStateOf(false) to it }
        }
    }

    Scaffold(
        containerColor = IrishGreenBg,
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("Document Checklist", fontWeight = FontWeight.ExtraBold)
                        Text(visaType.label, style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                },
                navigationIcon = {
                    TextButton(onClick = onBack) { Text("← Back", color = IrishGreen) }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = androidx.compose.ui.graphics.Color(0xFF063320),
                    titleContentColor = androidx.compose.ui.graphics.Color.White,
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
            sections.forEach { (sectionTitle, items) ->
                AccentCard(title = sectionTitle.uppercase(), accentColor = IrishGreen) {
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        items.forEach { (checkedState, text) ->
                            var checked by checkedState
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.Top,
                            ) {
                                Checkbox(
                                    checked = checked,
                                    onCheckedChange = { checked = it },
                                    colors = CheckboxDefaults.colors(
                                        checkedColor = IrishGreen,
                                        uncheckedColor = IrishGreen.copy(alpha = 0.5f),
                                    ),
                                    modifier = Modifier.size(36.dp),
                                )
                                Spacer(Modifier.width(4.dp))
                                Text(
                                    text = text,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = if (checked) TextSecondary else IrishGreenDark,
                                    modifier = Modifier.padding(top = 10.dp),
                                    lineHeight = 20.sp,
                                )
                            }
                        }
                    }
                }
            }

            Text(
                text = "Requirements may vary — always verify with the relevant embassy before submitting.",
                style = MaterialTheme.typography.labelSmall,
                color = TextSecondary,
                modifier = Modifier.padding(horizontal = 4.dp, vertical = 8.dp),
            )
        }
    }
}
