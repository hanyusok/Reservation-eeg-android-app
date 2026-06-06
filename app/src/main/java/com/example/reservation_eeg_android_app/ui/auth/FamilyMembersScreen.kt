package com.example.reservation_eeg_android_app.ui.auth

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.reservation_eeg_android_app.model.FamilyMember
import com.example.reservation_eeg_android_app.ui.auth.viewmodel.AuthViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FamilyMembersScreen(
    viewModel: AuthViewModel,
    onBack: () -> Unit
) {
    val familyMembers by viewModel.familyMembers.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val error by viewModel.error.collectAsState()
    
    var showAddDialog by remember { mutableStateOf(false) }
    var memberToEdit by remember { mutableStateOf<FamilyMember?>(null) }

    LaunchedEffect(Unit) {
        viewModel.fetchFamilyMembers()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("가족 구성원 관리") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { showAddDialog = true }) {
                Icon(Icons.Default.Add, contentDescription = "Add Family Member")
            }
        }
    ) { innerPadding ->
        Box(modifier = Modifier.fillMaxSize().padding(innerPadding)) {
            if (familyMembers.isEmpty() && !isLoading) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("등록된 가족 구성원이 없습니다.")
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize().padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(familyMembers) { member ->
                        FamilyMemberItem(
                            member = member,
                            onEdit = { memberToEdit = member },
                            onDelete = { viewModel.deleteFamilyMember(member.id ?: 0) }
                        )
                    }
                }
            }

            if (isLoading) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            }
            
            if (error != null) {
                Text(
                    text = error!!,
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.align(Alignment.BottomCenter).padding(16.dp)
                )
            }
        }
    }

    if (showAddDialog || memberToEdit != null) {
        FamilyMemberDialog(
            member = memberToEdit,
            onDismiss = { 
                showAddDialog = false
                memberToEdit = null
            },
            onConfirm = { name, relationship, residentId, phoneNumber ->
                if (memberToEdit != null) {
                    viewModel.updateFamilyMember(memberToEdit!!.copy(
                        name = name,
                        relationship = relationship,
                        residentId = residentId,
                        phoneNumber = phoneNumber
                    ))
                } else {
                    viewModel.addFamilyMember(FamilyMember(
                        name = name,
                        relationship = relationship,
                        residentId = residentId,
                        phoneNumber = phoneNumber
                    ))
                }
                showAddDialog = false
                memberToEdit = null
            }
        )
    }
}

@Composable
fun FamilyMemberItem(
    member: FamilyMember,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(text = member.name, style = MaterialTheme.typography.titleMedium)
                Text(text = member.relationship, style = MaterialTheme.typography.bodyMedium)
                if (member.phoneNumber.isNotBlank()) {
                    Text(text = member.phoneNumber, style = MaterialTheme.typography.bodySmall)
                }
            }
            IconButton(onClick = onEdit) {
                Icon(Icons.Default.Edit, contentDescription = "Edit")
            }
            IconButton(onClick = onDelete) {
                Icon(Icons.Default.Delete, contentDescription = "Delete", tint = MaterialTheme.colorScheme.error)
            }
        }
    }
}

@Composable
fun FamilyMemberDialog(
    member: FamilyMember?,
    onDismiss: () -> Unit,
    onConfirm: (String, String, String, String) -> Unit
) {
    var name by remember { mutableStateOf(member?.name ?: "") }
    var relationship by remember { mutableStateOf(member?.relationship ?: "") }
    var residentId by remember { mutableStateOf(member?.residentId ?: "") }
    var phoneNumber by remember { mutableStateOf(member?.phoneNumber ?: "") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (member == null) "가족 구성원 추가" else "가족 구성원 수정") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("이름") })
                OutlinedTextField(value = relationship, onValueChange = { relationship = it }, label = { Text("관계 (예: 배우자, 자녀)") })
                OutlinedTextField(value = residentId, onValueChange = { residentId = it }, label = { Text("주민등록번호") })
                OutlinedTextField(value = phoneNumber, onValueChange = { phoneNumber = it }, label = { Text("전화번호") })
            }
        },
        confirmButton = {
            Button(
                onClick = { onConfirm(name, relationship, residentId, phoneNumber) },
                enabled = name.isNotBlank() && relationship.isNotBlank()
            ) {
                Text("저장")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("취소")
            }
        }
    )
}
