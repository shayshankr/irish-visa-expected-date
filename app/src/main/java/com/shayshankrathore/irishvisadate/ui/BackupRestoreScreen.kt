package com.shayshankrathore.irishvisadate.ui

import android.content.Intent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import com.shayshankrathore.irishvisadate.ui.theme.*
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BackupRestoreScreen(onBack: () -> Unit) {
    val context  = LocalContext.current
    val scope    = rememberCoroutineScope()

    var snackMessage by remember { mutableStateOf<String?>(null) }
    var importing    by remember { mutableStateOf(false) }

    val snackState = remember { SnackbarHostState() }
    LaunchedEffect(snackMessage) {
        snackMessage?.let { snackState.showSnackbar(it); snackMessage = null }
    }

    val importLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri ->
        uri ?: return@rememberLauncherForActivityResult
        scope.launch {
            importing = true
            try {
                val json = context.contentResolver.openInputStream(uri)?.bufferedReader()?.readText() ?: ""
                val count = AppPreferences.importApps(context, json)
                snackMessage = if (count > 0) "Imported $count application${if (count != 1) "s" else ""}"
                              else "No new applications found in file"
            } catch (e: Exception) {
                snackMessage = "Import failed — check the file format"
            } finally {
                importing = false
            }
        }
    }

    Scaffold(
        containerColor = IrishGreenBg,
        snackbarHost   = { SnackbarHost(snackState) },
        topBar = {
            TopAppBar(
                title = { Text("Backup & Restore", fontWeight = FontWeight.ExtraBold) },
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

            AccentCard(title = "📤  EXPORT YOUR APPLICATIONS", accentColor = IrishGreen) {
                Text(
                    "Share a JSON file of all your saved applications. Use it to back up your data or transfer it to another device.",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondary,
                )
                Spacer(Modifier.height(12.dp))
                Button(
                    onClick = {
                        scope.launch {
                            val json = AppPreferences.exportApps(context)
                            context.startActivity(
                                Intent.createChooser(
                                    Intent(Intent.ACTION_SEND).apply {
                                        type = "application/json"
                                        putExtra(Intent.EXTRA_SUBJECT, "Irish Visa Tracker — My Applications")
                                        putExtra(Intent.EXTRA_TEXT, json)
                                    }, "Export applications"
                                )
                            )
                        }
                    },
                    modifier = Modifier.fillMaxWidth().height(48.dp),
                    shape    = RoundedCornerShape(10.dp),
                    colors   = ButtonDefaults.buttonColors(containerColor = IrishGreen, contentColor = Color.White),
                ) {
                    Text("📤  Export as JSON", fontWeight = FontWeight.Bold)
                }
            }

            AccentCard(title = "📥  IMPORT APPLICATIONS", accentColor = IrishGreen) {
                Text(
                    "Import from a previously exported JSON file. Duplicate entries (same ID) are skipped automatically.",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondary,
                )
                Spacer(Modifier.height(12.dp))
                if (importing) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        CircularProgressIndicator(color = IrishGreen, modifier = Modifier.size(20.dp))
                        Spacer(Modifier.width(12.dp))
                        Text("Importing…", color = TextSecondary, fontSize = 13.sp)
                    }
                } else {
                    OutlinedButton(
                        onClick  = { importLauncher.launch(arrayOf("application/json", "text/plain", "*/*")) },
                        modifier = Modifier.fillMaxWidth().height(48.dp),
                        shape    = RoundedCornerShape(10.dp),
                        border   = androidx.compose.foundation.BorderStroke(1.5.dp, IrishGreen),
                        colors   = ButtonDefaults.outlinedButtonColors(contentColor = IrishGreen),
                    ) {
                        Text("📥  Select JSON File", fontWeight = FontWeight.SemiBold)
                    }
                }
            }

            AccentCard(title = "ℹ️  ABOUT YOUR DATA", accentColor = IrishGreen) {
                Text(
                    "All data is stored locally on your device using Android's DataStore. This app does not send any data to external servers. Backup files are plain JSON — you can open and read them with any text editor.",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondary,
                    lineHeight = 18.sp,
                )
            }
        }
    }
}
