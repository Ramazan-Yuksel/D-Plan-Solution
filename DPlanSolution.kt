
package com.dplan.gorevler

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch


// VERİ MODELİ
data class Gorev(
    val id: String,
    val baslik: String,
    var saat: String,   // "08:00"
    var gun: String,    // "Eyl 9"
    var tamamlandi: Boolean = false
)


// 1) ESKİ KOD — sadece tamamlandı/tamamlanmadı toggle var,
//    saat/gün sabit, değiştirilemiyor

@Composable
fun GorevSatiriEski(
    gorev: Gorev,
    onToggle: (Gorev) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp, horizontal = 16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Checkbox(checked = gorev.tamamlandi, onCheckedChange = { onToggle(gorev) })
            Text(text = gorev.baslik)
        }
        Text(text = gorev.saat) // <-- burada sabit, dokunsan da bir şey olmuyor
    }
}


// 2) YENİ KOD — göreve uzun basınca saat/gün düzenleme
//    bottom sheet'i açılıyor, kapanış animasyonlu

@OptIn(ExperimentalFoundationApi::class, ExperimentalMaterial3Api::class)
@Composable
fun GorevSatiriYeni(
    gorev: Gorev,
    onToggle: (Gorev) -> Unit,
    onDuzenle: (Gorev) -> Unit   // kullanıcı kaydedince güncellenmiş Gorev döner
) {
    var duzenlemeAcik by remember { mutableStateOf(false) }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .combinedClickable(
                onClick = { onToggle(gorev) },
                onLongClick = { duzenlemeAcik = true }   // <-- yeni etkileşim noktası
            )
            .padding(vertical = 8.dp, horizontal = 16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Checkbox(checked = gorev.tamamlandi, onCheckedChange = { onToggle(gorev) })
            Text(text = gorev.baslik)
        }
        Text(text = gorev.saat)
    }

    if (duzenlemeAcik) {
        GorevDuzenleBottomSheet(
            gorev = gorev,
            onKaydet = { yeniSaat, yeniGun ->
                onDuzenle(gorev.copy(saat = yeniSaat, gun = yeniGun))
            },
            onKapat = { duzenlemeAcik = false }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GorevDuzenleBottomSheet(
    gorev: Gorev,
    onKaydet: (yeniSaat: String, yeniGun: String) -> Unit,
    onKapat: () -> Unit
) {
    var saat by remember { mutableStateOf(gorev.saat) }
    var gun by remember { mutableStateOf(gorev.gun) }

    val sheetState = rememberModalBottomSheetState()
    val scope = rememberCoroutineScope()

    // Ortak kapatma fonksiyonu: önce animasyonla kaydır, animasyon
    // bitince hem veriyi kaydet hem de composable'ı kaldır.
    fun kapatVeKaydet(kaydediliyorMu: Boolean) {
        scope.launch { sheetState.hide() }.invokeOnCompletion {
            if (!sheetState.isVisible) {
                if (kaydediliyorMu) onKaydet(saat, gun)
                onKapat()
            }
        }
    }

    ModalBottomSheet(
        sheetState = sheetState,
        onDismissRequest = { kapatVeKaydet(kaydediliyorMu = false) } // scrim'e tıklama / geri tuşu
    ) {
        Column(modifier = Modifier.padding(24.dp)) {
            Text(text = gorev.baslik, style = MaterialTheme.typography.titleMedium)
            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = saat,
                onValueChange = { saat = it },
                label = { Text("Saat") },
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(8.dp))

            OutlinedTextField(
                value = gun,
                onValueChange = { gun = it },
                label = { Text("Gün") },
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = { kapatVeKaydet(kaydediliyorMu = true) },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Kaydet")
            }
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}