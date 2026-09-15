package com.example.ui.home

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.ui.theme.*

@Composable
fun CustomTimePickerDialog(
    initialMinutes: Int,
    onDismiss: () -> Unit,
    onConfirm: (Int) -> Unit
) {
    var hours by remember { mutableIntStateOf(initialMinutes / 60) }
    var minutes by remember { mutableIntStateOf(initialMinutes % 60) }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = Color(0xFF0C101A),
            border = BorderStroke(
                1.dp,
                Brush.linearGradient(
                    listOf(
                        Color(0xFF1B2030),
                        Color(0xFF151926)
                    )
                )
            ),
            modifier = Modifier.fillMaxWidth(0.96f)
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Text(
                    text = "Custom Duration",
                    style = MaterialTheme.typography.titleMedium,
                    color = TextWhite,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(24.dp))
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Hours Column
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("Hours", color = SteelGray, style = MaterialTheme.typography.labelSmall)
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Button(
                                onClick = { if (hours > 0) hours-- },
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF131726), contentColor = SkyBlueLight),
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.size(40.dp),
                                contentPadding = PaddingValues(0.dp)
                            ) { Text("-", fontSize = 20.sp) }
                            
                            Text(
                                text = hours.toString().padStart(2, '0'),
                                style = MaterialTheme.typography.headlineMedium,
                                color = TextWhite,
                                modifier = Modifier.padding(horizontal = 16.dp)
                            )
                            
                            Button(
                                onClick = { if (hours < 23) hours++ },
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF131726), contentColor = SkyBlueLight),
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.size(40.dp),
                                contentPadding = PaddingValues(0.dp)
                            ) { Text("+", fontSize = 20.sp) }
                        }
                    }
                }
                
                Spacer(modifier = Modifier.height(20.dp))
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Minutes Column
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("Minutes", color = SteelGray, style = MaterialTheme.typography.labelSmall)
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Button(
                                onClick = { if (minutes > 0) minutes -= 5 else if (hours > 0) { hours--; minutes = 55 } },
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF131726), contentColor = SkyBlueLight),
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.size(40.dp),
                                contentPadding = PaddingValues(0.dp)
                            ) { Text("-", fontSize = 20.sp) }
                            
                            Text(
                                text = minutes.toString().padStart(2, '0'),
                                style = MaterialTheme.typography.headlineMedium,
                                color = TextWhite,
                                modifier = Modifier.padding(horizontal = 16.dp)
                            )
                            
                            Button(
                                onClick = { if (minutes < 55) minutes += 5 else { hours++; minutes = 0 } },
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF131726), contentColor = SkyBlueLight),
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.size(40.dp),
                                contentPadding = PaddingValues(0.dp)
                            ) { Text("+", fontSize = 20.sp) }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(32.dp))
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onDismiss) {
                        Text("Cancel", color = SteelGray)
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = {
                            val total = (hours * 60) + minutes
                            if (total > 0) {
                                onConfirm(total)
                            }
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = SkyBlue,
                            contentColor = Color(0xFF070A12)
                        ),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text("Set Time", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}
