package com.example.reservation_eeg_android_app.ui.auth

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import com.example.reservation_eeg_android_app.model.UserProfile
import com.example.reservation_eeg_android_app.ui.auth.viewmodel.AuthViewModel
import io.github.jan.supabase.auth.status.SessionStatus

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(viewModel: AuthViewModel) {
    val sessionStatus by viewModel.sessionStatus.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val error by viewModel.error.collectAsState()
    val userProfile by viewModel.userProfile.collectAsState()
    val updateSuccess by viewModel.updateSuccess.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    
    var isEditing by remember { mutableStateOf(false) }

    LaunchedEffect(updateSuccess) {
        if (updateSuccess) {
            snackbarHostState.showSnackbar("프로필 정보가 저장되었습니다.")
            isEditing = false
        }
    }

    Scaffold(
        topBar = { 
            TopAppBar(
                title = { Text(if (isEditing) "프로필 수정" else "프로필") },
                actions = {
                    if (sessionStatus is SessionStatus.Authenticated && !isEditing) {
                        IconButton(onClick = { isEditing = true }) {
                            Icon(Icons.Default.Edit, contentDescription = "수정")
                        }
                    }
                }
            ) 
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp),
            contentAlignment = Alignment.TopCenter
        ) {
            when (val status = sessionStatus) {
                is SessionStatus.Authenticated -> {
                    val currentProfile = userProfile ?: UserProfile(id = status.session.user?.id)
                    if (isEditing) {
                        ProfileDetailForm(
                            profile = currentProfile,
                            isLoading = isLoading,
                            error = error,
                            updateSuccess = updateSuccess,
                            onUpdate = { viewModel.updateProfile(it) },
                            onCancel = { isEditing = false },
                            onSignOut = { viewModel.signOut() }
                        )
                    } else {
                        UserProfileCard(
                            profile = currentProfile,
                            onEdit = { isEditing = true },
                            onSignOut = { viewModel.signOut() }
                        )
                    }
                }
                else -> {
                    AuthForm(viewModel, isLoading, error)
                }
            }
        }
    }
}

@Composable
fun UserProfileCard(
    profile: UserProfile,
    onEdit: () -> Unit,
    onSignOut: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                ProfileInfoRow("이름", profile.name.ifBlank { "미지정" })
                ProfileInfoRow("이메일", profile.email.ifBlank { "미지정" })
                ProfileInfoRow("전화번호", profile.phoneNumber.ifBlank { "미지정" })
                ProfileInfoRow("주소", profile.address.ifBlank { "미지정" })
                ProfileInfoRow("성별", profile.sex.ifBlank { "미지정" })
                ProfileInfoRow("가족 구성원", profile.familyMembers.ifBlank { "미지정" })
            }
        }

        Button(
            onClick = onEdit,
            modifier = Modifier.fillMaxWidth()
        ) {
            Icon(Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(ButtonDefaults.IconSize))
            Spacer(Modifier.size(ButtonDefaults.IconSpacing))
            Text("프로필 수정하기")
        }

        OutlinedButton(
            onClick = onSignOut,
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error)
        ) {
            Text("로그아웃")
        }
    }
}

@Composable
fun ProfileInfoRow(label: String, value: String) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(text = label, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.primary)
        Text(text = value, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Medium)
        HorizontalDivider(modifier = Modifier.padding(top = 4.dp), thickness = 0.5.dp, color = MaterialTheme.colorScheme.outlineVariant)
    }
}

