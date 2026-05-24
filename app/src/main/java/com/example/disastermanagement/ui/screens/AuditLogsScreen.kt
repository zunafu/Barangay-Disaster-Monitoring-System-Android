package com.example.disastermanagement.ui.screens

import android.content.Context
import android.graphics.Paint
import android.graphics.pdf.PdfDocument
import android.os.Environment
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.example.disastermanagement.R
import com.example.disastermanagement.data.database.AuditLog
import com.example.disastermanagement.data.database.AuditLogDao
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileOutputStream
import java.io.OutputStream
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun AuditLogsScreen(auditLogDao: AuditLogDao, context: Context) {
    val logsFlow = remember { auditLogDao.getAllLogs() }
    val coroutineScope = rememberCoroutineScope()
    var logs by remember { mutableStateOf<List<AuditLog>>(emptyList()) }

    LaunchedEffect(Unit) {
        logsFlow.collectLatest { list ->
            logs = list
        }
    }

    // Launcher to create a document (let user pick where to save)
    val createPdfLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("application/pdf")
    ) { uri ->
        if (uri != null) {
            coroutineScope.launch {
                val success = writeLogsPdfToUri(context, logs, uri)
                if (success) {
                    Toast.makeText(context, "Exported audit logs to: $uri", Toast.LENGTH_LONG).show()
                } else {
                    Toast.makeText(context, "Failed to export audit logs.", Toast.LENGTH_LONG).show()
                }
            }
        } else {
            Toast.makeText(context, "Export cancelled.", Toast.LENGTH_SHORT).show()
        }
    }

    Scaffold {
        Column(modifier = Modifier.padding(it).fillMaxSize()) {
            Row(modifier = Modifier.fillMaxWidth().padding(16.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Text("Audit Logs", style = MaterialTheme.typography.titleLarge)
                Row {
                    Button(onClick = {
                        // Suggest a filename when prompting the user
                        val suggested = "audit_logs_${System.currentTimeMillis()}.pdf"
                        createPdfLauncher.launch(suggested)
                    }) {
                        Text("Export to PDF")
                    }
                }
            }

            LazyColumn(modifier = Modifier.fillMaxSize().padding(8.dp)) {
                items(logs) { log ->
                    AuditLogItem(log = log)
                }
            }
        }
    }
}

@Composable
fun AuditLogItem(log: AuditLog) {
    var expanded by remember { mutableStateOf(false) }
    Card(modifier = Modifier
        .fillMaxWidth()
        .padding(vertical = 4.dp)
        .clickable { expanded = !expanded }) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text(text = "${formatTs(log.timestamp)} — ${log.actionType} (${log.targetType})", style = MaterialTheme.typography.bodyLarge)
            Text(text = "Actor: ${log.actorEmail} (${log.actorId})", style = MaterialTheme.typography.bodySmall)
            Text(text = "Target ID: ${log.targetId}", style = MaterialTheme.typography.bodySmall)
            if (expanded) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(text = "Log ID: ${log.id}", style = MaterialTheme.typography.bodySmall)
                Text(text = "Details: ${log.details}", style = MaterialTheme.typography.bodySmall)
                Spacer(modifier = Modifier.height(4.dp))
                Row(horizontalArrangement = Arrangement.End, modifier = Modifier.fillMaxWidth()) {
                    IconButton(onClick = { expanded = false }) {
                        Icon(painter = painterResource(id = android.R.drawable.ic_menu_close_clear_cancel), contentDescription = "Close")
                    }
                }
            } else {
                Spacer(modifier = Modifier.height(4.dp))
                Text(text = if (log.details.isNotEmpty()) log.details.take(100) + if (log.details.length > 100) "..." else "" else "", style = MaterialTheme.typography.bodySmall)
            }
        }
    }
}

private fun formatTs(ts: Long): String {
    val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
    return sdf.format(Date(ts))
}

suspend fun writeLogsPdfToUri(context: Context, logs: List<AuditLog>, uri: android.net.Uri): Boolean {
    if (logs.isEmpty()) return false
    return try {
        context.contentResolver.openOutputStream(uri)?.use { outStream ->
            writeLogsPdfToStream(outStream, logs)
        } ?: throw IllegalStateException("Unable to open output stream for URI: $uri")
        true
    } catch (e: Exception) {
        e.printStackTrace()
        false
    }
}

fun writeLogsPdfToStream(outStream: OutputStream, logs: List<AuditLog>) {
    val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
    val pdf = PdfDocument()
    val paint = Paint().apply { textSize = 12f }
    val pageWidth = 595
    val pageHeight = 842
    val margin = 40
    var yPosition = margin
    var pageNumber = 1

    fun newPage(): PdfDocument.Page {
        val info = PdfDocument.PageInfo.Builder(pageWidth, pageHeight, pageNumber++).create()
        return pdf.startPage(info)
    }

    var page = newPage()

    for (log in logs) {
        val lines = listOf(
            "${sdf.format(Date(log.timestamp))} | ${log.actionType} | ${log.targetType} | ${log.targetId}",
            "Actor: ${log.actorEmail} (${log.actorId})",
            "Log ID: ${log.id}",
            if (log.details.isNotEmpty()) "Details: ${log.details}" else ""
        ).filter { it.isNotEmpty() }

        for (line in lines) {
            if (yPosition + 20 > pageHeight - margin) {
                pdf.finishPage(page)
                page = newPage()
                yPosition = margin
            }
            page.canvas.drawText(line, margin.toFloat(), yPosition.toFloat(), paint)
            yPosition += 18
        }
        yPosition += 8
    }

    pdf.finishPage(page)
    pdf.writeTo(outStream)
    pdf.close()
}
