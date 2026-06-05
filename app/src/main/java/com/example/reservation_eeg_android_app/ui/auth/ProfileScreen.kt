package com.example.reservation_eeg_android_app.ui.auth

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
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

    Scaffold(
        topBar = { TopAppBar(title = { Text("프로필") }) }
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
                    ProfileDetailForm(
                        profile = userProfile ?: UserProfile(id = status.session.user?.id),
                        isLoading = isLoading,
                        error = error,
                        onUpdate = { viewModel.updateProfile(it) },
                        onSignOut = { viewModel.signOut() }
                    )
                }
                else -> {
                    AuthForm(viewModel, isLoading, error)
                }
            }
        }
    }
}

@Composable
fun ProfileDetailForm(
    profile: UserProfile,
    isLoading: Boolean,
    error: String?,
    onUpdate: (UserProfile) -> Unit,
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
        
        OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("이름") }, modifier = Modifier.fillMaxWidth())
        OutlinedTextField(value = sex, onValueChange = { sex = it }, label = { Text("성별") }, modifier = Modifier.fillMaxWidth())
        OutlinedTextField(value = residentId, onValueChange = { residentId = it }, label = { Text("주민등록번호") }, modifier = Modifier.fillMaxWidth())
        OutlinedTextField(value = address, onValueChange = { address = it }, label = { Text("주소") }, modifier = Modifier.fillMaxWidth())
        OutlinedTextField(value = phoneNumber, onValueChange = { phoneNumber = it }, label = { Text("전화번호") }, modifier = Modifier.fillMaxWidth())
        OutlinedTextField(value = familyMembers, onValueChange = { familyMembers = it }, label = { Text("가족 구성원") }, modifier = Modifier.fillMaxWidth())
        OutlinedTextField(value = email, onValueChange = { email = it }, label = { Text("이메일") }, modifier = Modifier.fillMaxWidth(), enabled = false)

        if (error != null) {
            Text(text = error, color = MaterialTheme.colorScheme.error)
        }

        Button(
            onClick = {
                onUpdate(profile.copy(
                    name = name,
                    sex = sex,
                    residentId = residentId,
                    address = address,
                    phoneNumber = phoneNumber,
                    familyMembers = familyMembers
                ))
            },
            modifier = Modifier.fillMaxWidth(),
            enabled = !isLoading
        ) {
            if (isLoading) CircularProgressIndicator(modifier = Modifier.size(24.dp), color = MaterialTheme.colorScheme.onPrimary, strokeWidth = 2.dp)
            else Text("저장하기")
        }

        OutlinedButton(onClick = onSignOut, modifier = Modifier.fillMaxWidth()) {
            Text("로그아웃")
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
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(8.dp))
        
        OutlinedTextField(
            value = password,
            onValueChange = { password = it },
            label = { Text("비밀번호") },
            visualTransformation = PasswordVisualTransformation(),
            modifier = Modifier.fillMaxWidth()
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
    }
}