@Composable
fun ProfileDetailForm(
    profile: UserProfile,
    isLoading: Boolean,
    error: String?,
    updateSuccess: Boolean,
    onUpdate: (UserProfile) -> Unit,
    onCancel: () -> Unit,
    onSignOut: () -> Unit
) {
    var name by remember(profile) { mutableStateOf(profile.name) }
    var sex by remember(profile) { mutableStateOf(profile.sex) }
    var residentId by remember(profile) { mutableStateOf(profile.residentId) }
    var address by remember(profile) { mutableStateOf(profile.address) }
    var phoneNumber by remember(profile) { mutableStateOf(profile.phoneNumber) }
    var familyMembers by remember(profile) { mutableStateOf(profile.familyMembers) }
    var email by remember(profile) { mutableStateOf(profile.email) }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text("사용자 정보 설정", style = MaterialTheme.typography.headlineSmall)
        
        if (updateSuccess) {
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                ),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    "프로필 정보가 성공적으로 저장되었습니다.",
                    modifier = Modifier.padding(16.dp),
                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }

        OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("이름") }, modifier = Modifier.fillMaxWidth())
        OutlinedTextField(value = sex, onValueChange = { sex = it }, label = { Text("성별") }, modifier = Modifier.fillMaxWidth())
        OutlinedTextField(
            value = residentId,
            onValueChange = { residentId = it },
            label = { Text("주민등록번호") },
            modifier = Modifier.fillMaxWidth(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
        )
        OutlinedTextField(value = address, onValueChange = { address = it }, label = { Text("주소") }, modifier = Modifier.fillMaxWidth())
        OutlinedTextField(
            value = phoneNumber,
            onValueChange = { phoneNumber = it },
            label = { Text("전화번호") },
            modifier = Modifier.fillMaxWidth(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone)
        )
        OutlinedTextField(value = familyMembers, onValueChange = { familyMembers = it }, label = { Text("가족 구성원") }, modifier = Modifier.fillMaxWidth())
        OutlinedTextField(
            value = email,
            onValueChange = { email = it },
            label = { Text("이메일") },
            modifier = Modifier.fillMaxWidth(),
            enabled = profile.email.isBlank(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email)
        )

        if (error != null) {
            Text(text = error, color = MaterialTheme.colorScheme.error)
        }

        val isFormValid = name.isNotBlank() && phoneNumber.isNotBlank() && residentId.isNotBlank()

        Button(
            onClick = {
                onUpdate(profile.copy(
                    name = name,
                    sex = sex,
                    residentId = residentId,
                    address = address,
                    phoneNumber = phoneNumber,
                    familyMembers = familyMembers,
                    email = email
                ))
            },
            modifier = Modifier.fillMaxWidth(),
            enabled = !isLoading && isFormValid
        ) {
            if (isLoading) CircularProgressIndicator(modifier = Modifier.size(24.dp), color = MaterialTheme.colorScheme.onPrimary, strokeWidth = 2.dp)
            else Text("저장하기")
        }

        OutlinedButton(onClick = onCancel, modifier = Modifier.fillMaxWidth()) {
            Text("취소")
        }

        TextButton(onClick = onSignOut, modifier = Modifier.fillMaxWidth()) {
            Text("로그아웃", color = MaterialTheme.colorScheme.error)
        }
    }
}

@Composable
fun AuthForm(viewModel: AuthViewModel, isLoading: Boolean, error: String?) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var isSignUp by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            if (isSignUp) "회원가입" else "로그인",
            style = MaterialTheme.typography.headlineSmall
        )
        Spacer(modifier = Modifier.height(16.dp))
        
        OutlinedTextField(
            value = email,
            onValueChange = { email = it },
            label = { Text("이메일") },
            modifier = Modifier.fillMaxWidth(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email)
        )
        Spacer(modifier = Modifier.height(8.dp))
        
        OutlinedTextField(
            value = password,
            onValueChange = { password = it },
            label = { Text("비밀번호") },
            visualTransformation = PasswordVisualTransformation(),
            modifier = Modifier.fillMaxWidth(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password)
        )
        
        if (error != null) {
            Text(
                text = error,
                color = MaterialTheme.colorScheme.error,
                modifier = Modifier.padding(top = 8.dp)
            )
        }
        
        Spacer(modifier = Modifier.height(24.dp))
        
        Button(
            onClick = {
                if (isSignUp) viewModel.signUp(email, password)
                else viewModel.signIn(email, password)
            },
            modifier = Modifier.fillMaxWidth(),
            enabled = !isLoading && email.isNotBlank() && password.isNotBlank()
        ) {
            if (isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(24.dp),
                    color = MaterialTheme.colorScheme.onPrimary,
                    strokeWidth = 2.dp
                )
            } else {
                Text(if (isSignUp) "가입하기" else "로그인")
            }
        }
        
        TextButton(onClick = { isSignUp = !isSignUp }) {
            Text(if (isSignUp) "이미 계정이 있으신가요? 로그인" else "계정이 없으신가요? 회원가입")
        }

        Spacer(modifier = Modifier.height(16.dp))
        HorizontalDivider()
        Spacer(modifier = Modifier.height(16.dp))

        OutlinedButton(
            onClick = { viewModel.signInWithGoogle() },
            modifier = Modifier.fillMaxWidth(),
            enabled = !isLoading
        ) {
            Text("Google로 로그인")
        }

        Spacer(modifier = Modifier.height(8.dp))

        Button(
            onClick = { viewModel.signInWithKakao() },
            modifier = Modifier.fillMaxWidth(),
            enabled = !isLoading,
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFFFEE500),
                contentColor = Color(0xFF191919)
            )
        ) {
            Text("카카오로 로그인")
        }
    }
}
