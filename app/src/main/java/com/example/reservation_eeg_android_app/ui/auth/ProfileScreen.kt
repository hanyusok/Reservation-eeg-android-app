package com.example.reservation_eeg_android_app.ui.auth

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.*
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.tooling.preview.Preview
import com.example.reservation_eeg_android_app.model.UserProfile
import com.example.reservation_eeg_android_app.ui.auth.viewmodel.AuthViewModel
import com.example.reservation_eeg_android_app.ui.theme.ReservationeegandroidappTheme
import io.github.jan.supabase.auth.status.SessionStatus
import io.github.jan.supabase.auth.user.UserInfo
import io.github.jan.supabase.auth.user.UserSession

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    viewModel: AuthViewModel,
    onNavigateToFamilyMembers: () -> Unit
) {
    val sessionStatus by viewModel.sessionStatus.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val error by viewModel.error.collectAsState()
    val userProfile by viewModel.userProfile.collectAsState()
    val updateSuccess by viewModel.updateSuccess.collectAsState()
    
    ProfileContent(
        sessionStatus = sessionStatus,
        isLoading = isLoading,
        error = error,
        userProfile = userProfile,
        updateSuccess = updateSuccess,
        onUpdateProfile = { viewModel.updateProfile(it) },
        onSignOut = { viewModel.signOut() },
        onSignIn = { email, password -> viewModel.signIn(email, password) },
        onSignUp = { email, password -> viewModel.signUp(email, password) },
        onSignInWithGoogle = { viewModel.signInWithGoogle() },
        onSignInWithKakao = { viewModel.signInWithKakao() },
        onNavigateToFamilyMembers = onNavigateToFamilyMembers
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileContent(
    sessionStatus: SessionStatus,
    isLoading: Boolean,
    error: String?,
    userProfile: UserProfile?,
    updateSuccess: Boolean,
    onUpdateProfile: (UserProfile) -> Unit,
    onSignOut: () -> Unit,
    onSignIn: (String, String) -> Unit,
    onSignUp: (String, String) -> Unit,
    onSignInWithGoogle: () -> Unit,
    onSignInWithKakao: () -> Unit,
    onNavigateToFamilyMembers: () -> Unit
) {
    val snackbarHostState = remember { SnackbarHostState() }
    var isEditing by remember { mutableStateOf(false) }

    LaunchedEffect(updateSuccess) {
        if (updateSuccess) {
            snackbarHostState.showSnackbar("프로필 정보가 저장되었습니다.")
            isEditing = false
        }
    }

    val gradientBackground = Brush.verticalGradient(
        colors = listOf(
            MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f),
            MaterialTheme.colorScheme.surface
        )
    )

    Scaffold(
        topBar = { 
            TopAppBar(
                title = { 
                    Text(
                        if (isEditing) "정보 수정" else "Profile", 
                        fontWeight = FontWeight.Bold, 
                        fontSize = 24.sp
                    ) 
                },
                navigationIcon = {
                    if (isEditing) {
                        IconButton(onClick = { isEditing = false }) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
            ) 
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        containerColor = Color.Transparent
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(gradientBackground)
                .padding(innerPadding)
        ) {
            when (sessionStatus) {
                is SessionStatus.Authenticated -> {
                    val currentProfile = userProfile ?: UserProfile(id = sessionStatus.session.user?.id)
                    if (isEditing) {
                        Box(modifier = Modifier.padding(16.dp)) {
                            ProfileDetailForm(
                                profile = currentProfile,
                                isLoading = isLoading,
                                error = error,
                                onUpdate = onUpdateProfile,
                                onCancel = { isEditing = false }
                            )
                        }
                    } else {
                        ProfileOverview(
                            profile = currentProfile,
                            onEditClick = { isEditing = true },
                            onSignOut = onSignOut,
                            onNavigateToFamilyMembers = onNavigateToFamilyMembers
                        )
                    }
                }
                else -> {
                    Box(modifier = Modifier.padding(16.dp)) {
                        AuthForm(
                            isLoading = isLoading,
                            error = error,
                            onSignIn = onSignIn,
                            onSignUp = onSignUp,
                            onSignInWithGoogle = onSignInWithGoogle,
                            onSignInWithKakao = onSignInWithKakao
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun ProfileOverview(
    profile: UserProfile,
    onEditClick: () -> Unit,
    onSignOut: () -> Unit,
    onNavigateToFamilyMembers: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp, vertical = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // ... (rest of ProfileOverview)
        // Profile Card matching reference image
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(
                modifier = Modifier
                    .padding(24.dp)
                    .fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Avatar Placeholder
                Box(
                    modifier = Modifier
                        .size(100.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primaryContainer),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Default.Person, 
                        contentDescription = null, 
                        modifier = Modifier.size(60.dp),
                        tint = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                Text(
                    text = profile.name.ifBlank { "이름을 설정해주세요" },
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold
                )
                
                Text(
                    text = profile.email,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.outline
                )
                
                if (profile.address.isNotBlank()) {
                    Text(
                        text = profile.address,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.outline
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))

                Button(
                    onClick = onEditClick,
                    shape = RoundedCornerShape(50),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFF4511E)), // Accent orange from image
                    contentPadding = PaddingValues(horizontal = 32.dp, vertical = 12.dp)
                ) {
                    Icon(Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Edit Profile", fontWeight = FontWeight.Bold)
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Menu Groups
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
        ) {
            Column {
                ProfileMenuItem(
                    icon = Icons.Default.People, 
                    label = "Family Members",
                    onClick = onNavigateToFamilyMembers
                )
                HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp), thickness = 0.5.dp, color = MaterialTheme.colorScheme.outlineVariant)
                ProfileMenuItem(icon = Icons.Default.AccountBalanceWallet, label = "Currencies")
                HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp), thickness = 0.5.dp, color = MaterialTheme.colorScheme.outlineVariant)
                ProfileMenuItem(icon = Icons.Default.Palette, label = "Appearance")
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
        ) {
            Column {
                ProfileMenuItem(icon = Icons.Default.Shield, label = "Application Security")
                HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp), thickness = 0.5.dp, color = MaterialTheme.colorScheme.outlineVariant)
                ProfileMenuItem(icon = Icons.Default.Devices, label = "Manage Devices")
                HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp), thickness = 0.5.dp, color = MaterialTheme.colorScheme.outlineVariant)
                ProfileMenuItem(icon = Icons.Default.Lock, label = "Change Password")
            }
        }
        
        Spacer(modifier = Modifier.height(24.dp))
        
        TextButton(onClick = onSignOut) {
            Text("로그아웃", color = MaterialTheme.colorScheme.error)
        }
        
        Spacer(modifier = Modifier.height(16.dp))
    }
}

@Composable
fun ProfileMenuItem(icon: ImageVector, label: String, onClick: () -> Unit = {}) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(36.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon, 
                contentDescription = null, 
                modifier = Modifier.size(20.dp),
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Spacer(modifier = Modifier.width(16.dp))
        Text(
            text = label, 
            modifier = Modifier.weight(1f),
            style = MaterialTheme.typography.bodyLarge,
            fontWeight = FontWeight.Medium
        )
        Icon(
            imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight, 
            contentDescription = null,
            tint = MaterialTheme.colorScheme.outline
        )
    }
}

@Composable
fun ProfileDetailForm(
    profile: UserProfile,
    isLoading: Boolean,
    error: String?,
    onUpdate: (UserProfile) -> Unit,
    onCancel: () -> Unit
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
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("이름") }, modifier = Modifier.fillMaxWidth())
        
        Column(modifier = Modifier.fillMaxWidth()) {
            Text("성별", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.primary)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                listOf("남성", "여성").forEach { option ->
                    FilterChip(
                        selected = sex == option,
                        onClick = { sex = option },
                        label = { Text(option) },
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }

        OutlinedTextField(
            value = residentId,
            onValueChange = { residentId = it.filter { char -> char.isDigit() }.take(13) },
            label = { Text("주민등록번호") },
            modifier = Modifier.fillMaxWidth(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            placeholder = { Text("123456-1234567") },
            visualTransformation = ResidentIdTransformation()
        )
        OutlinedTextField(value = address, onValueChange = { address = it }, label = { Text("주소") }, modifier = Modifier.fillMaxWidth())
        OutlinedTextField(
            value = phoneNumber,
            onValueChange = { phoneNumber = it.filter { char -> char.isDigit() }.take(11) },
            label = { Text("전화번호") },
            modifier = Modifier.fillMaxWidth(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
            placeholder = { Text("010-0000-0000") },
            visualTransformation = PhoneNumberTransformation()
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
    }
}

@Composable
fun AuthForm(
    isLoading: Boolean,
    error: String?,
    onSignIn: (String, String) -> Unit,
    onSignUp: (String, String) -> Unit,
    onSignInWithGoogle: () -> Unit,
    onSignInWithKakao: () -> Unit
) {
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
                if (isSignUp) onSignUp(email, password)
                else onSignIn(email, password)
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
            onClick = onSignInWithGoogle,
            modifier = Modifier.fillMaxWidth(),
            enabled = !isLoading
        ) {
            Text("Google로 로그인")
        }

        Spacer(modifier = Modifier.height(8.dp))

        Button(
            onClick = onSignInWithKakao,
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

class PhoneNumberTransformation : VisualTransformation {
    override fun filter(text: AnnotatedString): TransformedText {
        val trimmed = text.text
        var out = ""
        for (i in trimmed.indices) {
            out += trimmed[i]
            if (i == 2 || i == 6) out += "-"
        }

        val phoneNumberOffsetMapping = object : OffsetMapping {
            override fun originalToTransformed(offset: Int): Int {
                if (offset <= 3) return offset
                if (offset <= 7) return offset + 1
                return offset + 2
            }

            override fun transformedToOriginal(offset: Int): Int {
                if (offset <= 3) return offset
                if (offset <= 8) return offset - 1
                return offset - 2
            }
        }

        return TransformedText(AnnotatedString(out), phoneNumberOffsetMapping)
    }
}

class ResidentIdTransformation : VisualTransformation {
    override fun filter(text: AnnotatedString): TransformedText {
        val trimmed = text.text
        var out = ""
        for (i in trimmed.indices) {
            out += trimmed[i]
            if (i == 5) out += "-"
        }

        val residentIdOffsetMapping = object : OffsetMapping {
            override fun originalToTransformed(offset: Int): Int {
                if (offset <= 6) return offset
                return offset + 1
            }

            override fun transformedToOriginal(offset: Int): Int {
                if (offset <= 6) return offset
                return offset - 1
            }
        }

        return TransformedText(AnnotatedString(out), residentIdOffsetMapping)
    }
}

@OptIn(kotlin.time.ExperimentalTime::class)
@Preview(showBackground = true)
@Composable
fun ProfileScreenAuthenticatedPreview() {
    ReservationeegandroidappTheme {
        ProfileContent(
            sessionStatus = SessionStatus.Authenticated(
                session = UserSession(
                    accessToken = "",
                    refreshToken = "",
                    expiresIn = 3600,
                    tokenType = "",
                    user = UserInfo(id = "user123", email = "test@example.com", aud = "", createdAt = null, updatedAt = null)
                )
            ),
            isLoading = false,
            error = null,
            userProfile = UserProfile(
                id = "user123",
                name = "홍길동",
                email = "test@example.com",
                address = "서울특별시 강남구",
                phoneNumber = "010-1234-5678"
            ),
            updateSuccess = false,
            onUpdateProfile = {},
            onSignOut = {},
            onSignIn = { _, _ -> },
            onSignUp = { _, _ -> },
            onSignInWithGoogle = {},
            onSignInWithKakao = {},
            onNavigateToFamilyMembers = {}
        )
    }
}

@Preview(showBackground = true)
@Composable
fun ProfileScreenUnauthenticatedPreview() {
    ReservationeegandroidappTheme {
        ProfileContent(
            sessionStatus = SessionStatus.NotAuthenticated(),
            isLoading = false,
            error = null,
            userProfile = null,
            updateSuccess = false,
            onUpdateProfile = {},
            onSignOut = {},
            onSignIn = { _, _ -> },
            onSignUp = { _, _ -> },
            onSignInWithGoogle = {},
            onSignInWithKakao = {},
            onNavigateToFamilyMembers = {}
        )
    }
}
