package com.mitashi.ht4440bt.remote.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.FastForward
import androidx.compose.material.icons.rounded.FastRewind
import androidx.compose.material.icons.rounded.Pause
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material.icons.rounded.PowerSettingsNew
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mitashi.ht4440bt.remote.data.RemoteButton
import com.mitashi.ht4440bt.remote.ui.theme.PowerRed
import com.mitashi.ht4440bt.remote.ui.theme.RemoteKeyBorderDark
import com.mitashi.ht4440bt.remote.ui.theme.RemoteKeyBorderLight
import com.mitashi.ht4440bt.remote.ui.theme.RemoteKeyDark
import com.mitashi.ht4440bt.remote.ui.theme.RemoteKeyLight

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun PillKey(
    label: String,
    isDark: Boolean,
    onClick: () -> Unit,
    onLongClick: () -> Unit,
    modifier: Modifier = Modifier,
    height: Dp = 36.dp,
    fontSize: Int = 11
) {
    val bg = if (isDark) RemoteKeyDark else RemoteKeyLight
    val border = if (isDark) RemoteKeyBorderDark else RemoteKeyBorderLight
    val textColor = MaterialTheme.colorScheme.onSurface

    Box(
        modifier = modifier
            .height(height)
            .clip(RoundedCornerShape(18.dp))
            .background(bg)
            .border(1.dp, border, RoundedCornerShape(18.dp))
            .combinedClickable(onClick = onClick, onLongClick = onLongClick),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = label,
            color = textColor,
            fontSize = fontSize.sp,
            fontWeight = FontWeight.SemiBold,
            textAlign = TextAlign.Center,
            maxLines = 1
        )
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun RoundKey(
    label: String,
    isDark: Boolean,
    onClick: () -> Unit,
    onLongClick: () -> Unit,
    size: Dp = 38.dp
) {
    val bg = if (isDark) RemoteKeyDark else RemoteKeyLight
    val border = if (isDark) RemoteKeyBorderDark else RemoteKeyBorderLight

    Box(
        modifier = Modifier
            .size(size)
            .clip(CircleShape)
            .background(bg)
            .border(1.dp, border, CircleShape)
            .combinedClickable(onClick = onClick, onLongClick = onLongClick),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = label,
            color = MaterialTheme.colorScheme.onSurface,
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold
        )
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun PowerKey(
    onClick: () -> Unit,
    onLongClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .size(56.dp)
            .clip(CircleShape)
            .background(PowerRed)
            .border(2.dp, Color(0xFFFF8A80), CircleShape)
            .combinedClickable(onClick = onClick, onLongClick = onLongClick),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = Icons.Rounded.PowerSettingsNew,
            contentDescription = "Power",
            tint = Color.White,
            modifier = Modifier.size(30.dp)
        )
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun VerticalRockerKey(
    centerLabel: String,
    isDark: Boolean,
    onPlusClick: () -> Unit,
    onPlusLongClick: () -> Unit,
    onMinusClick: () -> Unit,
    onMinusLongClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val bg = if (isDark) RemoteKeyDark else RemoteKeyLight
    val border = if (isDark) RemoteKeyBorderDark else RemoteKeyBorderLight
    val textColor = MaterialTheme.colorScheme.onSurface

    Column(
        modifier = modifier
            .width(64.dp)
            .height(118.dp)
            .clip(RoundedCornerShape(32.dp))
            .background(bg)
            .border(1.dp, border, RoundedCornerShape(32.dp)),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .combinedClickable(onClick = onPlusClick, onLongClick = onPlusLongClick),
            contentAlignment = Alignment.Center
        ) {
            Text(text = "+", color = textColor, fontSize = 20.sp, fontWeight = FontWeight.Bold)
        }
        Text(
            text = centerLabel,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold
        )
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .combinedClickable(onClick = onMinusClick, onLongClick = onMinusLongClick),
            contentAlignment = Alignment.Center
        ) {
            Text(text = "−", color = textColor, fontSize = 20.sp, fontWeight = FontWeight.Bold)
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun CircularDPadCluster(
    isDark: Boolean,
    onPress: (RemoteButton) -> Unit,
    onLongPress: (RemoteButton) -> Unit
) {
    val bg = if (isDark) RemoteKeyDark else RemoteKeyLight
    val border = if (isDark) RemoteKeyBorderDark else RemoteKeyBorderLight
    val ringBg = if (isDark) Color(0xFF1F242E) else Color(0xFFDCE2EC)
    val textColor = MaterialTheme.colorScheme.onSurface

    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Top row: EQ- , EQ label , EQ+
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            RoundKey(
                label = "−",
                isDark = isDark,
                onClick = { onPress(RemoteButton.EQ_MINUS) },
                onLongClick = { onLongPress(RemoteButton.EQ_MINUS) }
            )
            Text(
                text = "EQ",
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp
            )
            RoundKey(
                label = "+",
                isDark = isDark,
                onClick = { onPress(RemoteButton.EQ_PLUS) },
                onLongClick = { onLongPress(RemoteButton.EQ_PLUS) }
            )
        }

        // Main circular D-Pad matching physical Mitashi HT4440BT wheel
        Box(
            modifier = Modifier
                .size(204.dp)
                .clip(CircleShape)
                .background(ringBg)
                .border(1.5.dp, border, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            // TUNE+ (Top)
            Box(
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .padding(top = 10.dp)
                    .width(96.dp)
                    .height(44.dp)
                    .clip(RoundedCornerShape(22.dp))
                    .background(bg)
                    .border(1.dp, border, RoundedCornerShape(22.dp))
                    .combinedClickable(
                        onClick = { onPress(RemoteButton.TUNE_UP) },
                        onLongClick = { onLongPress(RemoteButton.TUNE_UP) }
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text("TUNE+", color = textColor, fontSize = 11.sp, fontWeight = FontWeight.Bold)
            }

            // TUNE- (Bottom)
            Box(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 10.dp)
                    .width(96.dp)
                    .height(44.dp)
                    .clip(RoundedCornerShape(22.dp))
                    .background(bg)
                    .border(1.dp, border, RoundedCornerShape(22.dp))
                    .combinedClickable(
                        onClick = { onPress(RemoteButton.TUNE_DOWN) },
                        onLongClick = { onLongPress(RemoteButton.TUNE_DOWN) }
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text("TUNE−", color = textColor, fontSize = 11.sp, fontWeight = FontWeight.Bold)
            }

            // CH- / Prev (Left)
            Box(
                modifier = Modifier
                    .align(Alignment.CenterStart)
                    .padding(start = 8.dp)
                    .width(52.dp)
                    .height(74.dp)
                    .clip(RoundedCornerShape(26.dp))
                    .background(bg)
                    .border(1.dp, border, RoundedCornerShape(26.dp))
                    .combinedClickable(
                        onClick = { onPress(RemoteButton.CH_PREV) },
                        onLongClick = { onLongPress(RemoteButton.CH_PREV) }
                    ),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = Icons.Rounded.FastRewind,
                        contentDescription = "CH-",
                        tint = textColor,
                        modifier = Modifier.size(18.dp)
                    )
                    Text("CH−", color = textColor, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                }
            }

            // CH+ / Next (Right)
            Box(
                modifier = Modifier
                    .align(Alignment.CenterEnd)
                    .padding(end = 8.dp)
                    .width(52.dp)
                    .height(74.dp)
                    .clip(RoundedCornerShape(26.dp))
                    .background(bg)
                    .border(1.dp, border, RoundedCornerShape(26.dp))
                    .combinedClickable(
                        onClick = { onPress(RemoteButton.CH_NEXT) },
                        onLongClick = { onLongPress(RemoteButton.CH_NEXT) }
                    ),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = Icons.Rounded.FastForward,
                        contentDescription = "CH+",
                        tint = textColor,
                        modifier = Modifier.size(18.dp)
                    )
                    Text("CH+", color = textColor, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                }
            }

            // Center Play/Pause AUTO
            Box(
                modifier = Modifier
                    .size(72.dp)
                    .clip(CircleShape)
                    .background(if (isDark) Color(0xFF424B5E) else Color(0xFFD0D8E6))
                    .border(1.5.dp, border, CircleShape)
                    .combinedClickable(
                        onClick = { onPress(RemoteButton.PLAY_PAUSE_AUTO) },
                        onLongClick = { onLongPress(RemoteButton.PLAY_PAUSE_AUTO) }
                    ),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Rounded.PlayArrow,
                            contentDescription = "Play",
                            tint = textColor,
                            modifier = Modifier.size(16.dp)
                        )
                        Icon(
                            imageVector = Icons.Rounded.Pause,
                            contentDescription = "Pause",
                            tint = textColor,
                            modifier = Modifier.size(15.dp)
                        )
                    }
                    Text("AUTO", color = textColor, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                }
            }
        }

        // Bottom row: DISP- , DISP label , DISP+
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            RoundKey(
                label = "−",
                isDark = isDark,
                onClick = { onPress(RemoteButton.DISP_MINUS) },
                onLongClick = { onLongPress(RemoteButton.DISP_MINUS) }
            )
            Text(
                text = "DISP",
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp
            )
            RoundKey(
                label = "+",
                isDark = isDark,
                onClick = { onPress(RemoteButton.DISP_PLUS) },
                onLongClick = { onLongPress(RemoteButton.DISP_PLUS) }
            )
        }
    }
}
